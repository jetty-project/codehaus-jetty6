// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.component.Container;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.NotFoundHandler;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.log.Log;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.mortbay.util.LazyList;
import org.mortbay.util.MultiException;

/* ------------------------------------------------------------ */
/** Jetty HTTP Servlet Server.
 * This class is the main class for the Jetty HTTP Servlet server.
 * It aggregates Connectors (HTTP request receivers) and request Handlers.
 * The server is itself a handler and a ThreadPool.  Connectors use the ThreadPool methods
 * to run jobs that will eventually call the handle method.
 * 
 * @author gregw
 *
 */
public class Server extends HandlerWrapper 
{
    private static ShutdownHookThread hookThread = new ShutdownHookThread();
    
    private ThreadPool _threadPool;
    private Connector[] _connectors;
    private UserRealm[] _realms;
    private Handler _notFoundHandler;
    private Container _container=new Container();
    private SessionIdManager _sessionIdManager;
    private boolean _sendServerVersion = true; //send Server: header by default
    
    /* ------------------------------------------------------------ */
    public Server()
    {
        setServer(this);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the container.
     */
    public Container getContainer()
    {
        return _container;
    }

    /* ------------------------------------------------------------ */
    public boolean getStopAtShutdown()
    {
        return hookThread.contains(this);
    }
    
    /* ------------------------------------------------------------ */
    public void setStopAtShutdown(boolean stop)
    {
        if (stop)
            hookThread.add(this);
        else
            hookThread.remove(this);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the connectors.
     */
    public Connector[] getConnectors()
    {
        return _connectors;
    }
    

    /* ------------------------------------------------------------ */
    public void addConnector(Connector connector)
    {
        setConnectors((Connector[])LazyList.addToArray(getConnectors(), connector, Connector.class));
    }

    /* ------------------------------------------------------------ */
    /**
     * Conveniance method which calls {@link #getConnectors()} and {@link #setConnectors(Connector[])} to 
     * remove a connector.
     * @param connector The connector to remove.
     */
    public void removeConnector(Connector connector) {
        setConnectors((Connector[])LazyList.removeFromArray (getConnectors(), connector));
    }

    /* ------------------------------------------------------------ */
    /** Set the connectors for this server.
     * Each connector has this server set as it's ThreadPool and its Handler.
     * @param connectors The connectors to set.
     */
    public void setConnectors(Connector[] connectors)
    {
        _container.update(this, _connectors, connectors, "connector");
        if (_connectors!=null)
        {
            for (int i=0;i<_connectors.length;i++)
                if (_connectors[i]!=null)
                    _connectors[i].setServer(null);
        }

        _connectors = connectors;
        
        if (connectors!=null)
        {
            for (int i=0;i<connectors.length;i++)
                connectors[i].setServer(this);
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the threadPool.
     */
    public ThreadPool getThreadPool()
    {
        return _threadPool;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param threadPool The threadPool to set.
     */
    public void setThreadPool(ThreadPool threadPool)
    {
        _container.update(this,_threadPool,threadPool, "threadpool");
        _threadPool = threadPool;
    }

    /* ------------------------------------------------------------ */
    protected void doStart() throws Exception
    {
        Log.info(this.getClass().getPackage().getImplementationTitle()+" "+this.getClass().getPackage().getImplementationVersion());
        MultiException mex=new MultiException();
        
        if (_threadPool==null)
        {
            BoundedThreadPool btp=new BoundedThreadPool();
            setThreadPool(btp);
        }
        
        if (_sessionIdManager!=null)
            _sessionIdManager.start();
        
        try{_threadPool.start();} catch(Throwable e) { mex.add(e);}
        
        
        try { super.doStart(); } catch(Throwable e) { mex.add(e);}
        
        if (_connectors!=null)
        {
            for (int i=0;i<_connectors.length;i++)
            {
                try{_connectors[i].start();}
                catch(Throwable e)
                {
                    mex.add(e);
                }
            }
        }

        mex.ifExceptionThrow();
    }

    /* ------------------------------------------------------------ */
    protected void doStop() throws Exception
    {
        MultiException mex=new MultiException();
        
        if (_connectors!=null)
        {
            for (int i=_connectors.length;i-->0;)
                try{_connectors[i].stop();}catch(Throwable e){mex.add(e);}
        }

        try { super.doStop(); } catch(Throwable e) { mex.add(e);}
        
        if (_sessionIdManager!=null)
            _sessionIdManager.stop();
        
        try{_threadPool.stop();}catch(Throwable e){mex.add(e);}
        
        mex.ifExceptionThrow();
    }

    /* ------------------------------------------------------------ */
    /* Handle a request from a connection.
     * Called to handle a request on the connection when either the header has been received,
     * or after the entire request has been received (for short requests of known length).
     */
    public void handle(HttpConnection connection) throws IOException, ServletException
    {
        String target=connection.getRequest().getPathInfo();
        if (Log.isDebugEnabled())
        {
            Log.debug("REQUEST "+target+" on "+connection);
            handle(target, connection.getRequest(), connection.getResponse(), Handler.REQUEST);
            Log.debug("RESPONSE "+target+"  "+connection.getResponse().getStatus());
        }
        else
            handle(target, connection.getRequest(), connection.getResponse(), Handler.REQUEST);
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        boolean handled = super.handle(target, request, response, dispatch);
        if (handled)
            return handled;
        
        // TODO - somehow move this to a post handler?
        synchronized(this)
        {
            if (_notFoundHandler==null)
            {
                try
                {
                    NotFoundHandler nfh=new NotFoundHandler();
                    
                    setNotFoundHandler(nfh);
                }
                catch(Exception e)
                {
                    Log.warn(e);
                }
            }
        }
        return _notFoundHandler.handle(target, request, response, dispatch);
    }
    
    /* ------------------------------------------------------------ */
    public void join() throws InterruptedException 
    {
        getThreadPool().join();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Map of realm name to UserRealm instances.
     */
    public UserRealm[] getUserRealms()
    {
        return _realms;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param realms Map of realm name to UserRealm instances.
     */
    public void setUserRealms(UserRealm[] realms)
    {
        _container.update(this,_realms,realms, "realm");
        _realms=realms;
    }
    
    /* ------------------------------------------------------------ */
    public void addUserRealm(UserRealm realm)
    {
        setUserRealms((UserRealm[])LazyList.addToArray(getUserRealms(), realm, UserRealm.class));
    }
    
    /* ------------------------------------------------------------ */
    public void removeUserRealm(UserRealm realm)
    {
        setUserRealms((UserRealm[])LazyList.removeFromArray(getUserRealms(), realm));
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the notFoundHandler.
     */
    public Handler getNotFoundHandler()
    {
        return _notFoundHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param notFoundHandler The notFoundHandler to set.
     */
    public void setNotFoundHandler(Handler notFoundHandler)
    {
        _container.update(this, _notFoundHandler, notFoundHandler, "notFoundHandler");
        notFoundHandler.setServer(this);
        _notFoundHandler = notFoundHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the sessionIdManager.
     */
    public SessionIdManager getSessionIdManager()
    {
        return _sessionIdManager;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param sessionIdManager The sessionIdManager to set.
     */
    public void setSessionIdManager(SessionIdManager sessionIdManager)
    {
        _container.update(this,_sessionIdManager,sessionIdManager, "sessionIdManager");
        _sessionIdManager = sessionIdManager;
    }
    
    
    public void setSendServerVersion (boolean sendServerVersion)
    {
        _sendServerVersion = sendServerVersion;
    }
    
    public boolean getSendServerVersion()
    {
        
        return _sendServerVersion;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /**
     * ShutdownHook thread for stopping all servers.
     * 
     * Thread is hooked first time list of servers is changed.
     */
    private static class ShutdownHookThread extends Thread
    {
        private boolean hooked = false;
        private ArrayList servers = new ArrayList();

        /**
         * Hooks this thread for shutdown.
         * 
         * @see java.lang.Runtime#addShutdownHook(java.lang.Thread)
         */
        private void createShutdownHook()
        {
            if (!Boolean.getBoolean("JETTY_NO_SHUTDOWN_HOOK") && !hooked)
            {
                try
                {
                    Method shutdownHook = java.lang.Runtime.class.getMethod("addShutdownHook", new Class[]
                    { java.lang.Thread.class});
                    shutdownHook.invoke(Runtime.getRuntime(), new Object[]
                    { this});
                    this.hooked = true;
                }
                catch (Exception e)
                {
                    if (Log.isDebugEnabled())
                        Log.debug("No shutdown hook in JVM ", e);
                }
            }
        }

        /**
         * Add Server to servers list.
         */
        public boolean add(Server server)
        {
            createShutdownHook();
            return this.servers.add(server);
        }

        /**
         * Contains Server in servers list?
         */
        public boolean contains(Server server)
        {
            return this.servers.contains(server);
        }

        /**
         * Append all Servers from Collection
         */
        public boolean addAll(Collection c)
        {
            createShutdownHook();
            return this.servers.addAll(c);
        }

        /**
         * Clear list of Servers.
         */
        public void clear()
        {
            createShutdownHook();
            this.servers.clear();
        }

        /**
         * Remove Server from list.
         */
        public boolean remove(Server server)
        {
            createShutdownHook();
            return this.servers.remove(server);
        }

        /**
         * Remove all Servers in Collection from list.
         */
        public boolean removeAll(Collection c)
        {
            createShutdownHook();
            return this.servers.removeAll(c);
        }

        /**
         * Stop all Servers in list.
         */
        public void run()
        {
            setName("Shutdown");
            Log.info("Shutdown hook executing");
            Iterator it = servers.iterator();
            while (it.hasNext())
            {
                Server svr = (Server) it.next();
                if (svr == null)
                    continue;
                try
                {
                    svr.stop();
                }
                catch (Exception e)
                {
                    Log.warn(e);
                }
                Log.info("Shutdown hook complete");

                // Try to avoid JVM crash
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e)
                {
                    Log.warn(e);
                }
            }
        }
    }

    
    

    /* ------------------------------------------------------------ */
    /**
     * @deprecated 
     */
    public void addHandler(Handler handler)
    {
        getHandlerCollection().addHandler(handler);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @deprecated 
     */
    public void removeHandler(Handler handler)
        throws Exception
    {
        getHandlerCollection().removeHandler(handler);
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated 
     */
    public Handler[] getHandlers()
    {
        return getHandlerCollection().getHandlers();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @deprecated 
     */
    public void setHandlers(Handler[] handlers)
    {
        getHandlerCollection().setHandlers(handlers);
    }

    /* ------------------------------------------------------------ */
    private HandlerCollection getHandlerCollection()
    {
        HandlerCollection collection=(HandlerCollection)getChildHandlerByClass(ContextHandlerCollection.class);
        if (collection==null)
        {
            collection=(HandlerCollection)getChildHandlerByClass(HandlerCollection.class);
            if (collection==null)
            {
                collection=new ContextHandlerCollection();
                
                HandlerWrapper wrapper = this;
                while (wrapper.getHandler()!=null && wrapper.getHandler() instanceof HandlerWrapper)
                    wrapper=(HandlerWrapper)wrapper.getHandler();
                wrapper.setHandler(collection);
            }
        }
        return collection;
    }


}

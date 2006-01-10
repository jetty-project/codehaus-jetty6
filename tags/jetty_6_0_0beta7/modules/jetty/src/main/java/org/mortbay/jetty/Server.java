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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.component.Container;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.NotFoundHandler;
import org.mortbay.jetty.handler.WrappedHandler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.log.Log;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;
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
public class Server extends HandlerCollection 
{
    private static ShutdownHookThread hookThread = new ShutdownHookThread();
    
    private ThreadPool _threadPool;
    private Connector[] _connectors;
    private UserRealm[] _realms;
    private Handler _notFoundHandler;
    private Container _container=new Container();
    
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
        MultiException mex=new MultiException();
        
        if (_threadPool==null)
        {
            BoundedThreadPool btp=new BoundedThreadPool();
            btp.setQueue(true);
            setThreadPool(btp);
        }
        
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
        
        try{_threadPool.stop();}catch(Throwable e){mex.add(e);}
        
        mex.ifExceptionThrow();
    }

    /* ------------------------------------------------------------ */
    /* Handle a request from a connection.
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
     * @see org.mortbay.jetty.EventHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        Handler[] handlers = getHandlers();
        if (handlers==null || handlers.length==0)
        {
            response.sendError(500);
            return true;
        }
        else
        {
            for (int i=0;i<handlers.length;i++)
            {
                if (handlers[i].handle(target,request, response, dispatch))
                    return true;
            }
        }    
        
        synchronized(this)
        {
            if (_notFoundHandler==null)
            {
                try
                {
                    NotFoundHandler nfh=new NotFoundHandler();
                    nfh.setServer(this);
                    nfh.start();
                    setNotFoundHandler(nfh);
                }
                catch(Exception e)
                {
                    Log.warn(e);
                }
            }
            _notFoundHandler.handle(target, request, response, dispatch);
        }
        
        return false;
    }

    /* ------------------------------------------------------------ */
    public Handler[] getAllHandlers()
    {
        Handler[] handlers = getHandlers();
        List list = new ArrayList();
        for (int i=0;i<handlers.length;i++)
            expandHandler(handlers[i],list);
        return (Handler[])list.toArray(new Handler[list.size()]);
    }

    /* ------------------------------------------------------------ */
    private void expandHandler(Handler handler, List list)
    {
        if (handler==null)
            return;
        list.add(handler);
        if (handler instanceof WrappedHandler)
            expandHandler(((WrappedHandler)handler).getHandler(),list);
        if (handler instanceof HandlerCollection)
        {
            Handler[] ha = ((HandlerCollection)handler).getHandlers();
            for (int i=0;ha!=null && i<ha.length; i++)
                expandHandler(ha[i], list);
        }
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
        _notFoundHandler = notFoundHandler;
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

}

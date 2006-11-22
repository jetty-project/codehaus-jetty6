//========================================================================
// Parts Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//========================================================================



package org.mortbay.jetty.grizzly;


import java.io.IOException;
import java.net.InetAddress;

import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.AbstractNIOConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;

/* ------------------------------------------------------------------------------- */
/**
 * @author gregw
 *
 */
public class GrizzlyConnector extends AbstractNIOConnector
{
    private JettySelectorThread _selectorThread;
	
    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     * 
     */
    public GrizzlyConnector()
    {
        _selectorThread = new JettySelectorThread();
    }

    /* ------------------------------------------------------------ */
    public Object getConnection()
    {
        return _selectorThread.getServerSocketChannel();
    }
    
    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.AbstractConnector#doStart()
     */
    protected void doStart() throws Exception
    {
        super.doStart();  

        // TODO - is there a non-blocking way to do this?
        new Thread()
        {
            public void run()
            {
                try
                {
                    _selectorThread.startEndpoint();
                }
                catch(InstantiationException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.AbstractConnector#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();
        _selectorThread.stopEndpoint();
    }


    /* ------------------------------------------------------------ */
    public void open() throws IOException
    {
    	// TODO Open server socket
        try
        {
            _selectorThread.setPort(getPort());
            _selectorThread.setGrizzlyConnector(this);
            
            ThreadPool threadPool = getServer().getThreadPool();
            _selectorThread.setThreadPool(threadPool);  
            
            //TO DO: Needs to find a way to get this information from the 
            // config file directly.
            if ( threadPool instanceof BoundedThreadPool) {
                _selectorThread.setMaxThreads(
                        ((BoundedThreadPool)threadPool).getMaxThreads());
                _selectorThread.setMinThreads(
                        ((BoundedThreadPool)threadPool).getMinThreads());                   
            }
            _selectorThread.setDisplayConfiguration(true);
            
            if (getHost()!=null)
                _selectorThread.setAddress(InetAddress.getByName(getHost()));
            _selectorThread.initEndpoint();
        } 
        catch (InstantiationException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
    	// TODO Close server socket
        // XXX Only supported when calling selectorThread.stopEndpoint();
    }

    /* ------------------------------------------------------------ */
    public void accept(int acceptorID) throws IOException
    {
        try
        {
            // TODO - this may not exactly be right.  accept is called in a loop, so we
            // may need to wait on the _selectorThread somehow?
            // maybe we just set acceptors to zero and don't need to bother here as
            // grizzly has it's own accepting threads.
            _selectorThread.isAlive();
            Thread.sleep(5000);
            
        } 
        catch (Throwable e) 
        {
            // TODO Auto-generated catch block
            Log.ignore(e);
        }
        
    }

    /* ------------------------------------------------------------ */
    public void stopAccept(int acceptorID) throws Exception
    {
        // TODO 
    }
    
    
    /* ------------------------------------------------------------------------------- */
    public void customize(EndPoint endpoint, Request request) throws IOException
    {
        super.customize(endpoint, request);
    }


    /* ------------------------------------------------------------------------------- */
    public int getLocalPort()
    {
    	// TODO return the actual port we are listening on
    	return _selectorThread.getPort();
    }
    

    /* ------------------------------------------------------------------------------- */
    /** temp main - just to help testing */
    public static void main(String[] args)
        throws Exception
    {
        Server server = new Server();
        Connector connector=new GrizzlyConnector();
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});
        
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler()});
        server.setHandler(handlers);
        
        // TODO add javadoc context to contexts
        
        WebAppContext.addWebApplications(server, "../../webapps", "org/mortbay/jetty/webapp/webdefault.xml", true, false);
        
        HashUserRealm userRealm = new HashUserRealm();
        userRealm.setName("Test Realm");
        userRealm.setConfig("../../etc/realm.properties");
        server.setUserRealms(new UserRealm[]{userRealm});
        
        
        server.start();
        server.join();
        
    }
}

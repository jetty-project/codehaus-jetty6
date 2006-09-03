//========================================================================
// Parts Copyright 2006 Mort Bay Consulting Pty. Ltd.
// Parts Copyright 2006 Jeanfrancois Arcand
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.AbstractNIOConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.log.Log;

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
        Log.warn("The GrizzlyConnector is a pre-ALPHA work in progress!!!!");
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
            if (getHost()!=null)
                _selectorThread.setAddress(InetAddress.getByName(getHost()));
            _selectorThread.initEndpoint();
            _selectorThread.setGrizzlyConnector(this);
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
            e.printStackTrace();
        }
        
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
    

    /* TODO temp main - just to help testing */
    public static void main(String[] args)
        throws Exception
    {
        Server server = new Server();
        Connector connector=new GrizzlyConnector();
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});
        
        ServletHandler handler=new ServletHandler();
        server.setHandler(handler);
        
        handler.addServletWithMapping("org.mortbay.jetty.grizzly.GrizzlyConnector$HelloServlet", "/");
        
        server.start();
        server.join();
    }

    public static class HelloServlet extends HttpServlet
    {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello SimpleServlet</h1>");
        }
    }
    
}

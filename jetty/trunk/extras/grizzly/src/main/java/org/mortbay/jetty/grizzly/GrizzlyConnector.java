//========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
// expecting other bits copyrighted sun
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


import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.KeepAliveStats;
import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.TaskListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.coyote.RequestGroupInfo;
import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.nio.AbstractNIOConnector;
import org.mortbay.jetty.nio.NIOConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.mortbay.util.ajax.Continuation;

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

        _selectorThread.startEndpoint();
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
        try{
            _selectorThread.initEndpoint();
        } catch (InstantiationException ex){
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
    

    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    public static class GrizzlyEndPoint extends ChannelEndPoint implements EndPoint, Runnable
    {
        HttpConnection _connection;
        boolean dispatched=false;
        
        public GrizzlyEndPoint(GrizzlyConnector connector, ByteChannel channel)
        {
            super(channel);
            _connection = new HttpConnection(connector,this,connector.getServer());
        }

        public void run()
        {
            try
            {
                System.err.println("dispatched "+this);
                _connection.handle();
            }
            catch (ClosedChannelException e)
            {
                Log.ignore(e);
            }
            catch (EofException e)
            {
                Log.debug("EOF", e);
                try{close();}
                catch(IOException e2){Log.ignore(e2);}
            }
            catch (HttpException e)
            {
                Log.debug("BAD", e);
                try{close();}
                catch(IOException e2){Log.ignore(e2);}
            }
            catch (Throwable e)
            {
                Log.warn("handle failed", e);
                try{close();}
                catch(IOException e2){Log.ignore(e2);}
            }
            finally
            {
                Continuation continuation =  _connection.getRequest().getContinuation();
                if (continuation != null && continuation.isPending())
                {
                    // We have a continuation
                    // TODO something!
                }
                else
                {
                    dispatched=false;
                    // something else... normally re-enable this connection is the selectset with the latest interested ops
                }
            }
        
        }

        public void blockReadable(long millisecs)
        {
            // TODO implement
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        }

        public void blockWritable(long millisecs)
        {
            // TODO implement
            try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
        }

        public int fill(Buffer buffer) throws IOException
        {
            // TODO implement
            return 0;
        }

        public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
        {
            int len=0;
            
            // TODO gather operation.
            if (header!=null && header.hasContent())
                len+=flush(header);
            
            if (header==null || !header.hasContent())
            {
                if (buffer!=null && buffer.hasContent())
                    len+=flush(buffer);
            }

            if (buffer==null || !buffer.hasContent())
            {
                if (trailer!=null && trailer.hasContent())
                    len+=flush(trailer);
            }
            
            return len;
            
        }

        public int flush(Buffer buffer) throws IOException
        {
            // TODO implement
            return 0;
        }

        public boolean isBlocking()
        {
            return false;
        }
        
    }

    /** temp main - just to help testing */
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

// ========================================================================
// Copyright 2003-2005 Mort Bay Consulting Pty. Ltd.
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
 
package org.mortbay.jetty.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.bio.SocketEndPoint;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.log.Log;


/* ------------------------------------------------------------------------------- */
/**  Socket Connector.
 * This connector implements a traditional blocking IO and threading model.
 * Normal JRE sockets are used and a thread is allocated per connection.
 * Buffers are managed so that large buffers are only allocated to active connections.
 * 
 * This Connector should only be used if NIO is not available.
 * 
 * @author gregw
 */
public class SocketConnector extends AbstractConnector
{
    protected ServerSocket _serverSocket;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     * 
     */
    public SocketConnector()
    {
    }

    
    /* ------------------------------------------------------------ */
    public void open() throws IOException
    {
        // Create a new server socket and set to non blocking mode
        _serverSocket= newServerSocket(getAddress(),getAcceptQueueSize());
    }

    /* ------------------------------------------------------------ */
    protected ServerSocket newServerSocket(SocketAddress addr,int backlog) throws IOException
    {
        ServerSocket ss= new ServerSocket();
        ss.bind(addr,backlog);
        
        return ss;
    }
    
    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        if (_serverSocket!=null)
            _serverSocket.close();
        _serverSocket=null;
    }
    
    /* ------------------------------------------------------------ */
    public void accept(int acceptorID)
    	throws IOException, InterruptedException
    {   
        Socket socket = _serverSocket.accept();
        configure(socket);
        
        Connection connection=new Connection(socket);
        connection.dispatch();
    }

    /* ------------------------------------------------------------------------------- */
    protected Buffer newBuffer(int size)
    {
        return new ByteArrayBuffer(size);
    }

    /* ------------------------------------------------------------------------------- */
    public void customize(EndPoint endpoint, Request request)
        throws IOException
    {
        super.customize(endpoint, request);
        configure((Socket)endpoint.getConnection());
    }
    
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    protected class Connection extends SocketEndPoint implements Runnable
    {
        boolean _dispatched=false;
        HttpConnection _connection;
        
        public Connection(Socket socket) throws IOException
        {
            super(socket);
            _connection = new HttpConnection(SocketConnector.this,this,getServer());
        }
        
        public void dispatch() throws InterruptedException
        {
            getThreadPool().dispatch(this);
        }
        
        public int fill(Buffer buffer) throws IOException
        {
            int l = super.fill(buffer);
            if (l<0)
                close();
            return l;
        }
        
        
        public void run()
        {
            try
            {
                while (!isClosed())
                    _connection.handle();
            }
            catch(RetryRequest e)
            {
                throw e;
            }
            catch(IOException e)
            {
                // TODO - better than this
                if ("BAD".equals(e.getMessage()))
                {
                    Log.warn("BAD Request");
                    Log.debug("BAD",e);
                }
                else if ("EOF".equals(e.getMessage()))
                    Log.debug("EOF",e);
                else
                    Log.warn("IO",e);
                try{close();}
                catch(IOException e2){Log.ignore(e2);}
            }
            catch(Throwable e)
            {
                Log.warn("handle failed",e);
                try{close();}
                catch(IOException e2){Log.ignore(e2);}
            }
            finally
            {
            }
        }
    }

}

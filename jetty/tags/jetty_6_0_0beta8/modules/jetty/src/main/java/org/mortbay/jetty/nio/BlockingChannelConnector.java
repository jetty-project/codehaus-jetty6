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
 
package org.mortbay.jetty.nio;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.mortbay.io.Buffer;
import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.log.Log;


/* ------------------------------------------------------------------------------- */
/**  Blocking NIO connector.
 * This connector uses efficient NIO buffers with a traditional blocking thread model.
 * Direct NIO buffers are used and a thread is allocated per connections.
 * 
 * This connector is best used when there are a few very active connections.
 * 
 * @author gregw
 */
public class BlockingChannelConnector extends AbstractConnector
{
    private transient ServerSocketChannel _acceptChannel;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     * 
     */
    public BlockingChannelConnector()
    {
    }

    /* ------------------------------------------------------------ */
    public void open() throws IOException
    {
        // Create a new server socket and set to non blocking mode
        _acceptChannel= ServerSocketChannel.open();
        _acceptChannel.configureBlocking(true);

        // Bind the server socket to the local host and port
        _acceptChannel.socket().bind(getAddress());
        
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        if (_acceptChannel != null)
            _acceptChannel.close();
        _acceptChannel=null;
    }
    
    /* ------------------------------------------------------------ */
    public void accept(int acceptorID)
    	throws IOException, InterruptedException
    {   
        SocketChannel channel = _acceptChannel.accept();
        channel.configureBlocking(true);
        Socket socket=channel.socket();
        configure(socket);

        Connection connection=new Connection(channel);
        connection.dispatch();
    }

    /* ------------------------------------------------------------------------------- */
    protected Buffer newBuffer(int size)
    {
        return new NIOBuffer(size,NIOBuffer.DIRECT);
    }

    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    private class Connection extends ChannelEndPoint implements Runnable
    {
        boolean _dispatched=false;
        HttpConnection _connection;
        
        Connection(ByteChannel channel) throws IOException
        {
            super(channel);
            _connection = new HttpConnection(BlockingChannelConnector.this,this,getServer());
        }
        
        void dispatch() throws InterruptedException
        {
            getThreadPool().dispatch(this);
        }
        
        public int fill(Buffer buffer) throws IOException
        {
            int l = super.fill(buffer);
            if (l<0)
                getChannel().close();
            return l;
        }
        
        public void run()
        {
            try
            {
                while (isOpen())
                    _connection.handle();
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
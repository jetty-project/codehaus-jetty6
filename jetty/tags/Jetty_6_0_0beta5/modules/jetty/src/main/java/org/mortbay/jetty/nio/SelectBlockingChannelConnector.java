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
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import org.mortbay.io.Buffer;
import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.log.Log;

/* ------------------------------------------------------------------------------- */
/**  Selecting Blocking NIO connector.
 * This connector uses efficient NIO buffers with a non blocking threading model for
 * idle connections. When a connection receives a request, it is switched to a traditional
 * blocking IO model.
 * 
 * This connector is best used when there are a very many low activity connections. Even then
 * the SelectChannelConnector may give better performance.
 * 
 * @author gregw
 */
public class SelectBlockingChannelConnector extends AbstractConnector
{
    private transient ServerSocketChannel _acceptChannel;
    private transient SelectionKey _acceptKey;
    private transient Selector _selector;
    private transient ArrayList _unDispatched=new ArrayList();

    
    /* ------------------------------------------------------------------------------- */
    /** Constructor.
     * 
     */
    public SelectBlockingChannelConnector()
    {
    }


    /* ------------------------------------------------------------ */
    public void open() throws IOException
    {
        if (_acceptChannel==null)
        {
            // Create a new server socket and set to non blocking mode
            _acceptChannel= ServerSocketChannel.open();
            _acceptChannel.configureBlocking(false);

            // Bind the server socket to the local host and port
            _acceptChannel.socket().bind(getAddress());

            // create a selector;
            _selector= Selector.open();

            // Register accepts on the server socket with the selector.
            _acceptKey=_acceptChannel.register(_selector, SelectionKey.OP_ACCEPT);
        }
    }

    public void close() throws IOException
    {
        if (_acceptChannel != null)
            _acceptChannel.close();
        _acceptChannel=null;

        try
        {
            if (_selector != null)
                _selector.close();
        }
        catch (IOException e)
        {
            Log.ignore(e);
        }

        _selector= null;
    }

    /* ------------------------------------------------------------ */
    public void accept(int acceptorID)
    	throws IOException
    {      
        // Make any key changes required
        synchronized(_unDispatched)
        {
            if (_unDispatched.size()>0)
                _selector.selectNow();
            
            for (int i=0;i<_unDispatched.size();i++)
            {
                try
                {
                    HttpEndPoint c = (HttpEndPoint)_unDispatched.get(i);
                    {
                        SocketChannel channel = (SocketChannel)c.getChannel();
                        if (channel.isOpen())
                        {
                            channel.configureBlocking(false);
                            c.setKey(channel.register(_selector, SelectionKey.OP_READ));
                        }
                    }
                }
                catch(CancelledKeyException e)
                {
                    Log.warn(e);
                }
            }
            _unDispatched.clear();
        }
 
        // SELECT for things to do!
        if (_selector.selectedKeys().size()==0)
            _selector.select(_maxIdleTime);
        
        // Look for things to do
        Iterator iter= _selector.selectedKeys().iterator();
        while (iter.hasNext())
        {
            SelectionKey key= (SelectionKey)iter.next();
            iter.remove();
            
            try
            {
                if (!key.isValid())
                {
                    key.cancel();
                    HttpEndPoint connection = (HttpEndPoint)key.attachment();
                    if (connection!=null)
                        connection._key=null;
                    continue;
                }
                
                if (key.equals(_acceptKey))
                {
                    if (key.isAcceptable())
                    {
                        SocketChannel channel = _acceptChannel.accept();
                        channel.configureBlocking(true);
                        Socket socket=channel.socket();
                        configure(socket);
                        HttpEndPoint connection=new HttpEndPoint(channel);
                        
                        // assume something to do
                        connection.dispatch();
                    }
                }
                else
                {
                    HttpEndPoint connection = (HttpEndPoint)key.attachment();
                    if (connection!=null)
                        connection.dispatch();    
                }
                
                key= null;
            }
            catch (Exception e)
            {
                if (isRunning())
                    Log.warn(e);
                if (key != null && key!=_acceptKey)
                    key.interestOps(0);
            }
        }   
    }

    /* ------------------------------------------------------------------------------- */
    protected Buffer newBuffer(int size)
    {
        return new NIOBuffer(size, true);
    }

    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    private class HttpEndPoint extends ChannelEndPoint implements Runnable
    {
        boolean _dispatched=false;
        SelectionKey _key;
        HttpConnection _connection;
    
        /* ------------------------------------------------------------ */
        HttpEndPoint(SocketChannel channel) 
        {
            super(channel);
            _connection = new HttpConnection(SelectBlockingChannelConnector.this,this, getHandler());
        }

        /* ------------------------------------------------------------ */
        void setKey(SelectionKey key)
        {
            _key=key;
            _key.attach(this);
        }

        /* ------------------------------------------------------------ */
        /** Dispatch the endpoint by arranging for a thread to service it.
         * Either a blocked thread is woken up or the endpoint is passed to the server job queue.
         * If the thread is dispatched and then the selection key 
         * is modified  so that it is no longer selected.
         */
        boolean dispatch() 
            throws IOException
        {
            
            boolean dispatch_done=false;
            try
            {
                synchronized(this)
                {
                    // Otherwise if we are still dispatched
                    if (_dispatched)
                    {
                        // we are not interested in further selecting 
                        return false;
                    }
                    
                    if (_key!=null)
                    {
                        _key.cancel();
                        _key.attach(null);
                        _key=null;
                    }
                       
                    ((SelectableChannel)getChannel()).configureBlocking(true);
                    _dispatched=true;
                }
                dispatch_done=getThreadPool().dispatch(this);
            }
            finally
            {
                if (!dispatch_done)
                {
                    Log.warn("dispatch failed");
                    undispatch();
                }
            }
            return true;
        }

        /* ------------------------------------------------------------ */
        /** Called when a dispatched thread is no longer handling the endpoint.
         * The selection key operations are updated.
         */
        private void undispatch()
            throws IOException	
        {
            synchronized(this)
            {
                _dispatched=false;
                
                synchronized(_unDispatched)
                {
                    _unDispatched.add(this);
                }
            }
            _selector.wakeup();
        }

        /* ------------------------------------------------------------ */
        /* 
         */
        public int fill(Buffer buffer) throws IOException
        {
            int l = super.fill(buffer);
            if (l<0)
                getChannel().close();
            return l;
        }

        /* ------------------------------------------------------------ */
        /* 
         */
        public void run()
        {
            try
            {
                _connection.handle();
            }
            catch(ClosedChannelException e)
            {
                Log.ignore(e);
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
                if (_key!=null)
                    _key.cancel();
                _key=null;
                try{close();}
                catch(IOException e2){Log.ignore(e2);}
            }
            catch(Throwable e)
            {
                Log.warn("handle failed",e);
                if (_key!=null)
                    _key.cancel();
                _key=null;
                try{close();}
                catch(IOException e2){Log.ignore(e2);}
            }
            finally
            {
                synchronized(this)
                {
                    try{undispatch();}catch(Exception e){ Log.warn(e); }
                }
            }
        }
        
    }
    
}

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

package org.mortbay.jetty.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.mortbay.jetty.io.Connection;
import org.mortbay.jetty.io.EndPoint;
import org.mortbay.jetty.io.nio.SelectChannelEndPoint;
import org.mortbay.jetty.io.nio.SelectorManager;
import org.mortbay.jetty.io.nio.SelectorManager.SelectSet;
import org.mortbay.jetty.server.HttpConnection;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.thread.Timeout.Task;

/* ------------------------------------------------------------------------------- */
/**
 * Selecting NIO connector.
 * <p>
 * This connector uses efficient NIO buffers with a non blocking threading model. Direct NIO buffers
 * are used and threads are only allocated to connections with requests. Synchronization is used to
 * simulate blocking for the servlet API, and any unflushed content at the end of request handling
 * is written asynchronously.
 * </p>
 * <p>
 * This connector is best used when there are a many connections that have idle periods.
 * </p>
 * <p>
 * When used with {@link org.mortbay.jetty.server.server.util.ajax.Continuation}, threadless waits are supported. When
 * a filter or servlet calls getEvent on a Continuation, a {@link org.mortbay.jetty.server.server.RetryRequest}
 * runtime exception is thrown to allow the thread to exit the current request handling. Jetty will
 * catch this exception and will not send a response to the client. Instead the thread is released
 * and the Continuation is placed on the timer queue. If the Continuation timeout expires, or it's
 * resume method is called, then the request is again allocated a thread and the request is retried.
 * The limitation of this approach is that request content is not available on the retried request,
 * thus if possible it should be read after the continuation or saved as a request attribute or as the
 * associated object of the Continuation instance.
 * </p>
 * 
 * @org.apache.xbean.XBean element="nioConnector" description="Creates an NIO based socket connector"
 * 
 * @author gregw
 *
 */
public class SelectChannelConnector extends AbstractNIOConnector 
{
    protected transient ServerSocketChannel _acceptChannel;
    private long _lowResourcesConnections;
    private long _lowResourcesMaxIdleTime;

    private SelectorManager _manager = new SelectorManager()
    {
        protected SocketChannel acceptChannel(SelectionKey key) throws IOException
        {
            // TODO handle max connections
            SocketChannel channel = ((ServerSocketChannel)key.channel()).accept();
            if (channel==null)
                return null;
            channel.configureBlocking(false);
            Socket socket = channel.socket();
            configure(socket);
            return channel;
        }

        public boolean dispatch(Runnable task)
        {
            return getThreadPool().dispatch(task);
        }

        protected void endPointClosed(SelectChannelEndPoint endpoint)
        {
            // TODO handle max connections and low resources
            connectionClosed((HttpConnection)endpoint.getConnection());
        }

        protected void endPointOpened(SelectChannelEndPoint endpoint)
        {
            // TODO handle max connections and low resources
            connectionOpened((HttpConnection)endpoint.getConnection());
        }

        protected Connection newConnection(SocketChannel channel,SelectChannelEndPoint endpoint)
        {
            return SelectChannelConnector.this.newConnection(channel,endpoint);
        }

        protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectSet selectSet, SelectionKey sKey) throws IOException
        {
            return SelectChannelConnector.this.newEndPoint(channel,selectSet,sKey);
        }
    };
    
    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     * 
     */
    public SelectChannelConnector()
    {
    }
    
    /* ------------------------------------------------------------ */
    public void accept(int acceptorID) throws IOException
    {
        _manager.doSelect(acceptorID);
    }
    
    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        synchronized(this)
        {
            if(_manager.isRunning())
            {
                try
                {
                    _manager.stop();
                }
                catch (Exception e)
                {
                    Log.warn(e);
                }
            }
            if (_acceptChannel != null)
                _acceptChannel.close();
            _acceptChannel = null;
        }
    }
    
    /* ------------------------------------------------------------------------------- */
    @Override
    public void customize(EndPoint endpoint, Request request) throws IOException
    {
        SelectChannelEndPoint cep = ((SelectChannelEndPoint)endpoint);
        cep.cancelIdle();
        request.setTimeStamp(cep.getSelectSet().getNow());
        super.customize(endpoint, request);
    }
    
    /* ------------------------------------------------------------------------------- */
    @Override
    public void persist(EndPoint endpoint) throws IOException
    {
        ((SelectChannelEndPoint)endpoint).scheduleIdle();
        super.persist(endpoint);
    }

    /* ------------------------------------------------------------ */
    public Object getConnection()
    {
        return _acceptChannel;
    }

    /* ------------------------------------------------------------------------------- */
    public int getLocalPort()
    {
        synchronized(this)
        {
            if (_acceptChannel==null || !_acceptChannel.isOpen())
                return -1;
            return _acceptChannel.socket().getLocalPort();
        }
    }

    /* ------------------------------------------------------------ */
    public void open() throws IOException
    {
        synchronized(this)
        {
            if (_acceptChannel == null)
            {
                // Create a new server socket
                _acceptChannel = ServerSocketChannel.open();

                // Bind the server socket to the local host and port
                _acceptChannel.socket().setReuseAddress(getReuseAddress());
                InetSocketAddress addr = getHost()==null?new InetSocketAddress(getPort()):new InetSocketAddress(getHost(),getPort());
                _acceptChannel.socket().bind(addr,getAcceptQueueSize());

                // Set to non blocking mode
                _acceptChannel.configureBlocking(false);
                
            }
        }
    }

    /* ------------------------------------------------------------ */
    public void setMaxIdleTime(int maxIdleTime)
    {
        _manager.setMaxIdleTime(maxIdleTime);
        super.setMaxIdleTime(maxIdleTime);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the lowResourcesConnections
     */
    public long getLowResourcesConnections()
    {
        return _lowResourcesConnections;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the number of connections, which if exceeded places this manager in low resources state.
     * This is not an exact measure as the connection count is averaged over the select sets.
     * @param lowResourcesConnections the number of connections
     * @see {@link #setLowResourcesMaxIdleTime(long)}
     */
    public void setLowResourcesConnections(long lowResourcesConnections)
    {
        _lowResourcesConnections=lowResourcesConnections;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the lowResourcesMaxIdleTime
     */
    public long getLowResourcesMaxIdleTime()
    {
        return _lowResourcesMaxIdleTime;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the period in ms that a connection is allowed to be idle when this there are more
     * than {@link #getLowResourcesConnections()} connections.  This allows the server to rapidly close idle connections
     * in order to gracefully handle high load situations.
     * @param lowResourcesMaxIdleTime the period in ms that a connection is allowed to be idle when resources are low.
     * @see {@link #setMaxIdleTime(long)}
     * @deprecated use {@link #setLowResourceMaxIdleTime(int)}
     */
    public void setLowResourcesMaxIdleTime(long lowResourcesMaxIdleTime)
    {
        _lowResourcesMaxIdleTime=lowResourcesMaxIdleTime;
        super.setLowResourceMaxIdleTime((int)lowResourcesMaxIdleTime); // TODO fix the name duplications
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the period in ms that a connection is allowed to be idle when this there are more
     * than {@link #getLowResourcesConnections()} connections.  This allows the server to rapidly close idle connections
     * in order to gracefully handle high load situations.
     * @param lowResourcesMaxIdleTime the period in ms that a connection is allowed to be idle when resources are low.
     * @see {@link #setMaxIdleTime(long)}
     */
    public void setLowResourceMaxIdleTime(int lowResourcesMaxIdleTime)
    {
        _lowResourcesMaxIdleTime=lowResourcesMaxIdleTime;
        super.setLowResourceMaxIdleTime(lowResourcesMaxIdleTime); 
    }

    
    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.server.server.AbstractConnector#doStart()
     */
    protected void doStart() throws Exception
    {
        _manager.setSelectSets(getAcceptors());
        _manager.setMaxIdleTime(getMaxIdleTime());
        _manager.setLowResourcesConnections(getLowResourcesConnections());
        _manager.setLowResourcesMaxIdleTime(getLowResourcesMaxIdleTime());
        _manager.start();
        open();
        _manager.register(_acceptChannel);
        super.doStart();
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.server.server.AbstractConnector#doStop()
     */
    protected void doStop() throws Exception
    {        
        super.doStop();
    }

    /* ------------------------------------------------------------ */
    protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectSet selectSet, SelectionKey key) throws IOException
    {
        return new SelectChannelEndPoint(channel,selectSet,key)
        {
            // TODO remove this hack
            public boolean isReadyForDispatch()
            {
                Request request = ((HttpConnection)getConnection()).getRequest();
                return super.isReadyForDispatch() && !(request.getAsyncRequest().isSuspended());
            }
        };
    }

    /* ------------------------------------------------------------------------------- */
    protected Connection newConnection(SocketChannel channel,final SelectChannelEndPoint endpoint)
    {
        return new HttpConnection(SelectChannelConnector.this,endpoint,getServer())
        {
            /* ------------------------------------------------------------ */
            public void cancelTimeout(Task task)
            {
                endpoint.getSelectSet().cancelTimeout(task);
            }

            /* ------------------------------------------------------------ */
            public void scheduleTimeout(Task task, long timeoutMs)
            {
                endpoint.getSelectSet().scheduleTimeout(task,timeoutMs);
            }
        };
    }
}
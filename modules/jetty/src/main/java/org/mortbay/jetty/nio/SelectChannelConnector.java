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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.mortbay.io.Connection;
import org.mortbay.io.nio.SelectChannelEndPoint;
import org.mortbay.io.nio.SelectorManager;
import org.mortbay.io.nio.SelectorManager.SelectSet;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.log.Log;
import org.mortbay.thread.Timeout;
import org.mortbay.util.ajax.Continuation;

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
 * When used with {@link org.mortbay.util.ajax.Continuation}, threadless waits are supported. When
 * a filter or servlet calls getEvent on a Continuation, a {@link org.mortbay.jetty.RetryRequest}
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
    private transient ServerSocketChannel _acceptChannel;
    private long _lowResourcesConnections;
    private long _lowResourcesMaxIdleTime;
    private boolean _dispatchToSuspended=true;

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

        public boolean dispatch(Runnable task) throws IOException
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
            if (_acceptChannel != null)
                _acceptChannel.close();
            _acceptChannel = null;
        }
    }
    
    /* ------------------------------------------------------------------------------- */
    public void customize(org.mortbay.io.EndPoint endpoint, Request request) throws IOException
    {
        ConnectorEndPoint cep = ((ConnectorEndPoint)endpoint);
        cep.cancelIdle();
        request.setTimeStamp(cep.getSelectSet().getNow());
        super.customize(endpoint, request);
    }
    
    /* ------------------------------------------------------------------------------- */
    public void persist(org.mortbay.io.EndPoint endpoint) throws IOException
    {
        ((ConnectorEndPoint)endpoint).scheduleIdle();
        super.persist(endpoint);
    }

    /* ------------------------------------------------------------ */
    public Object getConnection()
    {
        return _acceptChannel;
    }
    
    /* ------------------------------------------------------------ */
    /** Get delay select key update
     * If true, the select set is not updated when a endpoint is dispatched for
     * reading. The assumption is that the task will be short and thus will probably
     * be complete before the select is tried again.
     * @return Returns the assumeShortDispatch.
     */
    public boolean getDelaySelectKeyUpdate()
    {
        return _manager.isDelaySelectKeyUpdate();
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
    /*
     * @see org.mortbay.jetty.Connector#newContinuation()
     */
    public Continuation newContinuation()
    {
        return new RetryContinuation();
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
    /**
     * @param delay If true, updating a {@link SelectionKey} is delayed until a redundant event is 
     * schedules.  This is an optimization that assumes event handling can be completed before the next select
     * completes.
     */
    public void setDelaySelectKeyUpdate(boolean delay)
    {
        _manager.setDelaySelectKeyUpdate(delay);
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
    /**
     * @return the dispatchToSuspended True if IO activity should cause a dispatch to a suspended request.
     */
    public boolean isDispatchToSuspended()
    {
        return _dispatchToSuspended;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param dispatchToSuspended True if IO activity should cause a dispatch to a suspended request.
     */
    public void setDispatchToSuspended(boolean dispatchToSuspended)
    {
        _dispatchToSuspended=dispatchToSuspended;
    }

    
    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.AbstractConnector#doStart()
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
     * @see org.mortbay.jetty.AbstractConnector#doStop()
     */
    protected void doStop() throws Exception
    {
        _manager.stop();
        super.doStop();
    }

    /* ------------------------------------------------------------ */
    protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectSet selectSet, SelectionKey key) throws IOException
    {
        return new ConnectorEndPoint(channel,selectSet,key);
    }

    /* ------------------------------------------------------------------------------- */
    protected Connection newConnection(SocketChannel channel,SelectChannelEndPoint endpoint)
    {
        return new HttpConnection(SelectChannelConnector.this,endpoint,getServer());
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class ConnectorEndPoint extends SelectChannelEndPoint
    {
        public ConnectorEndPoint(SocketChannel channel, SelectSet selectSet, SelectionKey key)
        {
            super(channel,selectSet,key);
            scheduleIdle();
        }

        public void close() throws IOException
        {
            RetryContinuation continuation = (RetryContinuation) ((HttpConnection)getConnection()).getRequest().getContinuation();
            if (continuation != null && continuation.isPending())
                continuation.reset();

            super.close();
        }

        /* ------------------------------------------------------------ */
        public void run()
        {
            RetryContinuation continuation = (RetryContinuation) ((HttpConnection)getConnection()).getRequest().getContinuation();
            if (continuation!=null)
                continuation.dispatch();
            super.run();
        }
        
        /* ------------------------------------------------------------ */
        public void undispatch()
        {
            RetryContinuation continuation = (RetryContinuation) ((HttpConnection)getConnection()).getRequest().getContinuation();

            
            if (continuation == null)
                super.undispatch();
            else
            {
                // We have a continuation
                Log.debug("continuation {}", continuation);
                super.undispatch();
                boolean unsuspend = continuation.undispatch();
                if (unsuspend)
                    continuation.unsuspend();
            }
            
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class RetryContinuation implements Continuation
    {
        // Configuration
        final SelectChannelEndPoint _endPoint;
        
        
        // Main states are D=Dispatched, S=suspended, T=triggered
        // dst IDLE          Dst DISPATCHED
        // dsT scheduled     DsT RETRY
        // dSt SUSPENDED     DSt Suspending
        // dST unsuspending  DST unsuspending 
        boolean _dispatched=true;
        boolean _suspended;
        boolean _triggered;
        
        
        // Secondary state
        boolean _new = true;
        boolean _resumed = false;   // resume called.
        Object _object;
        
        // other
        RetryRequest _retry;
        long _timeout;
        Object _mutex;
        
        final Timeout.Task _timeoutTask;
        
        
        RetryContinuation()
        {
            _mutex=this;
                
            HttpConnection connection = HttpConnection.getCurrentConnection();
            _endPoint=(SelectChannelEndPoint)connection.getEndPoint();
            
            _timeoutTask= new Timeout.Task(_mutex)
            {
                public void expire()
                {
                    RetryContinuation.this.expire();
                }
            };
        }
        
        public void setMutex(Object mutex)
        {
            synchronized(_mutex)
            {
                _mutex=mutex;
                _timeoutTask.setMutex(mutex);
            }
        }

        
        public Object getObject()
        {
            return _object;
        }

        public long getTimeout()
        {
            return _timeout;
        }

        public boolean isNew()
        {
            return _new;
        }

        public boolean isPending()
        {
            return _suspended||_triggered;
        }

        public boolean isResumed()
        {
            return _resumed;
        }
        
        public boolean isExpired()
        {
            return _timeoutTask.isExpired();
        }

        public void reset()
        {
            synchronized (_mutex)
            {
                // To ?-- state
                _suspended=false;
                _triggered=false;
                
                
                _resumed = false;
            }
            
            synchronized (_endPoint.getSelectSet())
            {
                _timeoutTask.cancel();   
            }
        } 

        public boolean suspend(long timeout)
        {
            synchronized (_mutex)
            {
                if (!_dispatched)
                    throw new IllegalStateException("!Dispatched");
                if (_suspended)
                    throw new IllegalStateException("Suspended");
                
                
                if (_triggered)
                {
                    // suspending a retry == reset
                    _suspended=false;
                    _triggered=false;
                    
                    boolean resumed=_resumed;
                    _resumed=false;
                    return resumed;
                }
                else
                {
                    _suspended=true;

                    _timeout = timeout;
                    if (_retry==null)
                        _retry = new RetryRequest();
                }
            }

            _endPoint.getSelectSet().scheduleTimeout(_timeoutTask,_timeout);
            throw _retry;      
        }
        
        
        public void resume()
        {
            boolean unsuspend=false;
            synchronized (_mutex)
            {
                if (_suspended)
                {
                    _triggered=true;   
                    _resumed=true;

                    if (!_dispatched)
                    {
                        unsuspend=true;
                        _suspended=false;
                    }
                }
            }

            if (unsuspend)
                unsuspend();
        }

        private void expire()
        {

            boolean unsuspend=false;
            synchronized (this)
            {
                // move to ?ST
                if (_suspended)
                {
                    _triggered=true;
                
                    if (!_dispatched)
                    {
                        // -ST -> --T
                        unsuspend=true;
                        _suspended=false;
                    }
                }
            }
            
            if (unsuspend)
                unsuspend();
        }
        

        
        public void dispatch()
        {
            boolean suspended=false;
            synchronized (_mutex)
            {
                _dispatched=true;
                
                if (_suspended)
                {
                    suspended=true;
                    _suspended=false;
                    _triggered=true;
                }
            }

            if (suspended)
            {
                SelectSet selectSet = _endPoint.getSelectSet();
                synchronized (selectSet)
                {
                    _timeoutTask.cancel();   
                }
            }
        }
        
        
        public boolean undispatch()
        {
            boolean unsuspend=false;
        
            synchronized (_mutex)
            {
                _dispatched=false;
                _new = false;
                
                if (_suspended && _triggered)
                {
                    unsuspend=true;
                    _suspended=false;
                }
            }
            
            return unsuspend;
        }
        
        public void unsuspend()
        {
            SelectSet selectSet = _endPoint.getSelectSet();
            
            synchronized (selectSet)
            {
                _timeoutTask.cancel();   
            }

            _endPoint.scheduleIdle(); 
            try
            {
                _endPoint.dispatch();
            }
            catch(IOException e)
            {
                Log.warn(e);
                selectSet.addChange(_endPoint);
            }
        }
        
        
        

        public void setObject(Object object)
        {
            _object = object;
        }
        
        public String toString()
        {
            synchronized (this)
            {
                return "RetryContinuation@"+hashCode()+
                (_dispatched?",D":",d")+
                (_suspended?"S":"s")+
                (_triggered?"T":"t")+
                (_new?",new":"")+
                (_resumed?",resumed":"")+
                (isExpired()?",expired":"");
            }
        }

    }
}

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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.mortbay.io.Connection;
import org.mortbay.io.nio.SelectChannelEndPoint;
import org.mortbay.io.nio.SelectorManager;
import org.mortbay.io.nio.SelectorManager.SelectSet;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpException;
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
            if (_acceptChannel != null)
                _acceptChannel.close();
            _acceptChannel = null;
        }
    }
    
    /* ------------------------------------------------------------------------------- */
    public void customize(org.mortbay.io.EndPoint endpoint, Request request) throws IOException
    {
        SuspendableEndPoint cep = ((SuspendableEndPoint)endpoint);
        cep.cancelIdle();
        request.setTimeStamp(cep.getSelectSet().getNow());
        super.customize(endpoint, request);
    }
    
    /* ------------------------------------------------------------------------------- */
    public void persist(org.mortbay.io.EndPoint endpoint) throws IOException
    {
        ((SuspendableEndPoint)endpoint).scheduleIdle();
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
    public Continuation newContinuation(Connection connection)
    {
        return (SuspendableEndPoint)((HttpConnection)connection).getEndPoint();
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
        return new SuspendableEndPoint(channel,selectSet,key);
    }

    /* ------------------------------------------------------------------------------- */
    protected Connection newConnection(SocketChannel channel,SelectChannelEndPoint endpoint)
    {
        return new HttpConnection(SelectChannelConnector.this,endpoint,getServer());
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class SuspendableEndPoint extends SelectChannelEndPoint implements Continuation
    {
        // Main states are D=Dispatched, S=suspended, T=triggered
        // dst IDLE          Dst DISPATCHED
        // dsT scheduled     DsT RETRY
        // dSt SUSPENDED     DSt Suspending
        // dST unsuspending  DST unsuspending 
        protected boolean _suspended;
        protected boolean _triggered;
        
        // Secondary state
        protected boolean _new = true;
        protected boolean _resumed = false;   // resume called.
        protected Object _object;
        
        // other
        protected RetryRequest _retry;
        protected long _timeout;
        protected Object _mutex;
        
        protected final Timeout.Task _timeoutTask;
        
        
        // TODO remove this debugging aid
        /*
        String[] last = {null,null,null,null,null,null};
        void last(String l)
        {
            last[5]=last[4];
            last[4]=last[3];
            last[3]=last[2];
            last[2]=last[1];
            last[1]=last[0];
            last[0]=l;
        }*/
        
        
        
        public SuspendableEndPoint(SocketChannel channel, SelectSet selectSet, SelectionKey key)
        {
            super(channel,selectSet,key);

            _mutex=this;
            _dispatched=false;
            _suspended=false;
            _triggered=false;
                
            HttpConnection connection = HttpConnection.getCurrentConnection();
            
            _timeoutTask= new Timeout.Task(_mutex)
            {
                public void expire()
                {
                    SuspendableEndPoint.this.expire();
                }
            };
            
            scheduleIdle();
        }

        
        public void close() throws IOException
        {
            reset();
            super.close();
        }
        
        public void setMutex(Object mutex)
        {
            synchronized(_mutex)
            {
                // TODO - is this a good idea?
                // _mutex=mutex;
                // _timeoutTask.setMutex(mutex);
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
            synchronized(_mutex)
            {
                return _new;
            }
        }

        public boolean isPending()
        {
            synchronized(_mutex)
            {
                return _suspended||_triggered;
            }
        }

        public boolean isResumed()
        {
            synchronized(_mutex)
            {
                return _resumed;
            }
        }
        
        public boolean isExpired()
        {
            synchronized(_mutex)
            {
                return _timeoutTask.isExpired();
            }
        }

        public void reset()
        {
            synchronized (_mutex)
            {
                // last("reset");
                _suspended=false;
                _triggered=false;
  
                _resumed = false;
            }
            
            synchronized (getSelectSet())
            {
                _timeoutTask.cancel();   
            }
        } 

        
        public boolean suspend(long timeout)
        {
            synchronized (_mutex)
            {
                // last("suspend");
                if (!_dispatched)
                    throw new IllegalStateException("!Dispatched "+this);
                if (_suspended)
                    throw new IllegalStateException("Suspended "+this);
                      
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

            getSelectSet().scheduleTimeout(_timeoutTask,_timeout);
            throw _retry;      
        }
        
        public void resume()
        {
            boolean dispatched=false;
            synchronized (_mutex)
            {
                // last("resume");
                if (_suspended)
                {
                    _triggered=true;   
                    _resumed=true;

                    if (!_dispatched)
                    {
                        _new =false;
                        dispatched=super.dispatch();
                        _suspended=false;
                    }
                }
            }

            if (dispatched)
                getSelectSet().cancelIdle(_timeoutTask);
        }

        private void expire()
        {
            boolean dispatched=false;
            synchronized (_mutex)
            {
                // last("expire");
                if (_suspended)
                {
                    _triggered=true;
                
                    if (!_dispatched)
                    {
                        // -ST -> --T
                        _new =false;
                        dispatched=super.dispatch();
                        _suspended=false;
                    }
                }
            }
            
            if (dispatched)
                getSelectSet().cancelIdle(_timeoutTask);
        }
           
        protected boolean dispatch()
        {
            boolean dispatched=false;
            boolean dispatchedFromSuspended=false;
            synchronized (_mutex)
            {
                // last("dispatch");
                _new=false;
                dispatched=super.dispatch();
                
                if (_suspended)
                {
                    _suspended=false;
                    _triggered=true;
                    dispatchedFromSuspended=true;
                }
            }

            if (dispatchedFromSuspended)
                getSelectSet().cancelTimeout(_timeoutTask);
            
            return dispatched;
        }
        
        public boolean undispatch()
        {
            synchronized (_mutex)
            {
                if (!_dispatched)
                    throw new IllegalStateException();
                // last("undispatch");
                
                if (_suspended&&_triggered)
                {
                    // short circuit
                    _suspended=false;
                    // last("!undispatch");
                    return false;
                }
                
                return super.undispatch();
            }
        }

        public void setObject(Object object)
        {
            _object = object;
        }
        
        public String toString()
        {
            synchronized (_mutex)
            {
                return "RetryContinuation@"+hashCode()+
                (_dispatched?",D":",d")+
                (_suspended?"S":"s")+
                (_triggered?"T":"t")+
                (_new?",new":"")+
                (_resumed?",resumed":"")+
                (isExpired()?",expired":"");
                // ">"+last[5]+">"+last[4]+">"+last[3]+">"+last[2]+">"+last[1]+">"+last[0];
            }
        }

    }
}

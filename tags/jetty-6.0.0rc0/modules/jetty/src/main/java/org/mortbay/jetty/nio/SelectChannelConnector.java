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
import java.nio.channels.ByteChannel;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.AbstractConnector;
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
public class SelectChannelConnector extends AbstractConnector implements NIOConnector
{
    private transient ServerSocketChannel _acceptChannel;
    private transient SelectionKey _acceptKey;
    private transient SelectSet[] _selectSets;
    private boolean _delaySelectKeyUpdate=false;


    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     * 
     */
    public SelectChannelConnector()
    {
    }

    /* ------------------------------------------------------------ */
    public Object getConnection()
    {
        return _acceptChannel;
    }
    
    /* ------------------------------------------------------------ */
    /** Assume Short Dispatch
     * If true, the select set is not updated when a endpoint is dispatched for
     * reading. The assumption is that the task will be short and thus will probably
     * be complete before the select is tried again.
     * @return Returns the assumeShortDispatch.
     */
    public boolean getDelaySelectKeyUpdate()
    {
        return _delaySelectKeyUpdate;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param delay If true, updating a {@link SelectionKey} is delayed until a redundant event is 
     * schedules.  This is an optimization that assumes event handling can be completed before the next select
     * completes.
     */
    public void setDelaySelectKeyUpdate(boolean delay)
    {
        _delaySelectKeyUpdate = delay;
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.AbstractConnector#doStart()
     */
    protected void doStart() throws Exception
    {
        _selectSets = new SelectSet[getAcceptors()];
        for (int i=0;i<_selectSets.length;i++)
            _selectSets[i]= new SelectSet(i);
        
        super.doStart();
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.AbstractConnector#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();
        for (int i=0;i<_selectSets.length;i++)
            _selectSets[i].stop();
        _selectSets=null;
    }


    /* ------------------------------------------------------------ */
    public void open() throws IOException
    {
        if (_acceptChannel == null)
        {
            // Create a new server socket and set to non blocking mode
            _acceptChannel = ServerSocketChannel.open();
            _acceptChannel.configureBlocking(false);

            // Bind the server socket to the local host and port
            InetSocketAddress addr = getHost()==null?new InetSocketAddress(getPort()):new InetSocketAddress(getHost(),getPort());
            _acceptChannel.socket().bind(addr,getAcceptQueueSize());

            // Register accepts on the server socket with the selector.
            synchronized (_selectSets[0])
            {
                _acceptKey = _acceptChannel.register(_selectSets[0].getSelector(), SelectionKey.OP_ACCEPT);
            }
        }
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        if (_acceptChannel != null)
            _acceptChannel.close();
        _acceptChannel = null;

    }

    /* ------------------------------------------------------------ */
    public void accept(int acceptorID) throws IOException
    {
        if (_selectSets!=null && _selectSets.length>acceptorID && _selectSets[acceptorID]!=null)
            _selectSets[acceptorID].accept();
    }

    /* ------------------------------------------------------------------------------- */
    protected Buffer newBuffer(int size)
    {
        // TODO
        // Header buffers always byte array buffers (efficiency of random access)
        // There are lots of things to consider here... DIRECT buffers are faster to
        // send but more expensive to build and access! so we have choices to make...
        // + headers are constructed bit by bit and parsed bit by bit, so INDiRECT looks
        // good for them.   
        // + but will a gather write of an INDIRECT header with a DIRECT body be any good?
        // this needs to be benchmarked.
        // + Will it be possible to get a DIRECT header buffer just for the gather writes of
        // content from file mapped buffers?  
        // + Are gather writes worth the effort?  Maybe they will work well with two INDIRECT
        // buffers being copied into a single kernel buffer?
        // 
        if (size==getHeaderBufferSize())
            return new NIOBuffer(size, NIOBuffer.INDIRECT);
        return new NIOBuffer(size, NIOBuffer.DIRECT);
    }

    /* ------------------------------------------------------------------------------- */
    public void customize(EndPoint endpoint, Request request) throws IOException
    {
        HttpChannelEndPoint ep = (HttpChannelEndPoint)endpoint;
        if (ep.getTimeoutTask().isShort())
            ep.getSelectSet().scheduleIdle(ep.getTimeoutTask(), false);
        
        super.customize(endpoint, request);
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * TODO Comments?
     */
    public HttpChannelEndPoint newHttpChannelEndPoint(SelectChannelConnector connector, SocketChannel channel, SelectChannelConnector.SelectSet selectSet, SelectionKey sKey) throws IOException
    {
        return new HttpChannelEndPoint(connector, channel, selectSet, sKey);
    }

    /* ------------------------------------------------------------------------------- */
    public int getLocalPort()
    {
        if (_acceptChannel==null || !_acceptChannel.isOpen())
            return -1;
        return _acceptChannel.socket().getLocalPort();
    }
    
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    public class SelectSet 
    {
        private transient int _setID;
        private transient Timeout _idleTimeout;
        private transient Timeout _shortIdleTimeout;
        private transient Timeout _retryTimeout;
        private transient Selector _selector;
        private transient List[] _changes;
        private transient int _change;
        private transient int _nextSet;

        /* ------------------------------------------------------------ */
        SelectSet(int acceptorID) throws Exception
        {
            _setID=acceptorID;
            
            _idleTimeout = new Timeout();
            _idleTimeout.setDuration(getMaxIdleTime());
            _shortIdleTimeout = new Timeout();
            _shortIdleTimeout.setDuration(getLowResourceMaxIdleTime());
            _retryTimeout = new Timeout();
            _retryTimeout.setDuration(0L);

            // create a selector;
            _selector = Selector.open();
            _changes = new ArrayList[] {new ArrayList(),new ArrayList()};
            _change=0;
        }

        /* ------------------------------------------------------------ */
        Selector getSelector()
        {
            return _selector;
        }

        /* ------------------------------------------------------------ */
        void stop() throws Exception
        {
            synchronized(this)
            {
                _idleTimeout.cancelAll();
                _shortIdleTimeout.cancelAll();
                _retryTimeout.cancelAll();
                try
                {
                    if (_selector != null)
                        _selector.close();
                }
                catch (IOException e)
                {
                    Log.ignore(e);
                } 
            }
        }

        /* ------------------------------------------------------------ */
        public void accept() throws IOException
        {
            long short_next = 0;
            long idle_next = 0;
            long retry_next = 0;
            
            List changes;
            synchronized (_changes)
            {
                changes=_changes[_change];
                _change=_change==0?1:0;
            }
            
            // Make any key changes required
            for (int i = 0; i < changes.size(); i++)
            {
                try
                {
                    Object o = changes.get(i);
                    if (o instanceof HttpChannelEndPoint)
                    {
                        // Update the operatios for a key.
                        HttpChannelEndPoint endpoint = (HttpChannelEndPoint)o;
                        endpoint.syncKey();
                    }
                    else if (o instanceof SocketChannel)
                    {
                        // finish accepting this connection
                        SocketChannel channel=(SocketChannel)o;
                        SelectionKey cKey = channel.register(_selector, SelectionKey.OP_READ);
                        HttpChannelEndPoint endpoint = newHttpChannelEndPoint(SelectChannelConnector.this,channel,this,cKey);
                        
                        if (_delaySelectKeyUpdate && endpoint.dispatch(_delaySelectKeyUpdate))
                            dispatch(endpoint);
                            
                    }
                    else if (o instanceof RetryContinuation)
                    {
                        dispatch(((RetryContinuation)o)._endPoint);
                    }
                    else
                        throw new IllegalStateException();
                }
                catch (CancelledKeyException e)
                {
                    if (isRunning())
                        Log.warn(e);
                    else
                        Log.debug(e);
                }
            }
            changes.clear();
            
            synchronized (this)
            {
                _shortIdleTimeout.setDuration(getLowResourceMaxIdleTime());
                _idleTimeout.setDuration(getMaxIdleTime());
                short_next=_shortIdleTimeout.getTimeToNext();
                idle_next=_idleTimeout.getTimeToNext();
                retry_next=_retryTimeout.getTimeToNext();
            }
                        
            // workout how low to wait in select
            long wait = getMaxIdleTime();
            if (wait < 0 || short_next >= 0 && wait > short_next)
                wait = short_next;
            if (wait < 0 || idle_next >= 0 && wait > idle_next)
                wait = idle_next;
            if (wait < 0 || retry_next >= 0 && wait > retry_next)
                wait = retry_next;
            
            
            // Do the select.
            if (wait > 0)
                _selector.select(wait);
            else if (wait == 0)
                _selector.selectNow();
            else
                _selector.select();
            
            long now=-1;
            
            // have we been destroyed while sleeping
            if (!_selector.isOpen())
                return;
            
            // update the timers for task schedule in this loop
            now = System.currentTimeMillis();
            _shortIdleTimeout.setNow(now);
            _idleTimeout.setNow(now);
            _retryTimeout.setNow(now);
            
            // Look for things to do
            Iterator iter = _selector.selectedKeys().iterator();
            while (iter.hasNext())
            {
                SelectionKey key = (SelectionKey) iter.next();
                iter.remove();

                try
                {
                    if (!key.isValid())
                    {
                        key.cancel();
                        HttpChannelEndPoint endpoint = (HttpChannelEndPoint)key.attachment();
                        if (endpoint != null)
                            endpoint.close();
                        continue;
                    }

                    if (key.equals(_acceptKey))
                    {
                        if (key.isAcceptable())
                        {
                            // Accept a new connection.
                            SocketChannel channel = _acceptChannel.accept();
                            if (channel==null)
                                continue;
                            channel.configureBlocking(false);
                            Socket socket = channel.socket();
                            configure(socket);
                            
                            // TODO make it reluctant to leave 0
                            _nextSet=++_nextSet%_selectSets.length;
                            
                            // Is this for this selectset
                            if (_nextSet!=_setID)
                            {
                                // nope - give it to another.
                                _selectSets[_nextSet].addChange(channel);
                                _selectSets[_nextSet].wakeup();
                            }
                            else
                            {
                                // bind connections to this select set.
                                SelectionKey cKey = channel.register(_selectSets[_nextSet].getSelector(), SelectionKey.OP_READ);
                                newHttpChannelEndPoint(SelectChannelConnector.this,channel,_selectSets[_nextSet],cKey);
                            }
                        }
                    }
                    else
                    {
                        HttpChannelEndPoint endpoint = (HttpChannelEndPoint)key.attachment();
                        if (endpoint != null && endpoint.dispatch(_delaySelectKeyUpdate))
                            dispatch(endpoint);
                    }

                    key = null;
                }
                catch (CancelledKeyException e)
                {
                    // TODO investigate if this actually is a problem?
                    if (isRunning())
                        Log.warn(e);
                    else
                        Log.ignore(e);
                }
                catch (Exception e)
                {
                    if (isRunning())
                        Log.warn(e);
                    else
                        Log.ignore(e);
                    if (key != null && key != _acceptKey)
                        key.interestOps(0);
                }
            }


            // tick over the timer
            synchronized (this)
            {
                now = System.currentTimeMillis();
                _retryTimeout.setNow(now);
                _shortIdleTimeout.setNow(now);
                _idleTimeout.setNow(now);
            }
            
            while (_selector!=null)
            {
                Timeout.Task task=null;
                synchronized(this)
                {
                    task=_shortIdleTimeout.expired();
                    if (task==null)
                        task=_idleTimeout.expired();
                    if (task==null)
                        task=_retryTimeout.expired();
                }
                if (task==null)
                    break;
                else
                    task.expire();
            }

        }
        
        /* ------------------------------------------------------------------------------- */
        private void dispatch(HttpChannelEndPoint endpoint)
        {
            boolean dispatch_done = false;
            try
            {
                dispatch_done = getThreadPool().dispatch(endpoint);
            }
            finally
            {
                if (!dispatch_done)
                {
                    Log.warn("dispatch failed! threads="+SelectChannelConnector.this.getThreadPool().getThreads()+" idle="+SelectChannelConnector.this.getThreadPool().getIdleThreads());
                    endpoint.undispatch();
                }
            }
        }

        /* ------------------------------------------------------------ */
        public void scheduleIdle(HttpChannelEndPoint.IdleTask task, boolean idle)
        {
            synchronized (this)
            {
                if (idle && getServer().getThreadPool().isLowOnThreads())
                {
                    task.setShort(true);
                    task.schedule(_shortIdleTimeout);
                }
                else
                {
                    task.setShort(false);
                    task.schedule(_idleTimeout);
                }
            }
        }
        
        /* ------------------------------------------------------------ */
        public void cancelIdle(HttpChannelEndPoint.IdleTask task)
        {
            synchronized (this)
            {
                task.cancel();
            }
        }

        /* ------------------------------------------------------------ */
        public void scheduleTimeout(Timeout.Task task, long timeout)
        {
            synchronized (this)
            {
                _retryTimeout.schedule(task, timeout);
            }
        }

        /* ------------------------------------------------------------ */
        public void addChange(Object point)
        {
            synchronized (_changes)
            {
                _changes[_change].add(point);
            }
        }

        /* ------------------------------------------------------------ */
        public void wakeup()
        {
            Selector selector = _selector;
            if (selector!=null)
                selector.wakeup();
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
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class RetryContinuation extends Timeout.Task implements Continuation
    {
        Object _object;
        HttpChannelEndPoint _endPoint=(HttpChannelEndPoint)HttpConnection.getCurrentConnection().getEndPoint();
        long _timeout;
        boolean _new = true;
        boolean _pending = false;
        boolean _resumed = false;
        boolean _scheduled =false;
        RetryRequest _retry;
        
        /* Called when a run exits */
        public boolean schedule()
        {
            boolean redispatch=false;
        
            synchronized (this)
            {
                if (!_pending)
                    return false;
                _scheduled = true;
                
                redispatch=isExpired() || _resumed;
            }
            
            if (redispatch)
                _endPoint._selectSet.addChange(this);
            else
                _endPoint._selectSet.scheduleTimeout(this,_timeout);
            
            _endPoint._selectSet.wakeup();
            return true;
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
            return _pending;
        }

        public void expire()
        {
            boolean redispatch=false;
            synchronized (this)
            {
                redispatch=_scheduled && _pending && !_resumed;
            }
            if (redispatch)
            {
                _endPoint._selectSet.addChange(this);
                _endPoint._selectSet.wakeup();
            }
        }

        public boolean suspend(long timeout)
        {
            boolean resumed=false;
            synchronized (this)
            {
                resumed=_resumed;
                _resumed=false;
                _new = false;
                if (!_pending && !resumed && timeout > 0)
                {
                    _pending=true;
                    _scheduled = false;
                    _timeout = timeout;
                    if (_retry==null)
                     _retry = new RetryRequest();
                    throw _retry;
                }
                
                // here only if suspend called on pending continuation.
                // acts like a reset
                _resumed = false;
                _pending = false;
            }

            synchronized (_endPoint._selectSet)
            {
                this.cancel();   
            }

            return resumed;
        } 
        
        public void resume()
        {
            boolean redispatch=false;
            synchronized (this)
            {
                if (_pending && !isExpired())
                {
                    _resumed = true;
                    redispatch=_scheduled;
                }
            }

            if (redispatch)
            {
                synchronized (_endPoint._selectSet)
                {
                    this.cancel();   
                }

                _endPoint._selectSet.addChange(this);
                _endPoint._selectSet.wakeup();
            }
        }
        
        public void reset()
        {
            synchronized (this)
            {
                _resumed = false;
                _pending = false;
                _scheduled = false;
            }
            
            synchronized (_endPoint._selectSet)
            {
                this.cancel();   
            }
        }
        
        public Object getObject()
        {
            return _object;
        }

        public void setObject(Object object)
        {
            _object = object;
        }

    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class SelectChannelEndPoint extends ChannelEndPoint
    {
        protected SelectChannelConnector _connector;
        protected HttpConnection _connection;
        private boolean _closed;

        /* ------------------------------------------------------------ */
        public SelectChannelEndPoint(SelectChannelConnector connector, ByteChannel channel)
        {
            super(channel);
        }

        /* ------------------------------------------------------------ */
        protected void open(HttpConnection connection)
        {
            _connector.connectionOpened(_connection);
        }
        
        /* ------------------------------------------------------------ */
        /*
         * @see org.mortbay.io.nio.ChannelEndPoint#close()
         */
        public void close() throws IOException
        {
            synchronized (this)
            {
                if (!_closed && _connection!=null)
                    _connector.connectionClosed(_connection);
                _closed = true;
            }
            super.close();
        }
    }
}

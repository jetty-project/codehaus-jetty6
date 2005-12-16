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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.HttpConnection;
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
 * @author gregw
 */
public class SelectChannelConnector extends AbstractConnector
{
    private transient ServerSocketChannel _acceptChannel;
    private transient SelectionKey _acceptKey;
    private transient SelectSet[] _selectSets;
    private boolean _assumeShortDispatch=false;


    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     * 
     */
    public SelectChannelConnector()
    {
    }

    /* ------------------------------------------------------------ */
    /** Assume Short Dispatch
     * If true, the select set is not updated when a endpoint is dispatched for
     * reading. The assumption is that the task will be short and thus will probably
     * be complete before the select is tried again.
     * @return Returns the assumeShortDispatch.
     */
    public boolean getAssumeShortDispatch()
    {
        return _assumeShortDispatch;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param assumeShortDispatch The assumeShortDispatch to set.
     */
    public void setAssumeShortDispatch(boolean assumeShortDispatch)
    {
        _assumeShortDispatch = assumeShortDispatch;
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
            _selectSets[i].destroy();
        _selectSets=null;
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.AbstractConnector#setMaxIdleTime(long)
     */
    public void setMaxIdleTime(long maxIdleTime)
    {
        super.setMaxIdleTime(maxIdleTime);
        // TODO update SelectSets
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
            _acceptChannel.socket().bind(getAddress());

            // Register accepts on the server socket with the selector.
            _acceptKey = _acceptChannel.register(_selectSets[0].getSelector(), SelectionKey.OP_ACCEPT);
        }
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        if (_acceptChannel != null)
            _acceptChannel.close();
        _acceptChannel = null;

        // TODO stop SelectSets

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
        // Header buffers always byte array buffers (efficiency of random access)
        if (size==getHeaderBufferSize())
            return new NIOBuffer(size,false);
        return new NIOBuffer(size, true);
    }

    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    private class SelectSet 
    {
        private transient int _setID;
        private transient Timeout _idleTimeout;
        private transient Timeout _retryTimeout;
        private transient Selector _selector;
        private transient List _changes;
        private transient int _nextSet;

        /* ------------------------------------------------------------ */
 
        SelectSet(int acceptorID) throws Exception
        {
            _setID=acceptorID;
            
            _idleTimeout = new Timeout();
            _idleTimeout.setDuration(getMaxIdleTime());
            _retryTimeout = new Timeout();
            _retryTimeout.setDuration(0L);

            // create a selector;
            _selector = Selector.open();
            _changes = new ArrayList();

        }

        Selector getSelector()
        {
            return _selector;
        }

        /* ------------------------------------------------------------ */
 
        void destroy() throws Exception
        {
            _idleTimeout.cancelAll();
            _idleTimeout = null;
            _retryTimeout.cancelAll();
            _retryTimeout = null;

            try
            {
                if (_selector != null)
                    _selector.close();
            }
            catch (IOException e)
            {
                Log.ignore(e);
            }

            _selector = null;

        }

        /* ------------------------------------------------------------ */
        public void accept() throws IOException
        {
            // Make any key changes required
            synchronized (_changes)
            {
                for (int i = 0; i < _changes.size(); i++)
                {
                    try
                    {
                        Object o = _changes.get(i);
                        if (o instanceof SocketChannel)
                        {
                            // finish accepting this connection
                            SocketChannel channel=(SocketChannel)o;
                            SelectionKey cKey = channel.register(_selector, SelectionKey.OP_READ);
                            HttpEndPoint connection = new HttpEndPoint(channel,this);
                            connection.setKey(cKey);
                           
                            // assume something to do for this connection.
                            connection.dispatch();
                        }
                        else
                        {
                            // Update the operatios for a key.
                            HttpEndPoint c = (HttpEndPoint) o;
                            if (c._interestOps >= 0 && c._key != null && c._key.isValid())
                            {
                                c._key.interestOps(c._interestOps);
                            }
                            else
                            {
                                if (c._key != null && c._key.isValid())
                                    c._key.cancel();
                                c._key = null;
                            }
                        }
                    }
                    catch (CancelledKeyException e)
                    {
                        Log.warn(e);
                    }
                }
                _changes.clear();
            }

            // workout how low to wait in select
            long wait = getMaxIdleTime();
            long to_next = _idleTimeout.getTimeToNext();
            if (wait < 0 || to_next >= 0 && wait > to_next)
                wait = to_next;
            to_next = _retryTimeout.getTimeToNext();
            if (wait < 0 || to_next >= 0 && wait > to_next)
                wait = to_next;
            
            // Do the select.
            if (wait > 0)
                _selector.select(wait);
            else if (wait == 0)
                _selector.selectNow();
            else
                _selector.select();
            
            // have we been destroyed while sleeping
            if (_selector==null)
                return;

            // update the timers for task schedule in this loop
            long now = System.currentTimeMillis();
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
                        HttpEndPoint connection = (HttpEndPoint) key.attachment();
                        if (connection != null)
                            connection._key = null;
                        continue;
                    }

                    if (key.equals(_acceptKey))
                    {
                        if (key.isAcceptable())
                        {
                            // Accept a new connection.
                            SocketChannel channel = _acceptChannel.accept();
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
                                HttpEndPoint connection = new HttpEndPoint(channel,_selectSets[_nextSet]);
                                connection.setKey(cKey);
                                
                                // assume something to do for this connection.
                                connection.dispatch();
                            }
                            
                        }
                    }
                    else
                    {
                        HttpEndPoint connection = (HttpEndPoint) key.attachment();
                        if (connection != null)
                            connection.dispatch();
                    }

                    key = null;
                }
                catch (CancelledKeyException e)
                {
                    if (isRunning()) // TODO investigate if this actually is a problem?
                        Log.debug(e.toString());
                }
                catch (Exception e)
                {
                    if (isRunning())
                        Log.warn(e);
                    if (key != null && key != _acceptKey)
                        key.interestOps(0);
                }
            }

            // tick over the timer
            now = System.currentTimeMillis();
            _retryTimeout.setNow(now);
            _retryTimeout.tick();
            _idleTimeout.setNow(now);
            _idleTimeout.tick();

        }

        public void scheduleIdle(Timeout.Task task)
        {
            task.schedule(_idleTimeout);
        }

        public void scheduleTimeout(Timeout.Task task, long timeout)
        {
            _retryTimeout.schedule(task, timeout);
        }

        public void addChange(Object point)
        {
            synchronized (_changes)
            {
                _changes.add(point);
            }
        }

        public void wakeup()
        {
            _selector.wakeup();
        }


    }

    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    private class HttpEndPoint extends ChannelEndPoint implements Runnable
    {
        SelectSet _selectSet;
        boolean _dispatched = false;
        boolean _writable = true; // TODO - get rid of this bad side effect
        SelectionKey _key;
        HttpConnection _connection;
        int _interestOps;
        int _readBlocked;
        int _writeBlocked;

        IdleTask _timeoutTask = new IdleTask();

        /* ------------------------------------------------------------ */
        HttpEndPoint(SocketChannel channel, SelectSet selectSet)
        {
            super(channel);
            _selectSet=selectSet;
            _connection = new HttpConnection(SelectChannelConnector.this, this, getServer());
            _selectSet.scheduleIdle(_timeoutTask);
      
        }

        /* ------------------------------------------------------------ */
        void setKey(SelectionKey key)
        {
            _key = key;
            _key.attach(this);
        }

        /* ------------------------------------------------------------ */
        /**
         * Dispatch the endpoint by arranging for a thread to service it. Either a blocked thread is
         * woken up or the endpoint is passed to the server job queue. If the thread is dispatched
         * and then the selection key is modified so that it is no longer selected.
         */
        void dispatch() throws IOException
        {
            synchronized (this)
            {
                if (_key==null)
                {
                    _timeoutTask.cancel();
                    return;
                }
                
                _timeoutTask.reschedule();

                // If threads are blocked on this
                if (_readBlocked > 0 || _writeBlocked > 0)
                {
                    // wake them up is as good as a dispatched.
                    this.notifyAll();
                    
                    // we are not interested in further selecting
                    _key.interestOps(0);
                    return;
                }
                else if (!_assumeShortDispatch)
                    _key.interestOps(0);
                    
                // Otherwise if we are still dispatched
                if (_dispatched)
                {
                    // we are not interested in further selecting
                    _key.interestOps(0);
                    return;
                }

                // Remove writeable op
                if (_key==null)
                    return;
                if ((_key.readyOps() | SelectionKey.OP_WRITE) != 0 && (_key.interestOps() | SelectionKey.OP_WRITE) != 0)
                    // Remove writeable op
                    _key.interestOps(_interestOps = _key.interestOps() & (-1 ^ SelectionKey.OP_WRITE));
                
                _dispatched = true;
            }

            boolean dispatch_done = false;
            try
            {
                dispatch_done = getThreadPool().dispatch(this);
            }
            finally
            {
                if (!dispatch_done)
                {
                    Log.warn("dispatch failed! threads="+getThreadPool().getThreads()+" idle="+getThreadPool().getIdleThreads());
                    undispatch();
                }
            }
        }

        /* ------------------------------------------------------------ */
        /**
         * Called when a dispatched thread is no longer handling the endpoint. The selection key
         * operations are updated.
         */
        private void undispatch()
        {
            try
            {
                _dispatched = false;

                if (getChannel().isOpen())
                    updateKey();
            }
            catch (Exception e)
            {
                Log.warn(e);
                _interestOps = -1;
                _selectSet.addChange(this);
            }
        }

        /* ------------------------------------------------------------ */
        /* 
         */
        public int fill(Buffer buffer) throws IOException
        {
            int l = super.fill(buffer);
            if (l < 0)
                getChannel().close();
            return l;
        }

        /* ------------------------------------------------------------ */
        /*
         */
        public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
        {
            int l = super.flush(header, buffer, trailer);
            _writable = l > 0;
            return l;
        }

        /* ------------------------------------------------------------ */
        /*
         */
        public int flush(Buffer buffer) throws IOException
        {
            int l = super.flush(buffer);
            _writable = l > 0;
            return l;
        }

        /* ------------------------------------------------------------ */
        /*
         * Allows thread to block waiting for further events.
         */
        public void blockReadable(long timeoutMs)
        {
            synchronized (this)
            {
                if (getChannel().isOpen() && _key.isValid())
                {
                    try
                    {
                        _readBlocked++;
                        updateKey();
                        this.wait(timeoutMs);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        _readBlocked--;
                    }
                }
            }
        }

        /* ------------------------------------------------------------ */
        /*
         * Allows thread to block waiting for further events.
         */
        public void blockWritable(long timeoutMs)
        {
            synchronized (this)
            {
                if (getChannel().isOpen() && _key.isValid())
                {
                    try
                    {
                        _writeBlocked++;
                        updateKey();
                        this.wait(timeoutMs);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        _writeBlocked--;
                    }
                }
            }
        }

        /* ------------------------------------------------------------ */
        /**
         * Updates selection key. Adds operations types to the selection key as needed. No
         * operations are removed as this is only done during dispatch
         */
        private void updateKey()
        {
            synchronized (this)
            {
                int ops = _key == null ? 0 : _key.interestOps();
                _interestOps = ops | ((!_dispatched || _readBlocked > 0) ? SelectionKey.OP_READ : 0) | ((!_writable || _writeBlocked > 0) ? SelectionKey.OP_WRITE : 0);
                _writable = true; // Once writable is in ops, only removed with dispatch.

                if (_interestOps != ops)
                {
                    _selectSet.addChange(this);
                    _selectSet.wakeup();
                }
            }
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
            catch (ClosedChannelException e)
            {
                Log.ignore(e);
            }
            catch (IOException e)
            {
                // TODO - better than this
                if ("BAD".equals(e.getMessage()))
                {
                    Log.warn("BAD Request");
                    Log.debug("BAD", e);
                }
                else if ("EOF".equals(e.getMessage()))
                    Log.debug("EOF", e);
                else
                    Log.warn("IO", e);
                if (_key != null)
                    _key.cancel();
                _key = null;
                try
                {
                    close();
                }
                catch (IOException e2)
                {
                    Log.ignore(e2);
                }
            }
            catch (Throwable e)
            {
                Log.warn("handle failed", e);
                if (_key != null)
                    _key.cancel();
                _key = null;
                try
                {
                    close();
                }
                catch (IOException e2)
                {
                    Log.ignore(e2);
                }
            }
            finally
            {
                synchronized (this)
                {
                    RetryContinuation continuation = (RetryContinuation) _connection.getRequest().getContinuation();
                    if (continuation != null && continuation.isPending())
                    {
                        // We have a continuation
                        Log.debug("continuation {}", continuation);
                        long timeout = continuation.getTimeout();
                        continuation.setEndPoint(this);
                        _selectSet.scheduleTimeout(continuation,timeout);
                        _selectSet.wakeup();
                    }
                    else
                        undispatch();
                }
            }
        }

        public String toString()
        {
            return "HEP[d=" + _dispatched + ",io=" + _interestOps + ",w=" + _writable + ",b=" + _readBlocked + "|" + _writeBlocked + "]";
        }

        /* ------------------------------------------------------------ */
        /* ------------------------------------------------------------ */
        /* ------------------------------------------------------------ */
        private class IdleTask extends Timeout.Task
        {
            /* ------------------------------------------------------------ */
            /*
             * @see org.mortbay.thread.Timeout.Task#expire()
             */
            public void expire()
            {
                try
                {
                    close();
                }
                catch (IOException e)
                {
                    Log.ignore(e);
                }
            }

            public String toString()
            {
                return "TimeoutTask:" + HttpEndPoint.this.toString();
            }

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
    private class RetryContinuation extends Timeout.Task implements Continuation
    {
        Object _object;
        HttpEndPoint _endPoint;
        long _timeout;
        boolean _new = true;
        boolean _pending = false;
        boolean _resumed = false;

        void setEndPoint(HttpEndPoint ep)
        {
            synchronized (this)
            {
                _endPoint = ep;

                if (_resumed)
                    redispatch();
            }
        }

        long getTimeout()
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
            synchronized (this)
            {
                if (_pending)
                    redispatch();
            }
        }

        public boolean suspend(long timeout)
        {
            synchronized (this)
            {
                _new = false;
                
                if (_pending)
                { 
                    _pending=false;
                    return _resumed;
                }
                
                _pending=true;
                _resumed=false;
                
                if (!isExpired() && timeout > 0)
                {
                    if (_endPoint != null) { throw new IllegalStateException(); }
                    _timeout = timeout;
                    throw new RetryRequest();
                }
            }

            return _resumed;
        }

        public void resume()
        {
            synchronized (this)
            {
                if (isExpired())
                    return;

                boolean wakeup=_pending && !_resumed;
                _resumed = true;
                
                this.cancel();
                if (wakeup && _endPoint != null)
                    redispatch();
            }
        }

        private void redispatch()
        {
            boolean dispatch_done = false;
            try
            {
                dispatch_done = getThreadPool().dispatch(_endPoint);
            }
            finally
            {
                if (!dispatch_done)
                {
                    Log.warn("redispatch failed");
                    _endPoint.undispatch();
                }
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
}

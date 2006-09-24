package org.mortbay.jetty.nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.nio.SelectChannelConnector.RetryContinuation;
import org.mortbay.jetty.nio.SelectChannelConnector.SelectChannelEndPoint;
import org.mortbay.jetty.nio.SelectChannelConnector.SelectSet;
import org.mortbay.log.Log;
import org.mortbay.thread.Timeout;

public class HttpChannelEndPoint extends SelectChannelEndPoint implements Runnable
{
    public SelectSet _selectSet;
    private boolean _dispatched = false;
    protected boolean _writable = true; // TODO - get rid of this bad side effect
    protected SelectionKey _key;
    private int _interestOps;
    private int _readBlocked;
    private int _writeBlocked;

    private HttpChannelEndPoint.IdleTask _timeoutTask = new IdleTask();

    /* ------------------------------------------------------------ */
    public HttpChannelEndPoint(SelectChannelConnector connector, SocketChannel channel, SelectSet selectSet, SelectionKey key)
    {
        super(connector,channel);
        _selectSet = selectSet;
        _connector = connector;
        _connection = new HttpConnection(connector, this, connector.getServer());
        connectionOpened();
        _key = key;
        _key.attach(this);
        _selectSet.scheduleIdle(_timeoutTask, _connection.isIdle());
    }

    /* ------------------------------------------------------------ */
    /**
     * Dispatch the endpoint by arranging for a thread to service it. Either a blocked thread is
     * woken up or the endpoint is passed to the server job queue. If the thread is dispatched and
     * then the selection key is modified so that it is no longer selected.
     */
    public boolean dispatch(boolean assumeShortDispatch) throws IOException
    {
        _selectSet.scheduleIdle(_timeoutTask, _connection.isIdle());

        // If threads are blocked on this
        synchronized (this)
        {
            if (_readBlocked > 0 || _writeBlocked > 0)
            {
                // wake them up is as good as a dispatched.
                this.notifyAll();

                // we are not interested in further selecting
                _key.interestOps(0);
                return false;
            }

            if (!assumeShortDispatch)
                _key.interestOps(0);

            // Otherwise if we are still dispatched
            if (_dispatched)
            {
                // we are not interested in further selecting
                _key.interestOps(0);
                return false;
            }

            // Remove writeable op
            if (_key == null)
                return false;
            if ((_key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && (_key.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
            {
                // Remove writeable op
                _interestOps = _key.interestOps() & ~SelectionKey.OP_WRITE;
                _key.interestOps(_interestOps);
            }

            _dispatched = true;
        }

        return true;

    }

    /* ------------------------------------------------------------ */
    /**
     * Called when a dispatched thread is no longer handling the endpoint. The selection key
     * operations are updated.
     */
    public void undispatch()
    {
        synchronized (this)
        {
            try
            {
                _dispatched = false;

                if (getChannel().isOpen())
                {
                    updateKey();
                    if (_connection.isIdle())
                        _selectSet.scheduleIdle(_timeoutTask, true);
                }
            }
            catch (Exception e)
            {
                // TODO investigate if this actually is a problem?
                Log.ignore(e);
                _interestOps = -1;
                _selectSet.addChange(this);
            }
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
    public boolean isOpen()
    {
        return super.isOpen() && _key.isValid();
    }

    /* ------------------------------------------------------------ */
    /*
     * Allows thread to block waiting for further events.
     */
    public void blockReadable(long timeoutMs)
    {
        synchronized (this)
        {
            if (isOpen())
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
            if (isOpen())
            {
                try
                {
                    _writeBlocked++;
                    updateKey();
                    this.wait(timeoutMs);
                }
                catch (InterruptedException e)
                {
                    Log.ignore(e);
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
     * Updates selection key. Adds operations types to the selection key as needed. No operations
     * are removed as this is only done during dispatch. This method records the new key and
     * schedules a call to syncKey to do the keyChange
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
    /**
     * Synchronize the interestOps with the actual key. Call is scheduled by a call to updateKey
     */
    public void syncKey()
    {
        synchronized (this)
        {
            if (_key != null && _key.isValid())
            {
                if (_interestOps >= 0)
                    _key.interestOps(_interestOps);
                else
                {
                    _key.cancel();
                    connectionClosed();
                    _key = null;
                }
            }
            else
                _key = null;
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
            RetryContinuation continuation = (RetryContinuation) _connection.getRequest().getContinuation();
            if (continuation != null)
            {
                // We have a continuation
                Log.debug("continuation {}", continuation);
                if (!continuation.schedule())
                    undispatch();
            }
            else
            {
                undispatch();
            }
        }
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.io.nio.ChannelEndPoint#close()
     */
    public void close() throws IOException
    {
        if (_key != null)
        {
            _key.cancel();
        }
        _key = null;
        _selectSet.cancelIdle(_timeoutTask);
        RetryContinuation continuation = (RetryContinuation) _connection.getRequest().getContinuation();
        if (continuation != null && continuation.isPending())
            continuation.reset();
        
        try
        {
            super.close();
        }
        catch (IOException e)
        {
            throw (e instanceof EofException) ? e : new EofException(e);
        }
        
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "HEP@" + hashCode() + "[d=" + _dispatched + ",io=" + _interestOps + ",w=" + _writable + ",b=" + _readBlocked + "|" + _writeBlocked + "]";
    }

    /* ------------------------------------------------------------ */
    public HttpChannelEndPoint.IdleTask getTimeoutTask()
    {
        return _timeoutTask;
    }

    /* ------------------------------------------------------------ */
    public SelectSet getSelectSet()
    {
        return _selectSet;
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public class IdleTask extends Timeout.Task // implements HttpChannelEndPointIdleTask
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
            return "TimeoutTask:" + HttpChannelEndPoint.this.toString();
        }

    }

}

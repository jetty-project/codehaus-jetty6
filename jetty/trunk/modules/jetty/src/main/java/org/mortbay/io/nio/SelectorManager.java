package org.mortbay.io.nio;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Connection;
import org.mortbay.io.EndPoint;
import org.mortbay.log.Log;
import org.mortbay.thread.Timeout;


/* ------------------------------------------------------------ */
/**
 * The Selector Manager manages and number of SelectSets to allow
 * NIO scheduling to scale to large numbers of connections.
 * 
 * @author gregw
 *
 */
public abstract class SelectorManager extends AbstractLifeCycle
{
    private boolean _delaySelectKeyUpdate=true;
    private long _maxIdleTime;
    private long _lowResourcesConnections;
    private long _lowResourcesMaxIdleTime;
    private transient SelectSet[] _selectSet;
    private int _selectSets=1;
    

    /* ------------------------------------------------------------ */
    /**
     * @param maxIdleTime The maximum period in milli seconds that a connection may be idle before it is closed.
     * @see {@link #setLowResourcesMaxIdleTime(long)}
     */
    public void setMaxIdleTime(long maxIdleTime)
    {
        _maxIdleTime=maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param selectSets
     */
    public void setSelectSets(int selectSets)
    {
        long lrc = _lowResourcesConnections * _selectSets; 
        _selectSets=selectSets;
        _lowResourcesConnections=lrc/_selectSets;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public long getMaxIdleTime()
    {
        return _maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public int getSelectSets()
    {
        return _selectSets;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public boolean isDelaySelectKeyUpdate()
    {
        return _delaySelectKeyUpdate;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param connectChannel
     * @param interestedOps
     * @param attchmt
     * @return
     * @throws ClosedChannelException
     */
    public SelectionKey register(SocketChannel connectChannel, int interestedOps, Object attchmt) throws ClosedChannelException
    {
        int set=0; // TODO next set?
        
        synchronized (_selectSet[set])
        {
            SelectionKey key = connectChannel.register(_selectSet[set].getSelector(), interestedOps, attchmt);
            return key;
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param acceptChannel
     * @return
     * @throws ClosedChannelException
     */
    public SelectionKey register(ServerSocketChannel acceptChannel, int interestedOps, Object attchmt) throws ClosedChannelException
    {
        int set=0; // TODO next set?
        
        synchronized (_selectSet[set])
        {
            SelectionKey key = acceptChannel.register(_selectSet[set].getSelector(),interestedOps , attchmt);
            return key;
        }
    }
    

    /* ------------------------------------------------------------ */
    /**
     * @return the lowResourcesConnections
     */
    public long getLowResourcesConnections()
    {
        return _lowResourcesConnections*_selectSets;
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
        _lowResourcesConnections=(lowResourcesConnections+_selectSets-1)/_selectSets;
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
     * @param lowResourcesMaxIdleTime the period in ms that a connection is allowed to be idle when this SelectSet has more connections than {@link #getLowResourcesConnections()}
     * @see {@link #setMaxIdleTime(long)}
     */
    public void setLowResourcesMaxIdleTime(long lowResourcesMaxIdleTime)
    {
        _lowResourcesMaxIdleTime=lowResourcesMaxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param acceptorID
     * @throws IOException
     */
    public void doSelect(int acceptorID) throws IOException
    {
        
        if (_selectSet!=null && _selectSet.length>acceptorID && _selectSet[acceptorID]!=null)
            _selectSet[acceptorID].doSelect();
        
    }


    /* ------------------------------------------------------------ */
    /**
     * @param delaySelectKeyUpdate
     */
    public void setDelaySelectKeyUpdate(boolean delaySelectKeyUpdate)
    {
        _delaySelectKeyUpdate=delaySelectKeyUpdate;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param key
     * @return
     * @throws IOException 
     */
    protected abstract SocketChannel acceptChannel(SelectionKey key) throws IOException;

    /* ------------------------------------------------------------------------------- */
    protected abstract boolean dispatch(Runnable task) throws IOException;

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.component.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        _selectSet = new SelectSet[_selectSets];
        for (int i=0;i<_selectSet.length;i++)
            _selectSet[i]= new SelectSet(i);

        super.doStart();
    }


    /* ------------------------------------------------------------------------------- */
    protected void doStop() throws Exception
    {
        for (int i=0;i<_selectSet.length;i++)
            _selectSet[i].stop();
        super.doStop();
        _selectSet=null;
    }

    /* ------------------------------------------------------------------------------- */
    public void doStop(int i) throws Exception
    {
        _selectSet[i].stop();
    }

    /* ------------------------------------------------------------------------------- */
    private void doDispatch(SelectChannelEndPoint endpoint) throws IOException
    {
        boolean dispatch_done = true;
        try
        {
            if (endpoint.dispatch(_delaySelectKeyUpdate))
            {
                dispatch_done= false;
                dispatch_done = dispatch((Runnable)endpoint);
            }
        }
        finally
        {
            if (!dispatch_done)
            {
                Log.warn("dispatch failed!");
                endpoint.undispatch();
            }
        }
    }
    /* ------------------------------------------------------------ */
    /**
     * @param endpoint
     */
    protected abstract void endPointClosed(SelectChannelEndPoint endpoint);

    /* ------------------------------------------------------------ */
    /**
     * @param endpoint
     */
    protected abstract void endPointOpened(SelectChannelEndPoint endpoint);

    /* ------------------------------------------------------------------------------- */
    protected abstract Connection newConnection(SocketChannel channel, SelectChannelEndPoint endpoint);

    /* ------------------------------------------------------------ */
    /**
     * @param channel
     * @param selectSet
     * @param sKey
     * @return
     * @throws IOException
     */
    protected abstract SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey sKey) throws IOException;

    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    public class SelectSet 
    {
        private transient int _change;
        private transient List[] _changes;
        private transient Timeout _idleTimeout;
        private transient int _nextSet;
        private transient Timeout _retryTimeout;
        private transient Selector _selector;
        private transient int _setID;
        private transient boolean _selecting;
        
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
            _changes = new ArrayList[] {new ArrayList(),new ArrayList()};
            _change=0;
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
        public void cancelIdle(Timeout.Task task)
        {
            synchronized (this)
            {
                task.cancel();
            }
        }

        /* ------------------------------------------------------------ */
        public void doSelect() throws IOException
        {
            long idle_next = 0;
            long retry_next = 0;

            try
            {
                List changes;
                synchronized (_changes)
                {
                    changes=_changes[_change];
                    _change=_change==0?1:0;
                    _selecting=true;
                }

                // Make any key changes required
                for (int i = 0; i < changes.size(); i++)
                {
                    try
                    {
                        Object o = changes.get(i);
                        if (o instanceof EndPoint)
                        {
                            // Update the operatios for a key.
                            SelectChannelEndPoint endpoint = (SelectChannelEndPoint)o;
                            endpoint.doUpdateKey();
                        }
                        else if (o instanceof SocketChannel)
                        {
                            // finish accepting this connection
                            SocketChannel channel=(SocketChannel)o;
                            SelectionKey cKey = channel.register(_selector, SelectionKey.OP_READ);
                            SelectChannelEndPoint endpoint = newEndPoint(channel,this,cKey);

                            if (_delaySelectKeyUpdate)
                                doDispatch(endpoint);
                        }
                        else if (o instanceof Runnable)
                        {
                            dispatch((Runnable)o);
                        }
                        else
                            throw new IllegalArgumentException(o.toString());
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
                    _idleTimeout.setDuration(getMaxIdleTime());
                    idle_next=_idleTimeout.getTimeToNext();
                    retry_next=_retryTimeout.getTimeToNext();
                }

                // workout how low to wait in select
                long wait = 1000L;  // not getMaxIdleTime() as the now value of the idle timers needs to be updated.
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

                // have we been destroyed while sleeping\
                if (_selector==null || !_selector.isOpen())
                    return;

                // update the timers for task schedule in this loop
                now = System.currentTimeMillis();
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
                            SelectChannelEndPoint endpoint = (SelectChannelEndPoint)key.attachment();
                            if (endpoint != null)
                                endpoint.doUpdateKey();
                            continue;
                        }

                        if (key.isAcceptable())
                        {

                            SocketChannel channel = acceptChannel(key);
                            if (channel==null)
                                continue;

                            channel.configureBlocking(false);

                            // TODO make it reluctant to leave 0
                            _nextSet=++_nextSet%_selectSet.length;

                            // Is this for this selectset
                            if (_nextSet!=_setID)
                            {
                                // nope - give it to another.
                                _selectSet[_nextSet].addChange(channel);
                                _selectSet[_nextSet].wakeup();
                            }
                            else
                            {
                                // bind connections to this select set.
                                SelectionKey cKey = channel.register(_selectSet[_nextSet].getSelector(), SelectionKey.OP_READ);
                                SelectChannelEndPoint endpoint=newEndPoint(channel,_selectSet[_nextSet],cKey);
                                if (endpoint != null)
                                    doDispatch(endpoint);
                            }
                        }
                        else if (key.isConnectable())
                        {
                            SocketChannel channel = (SocketChannel) key.channel();
                            channel.finishConnect();
                            SelectionKey cKey = channel.register(_selectSet[_nextSet].getSelector(), SelectionKey.OP_READ, key.attachment());
                            SelectChannelEndPoint endpoint=newEndPoint(channel,_selectSet[_nextSet],cKey);
                            if (endpoint != null)
                                doDispatch(endpoint);
                        }
                        else
                        {
                            SelectChannelEndPoint endpoint = (SelectChannelEndPoint)key.attachment();
                            if (endpoint != null)
                                doDispatch(endpoint);
                        }

                        key = null;
                    }
                    catch (CancelledKeyException e)
                    {
                        Log.ignore(e);
                    }
                    catch (Exception e)
                    {
                        if (isRunning())
                            Log.warn(e);
                        else
                            Log.ignore(e);

                        if (key != null && !(key.channel() instanceof ServerSocketChannel) && key.isValid())
                        {
                            key.interestOps(0);
                            key.cancel();
                        } 
                    }
                }


                // tick over the timers
                Timeout.Task task=null;
                synchronized (this)
                {
                    now = System.currentTimeMillis();
                    _retryTimeout.setNow(now);
                    _idleTimeout.setNow(now);
                    
                    if (_lowResourcesConnections>0 && _selector.keys().size()>_lowResourcesConnections)
                        _idleTimeout.setDuration(_lowResourcesMaxIdleTime);
                    else 
                        _idleTimeout.setDuration(_maxIdleTime);

                    task=_idleTimeout.expired();
                    if (task==null)
                        task=_retryTimeout.expired();
                }

                // handle any expired timers
                while (task!=null)
                {
                    task.expire();

                    // get the next timer tasks
                    synchronized(this)
                    {
                        if (_selector==null)
                            break;
                        task=_idleTimeout.expired();
                        if (task==null)
                            task=_retryTimeout.expired();
                    }
                }
            }
            catch (CancelledKeyException e)
            {
                if (isRunning())
                    Log.warn(e);
                else
                    Log.ignore(e);
            }
            finally
            {
                synchronized(this)
                {
                    _selecting=false;
                }
            }
        }

        /* ------------------------------------------------------------ */
        public SelectorManager getManager()
        {
            return SelectorManager.this;
        }

        /* ------------------------------------------------------------ */
        public long getNow()
        {
            return _idleTimeout.getNow();
        }
        
        /* ------------------------------------------------------------ */
        public void scheduleIdle(Timeout.Task task)
        {
            synchronized (this)
            {
                task.schedule(_idleTimeout);
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
        public void wakeup()
        {
            Selector selector = _selector;
            if (selector!=null)
                selector.wakeup();
        }

        /* ------------------------------------------------------------ */
        Selector getSelector()
        {
            return _selector;
        }

        /* ------------------------------------------------------------ */
        void stop() throws Exception
        {
            boolean selecting=true;
            while(selecting)
            {
                wakeup();
                Thread.yield();
                synchronized (this)
                {
                    selecting=_selecting;
                }
            }
            
            synchronized (this)
            {
                Iterator iter =_selector.keys().iterator();
                while (iter.hasNext())
                {
                    SelectionKey key = (SelectionKey)iter.next();
                    if (key==null)
                        continue;
                    EndPoint endpoint = (EndPoint)key.attachment();
                    if (endpoint!=null)
                    {
                        try
                        {
                            endpoint.close();
                        }
                        catch(IOException e)
                        {
                            Log.ignore(e);
                        }
                    }
                }


                _idleTimeout.cancelAll();
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
                _selector=null;
            }
        }
    }

}
//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.io.nio;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mortbay.jetty.io.Connection;
import org.mortbay.jetty.io.EndPoint;
import org.mortbay.jetty.util.component.AbstractLifeCycle;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.thread.Timeout;


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
    private long _maxIdleTime;
    private long _lowResourcesConnections;
    private long _lowResourcesMaxIdleTime;
    private transient SelectSet[] _selectSet;
    private int _selectSets=1;
    private volatile int _set;
    

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
    /** Register a channel
     * @param channel
     * @param att Attached Object
     * @throws IOException
     */
    public void register(SocketChannel channel, Object att) throws IOException
    {
        int s=_set++; 
        s=s%_selectSets;
        SelectSet[] sets=_selectSet;
        if (sets!=null)
        {
            SelectSet set=sets[s];
            set.addChange(channel,att);
            set.wakeup();
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Register a serverchannel
     * @param acceptChannel
     * @return
     * @throws IOException
     */
    public void register(ServerSocketChannel acceptChannel) throws IOException
    {
        int s=_set++; 
        s=s%_selectSets;
        SelectSet set=_selectSet[s];
        set.addChange(acceptChannel);
        set.wakeup();
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
        SelectSet[] sets= _selectSet;
        if (sets!=null && sets.length>acceptorID && sets[acceptorID]!=null)
            sets[acceptorID].doSelect();
    }

    /* ------------------------------------------------------------ */
    /**
     * @param key
     * @return
     * @throws IOException 
     */
    protected abstract SocketChannel acceptChannel(SelectionKey key) throws IOException;

    /* ------------------------------------------------------------------------------- */
    public abstract boolean dispatch(Runnable task);

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
        SelectSet[] sets= _selectSet;
        _selectSet=null;
        if (sets!=null)
            for (int i=0;i<sets.length;i++)
            {
                SelectSet set = sets[i];
                if (set!=null)
                    set.stop();
            }
        super.doStop();
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
    protected void connectionFailed(SocketChannel channel,Throwable ex,Object attachment)
    {
        Log.warn(ex);
    }
    
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    public class SelectSet 
    {
        private transient int _change;
        private transient List<Object>[] _changes;
        private transient Timeout _idleTimeout;
        private transient int _nextSet;
        private transient Timeout _timeout;
        private transient Selector _selector;
        private transient int _setID;
        private transient boolean _selecting;
        private transient int _jvmBug;
        
        /* ------------------------------------------------------------ */
        SelectSet(int acceptorID) throws Exception
        {
            _setID=acceptorID;

            _idleTimeout = new Timeout(this);
            _idleTimeout.setDuration(getMaxIdleTime());
            _timeout = new Timeout(this);
            _timeout.setDuration(0L);

            // create a selector;
            _selector = Selector.open();
            _changes = new List[] {new ArrayList(),new ArrayList()};
            _change=0;
        }
        
        /* ------------------------------------------------------------ */
        public void addChange(Object point)
        {
            synchronized (_changes)
            {
                _changes[_change].add(point);
                if (point instanceof SocketChannel)
                    _changes[_change].add(null);
            }
        }
        
        /* ------------------------------------------------------------ */
        public void addChange(SocketChannel channel, Object att)
        {   
            synchronized (_changes)
            {
                _changes[_change].add(channel);
                _changes[_change].add(att);
            }
        }
        
        /* ------------------------------------------------------------ */
        public void cancelIdle(Timeout.Task task)
        {
            task.cancel();
        }

        /* ------------------------------------------------------------ */
        /**
         * Select and dispatch tasks found from changes and the selector.
         * 
         * @throws IOException
         */
        public void doSelect() throws IOException
        {
            try
            {
                List<?> changes;
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
                            // Update the operations for a key.
                            SelectChannelEndPoint endpoint = (SelectChannelEndPoint)o;
                            endpoint.doUpdateKey();
                        }
                        else if (o instanceof Runnable)
                        {
                            dispatch((Runnable)o);
                        }
                        else if (o instanceof SocketChannel)
                        {
                            // finish accepting/connecting this connection
                            SocketChannel channel=(SocketChannel)o;
                            Object att = changes.get(++i);

                            if (channel.isConnected())
                            {
                                SelectionKey key = channel.register(_selector,SelectionKey.OP_READ,att);
                                SelectChannelEndPoint endpoint = newEndPoint(channel,this,key);
                                key.attach(endpoint);
                                endpoint.schedule();
                            }
                            else
                            {
                                channel.register(_selector,SelectionKey.OP_CONNECT,att);
                            }

                        }
                        else if (o instanceof ServerSocketChannel)
                        {
                            ServerSocketChannel channel = (ServerSocketChannel)o;
                            channel.register(getSelector(),SelectionKey.OP_ACCEPT);
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

                long idle_next = 0;
                long retry_next = 0;
                long now=System.currentTimeMillis();
                synchronized (this)
                {
                    _idleTimeout.setNow(now);
                    _timeout.setNow(now);
                    if (_lowResourcesConnections>0 && _selector.keys().size()>_lowResourcesConnections)
                        _idleTimeout.setDuration(_lowResourcesMaxIdleTime);
                    else 
                        _idleTimeout.setDuration(_maxIdleTime);
                    idle_next=_idleTimeout.getTimeToNext();
                    retry_next=_timeout.getTimeToNext();
                }

                // workout how low to wait in select
                long wait = 1000L;  // not getMaxIdleTime() as the now value of the idle timers needs to be updated.
                if (idle_next >= 0 && wait > idle_next)
                    wait = idle_next;
                if (wait > 0 && retry_next >= 0 && wait > retry_next)
                    wait = retry_next;
    
                // Do the select.
                if (wait > 0) 
                {
                    long before=now;
                    int selected=_selector.select(wait);
                    now = System.currentTimeMillis();
                    _idleTimeout.setNow(now);
                    _timeout.setNow(now);

                    // Look for JVM bug  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6403933
                    if (selected==0  && (now-before)<wait/2)
                    {
                        if (_jvmBug++>5) 
                        {
                            // Probably JVM BUG!    
                            for (SelectionKey key: _selector.keys())
                            {    
                                if (key.interestOps()==0 && key.isValid())
                                    key.cancel();
                            }
                            _selector.selectNow();
                        } 
                    }
                    else
                        _jvmBug=0;
                }
                else 
                {
                    _selector.selectNow();
                    _jvmBug=0;
                }

                // have we been destroyed while sleeping\
                if (_selector==null || !_selector.isOpen())
                    return;

                // Look for things to do
                for (SelectionKey key: _selector.selectedKeys())
                {   
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

                        Object att = key.attachment();
                        if (att instanceof SelectChannelEndPoint)
                        {
                            ((SelectChannelEndPoint)att).schedule();
                        }
                        else if (key.isAcceptable())
                        {
                            SocketChannel channel = acceptChannel(key);
                            if (channel==null)
                                continue;

                            channel.configureBlocking(false);

                            // TODO make it reluctant to leave 0
                            _nextSet=++_nextSet%_selectSet.length;

                            // Is this for this selectset
                            if (_nextSet==_setID)
                            {
                                // bind connections to this select set.
                                SelectionKey cKey = channel.register(_selectSet[_nextSet].getSelector(), SelectionKey.OP_READ);
                                SelectChannelEndPoint endpoint=newEndPoint(channel,_selectSet[_nextSet],cKey);
                                cKey.attach(endpoint);
                                if (endpoint != null)
                                    endpoint.schedule();
                            }
                            else
                            {
                                // nope - give it to another.
                                _selectSet[_nextSet].addChange(channel);
                                _selectSet[_nextSet].wakeup();
                            }
                        }
                        else if (key.isConnectable())
                        {
                            // Complete a connection of a registered channel
                            SocketChannel channel = (SocketChannel)key.channel();
                            boolean connected=false;
                            try
                            {
                                connected=channel.finishConnect();
                            }
                            catch(Exception e)
                            {
                                connectionFailed(channel,e,att);
                            }
                            finally
                            {
                                if (connected)
                                {
                                    key.interestOps(SelectionKey.OP_READ);
                                    SelectChannelEndPoint endpoint = newEndPoint(channel,this,key);
                                    key.attach(endpoint);
                                    endpoint.schedule();
                                }
                                else
                                {
                                    key.cancel();
                                }
                            }
                        }
                        else
                        {
                            // Wrap readable registered channel in an endpoint
                            SocketChannel channel = (SocketChannel)key.channel();
                            SelectChannelEndPoint endpoint = newEndPoint(channel,this,key);
                            key.attach(endpoint);
                            if (key.isReadable())
                                endpoint.schedule();                           
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
                            key.cancel();
                    }
                }
                
                // Everything always handled
                _selector.selectedKeys().clear();

                // tick over the timers
                _idleTimeout.tick(now);
                _timeout.tick(now);
            }
            catch (CancelledKeyException e)
            {
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
            if (_idleTimeout.getDuration() <= 0)
                return;
            _idleTimeout.schedule(task);
        }

        /* ------------------------------------------------------------ */
        public void scheduleTimeout(Timeout.Task task, long timeoutMs)
        {
            _timeout.schedule(task, timeoutMs);
        }
        
        /* ------------------------------------------------------------ */
        public void cancelTimeout(Timeout.Task task)
        {
            task.cancel();
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
                synchronized (this)
                {
                    selecting=_selecting;
                }
            }
            
            ArrayList<SelectionKey> keys=new ArrayList<SelectionKey>(_selector.keys());
            Iterator<SelectionKey> iter =keys.iterator();

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
            
            synchronized (this)
            {
                _idleTimeout.cancelAll();
                _timeout.cancelAll();
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

// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.thread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.log.Log;

/* ------------------------------------------------------------ */
/** A pool of threads.
 * <p>
 * Avoids the expense of thread creation by pooling threads after
 * their run methods exit for reuse.
 * <p>
 * If the maximum pool size is reached, jobs wait for a free thread.
 * By default there is no maximum pool size.  Idle threads timeout
 * and terminate until the minimum number of threads are running.
 * <p>
 * @author Greg Wilkins <gregw@mortbay.com>
 * @author Juancarlo Anez <juancarlo@modelistica.com>
 */
public class BoundedThreadPool extends AbstractLifeCycle implements Serializable, ThreadPool
{
    private static int __id;
    private transient List _blocked;
    
    private boolean _daemon;

    private transient int _id;

    private final String _lock = "LOCK";
    private final String _joinLock = "JOIN";
    private int _maxIdleTimeMs=60000;
    private int _maxThreads=255;
    private int _minThreads=1;
    private int _lowThreads=25;
    private String _name;
    int _priority= Thread.NORM_PRIORITY;
    private Set _threads;
    private List _idle;
    private boolean _warned=false;
    private long _lastShrink;

    /* ------------------------------------------------------------------- */
    /* Construct
     */
    public BoundedThreadPool()
    {
        _name="btpool"+__id++;
    }

    /* ------------------------------------------------------------ */
    /** Get the number of idle threads in the pool.
     * @see #getThreads
     * @return Number of threads
     */
    public int getIdleThreads()
    {
        return _idle==null?0:_idle.size();
    }


    /* ------------------------------------------------------------ */
    /**
     * @return low resource threads threshhold
     */
    public int getLowThreads()
    {
        return _lowThreads;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param lowThreads low resource threads threshhold
     */
    public void setLowThreads(int lowThreads)
    {
        _lowThreads = lowThreads;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isLowOnThreads()
    {
        return _maxThreads-getThreads()+getIdleThreads()<_lowThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the maximum thread idle time.
     * Delegated to the named or anonymous Pool.
     * @see #setMaxIdleTimeMs
     * @return Max idle time in ms.
     */
    public int getMaxIdleTimeMs()
    {
        return _maxIdleTimeMs;
    }

    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * Delegated to the named or anonymous Pool.
     * @see #setMaxThreads
     * @return maximum number of threads.
     */
    public int getMaxThreads()
    {
        return _maxThreads;
    }

    /* ------------------------------------------------------------ */
    /** Get the minimum number of threads.
     * Delegated to the named or anonymous Pool.
     * @see #setMinThreads
     * @return minimum number of threads.
     */
    public int getMinThreads()
    {
        return _minThreads;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return The name of the BoundedThreadPool.
     */
    public String getName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    /** Get the number of threads in the pool.
     * @see #getIdleThreads
     * @return Number of threads
     */
    public int getThreads()
    {
        return _threads.size();
    }

    /* ------------------------------------------------------------ */
    /** Get the priority of the pool threads.
     *  @return the priority of the pool threads.
     */
    public int getThreadsPriority()
    {
        return _priority;
    }


    /* ------------------------------------------------------------ */
    /** 
     * Delegated to the named or anonymous Pool.
     */
    public boolean isDaemon()
    {
        return _daemon;
    }

    /* ------------------------------------------------------------ */
    public void join() throws InterruptedException
    {
        synchronized (_joinLock)
        {
            while (isRunning())
                _joinLock.wait(getMaxIdleTimeMs());
        }
        
        // TODO remove this semi busy loop!
        while (isStopping())
            Thread.sleep(10);
    }

    /* ------------------------------------------------------------ */
    protected void newThread()
    {
        synchronized(_lock)
        {
            Thread thread =new PoolThread();
            _threads.add(thread);
            _idle.add(thread);
            thread.setName(_name+"-"+_id++);
            thread.start();   
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Run job.
     * @return true if the job was given to a thread, false if no thread was
     * available.
     */
    public boolean dispatch(Runnable job) 
    {
        boolean queued=false;
        synchronized(_lock)
        {	
            if (!isRunning())
                return false;
            
            int blockMs = _maxIdleTimeMs;
            
            // Wait for an idle thread!
            while (_idle.size()==0)
            {
                // Are we at max size?
                if (_threads.size()<_maxThreads)
                {    
                    // No
                    newThread();
                    break;
                }
                else if (!_warned)
                {
                    _warned=true;
                    Log.debug("Out of threads for {}",this);
                }
                
                // pool is full
                if (blockMs<0)
                    return false;
                    
                // Block waiting
                try
                {
                    _blocked.add(Thread.currentThread());
                    _lock.wait(blockMs);
                    blockMs=-1;
                }
                catch (InterruptedException ie)
                {}
                finally
                {
                    _blocked.remove(Thread.currentThread());
                }
            }

            PoolThread thread = (PoolThread)_idle.remove(_idle.size()-1);
            thread.dispatch(job);
            queued=true;
        }

        if (_idle.size()==0)
            Thread.yield();
        return queued;
    }

    /* ------------------------------------------------------------ */
    /** 
     * Delegated to the named or anonymous Pool.
     */
    public void setDaemon(boolean daemon)
    {
        _daemon=daemon;
    }

    /* ------------------------------------------------------------ */
    /** Set the maximum thread idle time.
     * Threads that are idle for longer than this period may be
     * stopped.
     * Delegated to the named or anonymous Pool.
     * @see #getMaxIdleTimeMs
     * @param maxIdleTimeMs Max idle time in ms.
     */
    public void setMaxIdleTimeMs(int maxIdleTimeMs)
    {
        _maxIdleTimeMs=maxIdleTimeMs;
    }

    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * Delegated to the named or anonymous Pool.
     * @see #getMaxThreads
     * @param maxThreads maximum number of threads.
     */
    public void setMaxThreads(int maxThreads)
    {
        if (isStarted() && maxThreads<_minThreads)
            throw new IllegalArgumentException("!minThreads<maxThreads");
        _maxThreads=maxThreads;
    }

    /* ------------------------------------------------------------ */
    /** Set the minimum number of threads.
     * Delegated to the named or anonymous Pool.
     * @see #getMinThreads
     * @param minThreads minimum number of threads
     */
    public void setMinThreads(int minThreads)
    {
        if (isStarted() && (minThreads<=0 || minThreads>_maxThreads))
            throw new IllegalArgumentException("!0<=minThreads<maxThreads");
        _minThreads=minThreads;
        synchronized (_lock)
        {
            while (isStarted() && _threads.size()<_minThreads)
            {
              newThread();   
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param name Name of the BoundedThreadPool to use when naming Threads.
     */
    public void setName(String name)
    {
        _name= name;
    }

    /* ------------------------------------------------------------ */
    /** Set the priority of the pool threads.
     *  @param priority the new thread priority.
     */
    public void setThreadsPriority(int priority)
    {
        _priority=priority;
    }

    /* ------------------------------------------------------------ */
    /* Start the BoundedThreadPool.
     * Construct the minimum number of threads.
     */
    protected void doStart() throws Exception
    {
        if (_maxThreads<_minThreads || _minThreads<=0)
            throw new IllegalArgumentException("!0<minThreads<maxThreads");
        
        _threads=new HashSet();
        _idle=new ArrayList();
        _blocked=new ArrayList();
        
        for (int i=0;i<_minThreads;i++)
        {
            newThread();
        }   
    }

    /* ------------------------------------------------------------ */
    /** Stop the BoundedThreadPool.
     * New jobs are no longer accepted,idle threads are interrupted
     * and stopJob is called on active threads.
     * The method then waits 
     * min(getMaxStopTimeMs(),getMaxIdleTimeMs()), for all jobs to
     * stop, at which time killJob is called.
     */
    protected void doStop() throws Exception
    {   
        super.doStop();
        
        for (int i=0;i<100;i++)
        {
            synchronized (_lock)
            {
                Iterator iter = _threads.iterator();
                while (iter.hasNext())
                    ((Thread)iter.next()).interrupt();
            }
            
            Thread.yield();
            if (_threads.size()==0)
               break;
            
            try
            {
                Thread.sleep(i*100);
            }
            catch(InterruptedException e){}
        }

        // TODO perhaps force stops
        if (_threads.size()>0)
            Log.warn(_threads.size()+" threads could not be stopped");
        
        
        
        synchronized (_joinLock)
        {
            _joinLock.notifyAll();
        }
    }

    /* ------------------------------------------------------------ */
    /** Stop a Job.
     * This method is called by the Pool if a job needs to be stopped.
     * The default implementation does nothing and should be extended by a
     * derived thread pool class if special action is required.
     * @param thread The thread allocated to the job, or null if no thread allocated.
     * @param job The job object passed to run.
     */
    protected void stopJob(Thread thread, Object job)
    {
        thread.interrupt();
    }
    

    /* ------------------------------------------------------------ */
    /** Pool Thread class.
     * The PoolThread allows the threads job to be
     * retrieved and active status to be indicated.
     */
    public class PoolThread extends Thread 
    {
        Runnable _job=null;
        
        PoolThread()
        {
            setDaemon(_daemon);
            setPriority(_priority);
        }
        
        void dispatch(Runnable job)
        {
            synchronized (this)
            {
                if(_job!=null || job==null)
                    throw new IllegalStateException();
                _job=job;
                this.notify();
            }
        }
        
        /* ------------------------------------------------------------ */
        /** BoundedThreadPool run.
         * Loop getting jobs and handling them until idle or stopped.
         */
        public void run()
        {

            Runnable job=null;
            try
            {
                while (isRunning())
                {
                    try
                    {
                        synchronized (_lock)
                        {
                            if (job!=null)
                            {
                                _idle.add(this);
                                if (_idle.size()>=_minThreads)
                                    _warned=false;
                                if (_blocked.size()>0)
                                    ((Thread)_blocked.get(0)).interrupt();
                            }
                        }
                        
                        synchronized (this)
                        {
                            job=null;
                            if(_job==null)
                                this.wait(getMaxIdleTimeMs());  
                            job=_job;
                            _job=null;
                        }
                            
                        if (isRunning() && job!=null)
                            job.run();
                        
                        synchronized (_lock)
                        {
                            if (job==null)
                            {
                                long now = System.currentTimeMillis();
                                if (_threads.size()>_maxThreads ||      // we have too many threads  OR
                                    _idle.size()-_blocked.size()>0 &&     // are there idle threads?
                                    _threads.size()>_minThreads &&        // are there more than min threads?
                                    (now-_lastShrink)>getMaxIdleTimeMs()) // have we shrunk recently?
                                {
                                    _lastShrink=now;
                                    _idle.remove(this);
                                    return;
                                }
                            }
                        }
                    }
                    catch (InterruptedException e) {Log.ignore(e); return;}
                }
            }
            finally
            {
                synchronized (_lock)
                {
                    _idle.remove(this);
                    _threads.remove(this);
                }
                
                synchronized (this)
                {
                    job=null;
                    job=_job;
                }
                
                // catch all!
                if (job!=null && isRunning())
                    BoundedThreadPool.this.dispatch(job);
            }
        }
    }

}

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mortbay.component.AbstractLifeCycle;

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
 * This implementation uses the run(Object) method to place a
 * job on a queue, which is read by the getJob(timeout) method.
 * Derived implementations may specialize getJob(timeout) to
 * obtain jobs from other sources without queing overheads.
 *
 * @author Greg Wilkins <gregw@mortbay.com>
 * @author Juancarlo Anez <juancarlo@modelistica.com>
 */
public class BoundedThreadPool extends AbstractLifeCycle implements Serializable, ThreadPool
{
    private static final long serialVersionUID = 2806675379803919606L;
    
    private static int __id;
    private transient List _blocked;
    private int _blockMs=10000;
    
    private boolean _daemon;

    private transient int _id;
    private transient int _idle;

    private final String _lock = "LOCK";
    private transient List _jobs;
    private final String _joinLock = "JOIN";
    private int _maxIdleTimeMs=10000;
    private int _maxThreads=255;
    private int _minThreads=1;
    private String _name;
    int _priority= Thread.NORM_PRIORITY;
    private boolean _queue=true;
    private transient Set _threads;

    /* ------------------------------------------------------------------- */
    /* Construct
     */
    public BoundedThreadPool()
    {
        _name= this.getClass().getName();
        int dot= _name.lastIndexOf('.');
        if (dot >= 0)
            _name= _name.substring(dot + 1);
        _name=_name+__id++;
    }

    /* ------------------------------------------------------------ */
    /** Get the number of idle threads in the pool.
     * @see #getThreads
     * @return Number of threads
     */
    public int getIdleThreads()
    {
        return _idle;
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
    /**
     * @return Returns the queue.
     */
    public boolean isQueue()
    {
        return _queue;
    }

    /* ------------------------------------------------------------ */
    public void join() throws InterruptedException
    {
        synchronized (_joinLock)
        {
            while (isRunning())
                _joinLock.wait(getMaxIdleTimeMs());
        }
    }

    /* ------------------------------------------------------------ */
    protected void newThread()
    {
        synchronized(_lock)
        {
            Thread thread =new PoolThread();
            _threads.add(thread);
            _idle++;
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
        return run(job,_queue);
    }

    /* ------------------------------------------------------------ */
    /** Run job.
     * @return true if the job was given to a thread, false if no thread was
     * available.
     */
    private boolean run(Runnable job, boolean queue) 
    {
        boolean queued=false;
        synchronized(_lock)
        {	
            if (!isRunning())
                return false;
            
            int blockMs = _blockMs;
            
            // Wait for an idle thread!
            while (_idle-_jobs.size()<=0)
            {
                // Are we at max size?
                if (_threads.size()<_maxThreads)
                {    
                    // No
                    newThread();
                    break;
                }
                 
                // Can we queue?
                if (queue)
                    break;
                
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
            }

            _jobs.add(job);
            queued=true;
            _lock.notify();
        }
        
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
        _minThreads=minThreads;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param name Name of the BoundedThreadPool to use when naming Threads.
     */
    public void setName(String name)
    {
        _name= name;
    }
    
    /**
     * @param queue The queue to set.
     */
    public void setQueue(boolean queue)
    {
        _queue = queue;
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
        _threads=new HashSet();
        _jobs=new LinkedList();
        _blocked=new LinkedList();
        _idle=0;
        
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
        synchronized (_lock)
        {
            Iterator iter = _threads.iterator();
            while (iter.hasNext())
                ((Thread)iter.next()).interrupt();
        }
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
        
        /* ------------------------------------------------------------ */
        /** BoundedThreadPool run.
         * Loop getting jobs and handling them until idle or stopped.
         */
        public void run()
        {
            try
            {
                while (isRunning())
                {
                    _job=null;
                    
                    try
                    {
                        synchronized (_lock)
                        {
                            while(_jobs.size()==0 && isRunning())
                                _lock.wait();
                            if (_jobs.size()>0 && isRunning())
                                _job=(Runnable)_jobs.remove(0);
                            if (_job!=null)
                                _idle--;
                        }
                        
                        if (isRunning() && _job!=null)
                            _job.run();
                    }
                    catch (InterruptedException e) {}
                    finally
                    {
                        synchronized (_lock)
                        {
                            if (_job!=null)
                                _idle++;
                            _job=null;
                            if (_blocked.size()>0)
                                ((Thread)_blocked.remove(0)).interrupt();
                        }
                    }
                }
            }
            finally
            {
                synchronized (_lock)
                {
                    _threads.remove(this);
                }
            }
        }
    }
}

// ========================================================================
// Copyright 2004-2009 Mort Bay Consulting Pty. Ltd.
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


package org.mortbay.jetty.util.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.mortbay.jetty.util.BlockingArrayQueue;
import org.mortbay.jetty.util.component.AbstractLifeCycle;
import org.mortbay.jetty.util.component.LifeCycle;
import org.mortbay.jetty.util.log.Log;


public class QueuedThreadPool extends AbstractLifeCycle implements ThreadPool, Executor
{
    private final AtomicInteger _threadsStarted = new AtomicInteger();
    private final AtomicInteger _threadsIdle = new AtomicInteger();
    private final AtomicLong _lastShrink = new AtomicLong();
    private final ConcurrentLinkedQueue<Thread> _threads=new ConcurrentLinkedQueue<Thread>();
    private final Object _joinLock = new Object();
    private BlockingArrayQueue<Runnable> _jobs;
    private String _name;
    private int _maxIdleTimeMs=60000;
    private int _maxThreads=254;
    private int _minThreads=8;
    private int _maxQueued=-1;
    private int _priority=Thread.NORM_PRIORITY;
    private boolean _daemon=false;
    private int _maxStopTime=100;

    /* ------------------------------------------------------------------- */
    /* Construct
     */
    public QueuedThreadPool()
    {
        _name="qtp"+super.hashCode();
    }
    
    /* ------------------------------------------------------------------- */
    /* Construct
     */
    public QueuedThreadPool(int maxThreads)
    {
        this();
        setMaxThreads(maxThreads);
    }
    
    /* ------------------------------------------------------------ */
    @Override
    protected void doStart() throws Exception
    {
        super.doStart();
        _threadsStarted.set(0);

        _jobs=_maxQueued>0 ?new BlockingArrayQueue<Runnable>(_minThreads,_minThreads,_maxQueued)
                :new BlockingArrayQueue<Runnable>(_minThreads,_minThreads);

        int threads=_threadsStarted.get();
        while (isRunning() && threads<_minThreads)
        {
            startThread(threads); 
            threads=_threadsStarted.get();  
        }
    }

    /* ------------------------------------------------------------ */
    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        long start=System.currentTimeMillis();

        // let jobs complete naturally for a while
        while (_threadsStarted.get()>0 && (System.currentTimeMillis()-start) < (_maxStopTime/2))
            Thread.sleep(1);
        
        // kill queued jobs and flush out idle jobs
        _jobs.clear();
        Runnable noop = new Runnable(){public void run(){}};
        for  (int i=_threadsIdle.get();i-->0;)
            _jobs.offer(noop);
        Thread.yield();

        // interrupt remaining threads
        if (_threadsStarted.get()>0)
            for (Thread thread : _threads)
                thread.interrupt();
        
        // wait for remaining threads to die
        while (_threadsStarted.get()>0 && (System.currentTimeMillis()-start) < _maxStopTime)
        {
            Thread.sleep(1);
        }
            
        if (_threads.size()>0)
            Log.warn(_threads.size()+" threads could not be stopped");
        
        synchronized (_joinLock)
        {
            _joinLock.notifyAll();
        }
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
    /**
     * @param stopTimeMs maximum total time that stop() will wait for threads to die.
     */
    public void setMaxStopTimeMs(int stopTimeMs)
    {
        _maxStopTime = stopTimeMs;
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
        
        int threads=_threadsStarted.get();
        while (isStarted() && threads<_minThreads)
        {
            startThread(threads); 
            threads=_threadsStarted.get();  
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param name Name of the BoundedThreadPool to use when naming Threads.
     */
    public void setName(String name)
    {
        if (isRunning())
            throw new IllegalStateException("started");
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
    /**
     * @return maximum queue size
     */
    public int getMaxQueued()
    {
        if (isRunning())
            throw new IllegalStateException("started");
        return _maxQueued;
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
    /**
     * @return maximum total time that stop() will wait for threads to die.
     */
    public int getMaxStopTimeMs()
    {
        return _maxStopTime;
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
    public boolean dispatch(Runnable job)
    {
        if (isRunning())
        {
            final int jobQ = _jobs.size();
            final int idle = getIdleThreads();
            if(_jobs.offer(job))
            {
                // If we had no idle threads or the jobQ is greater than the idle threads
                if (idle==0 || jobQ>idle)
                {
                    int threads=_threadsStarted.get();
                    if (threads<_maxThreads)
                        startThread(threads);
                }
                return true;
            }
        }
        return false;
    }
    
    /* ------------------------------------------------------------ */
    public void execute(Runnable job)
    {
        if (!dispatch(job))
            throw new RejectedExecutionException();
    }

    /* ------------------------------------------------------------ */
    /**
     * Blocks until the thread pool is {@link LifeCycle#stop stopped}.
     */
    public void join() throws InterruptedException
    {   
        synchronized (_joinLock)
        {
            while (isRunning())
                _joinLock.wait();
        }
        
        while (isStopping())
            Thread.sleep(1);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The total number of threads currently in the pool
     */
    public int getThreads()
    {
        return _threadsStarted.get();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The number of idle threads in the pool
     */
    public int getIdleThreads()
    {
        return _threadsIdle.get();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return True if the pool is at maxThreads and there are more queued jobs than idle threads
     */
    public boolean isLowOnThreads()
    {
        return _threadsStarted.get()==_maxThreads && _jobs.size()>_threadsIdle.get();
    }

    /* ------------------------------------------------------------ */
    private boolean startThread(int threads)
    {
        final int next=threads+1;
        if (!_threadsStarted.compareAndSet(threads,next))
            return false;
        
        boolean started=false;
        try
        {
            Thread thread=newThread(_runnable);
            thread.setDaemon(_daemon);
            thread.setPriority(_priority);
            thread.setName(_name+"-"+thread.getId());
            _threads.add(thread);
            
            thread.start();
            started=true;
        }
        finally
        {
            if (!started)
                _threadsStarted.decrementAndGet();
        }
        return started;
    }
    
    /* ------------------------------------------------------------ */
    protected Thread newThread(Runnable runnable)
    {
        return new Thread(runnable);
    }
    
    /* ------------------------------------------------------------ */
    private Runnable _runnable = new Runnable()
    {
        public void run()
        {
            boolean shrink=false;
            try
            {
                Runnable job=_jobs.poll();
                while (isRunning())
                {
                    // Job loop
                    while (job!=null && isRunning())
                    {
                        job.run();
                        job=_jobs.poll();
                    }
                        
                    // Idle loop
                    try
                    {
                        _threadsIdle.incrementAndGet();

                        while (isRunning() && job==null)
                        {
                            if (_maxIdleTimeMs<=0)
                                job=_jobs.take();
                            else
                            {
                                job=_jobs.poll(_maxIdleTimeMs,TimeUnit.MILLISECONDS);
                                
                                if (job==null)
                                {
                                    // maybe we should shrink?
                                    final int size=_threadsStarted.get();
                                    if (size>_minThreads)
                                    {
                                        long last=_lastShrink.get();
                                        long now=System.currentTimeMillis();
                                        if (last==0 || (now-last)>_maxIdleTimeMs)
                                        {
                                            shrink=_lastShrink.compareAndSet(last,now) &&
                                            _threadsStarted.compareAndSet(size,size-1);
                                            if (shrink)
                                                return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    finally
                    {
                        _threadsIdle.decrementAndGet();
                    }
                }
            }
            catch(InterruptedException e)
            {
                Log.ignore(e);
            }
            catch(Exception e)
            {
                Log.warn(e);
            }
            finally
            {
                if (!shrink)
                    _threadsStarted.decrementAndGet();
                _threads.remove(Thread.currentThread());
            }
        }
    };
}

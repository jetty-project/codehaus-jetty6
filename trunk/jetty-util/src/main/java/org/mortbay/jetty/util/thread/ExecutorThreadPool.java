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

package org.mortbay.jetty.util.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mortbay.jetty.util.component.AbstractLifeCycle;
import org.mortbay.jetty.util.component.LifeCycle;
import org.mortbay.jetty.util.log.Log;

/* ------------------------------------------------------------ */
/** Jetty ThreadPool using java 5 ThreadPoolExecutor
 * This class wraps a {@link ExecutorService} as a {@link ThreadPool} and 
 * {@link LifeCycle} interfaces so that it may be used by the Jetty {@link org.mortbay.jetty.Server}
 * 
 * 
 *
 */
public class ExecutorThreadPool extends AbstractLifeCycle implements ThreadPool, LifeCycle
{
    private final ExecutorService _executor;

    /* ------------------------------------------------------------ */
    public ExecutorThreadPool(ExecutorService executor)
    {
        _executor=executor;
    }
    
    /* ------------------------------------------------------------ */
    /** constructor.
     * Wraps an {@link ThreadPoolExecutor}.
     * Core size is 32, max pool size is 256, pool thread timeout after 60 seconds and
     * an unbounded {@link LinkedBlockingQueue} is used for the job queue;
     */
    public ExecutorThreadPool()
    {
        this(new ThreadPoolExecutor(32,256,60,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>()));
    }
    
    /* ------------------------------------------------------------ */
    /** constructor.
     * Wraps an {@link ThreadPoolExecutor}.
     * Core size is 32, max pool size is 256, pool thread timeout after 60 seconds
     * @param queueSize if -1, an unbounded {@link LinkedBlockingQueue} is used, if 0 then a
     * {@link SynchronousQueue} is used, other a {@link ArrayBlockingQueue} of the given size is used.
     */
    public ExecutorThreadPool(int queueSize)
    {
        this(new ThreadPoolExecutor(32,256,60,TimeUnit.SECONDS,
                queueSize<0?new LinkedBlockingQueue<Runnable>()
                        : (queueSize==0?new SynchronousQueue<Runnable>()
                                :new ArrayBlockingQueue<Runnable>(queueSize))));
    }

    /* ------------------------------------------------------------ */
    /** constructor.
     * Wraps an {@link ThreadPoolExecutor} using
     * an unbounded {@link LinkedBlockingQueue} is used for the jobs queue;
     */
    public ExecutorThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit)
    {
        this(new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,new LinkedBlockingQueue<Runnable>()));
    }

    /* ------------------------------------------------------------ */
    public ExecutorThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
        this(new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue));
    }


    /* ------------------------------------------------------------ */
    public boolean dispatch(Runnable job)
    {
        try
        {       
            _executor.execute(job);
            return true;
        }
        catch(RejectedExecutionException e)
        {
            Log.warn(e);
            return false;
        }
    }

    /* ------------------------------------------------------------ */
    public int getIdleThreads()
    {
        if (_executor instanceof ThreadPoolExecutor)
        {
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)_executor;
            return tpe.getPoolSize() -tpe.getActiveCount();
        }
        return -1;
    }

    /* ------------------------------------------------------------ */
    public int getThreads()
    {
        if (_executor instanceof ThreadPoolExecutor)
        {
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)_executor;
            return tpe.getPoolSize();
        }
        return -1;
    }

    /* ------------------------------------------------------------ */
    public boolean isLowOnThreads()
    {
        if (_executor instanceof ThreadPoolExecutor)
        {
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)_executor;
            return tpe.getActiveCount()>=tpe.getMaximumPoolSize();
        }
        return false;
    }

    /* ------------------------------------------------------------ */
    public void join() throws InterruptedException
    {
        _executor.awaitTermination(Long.MAX_VALUE,TimeUnit.MILLISECONDS);
    }

    /* ------------------------------------------------------------ */
    @Override
    protected void doStart() throws Exception
    {
        super.doStart();
    }

    /* ------------------------------------------------------------ */
    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        _executor.shutdownNow();
    }

}

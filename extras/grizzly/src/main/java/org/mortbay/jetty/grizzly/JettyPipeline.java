//========================================================================
// Parts Copyright 2006 Mort Bay Consulting Pty. Ltd.
// Parts Copyright 2006 Jeanfrancois Arcand
//------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//========================================================================



package org.mortbay.jetty.grizzly;

import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.PipelineStatistic;
import com.sun.enterprise.web.connector.grizzly.Task;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;

/**
 * Wrapper around Jetty's BoundedThreadPool Pool.
 * TODO: Support ThreadPool directly.
 *
 * @author Jeanfrancois Arcand
 */
public class JettyPipeline implements Pipeline
{
    private PipelineStatistic pipelineStat;
    
    private BoundedThreadPool _threadPool;
    
    public JettyPipeline()
    {
    }

    public void setThreadPool(BoundedThreadPool threadPool)
    {
        _threadPool = threadPool;
    }
    
    public void addTask(Task task)
    {
        _threadPool.dispatch(task);
    }

    public Task getTask() 
    {
        // Not used.
        return null;
    }

    public int getWaitingThread() 
    {
       return  _threadPool.getIdleThreads();
    }

    public int getMaxThreads() 
    {
        return _threadPool.getMaxThreads();
    }

    public int getCurrentThreadCount() 
    {
        return _threadPool.getThreads();
    }

    public int getCurrentThreadsBusy() 
    {
        return getMaxThreads() - getWaitingThread();
    }

    public void initPipeline() 
    {
    }

    public String getName() 
    {
        return "JettyPipeline";              
    }

    public void startPipeline() 
    {
    }

    public void stopPipeline()
    {
    }

    public void setPriority(int i)
    {
    }

    public void setMaxThreads(int i)
    {
    }

    public void setMinThreads(int i)
    {
    }

    public void setPort(int i)
    {
    }

    public void setName(String string)
    {
    }

    public void setQueueSizeInBytes(int i)
    {
    }

    public void setThreadsIncrement(int i) 
    {
    }

    public void setThreadsTimeout(int i) 
    {
    }

    public void setPipelineStatistic(PipelineStatistic pipelineStatistic) 
    {
    }

    public PipelineStatistic getPipelineStatistic() 
    {
        return pipelineStat;
    }

    public int size() 
    {
        return _threadPool.getThreads();
    }

    public int getMaxSpareThreads() 
    {
        return -1;
    }

    public int getMinSpareThreads()
    {
        return -1;
    }

    public void setMinSpareThreads(int i)
    {
    }

    public boolean interruptThread(long l)
    {
        return false;
    }
    
}

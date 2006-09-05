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
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.algorithms.NoParsingAlgorithm;
import java.util.logging.Level;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;

/**
 *
 * @author Jeanfrancois Arcand
 */
public class JettySelectorThread extends SelectorThread{
    
    private GrizzlyConnector grizzlyConnector;
    
    private ThreadPool _threadPool;
    
    public JettySelectorThread() 
    {
        super();  
        maxProcessorWorkerThreads = 8;
    }
    
    
    /**
     * Load using reflection the <code>Algorithm</code> class.
     */
    protected void initAlgorithm()
    {
        algorithmClass=JettyStreamAlgorithm.class;
        algorithmClassName=algorithmClass.getName();
    }    
    
    
    /**
     * Create a new <code>Pipeline</code> instance using the 
     * <code>pipelineClassName</code> value.
     */
    protected Pipeline newPipeline(int maxThreads,
                                   int minThreads,
                                   String name, 
                                   int port,
                                   int priority){
        
        Pipeline pipeline = 
                super.newPipeline(maxThreads,minThreads,name,port,priority);
        if ( pipeline instanceof JettyPipeline) {
            ((JettyPipeline)pipeline).
                    setThreadPool((BoundedThreadPool)_threadPool);
        }
        return pipeline;
    }
    
    
    /**
     * Return a new <code>ReadTask</code> instance
     */
    protected ReadTask newReadTask()
    {
        StreamAlgorithm streamAlgorithm = null;
        
        try{
            streamAlgorithm = (StreamAlgorithm)algorithmClass.newInstance();
        } catch (InstantiationException ex){
            logger.log(Level.WARNING,
                       "Unable to instantiate Algorithm: "+ algorithmClassName);
        } catch (IllegalAccessException ex){
            logger.log(Level.WARNING,
                       "Unable to instantiate Algorithm: " + algorithmClassName);
        } finally {
            if ( streamAlgorithm == null)
                streamAlgorithm = new NoParsingAlgorithm();
        }       
        streamAlgorithm.setPort(port);
        
        // TODO: For now, hardcode the JettyReadTask
        ReadTask task = new JettyReadTask();        
        task.initialize(streamAlgorithm, useDirectByteBuffer,useByteBufferView);
        task.setPipeline(readPipeline);  
        task.setSelectorThread(this);
        task.setRecycle(recycleTasks);
        
        return task;
    }
    
    
    public ProcessorTask newProcessorTask(boolean initialize)
    {
        JettyProcessorTask task = new JettyProcessorTask();
        task.setMaxHttpHeaderSize(maxHttpHeaderSize);
        task.setBufferSize(requestBufferSize);
        task.setSelectorThread(this);              
        task.setRecycle(recycleTasks);
 
        
        task.initialize();
 
        if ( keepAlivePipeline.dropConnection() ) 
        {
            task.setDropConnection(true);
        }    
        
        task.setPipeline(processorPipeline); 
        return task;
    }
    
    
    public void setGrizzlyConnector(GrizzlyConnector grizzlyConnector)
    {
        this.grizzlyConnector = grizzlyConnector;
    }
    
    
    public GrizzlyConnector getGrizzlyConnector()
    {
        return grizzlyConnector;
    }
    
    public void setThreadPool(ThreadPool threadPool)
    {
        _threadPool = threadPool;
    }
    
}

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

import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;

/**
 *
 * @author Jeanfrancois Arcand
 */
public class JettySelectorThread extends SelectorThread{
    
    private GrizzlyConnector grizzlyConnector;
    
    public JettySelectorThread() 
    {
        super();
        algorithmClass=JettyStreamAlgorithm.class;
        algorithmClassName=algorithmClass.getName();
        
        // minimize debugging output
        minProcessorQueueLength=4;
        minReadQueueLength=4;
        maxProcessorWorkerThreads=8;
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
    
}

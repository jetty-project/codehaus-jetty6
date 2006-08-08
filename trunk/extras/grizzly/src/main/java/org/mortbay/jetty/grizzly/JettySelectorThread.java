/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.mortbay.jetty.grizzly;

import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;

/**
 *
 * @author Jeanfrancois Arcand
 */
public class JettySelectorThread extends SelectorThread{
    
    private GrizzlyConnector grizzlyConnector;
    
    public JettySelectorThread() {
        super();
        // pipelineClassName = JettyPipelineWrapper.class.getName();
        // This is the wrapper around Jetty own ThreadPool.
    }
    
    
    
    public ProcessorTask newProcessorTask(boolean initialize){
        JettyProcessorTask task = new JettyProcessorTask();
        task.setMaxHttpHeaderSize(maxHttpHeaderSize);
        task.setBufferSize(requestBufferSize);
        task.setSelectorThread(this);              
        task.setRecycle(recycleTasks);
        
        task.initialize();
 
        if ( keepAlivePipeline.dropConnection() ) {
            task.setDropConnection(true);
        }    
        task.setPipeline(processorPipeline); 
        return task;
    }
    
    
    public void setConnector(GrizzlyConnector grizzlyConnector){
        this.grizzlyConnector = grizzlyConnector;
    }
    
    
    public GrizzlyConnector getGrizzlyConnector(){
        return grizzlyConnector;
    }
    
}

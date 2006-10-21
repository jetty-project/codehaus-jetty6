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
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;

/**
 * Extend the default Grizzly implementation to allow the customization of
 * <code>Task</code> used by the Jetty back-end
 * 
 * @author Jeanfrancois Arcand
 */
public class JettySelectorThread extends SelectorThread
{

    /**
     * The <code>AbstractNIOConnector</code> implementation for Grizzly.
     */
    private GrizzlyConnector grizzlyConnector;

    /**
     * The Jetty thread pool implementation.
     */
    private ThreadPool _threadPool;
    
    private static String JETTY_GRIZZLY_STRATEGY =
            "org.mortbay.jetty.grizzly.useTemporarySelector";
    
    
    private boolean useTemporarySelector = false;

    
    public JettySelectorThread()
    {
        super();
        System.err.println("JettySelectorThread");
        
        if (System.getProperty(JETTY_GRIZZLY_STRATEGY) != null){
            useTemporarySelector =
                 Boolean.valueOf
                    (System.getProperty(JETTY_GRIZZLY_STRATEGY)).booleanValue();
        }
        System.out.println("Using temporary Selectors strategy: " 
                    + useTemporarySelector );        
    }

    /**
     * Force Grizzly to use the <code>JettyStreamAlgorithm</code>
     * implementation by default.
     */
    protected void initAlgorithm()
    {
        algorithmClass=JettyStreamAlgorithm.class;
        algorithmClassName=algorithmClass.getName();
        
        // Tell the Selector to not clear the SelectionKey.attach(..)
        if ( !useTemporarySelector){
            defaultAlgorithmInstalled = false;
        }
    }

    /**
     * Create a new <code>Pipeline</code> instance using the
     * <code>pipelineClassName</code> value. If the pipeline is an instance of
     * <code>JettyPipeline</code>, use the Jetty thread pool implementation
     * (wrapped inside a Pipeline).
     */
    protected Pipeline newPipeline(int maxThreads, int minThreads, String name, int port, int priority)
    {
        System.err.println("JettySelectorThread.newPipeline");
        Pipeline pipeline=super.newPipeline(maxThreads,minThreads,name,port,priority);
        if (pipeline instanceof JettyPipeline)
        {
            ((JettyPipeline)pipeline).setThreadPool((BoundedThreadPool)_threadPool);
        }
        return pipeline;
    }

    /**
     * Return a new <code>JettyReadTask</code> instance
     */
    protected ReadTask newReadTask()
    {
        System.err.println("JettySelectorThread.newReadTask");
        StreamAlgorithm streamAlgorithm=null;

        try
        {
            streamAlgorithm=(StreamAlgorithm)algorithmClass.newInstance();
        }
        catch (InstantiationException ex)
        {
            logger.log(Level.WARNING,"Unable to instantiate Algorithm: "
                    +algorithmClassName);
        }
        catch (IllegalAccessException ex)
        {
            logger.log(Level.WARNING,"Unable to instantiate Algorithm: "
                    +algorithmClassName);
        }
        finally
        {
            if (streamAlgorithm==null)
                streamAlgorithm=new NoParsingAlgorithm();
        }
        streamAlgorithm.setPort(port);

        // TODO: For now, hardcode the JettyReadTask
        ReadTask task=new JettyReadTask();
        task.initialize(streamAlgorithm,useDirectByteBuffer,useByteBufferView);
        task.setPipeline(readPipeline);
        task.setSelectorThread(this);
        task.setRecycle(recycleTasks);

        return task;
    }

    /**
     * Return a <code>JettyProcessorTask</code> implementation.
     */
    public ProcessorTask newProcessorTask(boolean initialize)
    {
        JettyProcessorTask task=new JettyProcessorTask();
        task.setMaxHttpHeaderSize(maxHttpHeaderSize);
        task.setBufferSize(requestBufferSize);
        task.setSelectorThread(this);
        task.setRecycle(recycleTasks);

        task.initialize();

        if (keepAlivePipeline.dropConnection())
        {
            task.setDropConnection(true);
        }

        task.setPipeline(processorPipeline);
        return task;
    }
    
    /**
     * Enable all registered interestOps. Due a a NIO bug, all interestOps
     * invokation needs to occurs on the same thread as the selector thread.
     */
    public void enableSelectionKeys()
    {
        SelectionKey selectionKey;
        int size = getKeysToEnable().size();
        long currentTime = System.currentTimeMillis();
        for (int i=0; i < size; i++) {
            selectionKey = (SelectionKey)getKeysToEnable().poll();

            selectionKey.interestOps(
                    selectionKey.interestOps() | SelectionKey.OP_READ);

            if ( selectionKey.attachment() == null){
                ((JettyReadTask)selectionKey
                        .attachment()).setIdleTime(currentTime);
            }
            keepAlivePipeline.trap(selectionKey);   
        } 
    }     
    
    /**
     * Cancel keep-alive connections.
     */
    protected void expireIdleKeys()
    {
        if ( keepAliveTimeoutInSeconds <= 0 || !selector.isOpen()) return;
        long current = System.currentTimeMillis();

        if (current < getNextKeysExpiration()) {
            return;
        }
        setNextKeysExpiration(current + getKaTimeout());
        
        Set readyKeys = selector.keys();
        if (readyKeys.isEmpty()){
            return;
        }
        Iterator iterator = readyKeys.iterator();
        SelectionKey key;
        while (iterator.hasNext()) {
            key = (SelectionKey)iterator.next();
            if ( !key.isValid() ) {
                keepAlivePipeline.untrap(key); 
                continue;
            }  
                        
            // Keep-alive expired
            if ( key.attachment() != null ) {
                  
                long expire = ((JettyReadTask)key.attachment()).getIdleTime();
                if (current - expire >= getKaTimeout()) {                   
                    cancelKey(key);
                } else if (expire + getKaTimeout() < getNextKeysExpiration()){
                    setNextKeysExpiration(expire + getKaTimeout());
                }
            }
        }                    
    }
    
    
    public void setGrizzlyConnector(GrizzlyConnector grizzlyConnector)
    {
        this.grizzlyConnector=grizzlyConnector;
    }

    public GrizzlyConnector getGrizzlyConnector()
    {
        return grizzlyConnector;
    }

    public void setThreadPool(ThreadPool threadPool)
    {
        _threadPool=threadPool;
    }

    public boolean isUseTemporarySelector()
    {
        return useTemporarySelector;
    }

    public void setUseTemporarySelector(boolean useTemporarySelector)
    {
        this.useTemporarySelector = useTemporarySelector;
    }

}

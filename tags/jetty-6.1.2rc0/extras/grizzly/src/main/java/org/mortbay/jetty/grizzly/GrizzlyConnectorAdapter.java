//========================================================================
// Parts Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

import com.sun.enterprise.web.connector.grizzly.SelectorThreadConfig;
import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Simple connector that disable Grizzly's SelectorThread ServerSocketChannel.
 * This class must only be used when Jetty is embedded in GlassFish.
 *
 * @author Jeanfrancois Arcand
 */
public class GrizzlyConnectorAdapter extends GrizzlyConnector{
    
    public GrizzlyConnectorAdapter()
    {
        _selectorThread = new JettySelectorThread()
        {
            public void initEndpoint() throws IOException, InstantiationException 
            {
                SelectorThreadConfig.configure(this);
                initFileCacheFactory();
                initAlgorithm();
                initPipeline();
                initMonitoringLevel();

                setName("SelectorThread-" + port);
                initProcessorTask(maxProcessorWorkerThreads);
                initReadTask(minReadQueueLength);                
                initialized = true;           
            }                        
            
            /**
             * Don't start the selector.
             */
            public void startEndpoint() throws IOException, InstantiationException 
            {
                running = true;        
                setKaTimeout(keepAliveTimeoutInSeconds * 1000);
                rampUpProcessorTask();
                registerComponents();
                startPipelines();
            }     
            
            public void registerKey(SelectionKey key)
            {
                ;
            }
            
        };
    }
    
    
    public JettySelectorThread getSelectorThread(){
        return _selectorThread;
    }
}

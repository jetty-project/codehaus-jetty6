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

import com.sun.enterprise.web.connector.grizzly.MultiSelectorThread;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


/**
 * Specialized <code>SelectorThread</code> that only handle OP_READ.
 *
 * @author Jeanfrancois Arcand
 */
public class JettyMultiSelectorThread extends JettySelectorThread 
        implements MultiSelectorThread{

    /**
     * List of <code>Channel<code> to process.
     */
    ArrayList channels = new ArrayList();


    /**
     * Int used to differenciate thsi instance
     */
    public static int countName;

    
    /**
     * Add a <code>Channel</code> to be processed by this
     * <code>Selector</code>
     */
    public synchronized void addChannel(SocketChannel channel) 
            throws IOException, ClosedChannelException {
        channels.add(channel);
        getSelector().wakeup();
    }


    /**
     * Register all <code>Channel</code> with an OP_READ opeation.
     */
    private synchronized void registerNewChannels() throws IOException {
        int size = channels.size();
        for (int i = 0; i < size; i++) {
            SocketChannel sc = (SocketChannel)channels.get(i);
            sc.configureBlocking(false);
            try {
                SelectionKey readKey = 
                        sc.register(getSelector(), SelectionKey.OP_READ);
                setSocketOptions(((SocketChannel)readKey.channel()).socket());
            } catch (ClosedChannelException cce) {
            }
        }
        channels.clear();
    }

    
    /**
     * Initialize this <code>SelectorThread</code>
     */
    public void initEndpoint() throws IOException, InstantiationException { 
        setName("JettyMultiSelectorThread-" + getPort());
        initAlgorithm();
    }
    
    
    /**
     * Start and wait for incoming connection
     */
    public void startEndpoint() throws IOException, InstantiationException {
        setRunning(true);
        while (isRunning()) {
            try{
                if ( getSelector() == null ){
                    setSelector(Selector.open());
                }              
                
                registerNewChannels();
                doSelect();
            } catch (Throwable t){
                logger.log(Level.FINE,"selectorThread.errorOnRequest",t);
            }
        }
    }


    /**
     * Return a <code>ReadTask</code> configured to use this instance.
     */
    public ReadTask getReadTask(SelectionKey key) throws IOException{
        ReadTask task = super.getReadTask(key);
        task.setSelectorThread(this);
        return task;
    }
    
    
    /**
     * Provides the count of request threads that are currently
     * being processed by the container
     *
     * @return Count of requests 
     */
    public int getCurrentBusyProcessorThreads() {
        return (getProcessorPipeline().getCurrentThreadsBusy());
    }
    
}

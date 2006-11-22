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
 * Specialized <code>SelectorThread</code> that only handle OP_READ over SSL.
 *
 * @author Jean-Francois Arcand
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

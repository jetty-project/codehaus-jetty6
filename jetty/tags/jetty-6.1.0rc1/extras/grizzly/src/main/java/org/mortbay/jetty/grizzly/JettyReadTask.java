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

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import java.io.IOException;
import com.sun.enterprise.web.connector.grizzly.XAReadTask;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.logging.Level;

/**
 * Prepare the <code>SocketChannel</code> for reading, and delegates the
 * parsing works to the <code>JettyProcessorTask</code>
 * 
 * @author Jeanfrancois Atcand
 * @author gregw
 */
public class JettyReadTask extends XAReadTask
{

  
    public boolean executeProcessorTask() throws IOException
    {
        boolean registerKey=false;

        if (SelectorThread.logger().isLoggable(Level.FINEST))
            SelectorThread.logger().log(Level.FINEST,"executeProcessorTask");

        /**
         * TODO: File caching support? if ( algorithm.getHandler() != null &&
         * algorithm.getHandler() .handle(null, Handler.REQUEST_BUFFERED) ==
         * Handler.BREAK ){ return true; }
         */

        // Get a processor task. If the processorTask != null, that means we
        // failed to load all the bytes in a single channel.read().
        if (processorTask==null)
        {
            attachProcessor(selectorThread.getProcessorTask());
        } else {
            configureProcessorTask();
        }

        try
        {
            // The socket might not have been read entirely and the parsing
            // will fail, so we need to respin another event.
            registerKey=processorTask.process(inputStream,null);
        }
        catch (Exception e)
        {
            SelectorThread.logger().log(Level.SEVERE,"readTask.processException",e);
            registerKey=true;
        }
        finally
        {

            // if registerKey, that means we were'nt able to parse the request
            // properly because the bytes were not all read, so we need to
            // call again the Selector.
            if (registerKey){
                inKeepAliveProcess=false;
                if (processorTask.isError())
                {
                    byteBuffer = algorithm.rollbackParseState(byteBuffer);
                    return true;
                } else {
                    byteBuffer.clear();
                }
            }
        }

        return registerKey;
    }
}

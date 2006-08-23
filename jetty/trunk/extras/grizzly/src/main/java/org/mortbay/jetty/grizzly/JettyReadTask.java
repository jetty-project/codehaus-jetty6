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

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.WeakHashMap;

import com.sun.enterprise.web.connector.grizzly.DefaultReadTask;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;

/**
 * @author gregw
 *
 */
public class JettyReadTask extends DefaultReadTask // TODO should just be ReadTask, but algorithm needs defaultReadTask
{
    static WeakHashMap __endpoints = new WeakHashMap();  // Horrid hack to associate key with endpoint!
    
    public void attachProcessor(ProcessorTask processorTask)
    {
        System.err.println(this+" attachProcessor");
        super.attachProcessor(processorTask);
    }

    public void detachProcessor()
    {
        System.err.println(this+" detachProcessor");
        super.detachProcessor();
    }

    public void doTask() throws IOException
    {
        try
        {
            System.err.println(this+" doTask "+key+" "+key.attachment());
            GrizzlyEndPoint ep = (GrizzlyEndPoint)key.attachment();
            if (ep==null)
            {
                // TODO perhaps the attachment is being cleared???
                // this is a horrid hack and need a better way to associate the endpoint
                ep = (GrizzlyEndPoint)__endpoints.get(key);

                if (ep==null)
                {
                    JettySelectorThread selThread=(JettySelectorThread)selectorThread;
                    GrizzlyConnector grizzlyConnector=selThread.getGrizzlyConnector();
                    ep=new GrizzlyEndPoint(grizzlyConnector,(ByteChannel)key.channel());
                    key.attach(ep);
                    __endpoints.put(key,ep);
                }
            }
            ep.handle();
        }
        finally
        {
            // TODO filthy hack
            detachProcessor();  
            registerKey(); 
        }
    }

    public void initialize(StreamAlgorithm algorithm, boolean useDirectByteBuffer, boolean useByteBufferView)
    {
        System.err.println(this+" initialize");
        super.initialize(algorithm,useDirectByteBuffer,useByteBufferView);
    }

    public void recycle()
    {
        System.err.println(this+" recycle");
        super.recycle();
    }

    public void taskEvent(TaskEvent event)
    {
        System.err.println(this+" taskEvent");
        super.taskEvent(event);
    }
}

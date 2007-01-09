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

import com.sun.enterprise.web.connector.grizzly.DefaultReadTask;
import com.sun.enterprise.web.portunif.ProtocolHandler;
import com.sun.enterprise.web.portunif.util.ProtocolInfo;
import java.io.IOException;

/** 
 * Simple Protocol handler that redirect http request from GlassFish to Jetty.
 *
 * @author Jeanfrancois Arcand
 */
public class JettyProtocolHandler implements ProtocolHandler
{
    
    private String[] protocols = new String[]{"jetty-http"};
    
    private JettyEmbedder embedded;
    
    /** Creates a new instance of JettyProtocolHandler */
    public JettyProtocolHandler() 
    {        
    }

    public String[] getProtocols() 
    {
        return protocols;
    }

    public void handle(ProtocolInfo protocolInfo) throws IOException 
    {
        if (embedded == null)
        {
            embedded = new JettyEmbedder(
                    protocolInfo.socketChannel.socket().getLocalPort());
            try
            {
                embedded.start();
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        JettySelectorThread selectorThread = embedded.getConnector()
            .getSelectorThread();
        DefaultReadTask readTask = (DefaultReadTask)
            selectorThread.getReadTask();
      
        readTask.setBytesAvailable(true);
        readTask.setByteBuffer(protocolInfo.byteBuffer); 
        readTask.setSelectionKey(protocolInfo.key);
        readTask.doTask();
        selectorThread.returnTask(readTask);
    }
    
}

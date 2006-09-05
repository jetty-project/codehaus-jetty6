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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.sun.enterprise.web.connector.grizzly.ByteBufferInputStream;
import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.TaskBase;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import com.sun.enterprise.web.connector.grizzly.OutputWriter;
import com.sun.enterprise.web.connector.grizzly.SelectorFactory;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

/**
 * 
 * @author Jeanfrancois Arcand
 */
public class JettyProcessorTask extends TaskBase implements ProcessorTask
{    
    private Handler handler;
    
    private boolean keepAlive = true;
    
    private GrizzlyEndPoint endPoint;

    public void initialize()
    {
        GrizzlyConnector grizzlyConnector = 
               ((JettySelectorThread)selectorThread).getGrizzlyConnector();   
        try{
            endPoint =  new GrizzlyEndPoint(grizzlyConnector);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }
    

    public boolean process(AbstractSelectableChannel channel) throws Exception
    {
        endPoint.setChannel((ByteChannel)channel);
        // We are already using a Grizzly WorkerThread, so no need to 
        // invoke Jetty Thread Pool
        endPoint.handle();
        
        // How to find the keepAlive flag?
        
        return endPoint.keepAlive();
    }    
    
    
    public boolean isError()
    {
        return false;
    }
    

    // ---------------------------------------------------- Not Used for now ---//

    
    public JettyProcessorTask() 
    {
    }

    public void doTask() throws IOException 
    {
        
    }

    public void taskEvent(TaskEvent event)
    {
    }

    /**
     * The Default ReadTask will invoke that method.
     */
    public boolean process(InputStream input, OutputStream output) 
        throws Exception {

        return keepAlive;
    }
  
    
    public int getBufferSize() {
        return -1;
    }

    public boolean getDropConnection() {
        return false;
    }

    public int getMaxPostSize() {
        return -1;
    }

    public void invokeAdapter() {
    }
    
    
    public void setBufferSize(int requestBufferSize) {
    }

    public void setDropConnection(boolean dropConnection) {
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setMaxHttpHeaderSize(int maxHttpHeaderSize) {
    }

    public void setMaxPostSize(int mps) {
    }

    public void setSocket(Socket socket) {
    }

    public void setTimeout(int timeouts) {
    }

    public void terminateProcess() {
    }

    public String getRequestURI() {
        return null; 
    }

    public long getWorkerThreadID() {
        return -1;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    // --------------------------------------------------- Grizzly ARP ------//
    
    
    public void parseRequest() throws Exception {
    }

    public boolean parseRequest(InputStream input, OutputStream output, boolean keptAlive) throws Exception {
        return true;
    }

    public void postProcess() throws Exception {
    }

    public void postProcess(InputStream input, OutputStream output) throws Exception {
    }

    public void postResponse() throws Exception {
    }

    public void preProcess() throws Exception {
    }

    public void preProcess(InputStream input, OutputStream output) throws Exception {
    }   
    
    // ------------------------------------------------- Channel support ---//
    
    public boolean parseRequest(AbstractSelectableChannel channel, 
            boolean keptAlive) throws Exception {
        
        
        return true;
    }

    public void postProcess(AbstractSelectableChannel channel) throws Exception{
        
        
    }

    public void preProcess(AbstractSelectableChannel channel) throws Exception{
        
    }

    
}

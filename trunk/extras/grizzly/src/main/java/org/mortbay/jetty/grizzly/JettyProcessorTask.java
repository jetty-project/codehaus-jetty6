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


import com.sun.enterprise.web.connector.grizzly.ByteBufferInputStream;
import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.TaskBase;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import com.sun.enterprise.web.connector.grizzly.OutputWriter;
import com.sun.enterprise.web.connector.grizzly.SelectorFactory;
import org.mortbay.jetty.grizzly.GrizzlyConnector.GrizzlyEndPoint;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 *
 * @author Jeanfrancois Arcand
 */
public class JettyProcessorTask extends TaskBase implements ProcessorTask {
    
    private Handler handler;
    
    private ByteBufferChannel bbChannel;
    
    private boolean keepAlive = true;
    
    
    public JettyProcessorTask() {
    }

    public void doTask() throws IOException {
        
    }

    public void taskEvent(TaskEvent event) {
    }

    public void initialize() {
        bbChannel = new ByteBufferChannel();
    }

    /**
     * The Default ReadTask will invoke that method.
     */
    public boolean process(InputStream input, OutputStream output) 
        throws Exception {

        JettySelectorThread selThread = (JettySelectorThread)selectorThread;
        GrizzlyConnector grizzlyConnector = selThread.getGrizzlyConnector();
        
        bbChannel.setByteBuffer( ((ByteBufferInputStream)input).getByteBuffer());
        bbChannel.setSocketChannel( (SocketChannel) key.channel());
        GrizzlyEndPoint gep = new GrizzlyEndPoint(grizzlyConnector,bbChannel);
        
        // We are already using a Grizzly WorkerThread, so no need to 
        // invoke Jetty Thread Pool
        gep.run();
        
        // How to find the keepAlive flag?
        
        return keepAlive;
    }

    
    /**
     * Wrapper to pass to Jetty. Here we are copying bytes from
     * one ByteBuffer to the other, which is bad.
     * XXX FIX this coying
     */
    private class ByteBufferChannel implements ByteChannel{
        
        private ByteBuffer byteBuffer;
        
        private SocketChannel socketChannel;
        
        
        public ByteBufferChannel(){
        }
        
        
        protected void setByteBuffer(ByteBuffer byteBuffer){
            this.byteBuffer = byteBuffer;
        }
        
        
        protected void setSocketChannel(SocketChannel socketChannel){
            this.socketChannel = socketChannel;
        }
        
        
        public int read(ByteBuffer dst) throws IOException {
            int pos = dst.position();
            dst.put(byteBuffer);
            
            // Now let see if we can use a temporary Selector to read more bytes
            // Make this configurable ???
            if ( dst.remaining() > 0)
                readAllBytes(dst);    
            return dst.position() - pos;
        }
        
        
        /**
         * Use the temporary <code>Selector</code> to try to loads as much as we
         * can available bytes before delegating the request processing to 
         * <code>AsyncWeb</code>
         */
        private void readAllBytes(java.nio.ByteBuffer byteBuffer) throws IOException{
            int count = 1;
            int byteRead = 0;
            Selector readSelector = null;
            SelectionKey tmpKey = null;

            try{
                SocketChannel socketChannel = (SocketChannel)key.channel();
                while (count > 0){
                    count = socketChannel.read(byteBuffer);
                    if ( count > 0 )
                        byteRead += count;
                }            

                if ( byteRead == 0 ){
                    readSelector = SelectorFactory.getSelector();

                    if ( readSelector == null ){
                        return;
                    }
                    count = 1;
                    tmpKey = socketChannel
                            .register(readSelector,SelectionKey.OP_READ);               
                    tmpKey.interestOps(tmpKey.interestOps() | SelectionKey.OP_READ);
                    int code = readSelector.selectNow();
                    tmpKey.interestOps(
                        tmpKey.interestOps() & (~SelectionKey.OP_READ));

                    if ( code == 0 ){
                        return;
                    }

                    while (count > 0){
                        count = socketChannel.read(byteBuffer);
                        if ( count > 0 )
                            byteRead += count;                 
                    }
                }
            } finally {
                if (tmpKey != null)
                    tmpKey.cancel();

                if ( readSelector != null){
                    // Bug 6403933
                    try{
                        readSelector.selectNow();
                    } catch (IOException ex){
                        ;
                    }
                    SelectorFactory.returnSelector(readSelector);
                }
            }
        }
    
        
        public boolean isOpen() {
            return true;
        }

        public void close() throws IOException {
        }

        
        public int write(ByteBuffer src) throws IOException {
            int len = byteBuffer.flip().limit();
            OutputWriter.flushChannel(socketChannel,src);
            return len;
        }
        
        
        public void recycle(){
            byteBuffer = null;
            socketChannel = null;
        }
        
    }
    
    
    public void recycle(){
        bbChannel.recycle();
    }    
    
    // ---------------------------------------------------- Not Used for now ---//
    
    
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

    public boolean isError() {
        return false;
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
}

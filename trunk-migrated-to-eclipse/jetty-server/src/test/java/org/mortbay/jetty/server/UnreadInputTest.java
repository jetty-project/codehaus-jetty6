// ========================================================================
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.server.bio.SocketConnector;
import org.mortbay.jetty.server.handler.AbstractHandler;

public class UnreadInputTest extends TestCase
{
    public static final String __OK_RESPONSE = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\nServer: Jetty(7.0.x)\r\n\r\n";
    protected Server _server = new Server();
    protected SocketConnector _connector;
    protected int _port;
    protected Socket _socket;
    protected OutputStream _outputStream;
    protected InputStream _inputStream;
    
    public class NoopHandler extends AbstractHandler
    {
        public void handle(String target, HttpServletRequest request,
                HttpServletResponse response) throws IOException,
                ServletException
        {
           //don't read the input, just send something back
            ((Request)request).setHandled(true);
            response.setStatus(200);
        }       
    }
    
    
    
    protected void setUp() throws Exception
    {
        //server side
        _connector = new SocketConnector();
        _server.setConnectors(new Connector[]{ _connector });
        _server.setHandler(new NoopHandler());
        _server.start();
        _port = _connector.getLocalPort();
        
        //client side
        _socket = new Socket((String)null, _port);
        _outputStream = _socket.getOutputStream();
        _inputStream = _socket.getInputStream();
    }

    protected void tearDown() throws Exception
    {
        _server.stop();
    }



    public void testUnreadInput ()
    throws Exception
    {
        for (int i=0; i<2; i++)
        {
            String content = "This is a loooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooooooooooooooooooooooooo"+
                             "ooooooooooooooooooooooonnnnnnnnnnnnnnnnggggggggg content";
            byte[] bytes = content.getBytes();

            _outputStream.write("GET / HTTP/1.1\r\nHost: localhost\r\n".getBytes());
            Thread.currentThread().sleep(500L);

            String str = "Content-Length: "+bytes.length+"\r\n" + "\r\n";
            _outputStream.write(str.getBytes());
            Thread.currentThread().sleep(500L);

            //write some bytes of the content
            _outputStream.write(bytes, 0, (bytes.length/2));
            Thread.currentThread().sleep(1000L);

            //write the rest
            _outputStream.write(bytes, bytes.length/2, (bytes.length - bytes.length/2));       
        }

        
        byte[] inbuf = new byte[__OK_RESPONSE.getBytes().length*2];
        int x = _inputStream.read(inbuf);
        System.err.println(new String(inbuf, 0, x));
        
        _inputStream.close();
        _outputStream.close();
        _socket.close();
    }
    
   
}

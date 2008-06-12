// ========================================================================
// Copyright 2006-2007 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.*;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.security.*;
import org.mortbay.jetty.client.security.BasicAuthentication;
import org.mortbay.jetty.client.security.DigestAuthentication;
import org.mortbay.jetty.client.security.SecurityRealm;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

/**
 * Functional testing.
 */
public class SslConversationTest extends TestCase
{ 
    private HttpClient _httpClient;

    protected void setUp() throws Exception
    {
   
        _httpClient=new HttpClient();
        _httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        _httpClient.setMaxConnectionsPerAddress(2);
        _httpClient.start();
    }

    protected void tearDown() throws Exception
    {
 
        _httpClient.stop();
    }
        
    public void testThis() throws Exception
    {

        Socket socket = SSLSocketFactory.getDefault().createSocket( "dav.codehaus.org", 443 );
        try
        {
            Writer out = new OutputStreamWriter( socket.getOutputStream(), "ISO-8859-1" );
            out.write( "GET /user/jesse HTTP/1.1\r\n" );
            //out.write( "Host: " + "dav.codehaus.org:443\r\n");
            out.write( "Host: " + "dav.codehaus.org\r\n");// + ":" + 443 + "\r\n" );
           // out.write( "Agent: SSL-TEST\r\n" );
            out.write( "\r\n" );
            out.flush();
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream(), "ISO-8859-1" ) );
            String line = null;
            while ( ( line = in.readLine() ) != null )
            {
                System.out.println( line );
            }
        }
        finally
        {
            socket.close();
        }
    }
      
   
    
    public void testGetWithContentExchange() throws Exception
    {

        HttpExchange.ContentExchange httpExchange = new HttpExchange.ContentExchange();
        httpExchange.setURL("https://dav.codehaus.org/user/jesse");
        httpExchange.setMethod(HttpMethods.GET);

        HttpConversation wrapper = new HttpConversation(httpExchange)
        {
            public void success()
            {
                assertTrue(true);
            }

            public void failure()
            {
                assertTrue(false);
            }

            public void failure(Throwable t)
            {
                t.printStackTrace();
                assertTrue(false);
            }
        };
        wrapper.addAuthentication(new DigestAuthentication());
        wrapper.addSecurityRealm(new SecurityRealm()
        {
            public String getName()
            {
                return "test";
            }

            public String getPrincipal()
            {
                return "jetty";
            }

            public String getCredentials()
            {
                return "jetty";
            }
        });

        //_httpClient.setConnectorType( _httpClient.CONNECTOR_SOCKET );
        httpExchange.addRequestHeader("Host:","dav.codehaus.org:443" );
        _httpClient.send(wrapper);

        assertTrue( wrapper.waitForSuccess() );
        Thread.sleep(10);

    }
}
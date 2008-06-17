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
import java.io.File;
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
    public void testNothing()
    {
        
    }
    /* TODO
    private Server _server;
    private int _port;
    private HttpClient _httpClient;

    protected void setUp() throws Exception
    {
        startServer();
        _httpClient=new HttpClient();
        //_httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        _httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        _httpClient.setMaxConnectionsPerAddress(2);
        _httpClient.start();
    }

    protected void tearDown() throws Exception
    {
        stopServer();
        _httpClient.stop();
    }

        
    public void testThis() throws Exception
    {

        Socket socket = SSLSocketFactory.getDefault().createSocket( "dav.codehaus.org", 443 );
        //Socket socket = SSLSocketFactory.getDefault().createSocket( "localhost", _port );
        try
        {
            Writer out = new OutputStreamWriter( socket.getOutputStream(), "ISO-8859-1" );
            out.write( "GET / HTTP/1.1\r\n" );
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
            Thread.sleep( 10 );
            socket.close();
        }
    }
      
   
  
    public void testGetWithContentExchange() throws Exception
    {

        ContentExchange httpExchange = new ContentExchange();
        //httpExchange.setURL("https://dav.codehaus.org/user/jesse/index.html");
        httpExchange.setURL( "https://localhost:" + _port+ "/" );
        
        httpExchange.setVersion( HttpVersions.HTTP_1_1_ORDINAL );
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
        wrapper.enableAuthentication(new BasicAuthentication());
        wrapper.enableSecurityRealm(new SecurityRealm()
        {
            public String getId()
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

        _httpClient.send(wrapper);

        assertTrue( wrapper.waitForSuccess() );
        Thread.sleep(10);

    }
    
    
    private void startServer() throws Exception
    {
        _server = new Server();
        _server.setGracefulShutdown(500);
        SslSelectChannelConnector connector = new SslSelectChannelConnector();

      String keystore = System.getProperty("user.dir")+File.separator+"src"+File.separator+"test"+File.separator+"resources"+File.separator+"keystore";
        
        connector.setPort(0);
        connector.setKeystore(keystore);
        connector.setPassword("storepwd");
        connector.setKeyPassword("keypwd");

        _server.setConnectors(new Connector[]{connector});

        UserRealm userRealm = new HashUserRealm("MyRealm", "src/test/resources/realm.properties");

        Constraint constraint = new Constraint();
        constraint.setName("Need User or Admin");
        constraint.setRoles(new String[]{"user", "admin"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        SecurityHandler sh = new SecurityHandler();
        _server.setHandler(sh);
        sh.setUserRealm(userRealm);
        sh.setConstraintMappings(new ConstraintMapping[]{cm});
        sh.setAuthenticator(new BasicAuthenticator());

        Handler testHandler = new AbstractHandler()
        {

            public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
            {
                System.out.println("passed authentication!");
                Request base_request=(request instanceof Request)?(Request)request:HttpConnection.getCurrentConnection().getRequest();
                base_request.setHandled(true);
                response.setStatus(200);
                if (request.getServerName().equals("jetty.mortbay.org"))
                {
                    response.getOutputStream().println("Proxy request: "+request.getRequestURL());
                }
                else if (request.getMethod().equalsIgnoreCase("GET"))
                {
                    response.getOutputStream().println("<hello>");
                    for (int i=0; i<100; i++)
                    {
                        response.getOutputStream().println("  <world>"+i+"</world>");
                        if (i%20==0)
                            response.getOutputStream().flush();
                    }
                    response.getOutputStream().println("</hello>");
                }
                else
                {
                    copyStream(request.getInputStream(),response.getOutputStream());
                }
            }
        };

        sh.setHandler(testHandler);

        _server.start();
        _port = connector.getLocalPort();
    }


    public static void copyStream(InputStream in, OutputStream out)
    {
        try
        {
            byte[] buffer=new byte[1024];
            int len;
            while ((len=in.read(buffer))>=0)
            {
                out.write(buffer,0,len);
            }
        }
        catch (EofException e)
        {
            System.err.println(e);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
   private void stopServer() throws Exception
   {
       _server.stop();
   }
   
   */
}
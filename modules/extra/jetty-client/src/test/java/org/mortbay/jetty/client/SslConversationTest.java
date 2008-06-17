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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.client.security.SecurityRealm;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.security.BasicAuthenticator;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSelectChannelConnector;
import org.mortbay.jetty.security.UserRealm;

/**
 * Functional testing.
 */
public class SslConversationTest extends TestCase
{    
    private Server _server;
    private int _port;
    private HttpClient _httpClient;
    private SecurityRealm _jettyRealm;

    protected void setUp() throws Exception
    {
        startServer();
        _httpClient=new HttpClient();
        //_httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        _httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        _httpClient.setMaxConnectionsPerAddress(2);
        _httpClient.start();
        
        _jettyRealm = new SecurityRealm()
        {
            public String getId()
            {
                return "MyRealm";
            }

            public String getPrincipal()
            {
                return "jetty";
            }

            public String getCredentials()
            {
                return "jetty";
            }
        };
        
        _httpClient.addSecurityRealm( _jettyRealm );
    }

    protected void tearDown() throws Exception
    {
        stopServer();
        _httpClient.stop();
    }
      
   
    public void testSslGet() throws Exception
    {  
        ContentExchange httpExchange = new ContentExchange();
        //httpExchange.setURL("https://dav.codehaus.org/user/jesse/index.html");
        httpExchange.setURL( "https://localhost:" + _port+ "/" );        
        httpExchange.setMethod(HttpMethods.GET);

        _httpClient.send( httpExchange );

        httpExchange.waitForStatus( HttpExchange.STATUS_COMPLETED );
        
        assertEquals( HttpServletResponse.SC_OK, httpExchange.getResponseStatus() );
        
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
}
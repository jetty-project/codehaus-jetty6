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

import junit.framework.TestCase;

import org.mortbay.jetty.*;
import org.mortbay.jetty.client.security.DefaultRealmResolver;
import org.mortbay.jetty.client.security.SecurityRealm;
import org.mortbay.resource.FileResource;

import java.net.URL;
import java.io.FileInputStream;
import java.io.File;

/**
 * Functional testing for HttpExchange.
 *
 * @author Matthew Purland
 * @author Greg Wilkins
 */
public class WebdavListenerTest extends TestCase//extends HttpExchangeTest
{
    protected String _scheme = "http://";
    protected Server _server;
    protected int _port;
    protected HttpClient _httpClient;
    protected Connector _connector;

    private String _username = "foo";
    private String _password = "pwd";

    protected void setUp() throws Exception
    {
        _scheme="https://";
        //startServer();
        _httpClient=new HttpClient();
        //_httpClient.setMaxRetries( 10 );
        //_httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        _httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        _httpClient.setMaxConnectionsPerAddress(4);

        _httpClient.setSecurityRealmResolver( new DefaultRealmResolver());

        _httpClient.getSecurityRealmResolver().addSecurityRealm(
                new SecurityRealm(){
                    public String getId()
                    {
                        return _username + "'s webspace";  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public String getPrincipal()
                    {
                        return _username;  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public String getCredentials()
                    {
                        return _password;  //To change body of implemented methods use File | Settings | File Templates.
                    }
                }
        );

        //_httpClient.enableWebdav();
        _httpClient.start();
    }


    public void testSslGetWithContentExchange() throws Exception
    {
        //for (int i=0;i<20;i++)
        //{
            ContentExchange httpExchange=new ContentExchange();

            // Working GET method
            //httpExchange.setURL("https://dav.codehaus.org/user/" + _username + "/");
            //httpExchange.setMethod( HttpMethods.GET );




            // failing PUT
            httpExchange.setURL("https://dav.codehaus.org/user/" + _username + "/foo.txt");
            httpExchange.setMethod( HttpMethods.PUT );
            //FileResource file = new FileResource(new URL("file:///Users/jesse/src/codehaus/trunks/jetty/modules/extra/jetty-client/src/test/resources/foo.txt"));
            File file = new File("src/test/resources/foo.txt");
            httpExchange.setRequestContentSource( new FileInputStream( file ) );
            httpExchange.setRequestHeader( "Content-Type", "application/octet-stream");
            httpExchange.setRequestHeader("Content-Length", String.valueOf( file.length() ));


            // propfind works like this
            //httpExchange.setMethod("PROPFIND", "/user/jesse/file0.txt HTTP/1.1");
            //httpExchange.setRequestHeader( "Depth", "1");
                                       
            // Working MKCOL
            //httpExchange.setRequestHeader("MKCOL", "/user/jesse/foo/ HTTP/1.1 ");


            // failing MKCOL
            //httpExchange.setMethod("MKCOL /user/jesse/foo/foo/foo HTTP/1.1");


            _httpClient.send(httpExchange);

            httpExchange.waitForStatus(HttpExchange.STATUS_COMPLETED);
            String result=httpExchange.getResponseContent();

            System.out.println( result );

            Thread.sleep(5);
        //}
    }
}
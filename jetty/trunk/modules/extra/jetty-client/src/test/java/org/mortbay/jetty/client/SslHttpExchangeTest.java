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
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;

/**
 * Functional testing for HttpExchange.
 * 
 * @author Matthew Purland
 * @author Greg Wilkins
 */
public class SslHttpExchangeTest extends TestCase
{
    protected Server _server;
    protected int _port;
    protected HttpClient _httpClient;

    protected void setUp() throws Exception
    {
        startServer();
        _httpClient=new HttpClient();
        // _httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        _httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        _httpClient.setMaxConnectionsPerAddress(2);
        _httpClient.start();
    }

    protected void tearDown() throws Exception
    {
        stopServer();
        _httpClient.stop();
    }

    public void testPerf() throws Exception
    {
        sender(1,false);
        Thread.sleep(200);
        sender(1,true);
        Thread.sleep(200);
        
        sender(10,false);
        Thread.sleep(200);
        sender(10,true);
        
        Thread.sleep(200);
        sender(100,false);
        Thread.sleep(200);
        sender(100,true);
        
        /*
        Thread.sleep(200);
        sender(1000,false);
        Thread.sleep(200);
        sender(1000,true);
        */
    }

    /**
     * Test sending data through the exchange.
     * 
     * @throws IOException
     */
    public void sender(final int nb, boolean close) throws Exception
    {
        final CountDownLatch latch=new CountDownLatch(nb);
        long l0=System.currentTimeMillis();
        for (int i=0; i<nb; i++)
        {
            final int n=i;
            if (n%1000==0)
            {
                Thread.sleep(200);
            }
            
            HttpExchange httpExchange=new HttpExchange()
            {
                protected void onRequestCommitted()
                {
                    // System.err.println("Request committed");
                }

                protected void onResponseStatus(Buffer version, int status, Buffer reason)
                {
                    // System.err.println("Response Status: " + version+" "+status+" "+reason);
                }

                protected void onResponseHeader(Buffer name, Buffer value)
                {
                    // System.err.println("Response header: " + name + " = " + value);
                }

                protected void onResponseContent(Buffer content)
                {
                    // System.err.println("Response content:" + content);
                    // System.err.println("Response content "+content.length());
                }

                protected void onResponseComplete()
                {
                    // System.err.println("Response completed "+n);
                    latch.countDown();
                }
                
            };

            httpExchange.setURL("https://localhost:"+_port+"/");
            httpExchange.addRequestHeader("arbitrary","value");
            if (close)
                httpExchange.setRequestHeader("Connection","close");
            
            _httpClient.send(httpExchange);
        }
        
        long last=latch.getCount();
        while(last>0)
        {
            // System.err.println("waiting for "+last+" sent "+(System.currentTimeMillis()-l0)/1000 + "s ago ...");
            latch.await(5,TimeUnit.SECONDS);
            long next=latch.getCount();
            if (last==next)
                break;
            last=next;
        }
        // System.err.println("missed "+latch.getCount()+" sent "+(System.currentTimeMillis()-l0)/1000 + "s ago.");
        assertEquals(0,latch.getCount());
        long l1=System.currentTimeMillis();
    }

    public void testPostWithContentExchange() throws Exception
    {
        for (int i=0;i<20;i++)
        {
            ContentExchange httpExchange=new ContentExchange();
            httpExchange.setURL("https://localhost:"+_port+"/");
            httpExchange.setMethod(HttpMethods.POST);
            httpExchange.setRequestContent(new ByteArrayBuffer("<hello />"));
            _httpClient.send(httpExchange);
            httpExchange.waitForStatus(HttpExchange.STATUS_COMPLETED);
            String result=httpExchange.getResponseContent();
            assertEquals("i="+i,"<hello />",result);
            
            Thread.sleep(5);
        }
    }

    public void testGetWithContentExchange() throws Exception
    {
        for (int i=0;i<20;i++)
        {   
            ContentExchange httpExchange=new ContentExchange();
            httpExchange.setURL("https://localhost:"+_port+"/?i="+i);
            httpExchange.setMethod(HttpMethods.GET);
            _httpClient.send(httpExchange);
            httpExchange.waitForStatus(HttpExchange.STATUS_COMPLETED);
            String result=httpExchange.getResponseContent();
            assertEquals("i="+i,0,result.indexOf("<hello>"));
            assertEquals("i="+i,result.length()-10,result.indexOf("</hello>"));

            Thread.sleep(5);
        }
    }

    /*
    public void testProxy() throws Exception
    {
        try
        {
            _httpClient.setProxy(new InetSocketAddress("127.0.0.1",_port));

            ContentExchange httpExchange=new ContentExchange();
            httpExchange.setAddress(new InetSocketAddress("jetty.mortbay.org",8080));
            httpExchange.setMethod(HttpMethods.GET);
            httpExchange.setURI("/jetty-6");
            _httpClient.send(httpExchange);
            httpExchange.waitForStatus(HttpExchange.STATUS_COMPLETED);
            String result=httpExchange.getResponseContent();
            assertEquals("Proxy request: http://jetty.mortbay.org:8080/jetty-6",result.trim());
        }
        finally
        {
            _httpClient.setProxy(null);
        }
    }
    */

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

    protected void startServer() throws Exception
    {
        _server=new Server();
        _server.setGracefulShutdown(500);
        // SslSelectChannelConnector connector = new SslSelectChannelConnector();
        SslSocketConnector connector = new SslSocketConnector();

        String keystore = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "keystore";

        connector.setPort(0);
        connector.setKeystore(keystore);
        connector.setPassword("storepwd");
        connector.setKeyPassword("keypwd");
        
        _server.setConnectors(new Connector[] { connector });
        _server.setHandler(new AbstractHandler()
        {
            public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
            {
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
                        response.getOutputStream().println("  <world>"+i+"</world");
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
        });
        _server.start();
        _port=connector.getLocalPort();
    }

    private void stopServer() throws Exception
    {
        _server.stop();
    }
}

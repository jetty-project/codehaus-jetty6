//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.servlet.webapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.mortbay.io.IO;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;

import junit.framework.TestCase;

public class WebAppTest extends TestCase
{
    
    Server server = new Server();
    BoundedThreadPool threadPool = new BoundedThreadPool();
    Connector connector=new SelectChannelConnector();
    HandlerCollection handlers = new HandlerCollection();
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    HashUserRealm userRealm = new HashUserRealm();
    RequestLogHandler requestLogHandler = new RequestLogHandler();
    
    protected void setUp() throws Exception
    {
        File dir = new File(".").getAbsoluteFile();
        while (!new File(dir,"webapps").exists())
        {
            System.err.println("DIR="+dir);
            dir=dir.getParentFile();
        }
        
        
        threadPool.setMaxThreads(100);
        server.setThreadPool(threadPool);
        
        server.setConnectors(new Connector[]{connector});
        
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler(),requestLogHandler});
        server.setHandler(handlers);
        
        // TODO add javadoc context to contexts
        WebAppContext.addWebApplications(server, dir.getAbsolutePath()+"/webapps", "org/mortbay/jetty/webapp/webdefault.xml", true, false);
        
        userRealm.setName("Test Realm");
        userRealm.setConfig(dir.getAbsolutePath()+"/etc/realm.properties");
        server.setUserRealms(new UserRealm[]{userRealm});
        
        NCSARequestLog requestLog = new NCSARequestLog(dir.getAbsolutePath()+"/logs/jetty-yyyy-mm-dd.log");
        
        requestLog.setExtended(false);
        requestLogHandler.setRequestLog(requestLog);
        
        server.setSendServerVersion(true);
        
        server.start();
    }
    
    
    protected void tearDown() throws Exception
    {
        Thread.sleep(1000);
        server.stop();
        Thread.sleep(1000);
    }
    

    public void testDoGet() throws Exception
    {
        URL url = null;
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/dump/info?query=foo");
        assertTrue(IO.toString(url.openStream()).startsWith("<html>"));
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/");
        try{IO.toString(url.openStream()); assertTrue(false); } catch(FileNotFoundException e) { assertTrue(true); } 

        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test");
        IO.toString(url.openStream());
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/");
        String s1=IO.toString(url.openStream());
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/index.html");
        String s2=IO.toString(url.openStream());
        assertEquals(s1,s2);

        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/d.txt");
        assertTrue(IO.toString(url.openStream()).startsWith("0000"));
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/data.txt");
        assertTrue(IO.toString(url.openStream()).endsWith("9999 3333333333333333333333333333333333333333333333333333333\n"));
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/dispatch/forward/dump/info?query=foo");
        assertTrue(IO.toString(url.openStream()).startsWith("<html>"));
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/dispatch/includeW/dump/info?query=foo");
        assertTrue(IO.toString(url.openStream()).startsWith("<H1>"));
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/dispatch/includeS/dump/info?query=foo");
        assertTrue(IO.toString(url.openStream()).startsWith("<H1>"));
    }
    
    public void testDoPost() throws Exception
    {
        URL url = null;
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/test/dump/info?query=foo");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.addRequestProperty(HttpHeaders.CONTENT_TYPE,MimeTypes.FORM_ENCODED);
        connection.addRequestProperty(HttpHeaders.CONTENT_LENGTH, "10");
        connection.getOutputStream().write("abcd=1234\n".getBytes());
        connection.getOutputStream().flush();
        
        connection.connect(); 
        String s0=IO.toString(connection.getInputStream());
        assertTrue(s0.startsWith("<html>"));
        assertTrue(s0.indexOf("<td>POST</td>")>0);
        assertTrue(s0.indexOf("abcd:&nbsp;</th><td>1234")>0);
    }       
}

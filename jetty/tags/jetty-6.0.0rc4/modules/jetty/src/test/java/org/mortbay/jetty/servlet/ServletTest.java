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

package org.mortbay.jetty.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.util.IO;

public class ServletTest extends TestCase
{
    Server server = new Server();
    Connector connector=new SelectChannelConnector();
    HandlerCollection collection = new HandlerCollection();
    DefaultHandler dft = new DefaultHandler();
    ContextHandler context = new ContextHandler();
    ServletHandler handler=new ServletHandler();
    
    protected void setUp() throws Exception
    {
        server.setConnectors(new Connector[]{connector});
        context.setContextPath("/context");
        
        collection.setHandlers(new Handler[]{context,dft});
        server.setHandler(collection);
        
        context.setHandler(handler);
        handler.addServletWithMapping("org.mortbay.jetty.servlet.ServletTest$TestServlet", "/servlet/*");
        
        server.start();
        
    }

    protected void tearDown() throws Exception
    {
        server.stop();
    }

    public void testDoGet() throws Exception
    {
        URL url = null;
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/context/servlet/info?query=foo");
        assertEquals("<h1>Hello SimpleServlet</h1>",IO.toString(url.openStream()));
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/");
        try
        {
            String s=IO.toString(url.openStream()); 
            System.err.println("s="+s);
            assertTrue(false); 
        } 
        catch(FileNotFoundException e) { assertTrue(true); } 
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/context");
        try{IO.toString(url.openStream()); assertTrue(false); } catch(FileNotFoundException e) { assertTrue(true); } 
        
        url=new URL("http://127.0.0.1:"+connector.getLocalPort()+"/context/xxxxx");
        try{IO.toString(url.openStream()); assertTrue(false); } catch(FileNotFoundException e) { assertTrue(true); } 
    }

    
    public static class TestServlet extends HttpServlet
    {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            assertEquals("/context",request.getContextPath());
            assertEquals("/servlet",request.getServletPath());
            assertEquals("/info",request.getPathInfo());
            assertEquals("query=foo",request.getQueryString());
            assertEquals(1,request.getParameterMap().size());
            assertEquals(1,request.getParameterValues("query").length);
            assertEquals("foo",request.getParameter("query"));
            
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print("<h1>Hello SimpleServlet</h1>");
        }
    }
}

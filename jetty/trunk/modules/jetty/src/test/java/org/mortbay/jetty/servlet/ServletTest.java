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
import org.mortbay.jetty.HttpTester;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.util.IO;

public class ServletTest extends TestCase
{
    public void testRawDoGet() throws Exception
    {
        // Setup test context
        ServletTester tester=new ServletTester();
        tester.setContextPath("/context");
        tester.addServlet(TestServlet.class, "/servlet/*");
        tester.addServlet("org.mortbay.jetty.servlet.DefaultServlet", "/");
        tester.start();
        
        // Raw HTTP test
        String responses = tester.getResponses("GET /context/servlet/info?query=foo HTTP/1.0\r\n\r\n");
        assertEquals("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=iso-8859-1\r\nContent-Length: 28\r\nServer: Jetty(6.1.x)\r\n\r\n<h1>Hello SimpleServlet</h1>",responses);
        
        // generated and parsed test
        HttpTester http = new HttpTester();
        http.setMethod("GET");
        http.setUri("/context/servlet/info?query=foo");
        http.setVersion("HTTP/1.0");
        http.parse(tester.getResponses(http.generate()));
        assertTrue(http.getMethod()==null);
        assertEquals(200,http.getStatus());
        assertEquals("<h1>Hello SimpleServlet</h1>",http.getContent());
        
        
        /* TODO convert
        responses = tester.getResponses("GET /context HTTP/1.0\r\n\r\n");
        assertEquals("<h1>Hello SimpleServlet</h1>/",responses);
        
        responses = tester.getResponses("GET /context/xxxxx HTTP/1.0\r\n\r\n");
        assertEquals("<h1>Hello SimpleServlet</h1>",responses);
        */
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

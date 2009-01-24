//========================================================================
//$Id: HttpGeneratorTest.java,v 1.1 2005/10/05 14:09:41 janb Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.util.IO;

/**
 * @author gregw
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RequestTest extends TestCase
{
    Server _server = new Server();
    LocalConnector _connector = new LocalConnector();
    RequestHandler _handler = new RequestHandler();
    
    {
        _connector.setHeaderBufferSize(512);
        _connector.setRequestBufferSize(1024);
        _connector.setResponseBufferSize(2048);
    }
    
    public RequestTest(String arg0)
    {
        super(arg0);
        _server.setConnectors(new Connector[]{_connector});
        
        _server.setHandler(_handler);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RequestTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _server.start();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        _server.stop();
    }
    
    
    public void testContentTypeEncoding()
    	throws Exception
    {
        final ArrayList results = new ArrayList();
        _handler._checker = new RequestTester()
        {
            public boolean check(HttpServletRequest request,HttpServletResponse response)
            {
                results.add(request.getContentType());
                results.add(request.getCharacterEncoding());
                return true;
            }  
        };
        
        _connector.getResponses(
                "GET / HTTP/1.1\n"+
                "Host: whatever\n"+
                "Content-Type: text/test\n"+
                "\n"+
               
                "GET / HTTP/1.1\n"+
                "Host: whatever\n"+
                "Content-Type: text/html;charset=utf8\n"+
                "\n"+
                
                "GET / HTTP/1.1\n"+
                "Host: whatever\n"+
                "Content-Type: text/html; charset=\"utf8\"\n"+
                "\n"+
                
                "GET / HTTP/1.1\n"+
                "Host: whatever\n"+
                "Content-Type: text/html; other=foo ; blah=\"charset=wrong;\" ; charset =   \" x=z; \"   ; more=values \n"+
                "\n"
                );
        
        int i=0;
        assertEquals("text/test",results.get(i++));
        assertEquals(null,results.get(i++));
        
        assertEquals("text/html;charset=utf8",results.get(i++));
        assertEquals("utf8",results.get(i++));
        
        assertEquals("text/html; charset=\"utf8\"",results.get(i++));
        assertEquals("utf8",results.get(i++));
        
        assertTrue(((String)results.get(i++)).startsWith("text/html"));
        assertEquals(" x=z; ",results.get(i++));
        
        
    }
    

    
    public void testContent()
        throws Exception
    {
      
        final int[] length=new int[1];
        
        _handler._checker = new RequestTester()
        {
            public boolean check(HttpServletRequest request,HttpServletResponse response)
            {
                assertEquals(request.getContentLength(), ((Request)request).getContentRead());
                length[0]=request.getContentLength();
                return true;
            }  
        };
        
        String content="";
        
        for (int l=0;l<1025;l++)
        {
            String request="POST / HTTP/1.1\r\n"+
            "Host: whatever\r\n"+
            "Content-Type: text/test\r\n"+
            "Content-Length: "+l+"\r\n"+
            "Connection: close\r\n"+
            "\r\n"+
            content;           
            _connector.reopen();
            String response = _connector.getResponses(request);
            assertEquals(l,length[0]);
            if (l>0)
                assertEquals(l,_handler._content.length());
            content+="x";
        }
    }

    public void testConnectionClose()
        throws Exception
    {
        String response;

        _handler._checker = new RequestTester()
        {
            public boolean check(HttpServletRequest request,HttpServletResponse response) throws IOException
            {
                response.getOutputStream().println("Hello World");
                return true;
            }  
        };

        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.1\n"+
                    "Host: whatever\n"+
                    "\n"
                    );
        assertTrue(response.indexOf("200")>0);
        assertFalse(response.indexOf("Connection: close")>0);
        assertTrue(response.indexOf("Hello World")>0);
        
        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.1\n"+
                    "Host: whatever\n"+
                    "Connection: close\n"+
                    "\n"
                    );
        assertTrue(response.indexOf("200")>0);
        assertTrue(response.indexOf("Connection: close")>0);
        assertTrue(response.indexOf("Hello World")>0);
        
        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.1\n"+
                    "Host: whatever\n"+
                    "Connection: Other, close\n"+
                    "\n"
                    );

        assertTrue(response.indexOf("200")>0);
        assertTrue(response.indexOf("Connection: close")>0);
        assertTrue(response.indexOf("Hello World")>0);
        
        

        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.0\n"+
                    "Host: whatever\n"+
                    "\n"
                    );
        assertTrue(response.indexOf("200")>0);
        assertFalse(response.indexOf("Connection: close")>0);
        assertTrue(response.indexOf("Hello World")>0);
        
        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.0\n"+
                    "Host: whatever\n"+
                    "Connection: Other, close\n"+
                    "\n"
                    );
        assertTrue(response.indexOf("200")>0);
        assertTrue(response.indexOf("Connection: close")>0);
        assertTrue(response.indexOf("Hello World")>0);

        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.0\n"+
                    "Host: whatever\n"+
                    "Connection: Other, keep-alive\n"+
                    "\n"
                    );
        assertTrue(response.indexOf("200")>0);
        assertTrue(response.indexOf("Connection: keep-alive")>0);
        assertTrue(response.indexOf("Hello World")>0);
        
        
        

        _handler._checker = new RequestTester()
        {
            public boolean check(HttpServletRequest request,HttpServletResponse response) throws IOException
            {
                response.setHeader("Connection","TE");
                response.addHeader("Connection","Other");
                response.getOutputStream().println("Hello World");
                return true;
            }  
        };
        
        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.1\n"+
                    "Host: whatever\n"+
                    "\n"
                    );
        assertTrue(response.indexOf("200")>0);
        assertTrue(response.indexOf("Connection: TE,Other")>0);
        assertTrue(response.indexOf("Hello World")>0);
        
        _connector.reopen();
        response=_connector.getResponses(
                    "GET / HTTP/1.1\n"+
                    "Host: whatever\n"+
                    "Connection: close\n"+
                    "\n"
                    );
        assertTrue(response.indexOf("200")>0);
        assertTrue(response.indexOf("Connection: close")>0);
        assertTrue(response.indexOf("Hello World")>0);

        
        
        
    }
    
    
    public void testCookies() throws Exception
    {
        final ArrayList cookies = new ArrayList();

        _handler._checker = new RequestTester()
        {
            public boolean check(HttpServletRequest request,HttpServletResponse response) throws IOException
            {
                javax.servlet.http.Cookie[] ca = request.getCookies();
                if (ca!=null)
                    cookies.addAll(Arrays.asList(ca));
                response.getOutputStream().println("Hello World");
                return true;
            }  
        };

        String response;
        _connector.reopen();

        cookies.clear();
        response=_connector.getResponses(
                    "GET / HTTP/1.1\n"+
                    "Host: whatever\n"+
                    "\n"
                    );
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals(0,cookies.size());
        

        cookies.clear();
        response=_connector.getResponses(
                    "GET / HTTP/1.1\n"+
                    "Host: whatever\n"+
                    "Cookie: name=value\n" +
                    "\n"
        );
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals(1,cookies.size());
        assertEquals("name",((Cookie)cookies.get(0)).getName());
        assertEquals("value",((Cookie)cookies.get(0)).getValue());

        cookies.clear();
        response=_connector.getResponses(
                "GET / HTTP/1.1\n"+
                "Host: whatever\n"+
                "Cookie: name=value; other=\"quoted=;value\"\n" +
                "\n"
        );
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals(2,cookies.size());
        assertEquals("name",((Cookie)cookies.get(0)).getName());
        assertEquals("value",((Cookie)cookies.get(0)).getValue());
        assertEquals("other",((Cookie)cookies.get(1)).getName());
        assertEquals("quoted=;value",((Cookie)cookies.get(1)).getValue());


        cookies.clear();
        response=_connector.getResponses(
                "GET /other HTTP/1.1\n"+
                "Host: whatever\n"+
                "Other: header\n"+
                "Cookie: name=value; other=\"quoted=;value\"\n" +
                "\n"+
                "GET /other HTTP/1.1\n"+
                "Host: whatever\n"+
                "Other: header\n"+
                "Cookie: name=value; other=\"quoted=;value\"\n" +
                "\n"
        );
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals(4,cookies.size());
        assertEquals("name",((Cookie)cookies.get(0)).getName());
        assertEquals("value",((Cookie)cookies.get(0)).getValue());
        assertEquals("other",((Cookie)cookies.get(1)).getName());
        assertEquals("quoted=;value",((Cookie)cookies.get(1)).getValue());

        assertTrue((Cookie)cookies.get(0)==(Cookie)cookies.get(2));
        assertTrue((Cookie)cookies.get(1)==(Cookie)cookies.get(3));


        cookies.clear();
        response=_connector.getResponses(
                "GET /other HTTP/1.1\n"+
                "Host: whatever\n"+
                "Other: header\n"+
                "Cookie: name=value; other=\"quoted=;value\"\n" +
                "\n"+
                "GET /other HTTP/1.1\n"+
                "Host: whatever\n"+
                "Other: header\n"+
                "Cookie: name=value; other=\"othervalue\"\n" +
                "\n"
        );
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals(4,cookies.size());
        assertEquals("name",((Cookie)cookies.get(0)).getName());
        assertEquals("value",((Cookie)cookies.get(0)).getValue());
        assertEquals("other",((Cookie)cookies.get(1)).getName());
        assertEquals("quoted=;value",((Cookie)cookies.get(1)).getValue());

        assertTrue((Cookie)cookies.get(0)!=(Cookie)cookies.get(2));
        assertTrue((Cookie)cookies.get(1)!=(Cookie)cookies.get(3));


        
    }
    
    
    interface RequestTester
    {
        boolean check(HttpServletRequest request,HttpServletResponse response) throws IOException;
    }
    
    class RequestHandler extends AbstractHandler
    {
        RequestTester _checker;
        String _content;
        
        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            ((Request)request).setHandled(true);
            
            if (request.getContentLength()>0)
                _content=IO.toString(request.getInputStream());
            
            if (_checker!=null && _checker.check(request,response))
                response.setStatus(200);
            else
                response.sendError(500); 
            
            
        }   
    }

}

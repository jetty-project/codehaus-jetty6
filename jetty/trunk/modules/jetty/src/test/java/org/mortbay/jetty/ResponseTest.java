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
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionContext;

import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.servlet.HashSessionManager;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author gregw
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ResponseTest extends TestCase
{
    Server _server = new Server();
    LocalConnector _connector = new LocalConnector();
    RequestHandler _handler = new RequestHandler();
    
    public ResponseTest(String arg0)
    {
        super(arg0);
        _server.setConnectors(new Connector[]{_connector});
        
        _server.setHandler(_handler);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ResponseTest.class);
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
        _handler._checker = new RequestChecker()
        {
            public boolean check(HttpServletRequest request)
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
    
    interface RequestChecker
    {
        boolean check(HttpServletRequest request);
    }
    
    class RequestHandler extends AbstractHandler
    {
        RequestChecker _checker;
        
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            ((Request)request).setHandled(true);
            if (_checker!=null && _checker.check(request))
                response.setStatus(200);
            else
                response.sendError(500);   
        }   
    }

}

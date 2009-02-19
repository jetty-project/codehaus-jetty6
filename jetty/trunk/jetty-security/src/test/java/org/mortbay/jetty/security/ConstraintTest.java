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

package org.mortbay.jetty.security;


import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.mortbay.jetty.http.security.B64Code;
import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.security.authentication.BasicServerAuthentication;
import org.mortbay.jetty.security.authentication.FormServerAuthentication;
import org.mortbay.jetty.security.authentication.SessionCachingServerAuthentication;
import org.mortbay.jetty.security.HashLoginService;
import org.mortbay.jetty.server.Connector;
import org.mortbay.jetty.server.LocalConnector;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.server.handler.AbstractHandler;
import org.mortbay.jetty.server.handler.ContextHandler;
import org.mortbay.jetty.server.servlet.SessionHandler;

/**
 * @author gregw
 */
public class ConstraintTest extends TestCase
{

    private static final String TEST_REALM = "TestRealm";

    Server _server = new Server();
    LocalConnector _connector = new LocalConnector();
    ContextHandler _context = new ContextHandler();
    SessionHandler _session = new SessionHandler();
    ConstraintSecurityHandler _security = new ConstraintSecurityHandler();
    LoginService _loginService = new HashLoginService("TestRealm",
                                                      Collections.<String, HashLoginService.User>singletonMap("user", new HashLoginService.KnownUser("user", new Password("pass"), new String[] {"user"})));
    final ServletCallbackHandler _callbackHandler = new ServletCallbackHandler(_loginService);
    RequestHandler _handler = new RequestHandler();
    private static final String APP_CONTEXT = "localhost /ctx";

    public ConstraintTest(String arg0)
    {
        super(arg0);
        _server.setConnectors(new Connector[]{_connector});
        _context.setContextPath("/ctx");
        _server.setHandler(_context);
        _context.setHandler(_session);
        _session.setHandler(_security);
        _security.setHandler(_handler);
        _security.setAuthenticationManager(new DefaultAuthenticationManager());

        Constraint constraint0 = new Constraint();
        constraint0.setAuthenticate(true);
        constraint0.setName("forbid");
        ConstraintMapping mapping0 = new ConstraintMapping();
        mapping0.setPathSpec("/forbid/*");
        mapping0.setConstraint(constraint0);

        Constraint constraint1 = new Constraint();
        constraint1.setAuthenticate(true);
        constraint1.setName("auth");
        constraint1.setRoles(new String[]{Constraint.ANY_ROLE});
        ConstraintMapping mapping1 = new ConstraintMapping();
        mapping1.setPathSpec("/auth/*");
        mapping1.setConstraint(constraint1);

        _security.setConstraintMappings(new ConstraintMapping[]
                {
                        mapping0, mapping1
                },
                Collections.singleton("user"));
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConstraintTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        _server.stop();
    }


    public void testBasic()
            throws Exception
    {
        _security.getAuthenticationManager().setAuthMethod(Constraint.__BASIC_AUTH);
        _security.setUserRealm(_loginService);
        //ServerAuthentication serverAuthentication = new BasicServerAuthentication(_loginService, TEST_REALM);
        //_security.setServerAuthentication(serverAuthentication);
        _server.start();

        String response;

        response = _connector.getResponses("GET /ctx/noauth/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/forbid/info HTTP/1.0\r\n\r\n");
        System.out.println(response);
        assertTrue(response.startsWith("HTTP/1.1 403 Forbidden"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 401 Unauthorized"));
        assertTrue(response.indexOf("WWW-Authenticate: basic realm=\"TestRealm\"") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("user:wrong") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 401 Unauthorized"));
        assertTrue(response.indexOf("WWW-Authenticate: basic realm=\"TestRealm\"") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("user:pass") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

    }

    public void testForm()
            throws Exception
    {
        _security.getAuthenticationManager().setAuthMethod(Constraint.__FORM_AUTH);
        _security.getAuthenticationManager().setLoginPage("/testLoginPage");
        _security.getAuthenticationManager().setErrorPage("/testErrorPage");
        _security.setUserRealm(_loginService);
        
       // ServerAuthentication serverAuthentication = new SessionCachingServerAuthentication(new FormServerAuthentication("/testLoginPage", "/testErrorPage", _loginService));
       // _security.setServerAuthentication(serverAuthentication);
        _server.start();

        String response;

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/noauth/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/forbid/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 403 Forbidden"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n\r\n");
        System.err.println(response);
        assertTrue(response.startsWith("HTTP/1.1 200 "));
//        assertTrue(response.indexOf("Location") > 0);
        assertTrue(response.indexOf("testLoginPage") > 0);
        String session = response.substring(response.indexOf("JSESSIONID=") + 11, response.indexOf(";Path=/ctx"));

        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 31\r\n" +
                "\r\n" +
                "j_username=user&j_password=wrong\r\n");
        assertTrue(response.startsWith("HTTP/1.1 302 "));
        assertTrue(response.indexOf("Location") > 0);
        assertTrue(response.indexOf("testErrorPage") > 0);


        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 31\r\n" +
                "\r\n" +
                "j_username=user&j_password=pass\r\n");
        assertTrue(response.startsWith("HTTP/1.1 302 "));
        assertTrue(response.indexOf("Location") > 0);
        assertTrue(response.indexOf("/ctx/auth/info") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

    }


    class RequestHandler extends AbstractHandler
    {

        public void handle(String target, HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException
        {
            ((Request) request).setHandled(true);
            response.setStatus(200);
            response.getOutputStream().println(request.getRequestURI());
        }
	
    }
}

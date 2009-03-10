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

import junit.framework.TestCase;
import org.mortbay.jetty.http.security.B64Code;
import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.security.authentication.BasicAuthenticator;
import org.mortbay.jetty.security.authentication.FormAuthenticator;
import org.mortbay.jetty.security.authentication.SessionCachingAuthenticator;
import org.mortbay.jetty.security.jaspi.ServletCallbackHandler;
import org.mortbay.jetty.server.*;
import org.mortbay.jetty.server.handler.AbstractHandler;
import org.mortbay.jetty.server.handler.ContextHandler;
import org.mortbay.jetty.server.handler.HandlerWrapper;
import org.mortbay.jetty.server.session.SessionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    HashLoginService _loginService = new HashLoginService(TEST_REALM);
                                                      
    final ServletCallbackHandler _callbackHandler = new ServletCallbackHandler(_loginService);
    RequestHandler _handler = new RequestHandler();
    private static final String APP_CONTEXT = "localhost /ctx";

    {
        _server.setConnectors(new Connector[]{_connector});
        _context.setContextPath("/ctx");
        _server.setHandler(_context);
        _context.setHandler(_session);
        _session.setHandler(_security);
        _security.setHandler(_handler);
        
        _loginService.putUser("user",new Password("password"));
        _loginService.putUser("user2",new Password("password"), new String[] {"user"});
        _loginService.putUser("admin",new Password("password"), new String[] {"user","administrator"});
        _server.addBean(_loginService);
    }
    
    public ConstraintTest(String arg0)
    {
        super(arg0);
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
        
        Constraint constraint2 = new Constraint();
        constraint2.setAuthenticate(true);
        constraint2.setName("admin");
        constraint2.setRoles(new String[]{"administrator"});
        ConstraintMapping mapping2 = new ConstraintMapping();
        mapping2.setPathSpec("/admin/*");
        mapping2.setConstraint(constraint2);
        
        Constraint constraint3 = new Constraint();
        constraint3.setAuthenticate(false);
        constraint3.setName("relax");
        ConstraintMapping mapping3 = new ConstraintMapping();
        mapping3.setPathSpec("/admin/relax/*");
        mapping3.setConstraint(constraint3);

        Set<String> knownRoles=new HashSet<String>();
        knownRoles.add("user");
        knownRoles.add("administrator");
        
        _security.setConstraintMappings(new ConstraintMapping[]
                {
                        mapping0, mapping1, mapping2, mapping3
                },knownRoles);
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



    public void testConstraints()
            throws Exception
    {
        ConstraintMapping[] mappings =_security.getConstraintMappings();
        
        assertTrue (mappings[0].getConstraint().isForbidden());
        assertFalse(mappings[1].getConstraint().isForbidden());
        assertFalse(mappings[2].getConstraint().isForbidden());
        assertFalse(mappings[3].getConstraint().isForbidden());
        
        assertFalse(mappings[0].getConstraint().isAnyRole());
        assertTrue (mappings[1].getConstraint().isAnyRole());
        assertFalse(mappings[2].getConstraint().isAnyRole());
        assertFalse(mappings[3].getConstraint().isAnyRole());

        assertFalse(mappings[0].getConstraint().hasRole("administrator"));
        assertTrue (mappings[1].getConstraint().hasRole("administrator"));
        assertTrue (mappings[2].getConstraint().hasRole("administrator"));
        assertFalse(mappings[3].getConstraint().hasRole("administrator"));
        
        assertTrue (mappings[0].getConstraint().getAuthenticate());
        assertTrue (mappings[1].getConstraint().getAuthenticate());
        assertTrue (mappings[2].getConstraint().getAuthenticate());
        assertFalse(mappings[3].getConstraint().getAuthenticate());
    }
    
    
    public void testBasic()
            throws Exception
    {
        _security.setAuthenticator(new BasicAuthenticator());
        _security.setStrict(false);
        _server.start();

        String response;
        response = _connector.getResponses("GET /ctx/noauth/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/forbid/info HTTP/1.0\r\n\r\n");
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
                "Authorization: " + B64Code.encode("user:password") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        

        // test admin
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 401 Unauthorized"));
        assertTrue(response.indexOf("WWW-Authenticate: basic realm=\"TestRealm\"") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("admin:wrong") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 401 Unauthorized"));
        assertTrue(response.indexOf("WWW-Authenticate: basic realm=\"TestRealm\"") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("user:password") + "\r\n" +
                "\r\n");

        assertTrue(response.startsWith("HTTP/1.1 403 "));
        assertTrue(response.indexOf("User not in required role") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("admin:password") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/relax/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
    }

    public void testForm()
            throws Exception
    {
        _security.setAuthenticator(new SessionCachingAuthenticator(
                new FormAuthenticator("/testLoginPage","/testErrorPage")));
        _security.setStrict(false);
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
//        assertTrue(response.startsWith("HTTP/1.1 302 "));
//        assertTrue(response.indexOf("testLoginPage") > 0);
        String session = response.substring(response.indexOf("JSESSIONID=") + 11, response.indexOf(";Path=/ctx"));

        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 31\r\n" +
                "\r\n" +
                "j_username=user&j_password=wrong\r\n");
        //TODO we are forwarded to the error page now.  Is there any way to verify the contents?
        assertTrue(response.startsWith("HTTP/1.1 200 "));
//        assertTrue(response.indexOf("Location") > 0);
//        assertTrue(response.indexOf("testErrorPage") > 0);


        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 35\r\n" +
                "\r\n" +
                "j_username=user&j_password=password\r\n");
        assertTrue(response.startsWith("HTTP/1.1 302 "));
        assertTrue(response.indexOf("Location") > 0);
        assertTrue(response.indexOf("/ctx/auth/info") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 403"));
        assertTrue(response.indexOf("User not in required role") > 0);
        
    }

    public void testStrictBasic()
            throws Exception
    {
        _security.setAuthenticator(new BasicAuthenticator());
        _server.start();

        String response;
        response = _connector.getResponses("GET /ctx/noauth/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/forbid/info HTTP/1.0\r\n\r\n");
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
                "Authorization: " + B64Code.encode("user:password") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 403"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("user2:password") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        

        // test admin
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 401 Unauthorized"));
        assertTrue(response.indexOf("WWW-Authenticate: basic realm=\"TestRealm\"") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("admin:wrong") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 401 Unauthorized"));
        assertTrue(response.indexOf("WWW-Authenticate: basic realm=\"TestRealm\"") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("user:password") + "\r\n" +
                "\r\n");

        assertTrue(response.startsWith("HTTP/1.1 403 "));
        assertTrue(response.indexOf("User not in required role") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("admin:password") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/relax/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
    }

    public void testStrictForm()
            throws Exception
    {
        _security.setAuthenticator(new SessionCachingAuthenticator(
                new FormAuthenticator("/testLoginPage","/testErrorPage")));
        
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
//        assertTrue(response.startsWith("HTTP/1.1 302 "));
//        assertTrue(response.indexOf("testLoginPage") > 0);
        String session = response.substring(response.indexOf("JSESSIONID=") + 11, response.indexOf(";Path=/ctx"));

        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 31\r\n" +
                "\r\n" +
                "j_username=user&j_password=wrong\r\n");
        //TODO we are forwarded to the error page now.  Is there any way to verify the contents?
        assertTrue(response.startsWith("HTTP/1.1 200 "));
//        assertTrue(response.indexOf("Location") > 0);
//        assertTrue(response.indexOf("testErrorPage") > 0);


        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 35\r\n" +
                "\r\n" +
                "j_username=user&j_password=password\r\n");
        assertTrue(response.startsWith("HTTP/1.1 302 "));
        assertTrue(response.indexOf("Location") > 0);
        assertTrue(response.indexOf("/ctx/auth/info") > 0);

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 403"));
        assertTrue(response.indexOf("User not in required role") > 0);
        
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 403"));
        assertTrue(response.indexOf("User not in required role") > 0);
        
        
        
        // log in again as user2
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n\r\n");
//        assertTrue(response.startsWith("HTTP/1.1 302 "));
//        assertTrue(response.indexOf("testLoginPage") > 0);
        session = response.substring(response.indexOf("JSESSIONID=") + 11, response.indexOf(";Path=/ctx"));

        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 36\r\n" +
                "\r\n" +
                "j_username=user2&j_password=password\r\n");
        assertTrue(response.startsWith("HTTP/1.1 302 "));
        assertTrue(response.indexOf("Location") > 0);
        assertTrue(response.indexOf("/ctx/auth/info") > 0);
        

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 403"));
        assertTrue(response.indexOf("User not in required role") > 0);
        

        
        // log in again as admin
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n\r\n");
//        assertTrue(response.startsWith("HTTP/1.1 302 "));
//        assertTrue(response.indexOf("testLoginPage") > 0);
        session = response.substring(response.indexOf("JSESSIONID=") + 11, response.indexOf(";Path=/ctx"));

        _connector.reopen();
        response = _connector.getResponses("POST /ctx/j_security_check HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 36\r\n" +
                "\r\n" +
                "j_username=admin&j_password=password\r\n");
        assertTrue(response.startsWith("HTTP/1.1 302 "));
        assertTrue(response.indexOf("Location") > 0);
        assertTrue(response.indexOf("/ctx/auth/info") > 0);
        

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/admin/info HTTP/1.0\r\n" +
                "Cookie: JSESSIONID=" + session + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        
        
    }

    public void testRoleRef()
    throws Exception
    {
        RoleCheckHandler check=new RoleCheckHandler();
        _security.setHandler(check);
        _security.setAuthenticator(new BasicAuthenticator());
        _security.setStrict(false);
        _server.start();

        String response;
        response = _connector.getResponses("GET /ctx/noauth/info HTTP/1.0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));

        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("user2:password") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 500 "));

        _server.stop();
        
        RoleRefHandler roleref = new RoleRefHandler();
        _security.setHandler(roleref);
        roleref.setHandler(check);
        
        _server.start();
        
        _connector.reopen();
        response = _connector.getResponses("GET /ctx/auth/info HTTP/1.0\r\n" +
                "Authorization: " + B64Code.encode("user2:password") + "\r\n" +
                "\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
    }

    class RequestHandler extends AbstractHandler
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException
        {
            ((Request) request).setHandled(true);
            if (request.getAuthType()==null || "user".equals(request.getRemoteUser()) || request.isUserInRole("user"))
                response.setStatus(200);
            else
                response.sendError(500);
        }
    }

    class RoleRefHandler extends HandlerWrapper
    {
        /* ------------------------------------------------------------ */
        /**
         * @see org.mortbay.jetty.server.handler.HandlerWrapper#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        @Override
        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            UserIdentity old = ((Request) request).getUserIdentity();
            UserIdentity scoped = _security.getIdentityService().associate(old,
                    new UserIdentity.Scope()
                    {

                        public String getContextPath()
                        {
                            return "/";
                        }

                        public String getName()
                        {
                            return "someServlet";
                        }

                        public Map<String, String> getRoleRefMap()
                        {
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("untranslated", "user");
                            return map;
                        }

                        public String getRunAsRole()
                        {
                            return null;
                        }

                    });
            ((Request)request).setUserIdentity(scoped);

            try
            {
                super.handle(target,request,response);
            }
            finally
            {
                _security.getIdentityService().disassociate(scoped);
                ((Request)request).setUserIdentity(old);
            }
        }
    }
    
    class RoleCheckHandler extends AbstractHandler
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException
        {
            ((Request) request).setHandled(true);
            if (request.getAuthType()==null || "user".equals(request.getRemoteUser()) || request.isUserInRole("untranslated"))
                response.setStatus(200);
            else
                response.sendError(500);
        }
    }
}

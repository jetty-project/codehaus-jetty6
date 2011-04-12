package javax.servlet.aspect;
//========================================================================
//$Id:$
//Copyright 2011 Webtide, LLC
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


import java.security.AccessControlException;
import java.security.Policy;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.eclipse.jetty.policy.JettyPolicy;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CookiePolicyBarrierTest
{

    private HashMap<String, String> evaluator = new HashMap<String, String>();
    
    @Before
    public void setUp() throws Exception
    {
        
        evaluator.put("jetty.home",MavenTestingUtils.getBaseURI().toASCIIString());
        evaluator.put("basedir",MavenTestingUtils.getBaseURI().toASCIIString());
        
        System.setSecurityManager(null);
        Policy.setPolicy(null);
    }
    
    @After
    public void destroy() throws Exception
    {
        
        System.setSecurityManager(null);
        Policy.setPolicy(null);
    }
    
    private void assertCookieConstructor( String directory ) throws Exception
    {
        JettyPolicy ap = new JettyPolicy( directory, evaluator );
        ap.refresh();
                
        Policy.setPolicy(ap);
        System.setSecurityManager(new SecurityManager());
                
        Cookie cookie = new Cookie("foo", "bar");       
    }
    
    @Test (expected = AccessControlException.class)
    public void testCookieConstructorNotAllowed() throws Exception
    {
        assertCookieConstructor( MavenTestingUtils.getTestResourceDir("cookie-test-1").getAbsolutePath() );
    }
    
    @Test
    public void testCookieConstructorAllowed() throws Exception
    {
        assertCookieConstructor( MavenTestingUtils.getTestResourceDir("cookie-test-2").getAbsolutePath() );
    }
    
    private void assertCookieAccess( Cookie c, String directory ) throws Exception
    {
        JettyPolicy ap = new JettyPolicy( directory, evaluator );
        ap.refresh();
                
        Policy.setPolicy(ap);
        System.setSecurityManager(new SecurityManager());
                
        c.setComment("foo");
    }
    
    @Test (expected = AccessControlException.class)
    @Ignore ("pointcut too broad")
    public void testCookieAccessNotAllowed() throws Exception
    {
        Cookie cookie = new Cookie("foo", "boo");
        
        assertCookieAccess( cookie, MavenTestingUtils.getTestResourceDir("cookie-test-1").getAbsolutePath() );
    }
    
    @Test
    @Ignore ("pointcut too broad")
    public void testCookieAccessAllowed() throws Exception
    {
        Cookie cookie = new Cookie("foo", "boo");

        assertCookieAccess( cookie, MavenTestingUtils.getTestResourceDir("cookie-test-2").getAbsolutePath() );
    }
    
    
}

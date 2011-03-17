package org.mortbay.jetty.aspect.servlets;
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


import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.security.Policy;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.policy.JettyPolicy;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServletPolicyBarrierTest
{

    private HashMap<String, String> evaluator = new HashMap<String, String>();
    private Server server;
    private SocketConnector connector;
    private ServletContextHandler context;
    
    @Before
    public void setUp() throws Exception
    {
        evaluator.put("main.classes.dir",MavenTestingUtils.getTargetFile("classes").toString());
        evaluator.put("test.resource.dir",MavenTestingUtils.getTestResourcesDir().toString());
        evaluator.put("test.classes.dir",MavenTestingUtils.getTargetFile("test-classes").getAbsolutePath());
        evaluator.put("jetty.home",MavenTestingUtils.getBaseURI().toASCIIString());
        evaluator.put("basedir",MavenTestingUtils.getBaseURI().toASCIIString());
        
        System.setSecurityManager(null);
        Policy.setPolicy(null);

        server = new Server();
        server.setSendServerVersion(false);

        connector = new SocketConnector();

        context = new ServletContextHandler();
        context.setContextPath("/context");
        context.setWelcomeFiles(new String[] {"index.html","index.jsp","index.htm"});

        server.setHandler(context);
        server.addConnector(connector);

        server.start();
    }
    
    @After
    public void destroy() throws Exception
    {
        server.stop();
        server.join();
        
        System.setSecurityManager(null);
        Policy.setPolicy(null);
    }
    
    private void testServletDestroy( String directory ) throws Exception
    {
        JettyPolicy ap = new JettyPolicy( directory, evaluator );
        ap.refresh();
                
        Policy.setPolicy(ap);
        System.setSecurityManager(new SecurityManager());
        
        ServletHolder defholder = context.addServlet(DefaultServlet.class,"/*");
        
        defholder.getServlet().destroy();
    }
    
    @Test (expected = AccessControlException.class)
    public void testServletDestroyNotAllowed() throws Exception
    {
        testServletDestroy( MavenTestingUtils.getTestResourceDir("servlet-test-1").getAbsolutePath() );
    }
    
    @Test
    public void testServletDestroyAllowed() throws Exception
    {
        testServletDestroy( MavenTestingUtils.getTestResourceDir("servlet-test-2").getAbsolutePath() );
    }
    
    private void testServletGetServletInfo( String directory ) throws Exception
    {
        JettyPolicy ap = new JettyPolicy( directory, evaluator );
        ap.refresh();
                
        Policy.setPolicy(ap);
        System.setSecurityManager(new SecurityManager());
        
        ServletHolder defholder = context.addServlet(DefaultServlet.class,"/*");
        
        defholder.getServlet().getServletInfo();
    }
    
    @Test (expected = AccessControlException.class)
    public void testServletGetServletInfoNotAllowed() throws Exception
    {
        testServletGetServletInfo(MavenTestingUtils.getTestResourceDir("servlet-test-1").getAbsolutePath());
    }
    
    @Test
    public void testServletGetServletInfoAllowed() throws Exception
    {
        testServletGetServletInfo(MavenTestingUtils.getTestResourceDir("servlet-test-3").getAbsolutePath());

    }
    
    
    /**
     * This test configures an environment where you can apply permissions to a directory
     * containing a servlet and servlet api and show how the access controller allows you to 
     * apply permissions to user space applications and removing the need to apply 
     * that same permission to all of jetty.
     * 
     * @param directory
     * @throws Exception
     */
    private void testServletService( String directory ) throws Exception
    {
        JettyPolicy ap = new JettyPolicy( directory, evaluator );
        ap.refresh();
                
        Policy.setPolicy(ap);
        System.setSecurityManager(new SecurityManager());
        
        context.addServlet(HelloServlet.class,"/foo");
        
        SimpleRequest request = new SimpleRequest(getServerURI());

        // useful for viewing the protection domains referenced up to this point
        // ap.dump(System.out);
        request.getString("/context/foo");    
    }
    
    /*
     * TODO server is throwing ACE, here it shows as IOE, maybe we can do this check better and 
     * not mask other issues
     */
    @Test (expected = IOException.class)
    public void testServletServiceNotAllowed() throws Exception
    {
        testServletService(MavenTestingUtils.getTestResourceDir("service-test-1").getAbsolutePath());
    }
    
    @Test
    public void testServletServiceAllowed() throws Exception
    {
        testServletService(MavenTestingUtils.getTestResourceDir("service-test-2").getAbsolutePath());
    }
 
    public URI getServerURI() throws UnknownHostException
    {
        StringBuffer uri = new StringBuffer();
        uri.append(HttpSchemes.HTTP).append("://");
        uri.append(InetAddress.getLocalHost().getHostAddress());
        uri.append(":").append(connector.getLocalPort());
        return URI.create(uri.toString());
    }
    
}

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


import java.security.AccessControlException;
import java.security.Policy;
import java.util.HashMap;

import org.eclipse.jetty.policy.JettyPolicy;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServletPolicyBarrierTest
{

    private HashMap<String, String> evaluator = new HashMap<String, String>();
    private Server server;
    private LocalConnector connector;
    private ServletContextHandler context;
    
    @Before
    public void setUp() throws Exception
    {
        
        evaluator.put("jetty.home",MavenTestingUtils.getBaseURI().toASCIIString());
        evaluator.put("basedir",MavenTestingUtils.getBaseURI().toASCIIString());
        
        System.setSecurityManager(null);
        Policy.setPolicy(null);

        server = new Server();
        server.setSendServerVersion(false);

        connector = new LocalConnector();

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
}

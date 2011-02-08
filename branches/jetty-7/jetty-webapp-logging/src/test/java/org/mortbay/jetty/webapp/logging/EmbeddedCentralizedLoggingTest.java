// ========================================================================
// Copyright (c) Webtide LLC
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// The Apache License v2.0 is available at
// http://www.apache.org/licenses/LICENSE-2.0.txt
//
// You may elect to redistribute this code under either of these licenses.
// ========================================================================
package org.mortbay.jetty.webapp.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.PathAssert;
import org.eclipse.jetty.toolchain.test.TestingDir;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mortbay.jetty.webapp.logging.TestAppender.LogEvent;

/**
 * Test centralized logging in an embedded scenario
 */
public class EmbeddedCentralizedLoggingTest
{
    private static final String LOGGING_SERVLET_ID = "org.mortbay.jetty.tests.webapp.LoggingServlet";
    private static final boolean DUMP = false;
    @Rule
    public TestingDir testingdir = new TestingDir();
    
    private TestAppender testAppender;

    private void assertContainsLogEvents(TestAppender capturedEvents, List<LogEvent> expectedLogs)
    {
        for (LogEvent expectedEvent : expectedLogs)
        {
            if (!capturedEvents.contains(expectedEvent))
            {
                capturedEvents.dump();
                Assert.fail("LogEvent not found: " + expectedEvent);
            }
        }
    }

    private Handler createWebapp(String contextPath, String webappName)
    {
        File webappFile = MavenTestingUtils.getTargetFile("test-wars/" + webappName);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);
        webapp.setWar(webappFile.getAbsolutePath());

        return webapp;
    }

    protected Server createWebAppServer(String contextPath, String webappName) throws Exception
    {
        testAppender = TestAppender.findAppender();
        testAppender.reset();

        Server server = new Server();
        CentralizedLoggingBean.setWebAppContextConfigurations(server);

        Connector connector = new SelectChannelConnector();
        connector.setPort(0);
        server.setConnectors(new Connector[]
        { connector });

        File webappFile = MavenTestingUtils.getTargetFile("test-wars/" + webappName);
        PathAssert.assertFileExists("Test War",webappFile);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);
        webapp.setWar(webappFile.getAbsolutePath());

        server.setHandler(webapp);

        return server;
    }

    @Before
    public void setUp() throws Exception
    {
        testingdir.ensureEmpty();
        File testTmpDir = testingdir.getFile("workdir");
        testTmpDir.mkdirs();
        System.setProperty("java.io.tmpdir",testTmpDir.getAbsolutePath());

        TestAppender.initialize();
    }

    @Test
    public void testEmbeddedAll() throws Exception
    {
        testAppender = TestAppender.findAppender();
        testAppender.reset();

        Server server = new Server();
        CentralizedLoggingBean.setWebAppContextConfigurations(server);

        Connector connector = new SelectChannelConnector();
        connector.setPort(0);
        server.setConnectors(new Connector[]
        { connector });

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.addHandler(createWebapp("/log4j","test-war-log4j_1.2.15.war"));
        handlers.addHandler(createWebapp("/slf4j","test-war-slf4j_1.5.6.war"));
        handlers.addHandler(createWebapp("/clogging","test-war-commons_logging_1.1.war"));
        handlers.addHandler(createWebapp("/javalogging","test-war-java_util_logging.war"));

        ContextLogHandler loghandler = new ContextLogHandler();
        loghandler.setHandler(handlers);

        server.setHandler(loghandler);

        server.start();

        if (DUMP)
        {
            server.dump(System.out);
        }
        
        SimpleRequest.get(server,"/log4j/logging");
        SimpleRequest.get(server,"/slf4j/logging");
        SimpleRequest.get(server,"/clogging/logging");
        SimpleRequest.get(server,"/javalogging/logging");

        server.stop();

        String prefix = "LoggingServlet(commons-logging)";
        List<LogEvent> expectedLogs = new ArrayList<LogEvent>();
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));
        prefix = "LoggingServlet(log4j)";
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));
        prefix = "LoggingServlet(java)";
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));
        prefix = "LoggingServlet(slf4j)";
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));

        assertContainsLogEvents(testAppender,expectedLogs);
    }

    @Test
    public void testEmbeddedWebappCommonsLogging() throws Exception
    {
        Server server = createWebAppServer("/clogging","test-war-commons_logging_1.1.war");

        server.start();
        
        if (DUMP)
        {
            server.dump(System.out);
        }

        SimpleRequest.get(server,"/clogging/logging");

        server.stop();

        String prefix = "LoggingServlet(commons-logging)";
        List<LogEvent> expectedLogs = new ArrayList<LogEvent>();
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));

        assertContainsLogEvents(testAppender,expectedLogs);
    }

    @Test
    public void testEmbeddedWebappJavaUtil() throws Exception
    {
        Server server = createWebAppServer("/javalogging","test-war-java_util_logging.war");

        server.start();

        if (DUMP)
        {
            server.dump(System.out);
        }
        
        SimpleRequest.get(server,"/javalogging/logging");

        server.stop();

        String prefix = "LoggingServlet(java)";
        List<LogEvent> expectedLogs = new ArrayList<LogEvent>();
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));

        assertContainsLogEvents(testAppender,expectedLogs);
    }

    @Test
    public void testEmbeddedWebappLog4j() throws Exception
    {
        Server server = createWebAppServer("/log4j","test-war-log4j_1.2.15.war");

        server.start();

        if (DUMP)
        {
            server.dump(System.out);
        }
        
        SimpleRequest.get(server,"/log4j/logging");

        server.stop();

        String prefix = "LoggingServlet(log4j)";
        List<LogEvent> expectedLogs = new ArrayList<LogEvent>();
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));

        assertContainsLogEvents(testAppender,expectedLogs);
    }

    @Test
    public void testEmbeddedWebappSlf4j() throws Exception
    {
        Server server = createWebAppServer("/slf4j","test-war-slf4j_1.5.6.war");

        server.start();

        if (DUMP)
        {
            server.dump(System.out);
        }
        
        SimpleRequest.get(server,"/slf4j/logging");

        server.stop();

        String prefix = "LoggingServlet(slf4j)";
        List<LogEvent> expectedLogs = new ArrayList<LogEvent>();
        // expectedLogs.add(new LogEvent(Level.DEBUG,LOGGING_SERVLET_ID,prefix + " initialized"));
        expectedLogs.add(new LogEvent(Level.INFO,LOGGING_SERVLET_ID,prefix + " GET requested"));
        expectedLogs.add(new LogEvent(Level.WARNING,LOGGING_SERVLET_ID,prefix + " Slightly warn, with a chance of log events"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Nothing is (intentionally) being output by this Servlet"));
        expectedLogs.add(new LogEvent(Level.SEVERE,LOGGING_SERVLET_ID,prefix + " Whoops (intentionally) causing a Throwable")
                .expectedThrowable(new FileNotFoundException("A file cannot be found")));

        assertContainsLogEvents(testAppender,expectedLogs);
    }
}

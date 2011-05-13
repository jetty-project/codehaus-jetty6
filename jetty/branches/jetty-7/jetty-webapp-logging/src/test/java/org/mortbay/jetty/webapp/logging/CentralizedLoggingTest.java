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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.webapp.logging.TestAppender.LogEvent;

public class CentralizedLoggingTest
{
    private static final String LOGGING_SERVLET_ID = "org.mortbay.jetty.tests.webapp.LoggingServlet";
    private static XmlConfiguredJetty jetty;

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

    @BeforeClass
    public static void setUp() throws Exception
    {
        // Make sure that jetty uses our testing appender.
        TestAppender.initialize();

        jetty = new XmlConfiguredJetty(MavenTestingUtils.getTargetTestingDir(CentralizedLoggingTest.class,"jettyhome"));
        jetty.addConfiguration("jetty.xml");
        jetty.addConfiguration("jetty-deploy-contexts.xml");
        jetty.addConfiguration("jetty-deploy-wars.xml");
        jetty.addConfiguration("jetty-webapp-logging.xml");

        jetty.load();

        jetty.start();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        jetty.stop();
    }

    @Test
    public void testRoutingWarLog4j() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/test-war-log4j_1.2.15/logging");

        String prefix = "LoggingServlet(log4j-1.2.15)";
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
    public void testRoutingWarCommonsLogging() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/test-war-commons_logging_1.1/logging");

        String prefix = "LoggingServlet(commons-logging-1.1)";
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
    public void testRoutingWarSlf4j() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/test-war-slf4j_1.5.6/logging");

        String prefix = "LoggingServlet(slf4j-1.5.6)";
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
    public void testRoutingWarJavaUtilLogging() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/test-war-java_util_logging/logging");

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
    public void testRoutingContextLog4j() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/log4j/logging");

        String prefix = "LoggingServlet(log4j-1.2.15)";
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
    public void testRoutingContextCommonsLogging() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/commons/logging");

        String prefix = "LoggingServlet(commons-logging-1.1)";
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
    public void testRoutingContextSlf4j() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/slf4j/logging");

        String prefix = "LoggingServlet(slf4j-1.5.6)";
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
    public void testRoutingContextJavaUtilLogging() throws IOException
    {
        TestAppender testAppender = getServerTestAppender();

        SimpleRequest.get(jetty,"/java/logging");

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

    private TestAppender getServerTestAppender()
    {
        TestAppender testAppender = TestAppender.findAppender();
        Assert.assertNotNull("Should have found TestAppender in configuration",testAppender);
        testAppender.reset();
        return testAppender;
    }
}

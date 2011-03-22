package org.mortbay.jetty.tests.policy.integration;

import static org.hamcrest.Matchers.*;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test Jetty with 2 webapps, and no jetty-policy or java security in place.
 */
public class StandardTest
{
    private static JettyProcess jetty;

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyProcess(StandardTest.class);

        jetty.copyTestWar("test-war-java_util_logging.war");
        jetty.copyTestWar("test-war-policy.war");

        jetty.delete("webapps/test.war");
        jetty.delete("contexts/test.d");
        jetty.delete("contexts/javadoc.xml");
        jetty.delete("contexts/test.xml");

        jetty.overlayConfig("standard");

        jetty.start();
    }

    @AfterClass
    public static void shutdownJetty() throws Exception
    {
        if (jetty != null)
        {
            jetty.stop();
        }
    }

    @Test
    public void testFilesystem() throws Exception
    {
        assertCheckerSuccess("processFilesystemChecks");
    }

    @Test
    public void testJettyLog() throws Exception
    {
        assertCheckerSuccess("processJettyLogChecks");
    }

    @Test
    public void testServletContext() throws Exception
    {
        assertCheckerSuccess("processFooWebappContextChecks");
    }

    @Test
    public void testRequestDispatcher() throws Exception
    {
        assertCheckerSuccess("processFooWebappRequestDispatcherChecks");
    }

    @Test
    @Ignore("need to fix loadLibrary to actually load a real library")
    public void testLib() throws Exception
    {
        assertCheckerSuccess("processLibChecks");
    }

    @Test
    public void testSystemProperty() throws Exception
    {
        assertCheckerSuccess("processSystemPropertyChecks");
    }

    private void assertCheckerSuccess(String testname) throws Exception
    {
        SimpleRequest request = new SimpleRequest(jetty.getBaseUri());
        Properties props = request.getProperties("/policytests/checker/" + testname);
        @SuppressWarnings("unchecked")
        Enumeration<String> names = (Enumeration<String>)props.propertyNames();
        while (names.hasMoreElements())
        {
            String name = names.nextElement();
            String value = props.getProperty(name);
            Assert.assertThat("[" + testname + "] " + name,value,startsWith("Success"));
        }
    }
}

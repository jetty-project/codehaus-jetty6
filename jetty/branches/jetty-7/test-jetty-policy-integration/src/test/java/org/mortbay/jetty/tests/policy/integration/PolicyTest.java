package org.mortbay.jetty.tests.policy.integration;

import static org.hamcrest.Matchers.*;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Jetty with 2 webapps, with jetty-policy or java security in place.
 */
public class PolicyTest
{
    private static JettyProcess jetty;

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyProcess(PolicyTest.class);

        jetty.copyTestWar("test-war-java_util_logging.war");
        jetty.copyTestWar("test-war-policy.war");

        jetty.delete("webapps/test.war");
        jetty.delete("contexts/test.d");
        jetty.delete("contexts/javadoc.xml");
        jetty.delete("contexts/test.xml");
        
        // Remove standard servlet-api jar
        jetty.delete("lib/servlet-api-2.5.jar");
        // Use AspectJ + Security enhanced servlet-api jar
        jetty.copyLib("jetty-aspect-servlet-api-2.5.jar","lib-secure/servlet-api-2.5.jar");
        jetty.copyLib("aspectjrt.jar","lib-secure/aspectjrt.jar");

        jetty.overlayConfig("policy");

        jetty.setDebug(true);
        
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
        assertCheckerFailure("processFilesystemChecks");
    }

    @Test
    public void testJettyLog() throws Exception
    {
        assertCheckerFailure("processJettyLogChecks");
    }

    @Test
    public void testServletContext() throws Exception
    {
        assertCheckerFailure("processFooWebappContextChecks");
    }

    @Test
    public void testRequestDispatcher() throws Exception
    {
        assertCheckerFailure("processFooWebappRequestDispatcherChecks");
    }

    @Test
    public void testLib() throws Exception
    {
        assertCheckerFailure("processLibChecks");
    }

    @Test
    public void testSystemProperty() throws Exception
    {
        assertCheckerFailure("processSystemPropertyChecks");
    }

    private void assertCheckerFailure(String testname) throws Exception
    {
        SimpleRequest request = new SimpleRequest(jetty.getBaseUri());
        Properties props = request.getProperties("/policytests/checker/" + testname);
        @SuppressWarnings("unchecked")
        Enumeration<String> names = (Enumeration<String>)props.propertyNames();
        while (names.hasMoreElements())
        {
            String name = names.nextElement();
            String value = props.getProperty(name);
            Assert.assertThat("[" + testname + "] " + name,value,not(startsWith("Success")));
        }
    }
}

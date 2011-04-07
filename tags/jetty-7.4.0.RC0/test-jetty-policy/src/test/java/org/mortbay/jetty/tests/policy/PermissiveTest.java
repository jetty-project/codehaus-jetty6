package org.mortbay.jetty.tests.policy;

import static org.hamcrest.Matchers.*;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.jetty.toolchain.test.TestingDir;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class PermissiveTest
{
    @Rule
    public TestingDir testdir = new TestingDir();
    private XmlConfiguredJetty jetty;

    @Before
    public void initServer() throws Exception
    {
        jetty = new XmlConfiguredJetty(testdir);
        jetty.addConfiguration("jetty.xml");
        jetty.addConfiguration("jetty-deploys.xml");

        jetty.copyTestWar("test-war-java_util_logging.war");
        jetty.copyTestWar("test-war-policy.war");
        jetty.copyContext("foo.xml");
        jetty.copyContext("policytests.xml");

        // Load Configuration(s)
        jetty.load();

        // Start it
        jetty.start();
    }

    @After
    public void shutdownServer() throws Exception
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
        SimpleRequest request = new SimpleRequest(jetty);
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

package org.mortbay.jetty.tests.distribution.jmx;

import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.test.remote.RemoteTestSuiteClient;
import org.mortbay.jetty.test.remote.RemoteTestSuiteResults;
import org.mortbay.jetty.tests.distribution.JettyProcess;

/**
 * Test Jetty with 2 webapps, with jetty-policy or java security in place.
 */
public class JmxAppTest
{
    private static JettyProcess jetty;

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyProcess(JmxAppTest.class);

        jetty.copyTestWar("test-app-jmx.war");
        jetty.copyTestWar("test-war-dump.war");

        jetty.overlayConfig("jmx");

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
    public void testJmxAttributes() throws Exception
    {
        RemoteTestSuiteClient client = new RemoteTestSuiteClient(jetty.getBaseUri());
        
        RemoteTestSuiteResults results = client.getResults("/test-app-jmx/testSuite");
        
        results.assertSuccess();

    }
}

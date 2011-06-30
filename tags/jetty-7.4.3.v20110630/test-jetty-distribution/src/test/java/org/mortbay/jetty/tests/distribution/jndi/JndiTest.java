package org.mortbay.jetty.tests.distribution.jndi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.test.remote.RemoteTestSuiteClient;
import org.mortbay.jetty.test.remote.RemoteTestSuiteResults;
import org.mortbay.jetty.tests.distribution.JettyProcess;

/**
 * Test Jetty with 2 webapps, with jetty-policy or java security in place.
 */
public class JndiTest
{
    private static JettyProcess jetty;

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyProcess(JndiTest.class);

        jetty.copyTestWar("test-jndi-webapp.war");

        jetty.delete("webapps/test.war");
        jetty.delete("contexts/test.d");
        jetty.delete("contexts/javadoc.xml");
        jetty.delete("contexts/test.xml");

        jetty.overlayConfig("jndi");

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
    public void testJndi() throws Exception
    {
        //SimpleRequest request = new SimpleRequest(jetty.getBaseUri());
        //String path = String.format("/test-war-download/remoteAssert");
        //String response = request.getString(path);
        
        RemoteTestSuiteClient client = new RemoteTestSuiteClient(jetty.getBaseUri());
        
        RemoteTestSuiteResults results = client.getResults("/test-jndi-webapp/testSuite");
        
        results.assertSuccess();
        
        //System.out.println(response);
        //Assert.assertTrue("Expecting we got a validation string:", Validation.isNotBlank(response));
        
        //Assert.assertTrue("Expecting no failures:", Validation.passes(response));
    }
}

package org.mortbay.jetty.test.validation;

import static org.hamcrest.Matchers.*;

import java.net.URI;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.test.validation.client.RemoteAssertClient;
import org.mortbay.jetty.test.validation.client.RemoteAssertResults;
import org.mortbay.jetty.test.validation.client.RemoteAssertResults.TestClass;
import org.mortbay.jetty.test.validation.client.RemoteAssertResults.TestResult;
import org.mortbay.jetty.test.validation.fwk.ContextTest;
import org.mortbay.jetty.test.validation.fwk.SimpleTest;

public class FrameworkTest
{
    private static Server jetty;
    private static URI baseUri;

    @BeforeClass
    public static void initEmbeddedJetty() throws Exception
    {
        jetty = new Server();

        // Connectors
        Connector socketConnector = new SocketConnector();
        socketConnector.setPort(0);
        Connector connectors[] = new Connector[1];
        connectors[0] = socketConnector;
        jetty.setConnectors(connectors);

        // Servlet Context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        jetty.setHandler(context);

        // Servlet
        context.addServlet(BasicTestSuiteServlet.class,"/tests/");

        // Start Jetty
        jetty.start();

        // Figure out Base URI
        String host = socketConnector.getHost();
        if (host == null)
        {
            host = "localhost";
        }
        baseUri = URI.create("http://" + host + ":" + socketConnector.getLocalPort());
    }

    @AfterClass
    public static void stopEmbeddedJetty() throws Exception
    {
        if (jetty != null)
        {
            jetty.stop();
        }
    }

    @Test
    public void testRunAllTests() throws Exception
    {
        RemoteAssertClient raclient = new RemoteAssertClient(baseUri);

        RemoteAssertResults results = raclient.getResults("/tests/");

        Assert.assertThat("Class Count",results.getTestClassCount(),is(2));

        TestClass tc;
        TestResult tr;

        tc = results.getTestClass(SimpleTest.class.getName());
        Assert.assertNotNull("Should have found TestClass[SimpleTest]",tc);
        Assert.assertEquals("SimpleTest.testCount",5,tc.getTestCount());

        tr = tc.getTestResult("testNumberIgnored");
        Assert.assertTrue("test should be ignore",tr.isIgnored());

        tr = tc.getTestResult("testQuoteEqualsFailure");
        Assert.assertFalse("test should not be ignore",tr.isIgnored());
        Assert.assertFalse("test should not be success",tr.isSuccess());
        Assert.assertFalse("test should not have assumption failure",tr.isAssumptionFailure());
        Assert.assertEquals("test.failure.header","testQuoteEqualsFailure(org.mortbay.jetty.test.validation.fwk.SimpleTest)",tr.getFailureHeader());
        Assert.assertEquals("test.failure.message","Quote expected:<[Sweet]> but was:<[Dude]>",tr.getFailureMessage());
        Assert.assertNotNull("test.failure.trace should not be null",tr.getFailureTrace());

        tc = results.getTestClass(ContextTest.class.getName());
        Assert.assertNotNull("Should have found TestClass[ContextTest]",tc);
        Assert.assertEquals("ContextTest.testCount",3,tc.getTestCount());
    }
}

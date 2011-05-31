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
    }
}

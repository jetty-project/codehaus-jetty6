package org.mortbay.jetty.test.validation;

import java.net.URI;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FrameworkTest
{
    private static Server jetty;
    private static URI baseUri;

    @BeforeClass
    public static void initEmbeddedJetty() throws Exception
    {
        jetty = new Server();
        Connector socketConnector = new SocketConnector();
        socketConnector.setPort(0);
        Connector connectors[] = new Connector[1];
        connectors[0] = socketConnector;
        jetty.setConnectors(connectors);

        ServletHandler handler = new ServletHandler();
        jetty.setHandler(handler);

        ServletHolder servletHolder = new ServletHolder(BasicTestSuiteServlet.class);
        handler.addServletWithMapping(servletHolder,"/tests/");

        jetty.start();

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
    public void testSimpleTest() throws Exception
    {
        SimpleRequest request = new SimpleRequest(baseUri);

        String result = request.getString("/tests/");
        System.out.println("Result:\n" + result);
    }
}

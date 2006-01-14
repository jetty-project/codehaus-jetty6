package org.mortbay.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

import junit.framework.TestCase;

/**
 * HttpServer Tester.
 */
public class HttpServerTest
    extends TestCase
{
    private static final String REQUEST = "POST / HTTP/1.0\n"
        + "Host: localhost\n"
        + "Content-Type: text/xml\n"
        + "Content-Length: 181\n"
        + "Connection: close\n"
        + "\n"
        + "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<nimbus xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "        xsi:noNamespaceSchemaLocation=\"nimbus.xsd\" version=\"1.0\">\n"
        + "</nimbus>";

    /** The expected response. */
    private static final String RESPONSE = "HTTP/1.1 200 OK\n"
        + "Connection: close\n"
        + "Server: Jetty(6.0.x)\n"
        + "\n"
        + "Hello world\n";

    // Break the request up into three pieces, splitting the header.
    private static final String FRAGMENT1 = REQUEST.substring(0, 16);
    private static final String FRAGMENT2 = REQUEST.substring(16, 34);
    private static final String FRAGMENT3 = REQUEST.substring(34);

    // Useful constants
    private static final long   ONE_SECOND = 1000L;
    private static final long   TEN_SECONDS = 10000L;
    private static final int    PORT       = 8123;
    private static final String HOST       = "localhost";

    //~ Methods ----------------------------------------------------------------

    /**
     * Feed the server the entire request at once.
     *
     * @throws    Exception
     * @throws    InterruptedException
     */
    public void testRequest()
                     throws Exception, InterruptedException
    {
        // TODO does not work in maven
        /*
        Server       server = startServer();
        Socket       client = new Socket(HOST, PORT);
        OutputStream os     = client.getOutputStream();

        os.write(REQUEST.getBytes());
        os.flush();

        String response = readResponse(client);

        client.close();
        server.stop();

        assertEquals("response", RESPONSE, response);
        */
    }

    /**
     * Feed the server fragmentary headers and see how it copes with it.
     *
     * @throws    Exception
     * @throws    InterruptedException
     */
    public void testRequestFragments()
                              throws Exception, InterruptedException
    {
        // TODO does not work in maven
        /*
        Server server = startServer();
        Socket client = null;

        client = new Socket(HOST, PORT);

        OutputStream os = client.getOutputStream();

        os.write(FRAGMENT1.getBytes());
        os.flush();
        Thread.sleep(ONE_SECOND);
        os.write(FRAGMENT2.getBytes());
        os.flush();
        Thread.sleep(ONE_SECOND);
        os.write(FRAGMENT3.getBytes());
        os.flush();

        String response = readResponse(client);

        Thread.sleep(ONE_SECOND);
        
        client.close();
        server.stop();

        assertEquals("response", RESPONSE, response);
        */
    }

    /**
     * Read entire response from the client. Close the output.
     *
     * @param     client    Open client socket.
     *
     * @return    The response string.
     *
     * @throws    IOException
     */
    private static String readResponse(Socket client)
                                throws IOException
    {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(client
                                                              .getInputStream()));

            StringBuffer sb   = new StringBuffer();
            String        line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

            return sb.toString();
        }
        finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * Create and start the server.
     *
     * @return    Newly created, started server.
     *
     * @throws    Exception
     */
    private static Server startServer()
                               throws Exception
    {
        Server                 server    = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();

        server.setConnectors(new Connector[] {connector});
        server.setHandler(new HelloWorldHandler());
        server.start();

        return server;
    }

    //~ Inner Classes ----------------------------------------------------------
    private static class HelloWorldHandler
        extends AbstractHandler
    {
        //~ Methods ------------------------------------------------------------
        public boolean handle(String target, HttpServletRequest request,
                              HttpServletResponse response, int dispatch)
                       throws IOException, ServletException
        {
            response.getOutputStream().print("Hello world");
            return true;
        }
    }
}

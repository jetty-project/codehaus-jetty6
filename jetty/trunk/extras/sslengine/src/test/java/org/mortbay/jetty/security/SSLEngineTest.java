
// JettyTest.java --
//
// Junit test that shows the Jetty SSL bug.
//

package org.mortbay.jetty.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.security.SslSelectChannelConnector;

/**
 * HttpServer Tester.
 */
public class SSLEngineTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

   private static final String JETTY_VERSION    = "6.1.x";
   private static final String PROTOCOL_VERSION = "2.0";

    /** The request. */
   private static final String REQUEST1_HEADER  = "POST / HTTP/1.0\n"
        + "Host: localhost\n"
        + "Content-Type: text/xml\n"
        + "Connection: close\n"
        + "Content-Length: ";
   private static final String REQUEST1_CONTENT =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<requests xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "        xsi:noNamespaceSchemaLocation=\"commander.xsd\" version=\""
        + PROTOCOL_VERSION + "\">\n"
        + "</requests>";
    private static final String         REQUEST1         = REQUEST1_HEADER
        + REQUEST1_CONTENT.getBytes().length + "\n\n" + REQUEST1_CONTENT;

    /** The expected response. */
    private static final String RESPONSE1 = "HTTP/1.1 200 OK\n"
        + "Connection: close\n"
        + "Server: Jetty(" + JETTY_VERSION + ")\n" + '\n' + "Hello world\n";

    // Useful constants
    private static final String HELLO_WORLD = "Hello world";
    private static final TrustManager[] s_dummyTrustManagers = 
        new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType)
 
                {
                }
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType)
 
                {
                }
            }
        };
    
    //~ Methods ----------------------------------------------------------------

    /**
     * Feed the server the entire request at once.
     *
     * @throws    Exception
     */
    public void testRequest1_jetty_https()
                            throws Exception
    {
        Server                 server    = new Server();
        SslSelectChannelConnector connector = new SslSelectChannelConnector();

        String keystoreDir = System.getProperty("user.dir")+File.separator+".."+File.separator+".."+File.separator+"etc"+File.separator+"keystore";
        
        connector.setPort(8443);
        connector.setKeystore(keystoreDir);
        connector.setPassword("storepwd");
        connector.setKeyPassword("keypwd");

        server.setConnectors(new Connector[] {connector});
        server.setHandler(new HelloWorldHandler());
        server.start();

        final int numConns = 200;
        Socket[] client = new Socket[numConns];

        SSLContext ctx = SSLContext.getInstance("SSLv3");
        ctx.init(null, s_dummyTrustManagers, new java.security.SecureRandom());

        int port = 8443;

        try {
            for (int i = 0; i < numConns; ++i) {
                client[i] = ctx.getSocketFactory().createSocket("localhost", port); 
                OutputStream os = client[i].getOutputStream();
    
                os.write(REQUEST1.getBytes());
            }
            
            for (int i = 0; i < numConns; ++i) {
                OutputStream os = client[i].getOutputStream();
                os.flush();
            }
            
            System.err.println("Flushed the output streams of the requests");
            
            for (int i = 0; i < numConns; ++i) {
                // Read the response.
                System.err.println("Response: "+i);
                String response = readResponse(client[i]);
                System.err.println("Response: "+response);
    
                // Check the response
                assertEquals(String.format("response %d", i), RESPONSE1, 
                    response);
            }
        }
        finally {
            for (int i = 0; i < numConns; ++i) {
                if (client[i] != null) {
                    client[i].close();
                }
            }
            server.stop();
        }
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
            br = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));

            StringBuilder sb   = new StringBuilder(1000);
            String                line;

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

    private static class HelloWorldHandler
        extends AbstractHandler
    {
        //~ Methods ------------------------------------------------------------

        public void handle(String target, HttpServletRequest request,
                           HttpServletResponse response, int dispatch)
                    throws IOException, ServletException
        {
            PrintWriter out = response.getWriter();

            try {
                out.print(HELLO_WORLD);
            }
            finally {
                out.close();
            }
        }
    }
}

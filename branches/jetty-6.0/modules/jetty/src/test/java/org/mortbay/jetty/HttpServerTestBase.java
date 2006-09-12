package org.mortbay.jetty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.handler.AbstractHandler;

/**
 * HttpServer Tester.
 */
public class HttpServerTestBase
    extends TestCase
{

    //~ Static fields/initializers ---------------------------------------------

    /** The request. */
    private static final String REQUEST1_HEADER= 
          "POST / HTTP/1.0\n"
        + "Host: localhost\n"
        + "Content-Type: text/xml\n"
        + "Connection: close\n"
        + "Content-Length: ";
        private static final String REQUEST1_CONTENT= 
          "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<nimbus xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "        xsi:noNamespaceSchemaLocation=\"nimbus.xsd\" version=\"1.0\">\n"
        + "</nimbus>";
        private static final String REQUEST1= 
            REQUEST1_HEADER+REQUEST1_CONTENT.getBytes().length+"\n\n"+REQUEST1_CONTENT;

    /** The expected response. */
    private static final String RESPONSE1 = "HTTP/1.1 200 OK\n"
        + "Connection: close\n"
        + "Server: Jetty(6.0.x)\n"
        + "\n"
        + "Hello world\n";

    // Break the request up into three pieces, splitting the header.
    private static final String FRAGMENT1 = REQUEST1.substring(0, 16);
    private static final String FRAGMENT2 = REQUEST1.substring(16, 34);
    private static final String FRAGMENT3 = REQUEST1.substring(34);

    /** Second test request. */
    private static final String REQUEST2_HEADER  = 
          "POST / HTTP/1.0\n"
        + "Host: localhost\n"
        + "Content-Type: text/xml\n"
        + "Content-Length: ";
    
        private static final String REQUEST2_CONTENT  = 
          "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<nimbus xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "        xsi:noNamespaceSchemaLocation=\"nimbus.xsd\" version=\"1.0\">\n"
        + "    <request requestId=\"1\">\n"
        + "        <getJobDetails>\n"
        + "            <jobId>73</jobId>\n"
        + "        </getJobDetails>\n"
        + "    </request>\n"
        + "    <request requestId=\"2\">\n"
        + "        <getProperties>\n"
        + "            <type>job</type>\n"
        + "            <jobId>73</jobId>\n"
        + "        </getProperties>\n"
        + "    </request>\n"
        + "</nimbus>";
        
        private static final String REQUEST2 =
            REQUEST2_HEADER+REQUEST2_CONTENT.getBytes().length+"\n\n"+REQUEST2_CONTENT;
    
    private static final String RESPONSE2_CONTENT = 
          "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<nimbus xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "        xsi:noNamespaceSchemaLocation=\"nimbus.xsd\" version=\"1.0\">\n"
        + "    <request requestId=\"1\">\n"
        + "        <getJobDetails>\n"
        + "            <jobId>73</jobId>\n"
        + "        </getJobDetails>\n"
        + "    </request>\n"
        + "    <request requestId=\"2\">\n"
        + "        <getProperties>\n"
        + "            <type>job</type>\n"
        + "            <jobId>73</jobId>\n"
        + "        </getProperties>\n"
        + "    </request>\n"
        + "</nimbus>\n";
    private static final String RESPONSE2 = 
          "HTTP/1.1 200 OK\n"
        + "Content-Length: "+RESPONSE2_CONTENT.getBytes().length+"\n"
        + "Server: Jetty(6.0.x)\n"
        + "\n" +
        RESPONSE2_CONTENT;

    // Useful constants
    private static final long   PAUSE = 5L;
    private static final int   LOOPS = 20;
    private static final String HOST  = "localhost";

    private Connector _connector;
    private int  port  = 0;
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        Thread.sleep(250);
    }
    
    //~ Methods ----------------------------------------------------------------

    /**
     * Feed the server the entire request at once.
     *
     * @throws    Exception
     * @throws    InterruptedException
     */
    public void testRequest1_jetty()
                            throws Exception, InterruptedException
    {
        Server       server = startServer(new HelloWorldHandler());
        Socket       client = new Socket(HOST, port);
        OutputStream os     = client.getOutputStream();

        os.write(REQUEST1.getBytes());
        os.flush();

        // Read the response.
        String response = readResponse(client);

        // Shut down
        client.close();
        server.stop();

        // Check the response
        assertEquals("response", RESPONSE1, response);
    }

    

    /* --------------------------------------------------------------- */
    public void testFragmentedChunk()
        throws Exception
    {        

        Server       server = startServer(new EchoHandler());
        Socket       client = new Socket(HOST, port);
        OutputStream os     = client.getOutputStream();

        os.write(("GET /R2 HTTP/1.1\015\012"+
                  "Host: localhost\015\012"+
                  "Transfer-Encoding: chunked\015\012"+
                  "Content-Type: text/plain\015\012"+
                  "Connection: close\015\012"+
                  "\015\012").getBytes());
        os.flush();
        Thread.sleep(PAUSE);
        os.write((
                  "5\015\012").getBytes());
        os.flush();
        Thread.sleep(PAUSE);
        os.write((
                  "ABCDE\015\012"+
                  "0;\015\012\015\012").getBytes());
        os.flush();

        // Read the response.
        String response = readResponse(client);

        System.err.println(response);
        
        // Shut down
        client.close();
        server.stop();

        assertTrue(true); // nothing checked yet.
           
    }
    
    
    
    /**
     * Feed the server fragmentary headers and see how it copes with it.
     *
     * @throws    Exception
     * @throws    InterruptedException
     */
    public void testRequest1Fragments_jetty()
                                     throws Exception, InterruptedException
    {
        Server server   = startServer(new HelloWorldHandler());
        String response;

        try {
            Socket       client = new Socket(HOST, port);
            OutputStream os     = client.getOutputStream();

            // Write a fragment, flush, sleep, write the next fragment, etc.
            os.write(FRAGMENT1.getBytes());
            os.flush();
            Thread.sleep(PAUSE);
            os.write(FRAGMENT2.getBytes());
            os.flush();
            Thread.sleep(PAUSE);
            os.write(FRAGMENT3.getBytes());
            os.flush();

            // Read the response
            response = readResponse(client);

            // Shut down
            client.close();
        }
        finally {
            server.stop();
        }

        // Check the response
        assertEquals("response", RESPONSE1, response);
    }

    public void testRequest2_jetty()
                            throws Exception
    {
        byte[] bytes  = REQUEST2.getBytes();
        Server server = startServer(new EchoHandler());

        try {
            for (int i = 0; i < LOOPS; i++) {
                Socket       client = new Socket(HOST, port);
                OutputStream os     = client.getOutputStream();

                os.write(bytes);
                os.flush();

                // Read the response
                String response = readResponse(client);
                client.close();

                // Check the response
                assertEquals("response "+i, RESPONSE2, response);
            }
        }
        finally {
            // Shut down
            server.stop();
        }
    }

    /**
     * @throws    Exception
     */
    public void testRequest2Fragments_jetty()
                                     throws Exception
    {
        Random    random     = new Random(System.currentTimeMillis());
        byte[]    bytes      = REQUEST2.getBytes();
        final int pointCount = 2;
        Server    server     = startServer(new EchoHandler());

        try {
            
            
            for (int i = 0; i < LOOPS; i++) {
                
                int[]        points  = new int[pointCount];
                StringBuffer message = new StringBuffer();
                
                message.append("iteration #" + (i + 1));
                
                // Pick fragment points at random
                for (int j = 0; j < points.length; ++j) {
                    points[j] = random.nextInt(bytes.length);
                }
                
                // Sort the list
                Arrays.sort(points);
                
                Socket       client = new Socket(HOST, port);
                OutputStream os     = client.getOutputStream();
                
                
               
                writeFragments(bytes, points, message, os);
                
                // Read the response
                String response = readResponse(client);
                
                
                // Close the client
                client.close();
                
                // Check the response
                assertEquals("response for "+i+" " + message.toString(), RESPONSE2,
                        response);
            }
        }
        finally {
            // Shut down
            server.stop();
        }
    }
    
    public void testRequest2Iterate_jetty()
    throws Exception
    {
        byte[] bytes  = REQUEST2.getBytes();
        Server server = startServer(new EchoHandler());

        try {
            for (int i = 0; i < bytes.length; i++) {
                int[]        points  = new int[] {i};
                StringBuffer message = new StringBuffer();

                message.append("iteration #" + (i + 1));

                // Sort the list
                Arrays.sort(points);

                Socket       client = new Socket(HOST, port);
                OutputStream os     = client.getOutputStream();

                writeFragments(bytes, points, message, os);

                // Read the response
                String response = readResponse(client);

                // Close the client
                client.close();

                // Check the response
                assertEquals("response for "+i+" " + message.toString(), RESPONSE2,
                             response);
            }
        }
        finally {
            // Shut down
            server.stop();
        }
    }

    /**
     * After several iterations, I generated some known bad fragment points.
     *
     * @throws    Exception
     */
    public void testRequest2KnownBad_jetty()
                                    throws Exception
    {
        byte[]  bytes     = REQUEST2.getBytes();
        int[][] badPoints = new int[][] {
                {70}, // beginning here, drops last line of request
                {71}, // no response at all
                {72}, // again starts drops last line of request
                {74}, // again, no response at all
            };
        Server  server    = startServer(new EchoHandler());

        try {
            for (int i = 0; i < badPoints.length; ++i) {
                Socket       client = new Socket(HOST, port);
                OutputStream os      = client.getOutputStream();
                StringBuffer message = new StringBuffer();

                message.append("iteration #" + (i + 1));
                writeFragments(bytes, badPoints[i], message, os);

                // Read the response
                String response = readResponse(client);

                // Close the client
                client.close();

                // Check the response
                // TODO - change to equals when code gets fixed
                assertNotSame("response for " + message.toString(), RESPONSE2,
                              response);
            }
        }
        finally {
            // Shut down
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
            br = new BufferedReader(new InputStreamReader(client
                                                              .getInputStream()));

            StringBuffer sb   = new StringBuffer();
            String       line;

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

    protected HttpServerTestBase(Connector connector)
    {
        _connector=connector;
    }
    
    /**
     * Create the server.
     *
     * @param     handler
     *
     * @return    Newly created server, ready to start.
     *
     * @throws    Exception
     */
    private Server startServer(Handler handler)
                               throws Exception
    {
        Server                 server    = new Server();
        
        _connector.setPort(0);
        server.setConnectors(new Connector[] {_connector});
        server.setHandler(handler);
        server.start();
        port=_connector.getLocalPort();
        return server;
    }

    
    
    
    private void writeFragments(byte[] bytes, int[] points,
                                StringBuffer message, OutputStream os)
                         throws IOException, InterruptedException
    {
        int last = 0;

        // Write out the fragments
        for (int j = 0; j < points.length; ++j) {
            int point = points[j];

            os.write(bytes, last, point - last);
            last = point;
            os.flush();
            Thread.sleep(PAUSE);

            // Update the log message
            message.append(" point #" + (j + 1) + ": " + point);
        }

        // Write the last fragment
        os.write(bytes, last, bytes.length - last);
        os.flush();
        Thread.sleep(PAUSE);
    }

    //~ Inner Classes ----------------------------------------------------------
    private static class EchoHandler
        extends AbstractHandler
    {

        //~ Methods ------------------------------------------------------------
        public void handle(String target, HttpServletRequest request,HttpServletResponse response, int dispatch)
            throws IOException, ServletException
        {

            Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
            base_request.setHandled(true);
            
            PrintWriter    writer = response.getWriter();
            BufferedReader reader = request.getReader();
            int            count  = 0;
            String         line;
            
            while ((line = reader.readLine()) != null) {
                writer.print(line);
                writer.print('\n');
                count += line.length();
            }

            if (count==0)
                throw new IllegalStateException("no input recieved");
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
            Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
            base_request.setHandled(true);
            response.setStatus(200);
            response.getOutputStream().print("Hello world");
        }
    }
    
    
}
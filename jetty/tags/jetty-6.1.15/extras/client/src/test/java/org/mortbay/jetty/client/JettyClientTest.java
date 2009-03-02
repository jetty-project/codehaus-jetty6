package org.mortbay.jetty.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.client.CachedExchange;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.mortbay.log.StdErrLog;


public class JettyClientTest extends TestCase {
    private Server server;
    
    private HttpClient client;
    
    private static int _PORT = 0;
    private static String _url;
    
    // Restart the Jetty server
    public void restartServer() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
        
        // Create a Jetty server with a simple servlet that returns immediately
        server = new Server();
        SocketConnector connector = new SocketConnector();
        connector.setHost("127.0.0.1");
       
        if (_PORT == 0)
        {
        	connector.setPort( 0 );
        }
        else
        {
        	connector.setPort( _PORT );
        }
        
        server.addConnector(connector);
        Context context = new Context(server, "", Context.NO_SECURITY | Context.NO_SESSIONS);
        ServletHolder h = new ServletHolder(new HttpServlet() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
                resp.setContentType("text/plain");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("ok\n");
            }

            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
                resp.setContentType("text/plain");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("ok\n");
            }

        });
        context.addServlet(h, "/ping");
        server.start();
        _PORT = connector.getLocalPort();
        _url = String.format("http://localhost:%d/ping", _PORT);
    }
    
    // Restart the Jetty client
    public void restartClient() throws Exception {
        if (client != null) {
            client.stop();
            client = null;
        }
        
        client = new HttpClient();
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        client.setIdleTimeout(60000);
        client.setMaxRetries(0);
        client.setTimeout(60000);
        client.setSoTimeout(60000);
        client.setMaxConnectionsPerAddress(1);
        client.start();
    }

    public static enum Status {
        PENDING,
        SUCCESS,
        ERROR,
        TIMEOUT
    }

    // Simple HTTP exchange
    public static class SimpleExchange extends CachedExchange {
        private Status status = Status.PENDING;
        
        SimpleExchange() {
            super(true);
        }
        
        public void setup(String url) throws IOException {
            setMethod("POST");
            setURL(url);
            byte[] content = "hello".getBytes();
            setRequestHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
            setRequestHeader(HttpHeaders.CONTENT_LENGTH,
                    Integer.toString(content.length));
            setRequestContentSource(new ByteArrayInputStream(content));
        }
        
        @Override
        protected void onResponseComplete() throws IOException {
            super.onResponseComplete();
            synchronized (this) {
                status = Status.SUCCESS;
                notifyAll();
            }
        }
        
        @Override
        protected void onException(Throwable exc) {
            super.onException(exc);
            innerOnException(exc);
        }
        
        @Override
        protected void onConnectionFailed(Throwable exc) {
            super.onConnectionFailed(exc);
            innerOnException(exc);
        }
        
        @Override
        protected void onExpire() {
            super.onExpire();
            synchronized (this) {
                status = Status.TIMEOUT;
                notifyAll();
            }
        }
        
        private void innerOnException(Throwable exc) {
            synchronized (this) {
                status = Status.ERROR;
                notifyAll();
            }
        }
        
        public Status waitForCompletion() throws InterruptedException {
            synchronized (this) {
                while (status == Status.PENDING)
                    wait();
                return status;
            }
        }
    }

    private static final int NUM_BYTES = 4096 * 3;
    private static final int RESET_INTERVAL = 4096;
    // An exchange that restarts the server while flushing data, to simulate
    // the case where the server dies while the client is sending.
    public class InterruptedExchange extends SimpleExchange {
        @Override
        public void setup(String url) {
            setMethod("POST");
            setURL(url);
            final byte[] content = new byte[NUM_BYTES];
            Arrays.fill(content, (byte)0);
            setRequestHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
            setRequestHeader(HttpHeaders.CONTENT_LENGTH,
                    Integer.toString(content.length));
            
            // Restart the server every RESET_INTERVAL bytes
            setRequestContentSource(new InputStream() {
                private int pos = 0;
                private void checkReset() throws IOException {
                    if (pos != 0 && pos < content.length && (pos % RESET_INTERVAL) == 0) {
                        try {
                            restartServer();
                        } catch (Exception e) {
                            throw new IOException(e.toString());  // XXX
                        }
                    }
                }
                
                @Override
                public int read() throws IOException {
                    checkReset();
                    return (pos < content.length ? (int)content[pos++] : -1);
                }
                
                @Override
                public int available() {
                    int nextReset = ((pos / RESET_INTERVAL) + 1) * RESET_INTERVAL;
                    if (nextReset > content.length)
                        nextReset = content.length;
                    return nextReset - pos;
                }
                
                @Override
                public int read(byte[] b, int offset, int length) throws IOException {
                    checkReset();
                    int available = available();
                    if (available == 0)
                        return -1;
                    length = Math.min(length, available);
                    System.arraycopy(content, pos, b, offset, length);
                    pos += length;
                    return length;
                }
            });
        }
        
    }
    
    public Status runExchange(SimpleExchange exchange) throws Exception {
        exchange.setup(_url);
        client.send(exchange);
        return exchange.waitForCompletion();
    }
    
    public void setUp() throws Exception {
        restartServer();
        restartClient();
    }
    
    public void tearDown() throws Exception {
        server.stop();
        server = null;
        client.stop();
        client = null;
    }
    
    public void testSimple() throws Exception {
        assertEquals(Status.SUCCESS, runExchange(new SimpleExchange()));
    }
    /*
    public void testReconnect() throws Exception {

        assertEquals(Status.ERROR, runExchange(new InterruptedExchange()));
        
        // The bug causes the exchange above to be stuck in an infinite loop.
        // This means that the connection won't be reused, and so (as we set
        // max connections per host to 1) that the second exchange (below)
        // won't get to run.  We'll give it some time (500ms should be more than enough),
        // after which we get an InterruptedException which will cause the test to fail.
        final Thread t = Thread.currentThread();
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        
        timer.schedule(new Runnable() {
            public void run() {
                t.interrupt();
            }
        }, 500, TimeUnit.MILLISECONDS);

        Log.warn("EXPECTED  EoFException above");
        assertEquals(Status.SUCCESS, runExchange(new SimpleExchange()));
    }
    */
}

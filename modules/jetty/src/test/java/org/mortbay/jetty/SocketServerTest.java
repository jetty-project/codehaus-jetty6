package org.mortbay.jetty;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.thread.BoundedThreadPool;

/**
 * HttpServer Tester.
 */
public class SocketServerTest extends HttpServerTestBase
{
    public SocketServerTest()
    {
        super(new SocketConnector());
    }   
}
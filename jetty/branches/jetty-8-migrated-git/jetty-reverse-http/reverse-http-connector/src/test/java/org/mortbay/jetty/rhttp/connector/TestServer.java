package org.mortbay.jetty.rhttp.connector;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.embedded.HelloHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.JettyClient;

public class TestServer extends Server
{
    TestServer(String targetId)
    {
        setHandler(new HelloHandler("Hello "+targetId,"Hi from "+targetId));
        
        HttpClient httpClient = new HttpClient();
        RHTTPClient client = new JettyClient(httpClient,"http://localhost:8080/__rhttp",targetId);
        ReverseHTTPConnector connector = new ReverseHTTPConnector(client);
        
        addConnector(connector);
    }
    
    public static void main(String... args) throws Exception
    {
        Log.getLogger("org.mortbay.jetty.rhttp.client").setDebugEnabled(true);
        
        TestServer[] node = new TestServer[] { new TestServer("A"),new TestServer("B"),new TestServer("C") };
        
        for (TestServer s : node)
            s.start();

        for (TestServer s : node)
            s.join();
    }
}

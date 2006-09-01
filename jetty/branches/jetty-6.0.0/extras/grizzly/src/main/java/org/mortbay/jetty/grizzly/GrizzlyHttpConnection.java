package org.mortbay.jetty.grizzly;

import java.io.IOException;

import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Server;

public class GrizzlyHttpConnection extends HttpConnection
{
    public GrizzlyHttpConnection(Connector connector, EndPoint endpoint, Server server)
    {
        super(connector,endpoint,server);
    }

    protected void handleRequest() throws IOException
    {
        System.err.println("Should delegate to ProcessorTask");
        new Thread() 
        { 
            public void run() 
            { 
                
                try
                {
                    GrizzlyHttpConnection.super.handleRequest();
                    
                    handle(); // TODO Make sure the end is seen (Mmmmm dubious)
                }
                catch(Exception e)
                {
                    // TODO - must propogate this back if a Continuation (or must we???)
                    e.printStackTrace();
                }
            } 
        }.start();
    }

}

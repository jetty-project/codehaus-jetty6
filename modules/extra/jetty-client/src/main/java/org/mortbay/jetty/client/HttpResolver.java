package org.mortbay.jetty.client;

import java.io.IOException;

public interface HttpResolver 
{
    public boolean requiresResolution();
    
    public boolean canResolve();
    
    public void attemptResolution( HttpExchange exchange ) throws IOException;
}

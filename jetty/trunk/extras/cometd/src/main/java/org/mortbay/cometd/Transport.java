/**
 * 
 */
package org.mortbay.cometd;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface Transport 
{
    public void setResponse(HttpServletResponse response) throws IOException;
    
    public void send(Map reply) throws IOException;
    public void send(List replies) throws IOException;
    public void complete() throws IOException;
    
    public boolean isPolling();
    public void setPolling(boolean polling);
    public boolean keepAlive() throws IOException;
}
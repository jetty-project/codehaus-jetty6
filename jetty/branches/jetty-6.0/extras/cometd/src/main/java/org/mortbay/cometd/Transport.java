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
    public void preample(HttpServletResponse response) throws IOException;
    public void encode(Map reply) throws IOException;
    public void encode(List replies) throws IOException;
    public void complete() throws IOException;
    public boolean isPolling();
    public void setPolling(boolean polling);
    public boolean keepAlive() throws IOException;
    public void initTunnel(HttpServletResponse response) throws IOException;
}
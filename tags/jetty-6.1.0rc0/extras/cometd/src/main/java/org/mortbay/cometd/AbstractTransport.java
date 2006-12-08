package org.mortbay.cometd;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;


public abstract class AbstractTransport implements Transport
{
    private HttpServletResponse _response;
    private boolean _polling;
    
    public void setResponse(HttpServletResponse response) throws IOException
    {
        _response=response;
    }
    
    public HttpServletResponse getResponse()
    {
        return _response;
    }

    public void preample(HttpServletResponse resp, Map reply) throws IOException
    {
        // TODO REMOVE ME
    }
    
    public boolean isPolling()
    {
        return _polling;
    }

    public void setPolling(boolean polling)
    {
        _polling=polling;
    }
}

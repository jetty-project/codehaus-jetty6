/**
 * 
 */
package org.mortbay.cometd;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.LazyList;

public class PlainTextJSONTransport implements Transport
{
    HttpServletResponse _response;
    Object _responses=null;
    boolean _polling;
    
    public void preample(HttpServletResponse response)
    {
        _response=response;
        _responses=null;
    }
    
    public void encode(Map reply)
    {
        if (reply!=null)
            _responses=LazyList.add(_responses,reply);
    }
    
    public void encode(List replies)
    {
        if (replies!=null)
            _responses=LazyList.addCollection(_responses,replies);
    }
    
    public void complete() throws IOException
    {
        _response.setStatus(200);
        switch (LazyList.size(_responses))
        {
            case 0:
                _response.setContentLength(0);
                break;
                
            case 1:
            {
                _response.setContentType("text/plain; charset=utf-8");
                String s = JSON.toString(LazyList.get(_responses,0));
                System.err.println(s);
                _response.getWriter().write(s);
                break;
            }
                
            default:
            {
                _response.setContentType("text/plain; charset=utf-8");
                String s = JSON.toString(LazyList.getList(_responses));
                System.err.println(s);
                _response.getWriter().write(s); 
                break;   
            }
        }
        _response.getWriter().close();
        _response=null;
    }

    public boolean isPolling()
    {
        return _polling;
    }

    public void setPolling(boolean polling)
    {
        _polling=polling;
    }

    public boolean keepAlive() throws IOException
    {
        return false;
    }

    public void initTunnel(HttpServletResponse response) throws IOException
    {
        // TODO Auto-generated method stub
        
    }
}

/**
 * 
 */
package org.mortbay.cometd;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.LazyList;

public class PlainTextJSONTransport extends AbstractTransport
{
    Object _responses=null;
    
    
    public void send(Map reply)
    {
        if (reply!=null)
            _responses=LazyList.add(_responses,reply);
    }
    
    public void send(List replies)
    {
        if (replies!=null)
            _responses=LazyList.addCollection(_responses,replies);
    }
    
    public void complete() throws IOException
    {
        HttpServletResponse response=getResponse();
        response.setStatus(200);
        switch (LazyList.size(_responses))
        {
            case 0:
                response.setContentLength(0);
                break;
                
            case 1:
            {
                response.setContentType("text/plain; charset=utf-8");
                String s = "["+JSON.toString(LazyList.get(_responses,0))+"]";
                System.err.println("<=1="+s);
                response.getWriter().println(s);
                break;
            }
                
            default:
            {
                response.setContentType("text/plain; charset=utf-8");
                String s = JSON.toString(LazyList.getList(_responses));
                System.err.println("<=*="+s);
                response.getWriter().println(s); 
                break;   
            }
        }
        response.getWriter().close();
        response=null;
    }

    public boolean keepAlive() throws IOException
    {
        return false;
    }
}
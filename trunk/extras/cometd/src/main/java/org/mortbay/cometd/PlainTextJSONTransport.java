/**
 * 
 */
package org.mortbay.cometd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.LazyList;

public class PlainTextJSONTransport extends AbstractTransport
{
    int _responses=0;
    PrintWriter _out;
    boolean _verbose;
    
    public PlainTextJSONTransport()
    {
    }
    
    public void send(Map reply) throws IOException
    {
        if (reply!=null)
        {
            if (_responses==0)
            {
                HttpServletResponse response=getResponse();
                response.setContentType("text/plain; charset=utf-8");
                _out=response.getWriter();
                _out.write('[');
            }
            else
                _out.write(",\r\n");
            
            String r=JSON.toString(reply);
            if (_verbose) System.err.println("<="+_responses+"="+r);
            _responses++;
            _out.write(r);
        }
    }
    
    public void send(List replies) throws IOException
    {
        super.send(replies);
    }
    
    public void complete() throws IOException
    {
        HttpServletResponse response=getResponse();
        response.setStatus(200);
        
        if (_responses==0)
            response.setContentLength(0);
        else
        {
            if (_verbose) System.err.println("<=-=");
            _out.write("]\r\n");
            _out.flush();
        }
        
    }

    public boolean keepAlive() throws IOException
    {
        return false;
    }

    public void setVerbose(boolean verbose)
    {
        _verbose=verbose;
    }
}
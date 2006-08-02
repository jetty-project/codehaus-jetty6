/**
 * 
 */
package org.mortbay.cometd;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.MultiPartWriter;

public class MultiPartTransport implements Transport
{
    MultiPartWriter _writer;
    boolean _polling;
    
    public void preample(HttpServletResponse response) throws IOException
    {
        response.setCharacterEncoding("utf8");
        _writer = new MultiPartWriter(response.getWriter());
        response.setContentType(MultiPartWriter.MULTIPART_X_MIXED_REPLACE+"; boundary="+_writer.getBoundary());
    }
    
    public void encode(Map reply) throws IOException
    {
        if (reply!=null)
        {
            _writer.startPart("text/plain; charset=utf8");
            _writer.write(JSON.toString(reply));
            _writer.flush();
        }
    }
    
    public void encode(List replies) throws IOException
    {
        if (replies!=null)
        {
            for (int i=0;i<replies.size();i++)
            {
                _writer.startPart("text/plain; charset=utf8");
                _writer.write(JSON.toString(replies.get(i)));
            }
            _writer.endPart();
            _writer.flush();
        }
    }
    
    public void complete() throws IOException
    {
        _writer.close();
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
        _writer.startPart("text/plain; charset=utf8");
        _writer.write("{}");
        _writer.flush();
        return false;
    }

    public void initTunnel(HttpServletResponse response) throws IOException
    {
        // TODO Auto-generated method stub
        
    }
}
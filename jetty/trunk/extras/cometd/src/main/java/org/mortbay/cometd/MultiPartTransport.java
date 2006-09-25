/**
 * 
 */
package org.mortbay.cometd;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.MultiPartWriter;

public class MultiPartTransport extends AbstractTransport
{
    MultiPartWriter _writer;
    
    public boolean preample(HttpServletResponse response, Map reply) throws IOException
    {
        response.setCharacterEncoding("utf-8");
        _writer = new MultiPartWriter(response.getWriter());
        response.setContentType(MultiPartWriter.MULTIPART_X_MIXED_REPLACE+"; boundary="+_writer.getBoundary());
        setInitialized(true);
        if (reply!=null)
            encode(reply);
        return reply!=null;
    }
    
    public void encode(Map reply) throws IOException
    {
        if (reply!=null)
        {
            _writer.startPart("text/plain; charset=utf-8");
            _writer.write(JSON.toString(reply));
            _writer.flush();
        }
    }
    
    public void encode(List replies) throws IOException
    {
        if (replies!=null)
        {
            _writer.startPart("text/plain; charset=utf-8");
            _writer.write(JSON.toString(replies));
            _writer.endPart();
            _writer.flush();
        }
    }
    
    public void complete() throws IOException
    {
        _writer.close();
    }

    public boolean keepAlive() throws IOException
    {
        _writer.startPart("text/plain; charset=utf-8");
        _writer.write("{}");
        _writer.flush();
        return false;
    }

    public void initTunnel(HttpServletResponse response) throws IOException
    {
        // TODO Auto-generated method stub
        
    }
}
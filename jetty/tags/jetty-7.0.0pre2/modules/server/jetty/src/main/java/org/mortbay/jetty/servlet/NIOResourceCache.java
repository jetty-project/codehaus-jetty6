/**
 * 
 */
package org.mortbay.jetty.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.mortbay.io.Buffer;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.ResourceCache;
import org.mortbay.jetty.ResourceCache.Content;
import org.mortbay.jetty.nio.NIOConnector;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;

class NIOResourceCache extends ResourceCache
{
    boolean _useFileMappedBuffer;
    
    /* ------------------------------------------------------------ */
    public NIOResourceCache(MimeTypes mimeTypes)
    {
        super(mimeTypes);
    }

    /* ------------------------------------------------------------ */
    protected void fill(Content content) throws IOException
    {
        Buffer buffer=null;
        Resource resource=content.getResource();
        long length=resource.length();

        if (_useFileMappedBuffer && resource.getFile()!=null) 
        {    
            File file = resource.getFile();
            if (file != null) 
                buffer = new NIOBuffer(file);
        } 
        else 
        {
            InputStream is = resource.getInputStream();
            try
            {
                Connector connector = HttpConnection.getCurrentConnection().getConnector();
                buffer = new NIOBuffer((int) length, ((NIOConnector)connector).getUseDirectBuffers()?NIOBuffer.DIRECT:NIOBuffer.INDIRECT);
            }
            catch(OutOfMemoryError e)
            {
                Log.warn(e.toString());
                Log.debug(e);
                buffer = new NIOBuffer((int) length, NIOBuffer.INDIRECT);
            }
            buffer.readFrom(is,(int)length);
            is.close();
        }
        content.setBuffer(buffer);
    }

    public boolean isUseFileMappedBuffer()
    {
        return _useFileMappedBuffer;
    }

    public void setUseFileMappedBuffer(boolean useFileMappedBuffer)
    {
        _useFileMappedBuffer = useFileMappedBuffer;
    }
}
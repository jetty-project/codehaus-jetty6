//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

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
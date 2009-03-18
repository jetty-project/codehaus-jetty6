//========================================================================
//$Id: HttpGenerator.java,v 1.7 2005/11/25 21:17:12 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.http;

import java.io.IOException;

import org.mortbay.jetty.io.Buffer;
import org.mortbay.jetty.io.Buffers;
import org.mortbay.jetty.io.ByteArrayBuffer;
import org.mortbay.jetty.io.EndPoint;
import org.mortbay.jetty.io.EofException;
import org.mortbay.jetty.io.View;
import org.mortbay.jetty.util.log.Log;

/* ------------------------------------------------------------ */
/**
 * Abstract Generator. Builds HTTP Messages.
 * 
 * Currently this class uses a system parameter "jetty.direct.writers" to control
 * two optional writer to byte conversions. buffer.writers=true will probably be 
 * faster, but will consume more memory.   This option is just for testing and tuning.
 * 
 * 
 * 
 */
public abstract class AbstractGenerator implements Generator
{
    // states
    public final static int STATE_HEADER = 0;
    public final static int STATE_CONTENT = 2;
    public final static int STATE_FLUSHING = 3;
    public final static int STATE_END = 4;
    
    public static final byte[] NO_BYTES = {};
    public static final int MAX_OUTPUT_CHARS = 512; 

    // data

    protected final Buffers _buffers; // source of buffers
    protected final EndPoint _endp;
    protected final int _headerBufferSize;
    protected int _contentBufferSize;
    
    protected int _state = STATE_HEADER;
    
    protected int _status = 0;
    protected int _version = HttpVersions.HTTP_1_1_ORDINAL;
    protected  Buffer _reason;
    protected  Buffer _method;
    protected  String _uri;

    protected long _contentWritten = 0;
    protected long _contentLength = HttpTokens.UNKNOWN_CONTENT;
    protected boolean _last = false;
    protected boolean _head = false;
    protected boolean _noContent = false;
    protected boolean _close = false;

    
    protected Buffer _header; // Buffer for HTTP header (and maybe small _content)
    protected Buffer _buffer; // Buffer for copy of passed _content
    protected Buffer _content; // Buffer passed to addContent
    
    private boolean _sendServerVersion;

    
    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     * 
     * @param buffers buffer pool
     * @param headerBufferSize Size of the buffer to allocate for HTTP header
     * @param contentBufferSize Size of the buffer to allocate for HTTP content
     */
    public AbstractGenerator(Buffers buffers, EndPoint io, int headerBufferSize, int contentBufferSize)
    {
        this._buffers = buffers;
        this._endp = io;
        _headerBufferSize=headerBufferSize;
        _contentBufferSize=contentBufferSize;
    }

    /* ------------------------------------------------------------------------------- */
    public boolean isOpen()
    {
        return _endp.isOpen();
    }
    
    /* ------------------------------------------------------------------------------- */
    public void reset(boolean returnBuffers)
    {
        _state = STATE_HEADER;
        _status = 0;
        _version = HttpVersions.HTTP_1_1_ORDINAL;
        _reason = null;
        _last = false;
        _head = false;
        _noContent=false;
        _close = false;
        _contentWritten = 0;
        _contentLength = HttpTokens.UNKNOWN_CONTENT;

        synchronized(this)
        {
            if (returnBuffers)
            {
                if (_header != null) 
                    _buffers.returnBuffer(_header);
                _header = null;
                if (_buffer != null) 
                    _buffers.returnBuffer(_buffer);
                _buffer = null;
            }
            else
            {
                if (_header != null) 
                    _header.clear();

                if (_buffer != null)
                {
                    _buffers.returnBuffer(_buffer);
                    _buffer = null;
                }
            }
        }
        _content = null;
        _method=null;
    }

    /* ------------------------------------------------------------------------------- */
    public void resetBuffer()
    {                   
        if(_state>=STATE_FLUSHING)
            throw new IllegalStateException("Flushed");
        
        _last = false;
        _close = false;
        _contentWritten = 0;
        _contentLength = HttpTokens.UNKNOWN_CONTENT;
        _content=null;
        if (_buffer!=null)
            _buffer.clear();  
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the contentBufferSize.
     */
    public int getContentBufferSize()
    {
        return _contentBufferSize;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param contentBufferSize The contentBufferSize to set.
     */
    public void increaseContentBufferSize(int contentBufferSize)
    {
        if (contentBufferSize > _contentBufferSize)
        {
            _contentBufferSize = contentBufferSize;
            if (_buffer != null)
            {
                Buffer nb = _buffers.getBuffer(_contentBufferSize);
                nb.put(_buffer);
                _buffers.returnBuffer(_buffer);
                _buffer = nb;
            }
        }
    }
    
    /* ------------------------------------------------------------ */    
    public Buffer getUncheckedBuffer()
    {
        return _buffer;
    }
    
    /* ------------------------------------------------------------ */    
    public boolean getSendServerVersion ()
    {
        return _sendServerVersion;
    }
    
    /* ------------------------------------------------------------ */    
    public void setSendServerVersion (boolean sendServerVersion)
    {
        _sendServerVersion = sendServerVersion;
    }
    
    /* ------------------------------------------------------------ */
    public int getState()
    {
        return _state;
    }

    /* ------------------------------------------------------------ */
    public boolean isState(int state)
    {
        return _state == state;
    }

    /* ------------------------------------------------------------ */
    public boolean isComplete()
    {
        return _state == STATE_END;
    }

    /* ------------------------------------------------------------ */
    public boolean isIdle()
    {
        return _state == STATE_HEADER && _method==null && _status==0;
    }

    /* ------------------------------------------------------------ */
    public boolean isCommitted()
    {
        return _state != STATE_HEADER;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the head.
     */
    public boolean isHead()
    {
        return _head;
    }

    /* ------------------------------------------------------------ */
    public void setContentLength(long value)
    {
        if (value<0)
            _contentLength=HttpTokens.UNKNOWN_CONTENT;
        else
            _contentLength=value;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param head The head to set.
     */
    public void setHead(boolean head)
    {
        _head = head;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return <code>false</code> if the connection should be closed after a request has been read,
     * <code>true</code> if it should be used for additional requests.
     */
    public boolean isPersistent()
    {
        return !_close;
    }

    /* ------------------------------------------------------------ */
    public void setPersistent(boolean persistent)
    {
        _close=!persistent;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param version The version of the client the response is being sent to (NB. Not the version
     *            in the response, which is the version of the server).
     */
    public void setVersion(int version)
    {
        if (_state != STATE_HEADER) 
            throw new IllegalStateException("STATE!=START "+_state);
        _version = version;
        if (_version==HttpVersions.HTTP_0_9_ORDINAL && _method!=null)
            _noContent=true;
    }

    /* ------------------------------------------------------------ */
    public int getVersion()
    {
        return _version;
    }
    
    /* ------------------------------------------------------------ */
    /**
     */
    public void setRequest(String method, String uri)
    {
        if (method==null || HttpMethods.GET.equals(method) )
            _method=HttpMethods.GET_BUFFER;
        else
            _method=HttpMethods.CACHE.lookup(method);
        _uri=uri;
        if (_version==HttpVersions.HTTP_0_9_ORDINAL)
            _noContent=true;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param status The status code to send.
     * @param reason the status message to send.
     */
    public void setResponse(int status, String reason)
    {
        if (_state != STATE_HEADER) throw new IllegalStateException("STATE!=START");
        _method=null;
        _status = status;
        if (reason!=null)
        {
            int len=reason.length();
            if (len>_headerBufferSize/2)
                len=_headerBufferSize/2;
            _reason=new ByteArrayBuffer(len);
            for (int i=0;i<len;i++)
            {
                char ch = reason.charAt(i);
                if (ch!='\r'&&ch!='\n')
                    _reason.put((byte)ch);
                else
                    _reason.put((byte)' ');
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** Prepare buffer for unchecked writes.
     * Prepare the generator buffer to receive unchecked writes
     * @return the available space in the buffer.
     * @throws IOException
     */
    public abstract int prepareUncheckedAddContent() throws IOException;

    /* ------------------------------------------------------------ */
    void uncheckedAddContent(int b)
    {
        _buffer.put((byte)b);
    }

    /* ------------------------------------------------------------ */
    public void completeUncheckedAddContent()
    {
        if (_noContent)
        {
            if(_buffer!=null)
                _buffer.clear();
            return;
        }
        else 
        {
            _contentWritten+=_buffer.length();
            if (_head)
                _buffer.clear();
        }
    }
    
    /* ------------------------------------------------------------ */
    public boolean isBufferFull()
    {
        if (_buffer != null && _buffer.space()==0)
        {
            if (_buffer.length()==0 && !_buffer.isImmutable())
                _buffer.compact();
            return _buffer.space()==0;
        }

        return _content!=null && _content.length()>0;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isContentWritten()
    {
        return _contentLength>=0 && _contentWritten>=_contentLength;
    }
    
    /* ------------------------------------------------------------ */
    public abstract void completeHeader(HttpFields fields, boolean allContentAdded) throws IOException;
    
    /* ------------------------------------------------------------ */
    /**
     * Complete the message.
     * 
     * @throws IOException
     */
    public void complete() throws IOException
    {
        if (_state == STATE_HEADER)
        {
            throw new IllegalStateException("State==HEADER");
        }

        if (_contentLength >= 0 && _contentLength != _contentWritten && !_head)
        {
            if (Log.isDebugEnabled())
                Log.debug("ContentLength written=="+_contentWritten+" != contentLength=="+_contentLength);
            _close = true;
        }
    }

    /* ------------------------------------------------------------ */
    public abstract long flushBuffer() throws IOException;

    
    /* ------------------------------------------------------------ */
    public void flush(long maxIdleTime) throws IOException
    {
        // block until everything is flushed
        Buffer content = _content;
        Buffer buffer = _buffer;
        if (content!=null && content.length()>0 || buffer!=null && buffer.length()>0 || isBufferFull())
        {
            flushBuffer();
            
            while ((content!=null && content.length()>0 ||buffer!=null && buffer.length()>0) && _endp.isOpen())
                blockForOutput(maxIdleTime);
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * Utility method to send an error response. If the builder is not committed, this call is
     * equivalent to a setResponse, addcontent and complete call.
     * 
     * @param code
     * @param reason
     * @param content
     * @param close
     * @throws IOException
     */
    public void sendError(int code, String reason, String content, boolean close) throws IOException
    {
        if (!isCommitted())
        {
            setResponse(code, reason);
            _close = close;
            completeHeader(null, false);
            if (content != null) 
                addContent(new View(new ByteArrayBuffer(content)), Generator.LAST);
            complete();
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the contentWritten.
     */
    public long getContentWritten()
    {
        return _contentWritten;
    }
    


    /* ------------------------------------------------------------ */
    public void  blockForOutput(long maxIdleTime) throws IOException
    {
        if (_endp.isBlocking())
        {
            try
            {
                flushBuffer();
            }
            catch(IOException e)
            {
                _endp.close();
                throw e;
            }
        }
        else
        {
            if (!_endp.blockWritable(maxIdleTime))
            {
                _endp.close();
                throw new EofException("timeout");
            }
            
            flushBuffer();
        }
    }
    
}

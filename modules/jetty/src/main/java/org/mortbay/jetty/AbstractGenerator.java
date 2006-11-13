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

package org.mortbay.jetty;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.View;
import org.mortbay.log.Log;
import org.mortbay.util.ByteArrayOutputStream2;
import org.mortbay.util.StringUtil;
import org.mortbay.util.TypeUtil;

/* ------------------------------------------------------------ */
/**
 * Abstract Generator. Builds HTTP Messages.
 * 
 * @author gregw
 * 
 */
public abstract class AbstractGenerator implements Generator
{
    // states
    public final static int STATE_HEADER = 0;
    public final static int STATE_CONTENT = 2;
    public final static int STATE_FLUSHING = 3;
    public final static int STATE_END = 4;

    private static Buffer[] __reasons = new Buffer[505];
    static
    {
        Field[] fields = HttpServletResponse.class.getDeclaredFields();
        for (int i=0;i<fields.length;i++)
        {
            if ((fields[i].getModifiers()&Modifier.STATIC)!=0 &&
                            fields[i].getName().startsWith("SC_"))
            {
                try
                {
                    int code = fields[i].getInt(null);
                    if (code<__reasons.length)
                        __reasons[code]=new ByteArrayBuffer(fields[i].getName().substring(3));
                }
                catch(IllegalAccessException e)
                {}
            }    
        }
    }
    
    protected static Buffer getReasonBuffer(int code)
    {
        Buffer reason=(code<__reasons.length)?__reasons[code]:null;
        return reason==null?null:reason;
    }
    
    public static String getReason(int code)
    {
        Buffer reason=(code<__reasons.length)?__reasons[code]:null;
        return reason==null?TypeUtil.toString(code):reason.toString();
    }

    // data
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

    protected Buffers _buffers; // source of buffers
    protected EndPoint _endp;

    protected int _headerBufferSize;
    protected int _contentBufferSize;
    
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
    public void reset(boolean returnBuffers)
    {
        _state = STATE_HEADER;
        _status = HttpStatus.ORDINAL_200_OK;
        _version = HttpVersions.HTTP_1_1_ORDINAL;
        _last = false;
        _head = false;
        _noContent=false;
        _close = false;
        _contentWritten = 0;
        _contentLength = HttpTokens.UNKNOWN_CONTENT;

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
        if (_state != STATE_HEADER) throw new IllegalStateException("STATE!=START");
        _version = version;
        if (_version==HttpVersions.HTTP_0_9_ORDINAL && _method!=null)
            _noContent=true;
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
                if (Character.isWhitespace(ch))
                    _reason.put((byte)'_');
                else if (Character.isJavaIdentifierPart(ch))
                    _reason.put((byte)ch);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** Prepare buffer for unchecked writes.
     * Prepare the generator buffer to receive unchecked writes
     * @return the available space in the buffer.
     * @throws IOException
     */
    protected abstract int prepareUncheckedAddContent() throws IOException;

    /* ------------------------------------------------------------ */
    void uncheckedAddContent(int b)
    {
        _buffer.put((byte)b);
    }

    /* ------------------------------------------------------------ */
    void completeUncheckedAddContent()
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
        // Should we flush the buffers?
        boolean full = (
             _state == STATE_FLUSHING || 
             (_buffer != null && _buffer.space() == 0));
        return full;
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
    public abstract long flush() throws IOException;
    

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
            // TODO something better than this!
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
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** Output.
     * A {@link ServletOutputStream} implementation that writes content
     * to a {@link AbstractGenerator}.   The class is designed to be reused
     * and can be reopened after a close.
     */
    public static class Output extends ServletOutputStream 
    {
        protected AbstractGenerator _generator;
        protected long _maxIdleTime;
        protected ByteArrayBuffer _buf1 = null;
        protected ByteArrayBuffer _bufn = null;
        protected boolean _closed;
        
        public Output(AbstractGenerator generator, long maxIdleTime)
        {
            _generator=generator;
            _maxIdleTime=maxIdleTime;
        }
        
        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#close()
         */
        public void close() throws IOException
        {
            _closed=true;
        }

        void  blockForOutput() throws IOException
        {
            if (_generator._endp.isBlocking())
            {
                try
                {
                    flush();
                }
                catch(IOException e)
                {
                    _generator._endp.close();
                    throw e;
                }
            }
            else
            {
                if (!_generator._endp.blockWritable(_maxIdleTime))
                {
                    _generator._endp.close();
                    throw new EofException("timeout");
                }
                
                _generator.flush();
            }
        }
        
        /* ------------------------------------------------------------ */
        void reopen()
        {
            _closed=false;
        }
        
        /* ------------------------------------------------------------ */
        public void flush() throws IOException
        {
            // block until everything is flushed
            Buffer content = _generator._content;
            Buffer buffer = _generator._buffer;
            if (content!=null && content.length()>0 ||buffer!=null && buffer.length()>0)
            {
                _generator.flush();
                
                while ((content!=null && content.length()>0 ||buffer!=null && buffer.length()>0) && _generator._endp.isOpen())
                    blockForOutput();
            }
        }

        /* ------------------------------------------------------------ */
        public void write(byte[] b, int off, int len) throws IOException
        {
            if (_bufn == null)
                _bufn = new ByteArrayBuffer(b, off, len);
            else
                _bufn.wrap(b, off, len);
            write(_bufn);
        }

        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#write(byte[])
         */
        public void write(byte[] b) throws IOException
        {
            if (_bufn == null)
                _bufn = new ByteArrayBuffer(b);
            else
                _bufn.wrap(b);
            write(_bufn);
        }

        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b) throws IOException
        {
            if (_closed)
                throw new IOException("Closed");
            
            // Block until we can add _content.
            while (_generator.isBufferFull() && _generator._endp.isOpen())
                blockForOutput();

            // Add the _content
            if (_generator.addContent((byte)b))
            {
                // Buffers are full so flush.
                flush();
            }
        }

        /* ------------------------------------------------------------ */
        private void write(Buffer buffer) throws IOException
        {
            if (_closed)
                throw new IOException("Closed");
            
            // Block until we can add _content.
            while (_generator.isBufferFull() && _generator._endp.isOpen())
                blockForOutput();

            // Add the _content
            _generator.addContent(buffer, Generator.MORE);

            // Have to flush and complete headers?
            if (_generator.isBufferFull())
                flush();

            // Block until our buffer is free
            while (buffer.length() > 0 && _generator._endp.isOpen())
                blockForOutput();
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletOutputStream#print(java.lang.String)
         */
        public void print(String s) throws IOException
        {
            write(s.getBytes());
        }
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** OutputWriter.
     * A writer that can wrap a {@link Output} stream and provide
     * character encodings.
     *
     * The UTF-8 encoding is done by this class and no additional 
     * buffers or Writers are used.
     * The UTF-8 code was inspired by http://javolution.org
     */
    public static class OutputWriter extends Writer
    {
        Output _out;
        AbstractGenerator _generator;
        EndPoint _endp;
        int _maxChar;
        String _characterEncoding;
        Writer _converter;
        ByteArrayOutputStream2 _bytes;
        char[] _chars;
        private int _space;
        int _surrogate;
        int _writeChunk = 1500; // TODO configure or tune
        
        public OutputWriter(Output out)
        {
            _out=out;
            _generator=_out._generator;
            _endp=_generator._endp;
        }

        public void setCharacterEncoding(String encoding)
        {
            if (_characterEncoding==null || !_characterEncoding.equalsIgnoreCase(encoding))
                _converter=null;
            _characterEncoding=encoding;
            if (_characterEncoding==null)
                _maxChar=0x100;
            else if (StringUtil.__ISO_8859_1.equalsIgnoreCase(_characterEncoding))
                _maxChar=0x100;
            else if (StringUtil.__UTF8.equalsIgnoreCase(_characterEncoding))
                _maxChar=0x80;
            else
                _maxChar=0;
        }
        
        public void close() throws IOException
        {
            _out.close();
        }

        public void flush() throws IOException
        {
            _out.flush();
        }
        
        public void write (String s,int offset, int length) throws IOException
        {   
            if (_maxChar==0x80)
            {
                writeUtf8(s,offset,length);
                return;
            }
            
            if (_bytes==null)
                _bytes=new ByteArrayOutputStream2(_writeChunk*2);
            else
                _bytes.reset();
            
            if (_converter==null)
                _converter=_characterEncoding==null?new OutputStreamWriter(_bytes):new OutputStreamWriter(_bytes,_characterEncoding);
            
            if (length>16) // TODO - tune or perhaps remove
            {
                if (_chars==null)
                    _chars=new char[_writeChunk];
                    
                int end=offset+length;
                
                // for each CHAR in the string 
                for (int i=offset;i<end; )
                {
                    // work out the size of a good chunk to convert
                    int chunk=_writeChunk;
                    int next=i+chunk;
                    if (next>end)
                    {
                        next=end;
                        chunk=next-i;
                    }
                    
                    // get a chunk of characters to convert
                    s.getChars(i, next, _chars, 0);
                    i+=chunk;
                    
                    // for each CHAR in the chunk
                    for (int n=0;n<chunk;)
                    {
                        char c=_chars[n];
                        
                        // convert and write
                        if (c<_maxChar) 
                        {
                            _bytes.writeUnchecked(c); 
                            n++;
                        }
                        else
                        {
                            // write a chunk characters to the converter
                            int i0=n++;
                            n+=(_writeChunk+5)/6;
                            if (n>chunk)
                                n=chunk;
                            _converter.write(_chars,i0,n-i0);
                            _converter.flush();
                        }
                    }
                    
                    _out.write(_bytes.getBuf(),0,_bytes.getCount());
                }
            }
            else
            {
                synchronized (_bytes)
                {
                    int end=offset+length;
                    
                    for (int i=offset;i<end; )
                    {
                        int next=i+_writeChunk;
                        if (next>end)
                            next=end;
                        
                        while (i<next)
                        {
                            char c=s.charAt(i);
                            
                            if (c<_maxChar) 
                            {
                                _bytes.writeUnchecked(c);
                                i++;
                            }
                            else
                            {   
                                // write a chunket
                                int i0=i;
                                i+=_writeChunk/2;
                                if (i>next)
                                    i=next;
                                
                                _converter.write(s,i0,i-i0);
                                _converter.flush();
                            }
                        }
                        
                        _out.write(_bytes.getBuf(),0,_bytes.getCount());
                    }
                } 
            }
        }
        

        public void write (char[] s,int offset, int length) throws IOException
        {
            if (_maxChar==0x80)
                writeUtf8(s,offset,length);
            else
            {
                if (_bytes==null)
                    _bytes=new ByteArrayOutputStream2(_writeChunk*2);
                else
                    _bytes.reset();
                
                synchronized (_bytes)
                {
                    int end=offset+length;
                    for (int i=offset;i<end; )
                    {
                        int next=i+_writeChunk;
                        if (next>end)
                            next=end;
                        
                        while (i<next)
                        {
                            char c=s[i];
                            
                            if (c<_maxChar) 
                            {
                                i++;
                                _bytes.writeUnchecked(c);
                            }
                            else 
                            {
                                if (_converter==null)
                                    _converter=new OutputStreamWriter(_bytes,_characterEncoding);
                                
                                // write a chunket
                                int i0=i;
                                i+=_writeChunk/2;
                                if (i>next)
                                    i=next;
                                _converter.write(s,i0,i-i0);
                                _converter.flush();
                            }
                        }
                        
                        byte[] b=_bytes.getBuf();
                        _out.write(b,0,_bytes.getCount());
                    }
                }
            }
        }
        
        public void writeUtf8 (char[] s,int offset, int length) throws IOException
        {
            if (_out._closed)
                throw new IOException("Closed");
            
            _space= _generator.prepareUncheckedAddContent();
            if (_space<0)
                return;
    
            int end=offset+length;
            for (int i=offset;i<end;i++)
            {
                // Block until we can add _content.
                if (_space<6 && _endp.isOpen())
                {
                    _generator.completeUncheckedAddContent();
                    _out.flush();
                    _space= _generator.prepareUncheckedAddContent();
                }
                
                int c=s[i];
                if ((c<0xd800) || (c>0xdfff)) 
                    writeUtf8(c);
                else if (c < 0xdc00) 
                    _surrogate = (c-0xd800)<<10;
                else 
                    writeUtf8(_surrogate+c-0xdc00);
            }
            _generator.completeUncheckedAddContent();
            if (_space==0 && _endp.isOpen())
            {
                _out.flush();
            }
        }
        
        
        public void writeUtf8 (String s,int offset, int length) throws IOException
        {
            if (_out._closed)
                throw new IOException("Closed");
            
            _space= _generator.prepareUncheckedAddContent();
            if (_space<0)
                return;
    
            int end=offset+length;
            for (int i=offset;i<end;i++)
            {
                // Do we need to flush?
                if (_space<6 && _endp.isOpen()) // 6 is maximum UTF-8 encoded character length
                {
                    _generator.completeUncheckedAddContent();
                    _out.flush();
                    _space= _generator.prepareUncheckedAddContent();
                }
                
                int c=s.charAt(i);
                if ((c<0xd800) || (c>0xdfff)) 
                    writeUtf8(c);
                else if (c < 0xdc00) 
                    _surrogate = (c-0xd800)<<10;
                else 
                    writeUtf8(_surrogate+c-0xdc00);
            }
            _generator.completeUncheckedAddContent();
            if (_space==0 && _endp.isOpen())
            {
                _out.flush();
            }
        }
      
        private void writeUtf8(int code)
        {
            if ((code & 0xffffff80) == 0) 
            {
                // 1b
                _generator.uncheckedAddContent(code);
                _space--;
            }
            else if((code&0xfffff800)==0)
            {
                // 2b
                _generator.uncheckedAddContent(0xc0|(code>>6));
                _generator.uncheckedAddContent(0x80|(code&0x3f));
                _space-=2;
            }
            else if((code&0xffff0000)==0)
            {
                // 3b
                _generator.uncheckedAddContent(0xe0|(code>>12));
                _generator.uncheckedAddContent(0x80|((code>>6)&0x3f));
                _generator.uncheckedAddContent(0x80|(code&0x3f));
                _space-=3;
            }
            else if((code&0xff200000)==0)
            {
                // 4b
                _generator.uncheckedAddContent(0xf0|(code>>18));
                _generator.uncheckedAddContent(0x80|((code>>12)&0x3f));
                _generator.uncheckedAddContent(0x80|((code>>6)&0x3f));
                _generator.uncheckedAddContent(0x80|(code&0x3f));
                _space-=4;
            }
            else if((code&0xf4000000)==0)
            {
                 // 5
                _generator.uncheckedAddContent(0xf8|(code>>24));
                _generator.uncheckedAddContent(0x80|((code>>18)&0x3f));
                _generator.uncheckedAddContent(0x80|((code>>12)&0x3f));
                _generator.uncheckedAddContent(0x80|((code>>6)&0x3f));
                _generator.uncheckedAddContent(0x80|(code&0x3f));
                _space-=5;
            }
            else if((code&0x80000000)==0)
            {
                // 6b
                _generator.uncheckedAddContent(0xfc|(code>>30));
                _generator.uncheckedAddContent(0x80|((code>>24)&0x3f));
                _generator.uncheckedAddContent(0x80|((code>>18)&0x3f));
                _generator.uncheckedAddContent(0x80|((code>>12)&0x3f));
                _generator.uncheckedAddContent(0x80|((code>>6)&0x3f));
                _generator.uncheckedAddContent(0x80|(code&0x3f));
                _space-=6;
            }
            else
            {
                _generator.uncheckedAddContent('?');
            }
        }
    }
    
}

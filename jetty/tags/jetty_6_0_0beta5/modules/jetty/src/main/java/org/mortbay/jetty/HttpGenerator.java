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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.io.BufferUtil;
import org.mortbay.io.Buffers;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.Portable;
import org.mortbay.util.TypeUtil;
import org.mortbay.util.UrlEncoded;

/* ------------------------------------------------------------ */
/**
 * HttpGenerator. Builds HTTP Messages.
 * 
 * @author gregw
 * 
 */
public class HttpGenerator implements HttpTokens
{
    // states
    public final static int STATE_HEADER = 0;
    public final static int STATE_CONTENT = 2;
    public final static int STATE_FLUSHING = 3;
    public final static int STATE_END = 4;

    // Last Content
    public final static boolean LAST = true;
    public final static boolean MORE = false;

    // common _content
    private static byte[] LAST_CHUNK =
    { (byte) '0', (byte) '\015', (byte) '\012', (byte) '\015', (byte) '\012'};
    private static byte[] CONTENT_LENGTH_0 = Portable.getBytes("Content-Length: 0\015\012");
    private static byte[] CONNECTION_KEEP_ALIVE = Portable.getBytes("HttpConnection: keep-alive\015\012");
    private static byte[] CONNECTION_CLOSE = Portable.getBytes("HttpConnection: close\015\012");
    private static byte[] TRANSFER_ENCODING_CHUNKED = Portable.getBytes("Transfer-Encoding: chunked\015\012");
    private static byte[] SERVER = Portable.getBytes("Server: Jetty(experimental)\015\012");

    // other statics
    private static int CHUNK_SPACE = 12;
    

    private static String[] __reasons = new String[505];
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
                        __reasons[code]=fields[i].getName().substring(3);
                }
                catch(IllegalAccessException e)
                {}
            }    
        }
    }
    
    public static String getReason(int code)
    {
        if (code<__reasons.length)
            return __reasons[code];
        return TypeUtil.toString(code);
    }

    // data
    private int _state = STATE_HEADER;
    private int _version = HttpVersions.HTTP_1_1_ORDINAL;
    private int _status = HttpStatus.ORDINAL_200_OK;
    private String _reason;

    private long _contentAdded = 0;
    private long _contentLength = UNKNOWN_CONTENT;
    private boolean _last = false;
    private boolean _head = false;
    private boolean _close = false;

    private Buffers _buffers; // source of buffers
    private EndPoint _endp;

    private Buffer _header; // Buffer for HTTP header (and maybe small _content)
    private Buffer _buffer; // Buffer for copy of passed _content
    private Buffer _content; // Buffer passed to addContent
    boolean _direct = false; // True if _content buffer can be written directly to endp
    private boolean _needCRLF = false;
    private boolean _needEOC = false;
    private boolean _bufferChunked = false;
    private int _headerBufferSize;
    private int _contentBufferSize;

    
    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     * 
     * @param buffers buffer pool
     * @param headerBufferSize Size of the buffer to allocate for HTTP header
     * @param contentBufferSize Size of the buffer to allocate for HTTP content
     */
    public HttpGenerator(Buffers buffers, EndPoint io, int headerBufferSize, int contentBufferSize)
    {
        this._buffers = buffers;
        this._endp = io;
        _headerBufferSize = headerBufferSize;
        _contentBufferSize = contentBufferSize;
    }

    /* ------------------------------------------------------------------------------- */
    public void reset(boolean returnBuffers)
    {
        _state = STATE_HEADER;
        _version = HttpVersions.HTTP_1_1_ORDINAL;
        _status = HttpStatus.ORDINAL_200_OK;
        _last = false;
        _head = false;
        _close = false;
        _contentAdded = 0;
        _contentLength = UNKNOWN_CONTENT;

        if (returnBuffers)
        {
            if (_header != null) _buffers.returnBuffer(_header);
            _header = null;
            if (_buffer != null) _buffers.returnBuffer(_buffer);
            _buffer = null;
        }
        else
        {
            if (_header != null) _header.clear();

            if (_buffer != null)
            {
                _buffers.returnBuffer(_buffer);
                _buffer = null;
            }
        }
        _content = null;
        _direct = false;
        _needCRLF = false;
        _needEOC = false;
    }

    /* ------------------------------------------------------------------------------- */
    public void resetBuffer()
    {                   
        if(_state>=STATE_FLUSHING)
            throw new IllegalStateException("Flushed");
        
        _last = false;
        _close = false;
        _contentAdded = 0;
        _contentLength = UNKNOWN_CONTENT;
        _direct=false;
        _content=null;
        if (_buffer!=null)
            _buffer.clear();  
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
     * @return
     */
    public boolean isPersistent()
    {
        return !_close;
    }

    /* ------------------------------------------------------------ */
    public long getContentAdded()
    {
        return _contentAdded;
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
    }

    /* ------------------------------------------------------------ */
    /**
     */
    public void setRequest(Buffer method, Buffer uri)
    {
        Portable.throwNotSupported();
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
        _reason = reason==null?reason:UrlEncoded.encodeString(reason); // encode user supplied reasons!
    }

    /* ------------------------------------------------------------ */
    /**
     * Add _content.
     * 
     * @param _content
     * @return true if the buffers are full
     * @throws IOException
     */
    public void addContent(Buffer content, boolean last) throws IOException
    {
        if (content.isImmutable()) throw new IllegalArgumentException("immutable");

        if (_last || _state==STATE_END) 
            throw new IllegalStateException("Closed");
        _last = last;

        // Handle any unfinished business?
        if (_content != null || _bufferChunked)
        {
            flushBuffers();
            if (_content != null || _bufferChunked) throw new IllegalStateException("FULL");
        }

        _content = content;
        _contentAdded += content.length();

        // Handle the _content
        if (_head)
            content.clear();
        else if (_endp != null && _buffer == null && content.length() > 0 && _last)
        {
            // TODO - use direct in more cases.
            // Make _content a direct buffer
            _direct = true;
        }
        else
        {
            // Yes - so we better check we have a buffer
            if (_buffer == null) _buffer = _buffers.getBuffer(_contentBufferSize);

            // Copy _content to buffer;
            int len=_buffer.put(_content);
            _content.skip(len);
            if (_content.length() == 0) _content = null;
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isBufferFull()
    {
        // Should we flush the buffers?
        boolean full = (_state == STATE_FLUSHING || _direct || (_buffer != null && _buffer.space() == 0) || (_contentLength == CHUNKED_CONTENT && _buffer != null && _buffer.space() < CHUNK_SPACE));
        return full;
    }

    /* ------------------------------------------------------------ */
    public void completeHeader(HttpFields fields, boolean allContentAdded) throws IOException
    {
        if (_state != STATE_HEADER) return;

        if (_last && !allContentAdded) throw new IllegalStateException("last?");
        _last = _last | allContentAdded;

        if (_version == HttpVersions.HTTP_0_9_ORDINAL)
        {
            _close = true;
            _contentLength = EOF_CONTENT;
        }
        else
        {
            boolean has_server = false;
            
            if (_version == HttpVersions.HTTP_1_0_ORDINAL) _close = true;

            // get a header buffer
            if (_header == null) _header = _buffers.getBuffer(_headerBufferSize);

            // add response line
            Buffer line = HttpStatus.getResponseLine(_status);

            // check reason
            if (_reason!=null)
            {
                if (_reason.length()>_header.capacity()/2)
                    _reason=_reason.substring(0,_header.capacity()/2);
            }
            
            if (_reason==null)
                _reason=getReason(_status);
            
            if (line == null)
            {
                _header.put(HttpVersions.HTTP_1_1_BUFFER);
                _header.put((byte) ' ');
                _header.put((byte) ('0' + _status / 100));
                _header.put((byte) ('0' + (_status % 100) / 10));
                _header.put((byte) ('0' + (_status % 10)));
                _header.put((byte) ' ');
                byte[] r = Portable.getBytes(_reason == null ? "Uknown" : _reason);
                _header.put(r, 0, r.length);
                _header.put(CRLF);
            }
            else if (_reason != null)
            {
                _header.put(line.array(), 0, HttpVersions.HTTP_1_1_BUFFER.length() + 5);
                byte[] r = Portable.getBytes(_reason);
                _header.put(r, 0, r.length);
                _header.put(CRLF);
            }
            else
                _header.put(line);

            // Add headers

            // key field values
            HttpFields.Field content_length = null;
            HttpFields.Field transfer_encoding = null;
            HttpFields.Field connection = null;
            boolean keep_alive = false;

            if (fields != null)
            {
                Iterator iter = fields.getFields();

                while (iter.hasNext())
                {
                    HttpFields.Field field = (HttpFields.Field) iter.next();

                    switch (field.getNameOrdinal())
                    {
                        case HttpHeaders.CONTENT_LENGTH_ORDINAL:
                            content_length = field;
                            _contentLength = field.getLongValue();

                            if (_contentLength < _contentAdded || _last && _contentLength != _contentAdded)
                            {
                                // TODO - warn of incorrect _content length
                                content_length = null;
                            }

                            // write the field to the header buffer
                            field.put(_header);
                            break;

                        case HttpHeaders.CONTENT_TYPE_ORDINAL:
                            if (BufferUtil.isPrefix(MimeTypes.MULTIPART_BYTERANGES_BUFFER, field.getValueBuffer())) _contentLength = SELF_DEFINING_CONTENT;

                            // write the field to the header buffer
                            field.put(_header);
                            break;

                        case HttpHeaders.TRANSFER_ENCODING_ORDINAL:
                            if (_version == HttpVersions.HTTP_1_1_ORDINAL) transfer_encoding = field;
                            // Do NOT add yet!
                            break;

                        case HttpHeaders.CONNECTION_ORDINAL:
                            connection = field;

                            int connection_value = field.getValueOrdinal();

                            // TODO handle multivalue HttpConnection
                            _close = HttpHeaderValues.CLOSE_ORDINAL == connection_value;
                            keep_alive = HttpHeaderValues.KEEP_ALIVE_ORDINAL == connection_value;
                            if (keep_alive && _version == HttpVersions.HTTP_1_0_ORDINAL) _close = false;

                            if (_close && _contentLength == UNKNOWN_CONTENT) _contentLength = EOF_CONTENT;

                            // Do NOT add yet!
                            break;

                        case HttpHeaders.SERVER_ORDINAL:
                            has_server=true;
                            field.put(_header);
                            break;
                            
                        default:
                            // write the field to the header buffer
                            field.put(_header);
                    }
                }
            }

            // Calculate how to end _content and connection, _content length and transfer encoding
            // settings.
            // From RFC 2616 4.4:
            // 1. No body for 1xx, 204, 304 & HEAD response
            // 2. Force _content-length?
            // 3. If Transfer-Encoding!=identity && HTTP/1.1 && !HttpConnection==close then chunk
            // 4. Content-Length
            // 5. multipart/byteranges
            // 6. close

            switch ((int) _contentLength)
            {
                case UNKNOWN_CONTENT:
                    // It may be that we have no _content, or perhaps _content just has not been
                    // written yet?

                    // Response known not to have a body
                    if (_contentAdded == 0 && (_status < 200 || _status == 204 || _status == 304))
                        _contentLength = NO_CONTENT;
                    else if (_last)
                    {
                        // we have seen all the _content there is
                        _contentLength = _contentAdded;
                        if (content_length == null)
                        {
                            // known length but not actually set.
                            _header.put(HttpHeaders.CONTENT_LENGTH_BUFFER);
                            _header.put(COLON);
                            _header.put((byte) ' ');
                            BufferUtil.putDecLong(_header, _contentLength);
                            _header.put(CRLF);
                        }
                    }
                    else
                        // No idea, so we must assume that a body is coming
                        _contentLength = (_close || _version < HttpVersions.HTTP_1_1_ORDINAL) ? EOF_CONTENT : CHUNKED_CONTENT;
                    break;

                case NO_CONTENT:
                    if (content_length == null && _status >= 200 && _status != 204 && _status != 304) _header.put(CONTENT_LENGTH_0);
                    break;

                case EOF_CONTENT:
                    _close = true;
                    break;

                case CHUNKED_CONTENT:
                    break;

                default:
                    // TODO - maybe allow forced chunking by setting te ???
                    break;
            }

            // Add transfer_encoding if needed
            if (_contentLength == CHUNKED_CONTENT)
            {
                // try to use user supplied encoding as it may have other values.
                if (transfer_encoding != null && HttpHeaderValues.CHUNKED_ORDINAL != transfer_encoding.getValueOrdinal())
                {
                    // TODO avoid string conversion here
                    String c = transfer_encoding.getValue();
                    if (c.endsWith(HttpHeaderValues.CHUNKED))
                        transfer_encoding.put(_header);
                    else
                        throw new IllegalArgumentException("BAD TE");
                }
                else
                    _header.put(TRANSFER_ENCODING_CHUNKED);

            }

            // Handle connection if need be
            if (_close)
                _header.put(CONNECTION_CLOSE);
            else if (keep_alive && _version == HttpVersions.HTTP_1_0_ORDINAL)
                _header.put(CONNECTION_KEEP_ALIVE);
            else if (connection != null) connection.put(_header);

            if (!has_server)
                _header.put(SERVER);

            // end the header.
            _header.put(CRLF);

        }

        _state = STATE_CONTENT;

    }

    /* ------------------------------------------------------------ */
    /**
     * Complete the message.
     * 
     * @throws IOException
     */
    public void complete() throws IOException
    {
        if (_state == STATE_END) return;

        if (_state == STATE_HEADER)
            throw new IllegalStateException("State==HEADER");

        else if (_contentLength >= 0 && _contentLength != _contentAdded)
        {
            // TODO warning.
            _close = true;
        }

        if (_state != STATE_FLUSHING)
        {
            _state = STATE_FLUSHING;
            if (_contentLength == CHUNKED_CONTENT) _needEOC = true;
        }
        flushBuffers();
    }

    /* ------------------------------------------------------------ */
    public void flushBuffers() throws IOException
    {
        if (_state == STATE_HEADER) throw new IllegalStateException("State==HEADER");

        prepareBuffers();

        if (_endp == null)
        {
            if (_needCRLF && _buffer != null) _buffer.put(CRLF);
            if (_needEOC && _buffer != null) _buffer.put(LAST_CHUNK);
            return;
        }

        // Keep flushing while there is something to flush (except break below)
        int last_len = -1;
        Flushing: while (true)
        {
            int len = -1;
            int to_flush = ((_header != null && _header.length() > 0) ? 4 : 0) | ((_buffer != null && _buffer.length() > 0) ? 2 : 0) | ((_direct && _content != null && _content.length() > 0) ? 1 : 0);

            switch (to_flush)
            {
                case 7:
                    throw new IllegalStateException(); // should never happen!
                case 6:
                    len = _endp.flush(_header, _buffer, null);
                    break;
                case 5:
                    len = _endp.flush(_header, _content, null);
                    break;
                case 4:
                    len = _endp.flush(_header);
                    break;
                case 3:
                    throw new IllegalStateException(); // should never happen!
                case 2:
                    len = _endp.flush(_buffer);
                    break;
                case 1:
                    len = _endp.flush(_content);
                    break;
                case 0:
                {
                    // Nothing more we can write now.
                    if (_header != null) _header.clear();

                    if (_buffer != null)
                    {
                        _buffer.clear();
                        if (_contentLength == CHUNKED_CONTENT)
                        {
                            // reserve some space for the chunk header
                            _buffer.setPutIndex(CHUNK_SPACE);
                            _buffer.setGetIndex(CHUNK_SPACE);
                            _bufferChunked = false;

                            // Special case handling for small left over buffer from
                            // an addContent that caused a buffer flush.
                            if (_content != null && _content.length() < _buffer.space() && _state != STATE_FLUSHING)
                            {
                                _buffer.put(_content);
                                _content = null;
                                break Flushing;
                            }
                        }
                    }

                    _direct = false;

                    // Are we completely finished for now?
                    if (!_needCRLF && !_needEOC && (_content == null || _content.length() == 0))
                    {
                        if (_state == STATE_FLUSHING)
                        {
                            _state = STATE_END;
                            if (_close) _endp.close();
                        }

                        break Flushing;
                    }

                    // Try to prepare more to write.
                    prepareBuffers();
                }
            }

            // If we failed to flush anything twice in a row break
            if (len <= 0)
            {
                if (last_len <= 0) break Flushing;
                // TODO ?? else
                // Thread.yield(); // Give other threads a chance and hopefully we unblock.
                break;
            }
            last_len = len;
        }

    }

    /* ------------------------------------------------------------ */
    private void prepareBuffers() throws IOException
    {
        // if we are not flushing an existing chunk
        if (!_bufferChunked)
        {
            // Refill buffer if possible
            if (_content != null && _content.length() > 0 && _buffer != null && _buffer.space() > 0)
            {
                int len = _buffer.put(_content);
                _content.skip(len);
                if (_content.length() == 0) _content = null;
            }

            // Chunk buffer if need be
            if (_contentLength == CHUNKED_CONTENT)
            {
                int size = _buffer == null ? 0 : _buffer.length();
                if (size > 0)
                {
                    // Prepare a chunk!
                    _bufferChunked = true;

                    // Did we leave space at the start of the buffer.
                    if (_buffer.getIndex() == CHUNK_SPACE)
                    {
                        // Oh yes, goodie! let's use it then!
                        _buffer.poke(_buffer.getIndex() - 2, CRLF, 0, 2);
                        _buffer.setGetIndex(_buffer.getIndex() - 2);
                        BufferUtil.prependHexInt(_buffer, size);

                        if (_needCRLF)
                        {
                            _buffer.poke(_buffer.getIndex() - 2, CRLF, 0, 2);
                            _buffer.setGetIndex(_buffer.getIndex() - 2);
                            _needCRLF = false;
                        }
                    }
                    else
                    {
                        // No space so lets use the header buffer.
                        if (_needCRLF)
                        {
                            if (_header.length() > 0) throw new IllegalStateException("EOC");
                            _header.put(CRLF);
                            _needCRLF = false;
                        }
                        BufferUtil.putHexInt(_header, size);
                        _header.put(CRLF);
                    }

                    // Add end chunk trailer.
                    if (_buffer.space() >= 2)
                        _buffer.put(CRLF);
                    else
                        _needCRLF = true;
                }

                // If we need EOC and everything written
                if (_needEOC && (_content == null || _content.length() == 0))
                {
                    if (_needCRLF)
                    {
                        if (_buffer == null && _header.space() >= 2)
                        {
                            _header.put(CRLF);
                            _needCRLF = false;
                        }
                        else if (_buffer.space() >= 2)
                        {
                            _buffer.put(CRLF);
                            _needCRLF = false;
                        }
                    }

                    if (!_needCRLF && _needEOC)
                    {
                        if (_buffer == null && _header.space() >= LAST_CHUNK.length)
                        {
                            _header.put(LAST_CHUNK);
                            _needEOC = false;
                        }
                        else if (_buffer.space() >= LAST_CHUNK.length)
                        {
                            _buffer.put(LAST_CHUNK);
                            _needEOC = false;
                        }
                    }
                }
            }
        }

        if (_content != null && _content.length() == 0) _content = null;

    }

    /* ------------------------------------------------------------ */
    /**
     * Utility method to send an error response. If the builder is not committed, this call is
     * equivalent to a setResponse, addcontent and complete call.
     * 
     * @param code
     * @param reason
     * @param _content
     * @param close
     * @throws IOException
     */
    public void sendError(int code, String reason, String content, boolean close) throws IOException
    {
        if (!isCommitted())
        {
            setResponse(code, reason);
            _close = close;
            if (content != null) addContent(new ByteArrayBuffer(content), HttpGenerator.LAST);
            complete();
        }
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
}

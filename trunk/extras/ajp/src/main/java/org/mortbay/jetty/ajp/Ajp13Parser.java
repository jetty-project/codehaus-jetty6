//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.ajp;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.servlet.ServletInputStream;

import org.mortbay.io.Buffer;
import org.mortbay.io.BufferUtil;
import org.mortbay.io.Buffers;
import org.mortbay.io.EndPoint;
import org.mortbay.io.View;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpTokens;
import org.mortbay.jetty.Parser;
import org.mortbay.log.Log;

/**
 * @author Markus Kobler
 */
public class Ajp13Parser implements Parser
{
    private final static int STATE_START=-7;
    private final static int STATE_AJP13HEADER_PACKET_LENGTH=-6;
    private final static int STATE_AJP13HEADER_PACKET_TYPE=-5;
    private final static int STATE_AJP13HEADER_REQUEST_ATTR=-4;
    private final static int STATE_AJP13HEADER_REQUEST_ATTR_VALUE=-3;
    private final static int STATE_AJP13HEADER_REQUEST_HEADER_NAME=-2;
    private final static int STATE_AJP13HEADER_REQUEST_METHOD=-1;
    private final static int STATE_END=0;

    private final static int STATE_AJP13CHUNK_START=1;
    private final static int STATE_AJP13CHUNK_LENGTH=2;
    private final static int STATE_AJP13CHUNK_LENGTH2=3;
    private final static int STATE_AJP13CHUNK=4;

    
    private int _state=STATE_START;
    private long _contentLength;
    private long _contentPosition;
    
    private int _chunkLength;
    private int _chunkPosition;
    private int _headers;

    private Buffers _buffers;
    private EndPoint _endp;
    private Buffer _buffer;
    
    private View _contentView=new View();
    private EventHandler _handler;

    private Ajp13RequestPacket _ajpRequestPacket=new Ajp13RequestPacket();
    private Ajp13Generator _generator;

    /* ------------------------------------------------------------------------------- */
    public Ajp13Parser(Buffers buffers, EndPoint endPoint, EventHandler handler, Ajp13Generator generator)
    {
        _buffers=buffers;
        _endp=endPoint;
        _handler=handler;
        _generator=generator;
    }
    
    /* ------------------------------------------------------------------------------- */
    public long getContentLength()
    {
        return _contentLength;
    }

    /* ------------------------------------------------------------------------------- */
    public int getState()
    {
        return _state;
    }

    /* ------------------------------------------------------------------------------- */
    public boolean inContentState()
    {
        return _state > 0;
    }

    /* ------------------------------------------------------------------------------- */
    public boolean inHeaderState()
    {
        return _state < 0;
    }
    
    /* ------------------------------------------------------------------------------- */
    public boolean isComplete()
    {
        return _state==STATE_END;
    }

    /* ------------------------------------------------------------------------------- */
    public boolean isMoreInBuffer()
    {
        return _buffer!=null && _buffer.hasContent();
    }

    /* ------------------------------------------------------------------------------- */
    public boolean isState(int state)
    {
        return _state==state;
    }

    /* ------------------------------------------------------------------------------- */
    public void parse() throws IOException
    {
        if (_state==STATE_END)
            reset(false);
        if (_state!=STATE_START)
            throw new IllegalStateException("!START");

        // continue parsing
        while (_state != STATE_END)
            parseNext();
    }

    /* ------------------------------------------------------------------------------- */
    public long parseAvailable() throws IOException
    {
        long len=parseNext();
        long total=len>0?len:0;

        while (!isComplete() && _buffer!=null && _buffer.length()>0)
        {
            len=parseNext();
            if (len>0)
                total+=len;
        }
        return total;
    }

    /* ------------------------------------------------------------------------------- */
    public long parseNext() throws IOException
    {
        long total_filled=-1;

        if (_buffer==null)
        {
            _buffer=_buffers.getBuffer(Ajp13Packet.MAX_PACKET_SIZE);
            _ajpRequestPacket.setBuffer(_buffer);
        }

        if (_state==STATE_END)
            throw new IllegalStateException("STATE_END");
        if (_state >0 && _contentPosition==_contentLength)
        {
            _state=STATE_END;
            _handler.messageComplete(_contentPosition);
            return total_filled;
        }

        int length=_buffer.length();
        
        // TODO - this will fail if half parsed packet - should use a real mark!
        int mark=_buffer.getIndex();
        int packet_length=length<4?-1:( ((_buffer.peek(mark+2)&0xff)<<8)+_buffer.peek(mark+3)&0xff);

        // Fill buffer if we can
        if (length<4 || length<packet_length+4)
        {
            _buffer.compact();
            if (_generator!=null && _generator.isNeedMore())
                _generator.flush();
            
            int filled=-1;
            
            if (_endp!=null&&filled<=0)
            {
                try
                {
                    if (total_filled<0)
                        total_filled=0;
                    filled=_endp.fill(_buffer);
                    if (filled>0)
                        total_filled+=filled;
                }
                catch (IOException ioe)
                {
                    Log.debug(ioe);
                    reset(true);
                    throw (ioe instanceof EofException)?ioe:new EofException(ioe);
                }
            }

            if (filled<0)
            {
                reset(true);
                throw new EofException();
            }

            length=_buffer.length();
            packet_length=length<4?-1:( ((_buffer.peek(mark+2)&0xff)<<8)+_buffer.peek(mark+3)&0xff);

            if (length<4 || length<packet_length+4)
            {
                if (_state>0)
                    _generator.setNeedMore(true);
                return total_filled;
            }
        }

        _ajpRequestPacket.setBuffer(_buffer);

        // Parse Header
        Buffer bufHeaderName=null;
        Buffer bufHeaderValue=null;
        int attr_type=0;

        while (_state<STATE_END)
        {
            switch (_state)
            {
                case STATE_START:
                    _contentLength=HttpTokens.UNKNOWN_CONTENT;
                     int _magic=_ajpRequestPacket.getInt();
                     if (_magic!=Ajp13RequestHeaders.MAGIC)
                     {
                         throw new IOException("Bad AJP13 rcv packet: "+"0x"+Integer.toHexString(_magic)+" expected "+"0x"
                                 +Integer.toHexString(Ajp13RequestHeaders.MAGIC)+" "+this);
                     }
                     _state=STATE_AJP13HEADER_PACKET_LENGTH;

                case STATE_AJP13HEADER_PACKET_LENGTH:
                    int packetLength=_ajpRequestPacket.getInt();
                    if (packetLength>Ajp13Packet.MAX_PACKET_SIZE)
                        throw new IOException("AJP13 packet ("+packetLength+"bytes) too large for buffer");
                    _state=STATE_AJP13HEADER_PACKET_TYPE;
                    
                case STATE_AJP13HEADER_PACKET_TYPE:
                    byte packetType=_ajpRequestPacket.getByte();
                    switch (packetType)
                    {
                        case Ajp13Packet.FORWARD_REQUEST_ORDINAL:
                            _handler.startForwardRequest();
                            _state=STATE_AJP13HEADER_REQUEST_METHOD;
                            break;
                        case Ajp13Packet.SHUTDOWN_ORDINAL:
                            // Log.warn("AJP13 SHUTDOWN not
                            // supported!");
                            // break;
                        case Ajp13Packet.CPING_REQUEST_ORDINAL:
                            // handler.cpingRequest();
                            // break;
                        default:
                            // XXX Throw an Exception here?? Close
                            // connection!
                            Log.warn("AJP13 message type ({}) not supported/regonised as a "+"container request",Integer.toString(packetType));
                            // throw new IllegalStateException("Not
                            // implemented packet type: " +
                            // packetType);

                    }
                    break;

                    
                case STATE_AJP13HEADER_REQUEST_METHOD:
                    _handler.parsedMethod(_ajpRequestPacket.getMethod());
                    _handler.parsedProtocol(_ajpRequestPacket.getString());
                    _handler.parsedUri(_ajpRequestPacket.getString());
                    _handler.parsedRemoteAddr(_ajpRequestPacket.getString());
                    _handler.parsedRemoteHost(_ajpRequestPacket.getString());
                    _handler.parsedServerName(_ajpRequestPacket.getString());
                    _handler.parsedServerPort(_ajpRequestPacket.getInt());
                    _handler.parsedSslSecure(_ajpRequestPacket.getBool());
                    
                    _headers=_ajpRequestPacket.getInt();
                    _state=_headers==0?STATE_AJP13HEADER_REQUEST_ATTR:STATE_AJP13HEADER_REQUEST_HEADER_NAME;
                    break;

                case STATE_AJP13HEADER_REQUEST_HEADER_NAME:
                    bufHeaderName=_ajpRequestPacket.getHeaderName();
                    bufHeaderValue=_ajpRequestPacket.getString();

                    if (bufHeaderName!=null&&bufHeaderName.toString().equals(Ajp13RequestHeaders.CONTENT_LENGTH))
                    {
                        _contentLength=BufferUtil.toLong(bufHeaderValue);   
                        if (_contentLength <= 0)
                            _contentLength=HttpTokens.NO_CONTENT;
                    }

                    _handler.parsedHeader(bufHeaderName,bufHeaderValue);

                    _state=--_headers==0?STATE_AJP13HEADER_REQUEST_ATTR:STATE_AJP13HEADER_REQUEST_HEADER_NAME;

                    break;
                    
                case STATE_AJP13HEADER_REQUEST_ATTR:
                    attr_type=_ajpRequestPacket.getByte()&0xff;
                    if (attr_type==0xFF)
                    {
                        _contentPosition=0;
                        switch ((int)_contentLength)
                        {
                            case HttpTokens.UNKNOWN_CONTENT:
                            case HttpTokens.NO_CONTENT:
                                _state=STATE_END;
                                _handler.headerComplete();
                                _handler.messageComplete(_contentPosition);
                                break;

                            default:
                                _state=STATE_AJP13CHUNK_START;
                                _handler.headerComplete(); // May recurse here!
                                break;
                        }
                        return total_filled;
                    }
                    
                    _state=STATE_AJP13HEADER_REQUEST_ATTR_VALUE;
                    

                case STATE_AJP13HEADER_REQUEST_ATTR_VALUE:
                    
                    _state=STATE_AJP13HEADER_REQUEST_ATTR;
                    switch (attr_type)
                    {
                        // XXX How does this plug into the web
                        // containers
                        // authentication?
                        case Ajp13RequestHeaders.REMOTE_USER_ATTR:
                        case Ajp13RequestHeaders.AUTH_TYPE_ATTR:
                            break;

                        case Ajp13RequestHeaders.QUERY_STRING_ATTR:
                            _handler.parsedQueryString(_ajpRequestPacket.getString());
                            break;

                        case Ajp13RequestHeaders.JVM_ROUTE_ATTR:
                            // XXX Using old Jetty 5 key,
                            // should change!
                            // Note used in
                            // org.mortbay.jetty.servlet.HashSessionIdManager
                            _handler.parsedRequestAttribute("org.mortbay.http.ajp.JVMRoute",_ajpRequestPacket.getString());
                            break;

                        case Ajp13RequestHeaders.SSL_CERT_ATTR:
                            _handler.parsedRequestAttribute("javax.servlet.request.cipher_suite",_ajpRequestPacket.getString());
                            break;

                        case Ajp13RequestHeaders.SSL_CIPHER_ATTR:
                            // XXX Implement! Investigate
                            // SslSocketConnector.customize()
                            break;

                        case Ajp13RequestHeaders.SSL_SESSION_ATTR:
                            _handler.parsedRequestAttribute("javax.servlet.request.ssl_session",_ajpRequestPacket.getString());
                            break;

                        case Ajp13RequestHeaders.REQUEST_ATTR:
                            _handler.parsedRequestAttribute(_ajpRequestPacket.getString().toString(),_ajpRequestPacket.getString());
                            break;

                            // New Jk API?
                            // Check if experimental or can they
                            // assumed to be
                            // supported
                        case Ajp13RequestHeaders.SSL_KEYSIZE_ATTR:
                            _handler.parsedRequestAttribute("javax.servlet.request.key_size",_ajpRequestPacket.getString());
                            break;

                            // Used to lock down jk requests with a
                            // secreate
                            // key.
                        case Ajp13RequestHeaders.SECRET_ATTR:
                            // XXX Investigate safest way to
                            // deal with
                            // this...
                            // should this tie into shutdown
                            // packet?
                            break;

                        case Ajp13RequestHeaders.STORED_METHOD_ATTR:
                            // XXX Confirm this should
                            // really overide
                            // previously parsed method?
                            // _handler.parsedMethod(Ajp13PacketMethods.CACHE.get(_ajpRequestPacket.getString()));
                            break;

                            // Legacy codes, simply ignore
                        case Ajp13RequestHeaders.CONTEXT_ATTR:
                        case Ajp13RequestHeaders.SERVLET_PATH_ATTR:
                        default:
                            Log.warn("Unsupported Ajp13 Request Attribute {}",new Integer(attr_type));
                        break;
                    }
  
                    break;

                default:
                    throw new IllegalStateException("State not regonised {"+_state+"}");
            }

        } // end of HEADER states loop

        Buffer chunk;

        while (_state>STATE_END)
        {
            switch (_state)
            {
                case STATE_AJP13CHUNK_START:
                    int _magic=_ajpRequestPacket.getInt();
                    if (_magic!=Ajp13RequestHeaders.MAGIC)
                    {
                        throw new IOException("Bad AJP13 rcv packet: "+"0x"+Integer.toHexString(_magic)+" expected "+"0x"
                                +Integer.toHexString(Ajp13RequestHeaders.MAGIC)+" "+this);
                    }
                    _chunkLength=0;
                    _chunkPosition=0;
                    _state=STATE_AJP13CHUNK_LENGTH;

                case STATE_AJP13CHUNK_LENGTH:
                    _chunkLength=_ajpRequestPacket.getInt()-2;
                    _state=STATE_AJP13CHUNK_LENGTH2;
                    
                case STATE_AJP13CHUNK_LENGTH2:
                    _ajpRequestPacket.getInt();
                    if (_chunkLength==0)
                    {
                        _buffer.clear();
                        _state=STATE_END;
                        _handler.messageComplete(_contentPosition);
                        return total_filled;
                    }
                    _state=STATE_AJP13CHUNK;
                
                case STATE_AJP13CHUNK:
                    int remaining=_chunkLength - _chunkPosition;
                    if (remaining==0)
                    {
                        _state=STATE_AJP13CHUNK_START;
                        if (_contentPosition<_contentLength)
                            _generator.setNeedMore(true);
                        return total_filled;
                    }

                    chunk=_ajpRequestPacket.get((int)remaining);
                    _contentPosition+=chunk.length();
                    _chunkPosition += chunk.length();
                    _contentView.update(chunk);
                    
                    remaining=_chunkLength - _chunkPosition;
                    if (remaining==0)
                    {
                        _state=STATE_AJP13CHUNK_START;
                        if (_contentPosition<_contentLength)
                            _generator.setNeedMore(true);
                    }
                    
                    _handler.content(chunk);
                    
                    return total_filled;

                default:
                    throw new IllegalStateException("Invalid Content State");

            }

        }

        return total_filled;
    }

    /* ------------------------------------------------------------------------------- */
    public void reset(boolean returnBuffers)
    {
        _state=STATE_START;
        _contentPosition=0;

        if (_buffer!=null)
        {

            _buffer.setMarkIndex(-1);

            if (!_buffer.hasContent()&&_buffers!=null&&returnBuffers)
            {
                _buffers.returnBuffer(_buffer);
            }
            else
            {
                _buffer.compact();
                _ajpRequestPacket.reset();
            }
        }
    }

    /* ------------------------------------------------------------------------------- */
    Buffer getHeaderBuffer()
    {
        return _buffer;
    }

    /* ------------------------------------------------------------------------------- */
    public interface EventHandler
    {

        // public void shutdownRequest() throws IOException;
        // public void cpingRequest() throws IOException;

        public void content(Buffer ref) throws IOException;
        public void headerComplete() throws IOException;
        public void messageComplete(long contextLength) throws IOException;
        public void parsedHeader(Buffer name, Buffer value) throws IOException;
        public void parsedMethod(Buffer method) throws IOException;
        public void parsedProtocol(Buffer protocol) throws IOException;
        public void parsedQueryString(Buffer value) throws IOException;
        public void parsedRemoteAddr(Buffer addr) throws IOException;
        public void parsedRemoteHost(Buffer host) throws IOException;
        public void parsedRequestAttribute(String key, Buffer value) throws IOException;
        public void parsedServerName(Buffer name) throws IOException;
        public void parsedServerPort(int port) throws IOException;
        public void parsedSslSecure(boolean secure) throws IOException;
        public void parsedUri(Buffer uri) throws IOException;
        public void startForwardRequest() throws IOException;

    }

    /* ------------------------------------------------------------ */
    /** TODO Make this common with HttpParser
     * 
     */
    public static class Input extends ServletInputStream
    {
        private Ajp13Parser _parser;
        private EndPoint _endp;
        private long _maxIdleTime;
        private View _content;

        /* ------------------------------------------------------------ */
        public Input(Ajp13Parser parser, long maxIdleTime)
        {
            _parser=parser;
            _endp=parser._endp;
            _maxIdleTime=maxIdleTime;
            _content=_parser._contentView;
        }


        /* ------------------------------------------------------------ */
        public int read() throws IOException
        {
            int c=-1;
            if (blockForContent())
                c=0xff&_content.get();
            return c;
        }

        /* ------------------------------------------------------------ */
        /*
         * @see java.io.InputStream#read(byte[], int, int)
         */
        public int read(byte[] b, int off, int len) throws IOException
        {
            int l=-1;
            if (blockForContent())
                l=_content.get(b,off,len);
            return l;
        }

        /* ------------------------------------------------------------ */
        private boolean blockForContent() throws IOException
        {
            if (_content.length()>0)
                return true;
            if (_parser.isState(Ajp13Parser.STATE_END))
                return false;

            // Handle simple end points.
            if (_endp==null)
                _parser.parseNext();

            // Handle blocking end points
            else if (_endp.isBlocking())
            {
                long filled=_parser.parseNext();

                // parse until some progress is made (or
                // IOException thrown for timeout)
                while (_content.length()==0&&filled!=0&&!_parser.isState(Ajp13Parser.STATE_END))
                {
                    // Try to get more _parser._content
                    filled=_parser.parseNext();
                }

            }
            // Handle non-blocking end point
            else
            {
                long filled=_parser.parseNext();
                boolean blocked=false;

                // parse until some progress is made (or
                // IOException thrown for timeout)
                while (_content.length()==0&&!_parser.isState(Ajp13Parser.STATE_END))
                {
                    // if fill called, but no bytes read,
                    // then block
                    if (filled>0)
                        blocked=false;
                    else if (filled==0)
                    {
                        if (blocked)
                            throw new InterruptedIOException("timeout");

                        blocked=true;
                        _endp.blockReadable(_maxIdleTime);
                    }

                    // Try to get more _parser._content
                    filled=_parser.parseNext();
                }
            }

            return _content.length()>0;
        }

    }
}

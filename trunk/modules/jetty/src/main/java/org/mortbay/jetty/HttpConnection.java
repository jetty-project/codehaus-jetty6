//========================================================================
//$Id: HttpConnection.java,v 1.13 2005/11/25 21:01:45 gregwilkins Exp $
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
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.EndPoint;
import org.mortbay.log.Log;
import org.mortbay.util.ByteArrayOutputStream2;
import org.mortbay.util.StringUtil;
import org.mortbay.util.URIUtil;
import org.mortbay.util.ajax.Continuation;

/**
 * @author gregw
 * 
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public class HttpConnection
{
    private static int UNKNOWN = -2;
    private static ThreadLocal __currentConnection = new ThreadLocal();

    private Connector _connector;
    private EndPoint _endp;
    private Server _server;
    private boolean _expectingContinues;

    private URI _uri;

    private HttpParser _parser;
    private HttpFields _requestFields;
    private Request _request;
    private Input _in;

    private HttpGenerator _generator;
    private HttpFields _responseFields;
    private Response _response;
    private Output _out;
    private OutputWriter _writer;
    private PrintWriter _printWriter;
    int _writeChunk = 4096; // TODO configure or tune

    int _include;
    
    private Object _associatedObject; // associated object
    
    private transient Buffer _content;
    private transient int _connection = UNKNOWN;
    private transient int _expect = UNKNOWN;
    private transient int _version = UNKNOWN;
    private transient boolean _head = false;
    private transient boolean _host = false;

    public static HttpConnection getCurrentConnection()
    {
        return (HttpConnection) __currentConnection.get();
    }

    /**
     * 
     */
    public HttpConnection(Connector connector, EndPoint endpoint, Server server)
    {
        _connector = connector;
        _endp = endpoint;
        _parser = new HttpParser(_connector, endpoint, new RequestHandler(), _connector.getHeaderBufferSize(), _connector.getRequestBufferSize());
        _requestFields = new HttpFields();
        _responseFields = new HttpFields();
        _request = new Request(this);
        _response = new Response(this);
        _generator = new HttpGenerator(_connector, _endp, _connector.getHeaderBufferSize(), _connector.getResponseBufferSize());
        _server = server;
    }
    

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the associatedObject.
     */
    public Object getAssociatedObject()
    {
        return _associatedObject;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param associatedObject The associatedObject to set.
     */
    public void setAssociatedObject(Object associatedObject)
    {
        _associatedObject = associatedObject;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the connector.
     */
    public Connector getConnector()
    {
        return _connector;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the handler.
     */
    public Handler getHandler()
    {
        return _server;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the requestFields.
     */
    public HttpFields getRequestFields()
    {
        return _requestFields;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the responseFields.
     */
    public HttpFields getResponseFields()
    {
        return _responseFields;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public boolean isConfidential(Request request)
    {
        if (_connector!=null)
            return _connector.isConfidential(request);
        return false;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public EndPoint getEndPoint()
    {
        return _endp;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public boolean useDNS()
    {
        // TODO configure
        return false;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the request.
     */
    public Request getRequest()
    {
        return _request;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the response.
     */
    public Response getResponse()
    {
        return _response;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public ServletInputStream getInputStream()
    {
        if (_in == null) _in = new Input();
        return _in;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public ServletOutputStream getOutputStream()
    {
        if (_out == null) _out = new Output();
        return _out;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public PrintWriter getPrintWriter(String encoding)
    {
        getOutputStream();
        if (_writer==null)
        {
            _writer=new OutputWriter();
            _printWriter=new PrintWriter(_writer)
            {

                /* ------------------------------------------------------------ */
                /* 
                 * @see java.io.PrintWriter#close()
                 */
                public void close() 
                {
                    try
                    {
                        _out.close();
                    }
                    catch(IOException e)
                    {
                        Log.debug(e);
                        setError();
                    }
                }
                
            };
        }
        _writer.setCharacterEncoding(encoding);
        return _printWriter;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isResponseCommitted()
    {
        return _generator.isCommitted();
    }

    /* ------------------------------------------------------------ */
    public void handle() throws IOException
    {
        try
        {
            __currentConnection.set(this);

            Continuation continuation = _request.getContinuation();
            if (continuation != null && continuation.isPending())
            {
                Log.debug("resume continuation {}",continuation);
                doHandler();
                
            }
            else
            {
                // If we are not ended then parse available
                if (!_parser.isState(HttpParser.STATE_END) && (_content == null || _content.length() == 0)) _parser.parseAvailable();

                // Do we have more writting to do?
                if (_generator.isState(HttpGenerator.STATE_FLUSHING) || _generator.isState(HttpGenerator.STATE_CONTENT)) _generator.flushBuffers();
            }
        }
        finally
        {
            __currentConnection.set(null);

            // TODO - maybe do this at start of handle aswell or instead?
            if (_parser.isState(HttpParser.STATE_END) && _generator.isState(HttpGenerator.STATE_END))
            {
                _expectingContinues = false; // TODO do something with this!
                _parser.reset(true); // TODO maybe only release when low on resources
                _requestFields.clear();
                _request.recycle();

                _generator.reset(true); // TODO maybe only release when low on resources
                _responseFields.clear();
                _response.recycle();
                
                // TODO low resources handling?
            }
        }
    }

    /* ------------------------------------------------------------ */
    private void doHandler() throws IOException
    {
        if (_server != null)
        {
            boolean retry = false;
            try
            {
                _request.setRequestURI(_uri.getRawPath());
                _request.setQueryString(_uri.getQuery());
                _request.setPathInfo(URIUtil.canonicalPath(_uri.getPath()));
                if (_out!=null)
                    _out.reopen();
                
                _connector.customize(_endp, _request);
                
                _server.handle(this);
            }
            catch (RetryRequest r)
            {
                Log.ignore(r);
                retry = true;
            }
            catch (ServletException e)
            {
                Log.warn(e);
                _generator.sendError(500, null, null, true);
            }
            finally
            {
                if (!retry)
                {
                    if (_request.getContinuation()!=null && _request.getContinuation().isPending())
                    {
                        Log.debug("continuation still pending {}");
                        _request.getContinuation().suspend(0);
                    }
                    
                    if (_response != null) _response.complete();

                    if (!_generator.isComplete())
                    {
                        _generator.completeHeader(_responseFields, HttpGenerator.LAST);
                        _generator.complete();
                    }
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    public void commitResponse(boolean last) throws IOException
    {
        if (!_generator.isCommitted())
        {
            _generator.setResponse(_response.getStatus(), _response.getReason());
            _generator.completeHeader(_responseFields, last);
        }
        if (last) _generator.complete();
    }

    /* ------------------------------------------------------------ */
    public void completeResponse() throws IOException
    {
        if (!_generator.isCommitted())
        {
            _generator.setResponse(_response.getStatus(), _response.getReason());
            _generator.completeHeader(_responseFields, HttpGenerator.LAST);
        }

        if (!_generator.isComplete())
        {
            _generator.complete();
        }
    }

    /* ------------------------------------------------------------ */
    public void flushResponse() throws IOException
    {
        commitResponse(HttpGenerator.MORE);
        _generator.flushBuffers();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    HttpGenerator getGenerator()
    {
        return _generator;
    }
    

    /* ------------------------------------------------------------ */
    public boolean isIncluding()
    {
        return _include>0;
    }

    /* ------------------------------------------------------------ */
    public void include()
    {
        _include++;
    }

    /* ------------------------------------------------------------ */
    public void included()
    {
        _include--;
        if (_out!=null)
            _out.reopen();
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class RequestHandler extends HttpParser.EventHandler
    {

        /*
         * 
         * @see org.mortbay.jetty.HttpParser.EventHandler#startRequest(org.mortbay.io.Buffer,
         *      org.mortbay.io.Buffer, org.mortbay.io.Buffer)
         */
        public void startRequest(Buffer method, Buffer uri, Buffer version) throws IOException
        {
            _host = false;
            _expect = UNKNOWN;
            _connection = UNKNOWN;

            _request.setTimeStamp(System.currentTimeMillis());
            _request.setMethod(method.toString());

            try
            {
                _uri = new URI(new String(uri.asArray(),"UTF-8"));  // TODO - reduce object creation
                _uri = _uri.normalize();
                _request.setUri(_uri);

                _version = version == null ? HttpVersions.HTTP_0_9_ORDINAL : HttpVersions.CACHE.getOrdinal(version);
                if (_version <= 0) _version = HttpVersions.HTTP_1_0_ORDINAL;
                _request.setProtocol(HttpVersions.CACHE.get(_version).toString());

                _head = HttpMethods.CACHE.getOrdinal(method) == HttpMethods.HEAD_ORDINAL;
            }
            catch (URISyntaxException e)
            {
                _parser.reset(true); 
                // TODO prebuilt response
                _generator.setResponse(400, null);
                _responseFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.CLOSE_BUFFER);
                _generator.complete();
                return;
            }
        }

        /*
         * @see org.mortbay.jetty.HttpParser.EventHandler#parsedHeaderValue(org.mortbay.io.Buffer)
         */
        public void parsedHeader(Buffer name, Buffer value)
        {
            int ho = HttpHeaders.CACHE.getOrdinal(name);
            switch (ho)
            {
                case HttpHeaders.HOST_ORDINAL:
                    // TODO check if host matched a host in the URI.
                    _host = true;
                    break;

                case HttpHeaders.EXPECT_ORDINAL:
                    _expect = HttpHeaderValues.CACHE.getOrdinal(value);
                    break;
                    
                case HttpHeaders.ACCEPT_ENCODING_ORDINAL:
                    value = HttpHeaderValues.CACHE.lookup(value);
                    break;

                case HttpHeaders.CONNECTION_ORDINAL:
                    // TODO coma list of connections ???
                    _connection = HttpHeaderValues.CACHE.getOrdinal(value);
                    if (_connection<0)
                        _responseFields.put(HttpHeaders.CONNECTION_BUFFER, value);
                    else
                    {
                        value=HttpHeaderValues.CACHE.get(_connection);
                        _responseFields.put(HttpHeaders.CONNECTION_BUFFER,value);
                    }
                        
                    // TODO something with this???
            }

            _requestFields.add(name, value);
        }

        /*
         * @see org.mortbay.jetty.HttpParser.EventHandler#headerComplete()
         */
        public void headerComplete() throws IOException
        {
            _generator.setVersion(_version);
            switch (_version)
            {
                case HttpVersions.HTTP_0_9_ORDINAL:
                    break;
                case HttpVersions.HTTP_1_0_ORDINAL:
                    _generator.setHead(_head);
                    break;
                case HttpVersions.HTTP_1_1_ORDINAL:
                    _generator.setHead(_head);
                    if (!_host)
                    {
                        _generator.setResponse(400, null);
                        _responseFields.put(HttpHeaders.CONNECTION_BUFFER, HttpHeaderValues.CLOSE_BUFFER);
                        _generator.complete();
                        return;
                    }

                    if (_expect != UNKNOWN)
                    {
                        if (_expect == HttpHeaderValues.CONTINUE_ORDINAL)
                        {
                            _expectingContinues = true;
                            
                            // TODO delay sending 100 response until a read is attempted.
                            _generator.setResponse(100, null);
                            _generator.complete();
                            _generator.reset(false);
                        }
                        else
                        {
                            _generator.sendError(417, null, null, true);
                            return;
                        }
                    }
                    break;
                default:
            }

            doHandler();
        }

        /* ------------------------------------------------------------ */
        /*
         * @see org.mortbay.jetty.HttpParser.EventHandler#content(int, org.mortbay.io.Buffer)
         */
        public void content(int index, Buffer ref) throws IOException
        {
            if (_content != null) throw new IllegalStateException("content not read");
            _content = ref;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mortbay.jetty.HttpParser.EventHandler#messageComplete(int)
         */
        public void messageComplete(int contextLength) throws IOException
        {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mortbay.jetty.HttpParser.EventHandler#startResponse(org.mortbay.io.Buffer, int,
         *      org.mortbay.io.Buffer)
         */
        public void startResponse(Buffer version, int status, Buffer reason)
        {
            throw new IllegalStateException("response");
        }

    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class Input extends ServletInputStream
    {
        private boolean blockForContent() throws IOException
        {
            while (_content == null || _content.length() == 0)
            {
                if (_parser.isState(HttpParser.STATE_END)) return false;

                // Try to get more _content
                _content = null;
                boolean filled=_parser.parseNext();

                // If unsuccessful and if we are we are not blocking then block
                if (!_parser.isState(HttpParser.STATE_END) && (_content == null || _content.length() == 0) && _endp != null)
                {
                    if(!_endp.isBlocking())
                    {
                        _endp.blockReadable(_connector.getMaxIdleTime()); 
                        _parser.parseNext();
                    }
                    else if (!filled)
                        continue;
                }

                // if still no _content - this is a timeout
                if (!_parser.isState(HttpParser.STATE_END) && (_content == null || _content.length() == 0)) throw new InterruptedIOException("timeout");
            }
            return true;
        }
        
        /* ------------------------------------------------------------ */
        /*
         * @see java.io.InputStream#read()
         */
        public int read() throws IOException
        {
            int c=-1;
            if (blockForContent())
                c= _content.get();
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
                l= _content.get(b, off, len);
            return l;
        }       
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public class Output extends ServletOutputStream 
    {
        ByteArrayBuffer _buf1 = null;
        ByteArrayBuffer _bufn = null;
        boolean _closed;
        
        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#close()
         */
        public void close() throws IOException
        {
            if (_closed)
                return;
            
            flushResponse();
            
            // TODO sometimes can do a last here!
            // commitResponse(HttpGenerator.LAST);
            
            // TODO need IOExceptions for any further writes.
            
            _closed=true;
        }

        /* ------------------------------------------------------------ */
        void reopen()
        {
            _closed=false;
            
        }
        
        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#flush()
         */
        public void flush() throws IOException
        {
            flushResponse();
        }

        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#write(byte[], int, int)
         */
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
            if (_buf1 == null)
                _buf1 = new ByteArrayBuffer(1);
            else
                _buf1.clear();
            _buf1.put((byte) b);
            write(_buf1);
        }

        /* ------------------------------------------------------------ */
        private void write(Buffer buffer) throws IOException
        {
            if (_closed)
                throw new IOException("Closed");
            
            // Block until we can add _content.
            while (_generator.isBufferFull() && !_endp.isClosed() && !_endp.isBlocking())
            {
                _endp.blockWritable(60000); // TODO Configure timeout
                _generator.flushBuffers();
            }

            // Add the _content
            _generator.addContent(buffer, HttpGenerator.MORE);

            // Have to flush and complete headers?
            if (_generator.isBufferFull())
            {
                // Buffers are full so flush.
                commitResponse(HttpGenerator.MORE);
                _generator.flushBuffers();
            }

            // Block until our buffer is free
            while (buffer.length() > 0 && !_endp.isClosed() && !_endp.isBlocking())
            {
                
                _endp.blockWritable(_connector.getMaxIdleTime()); // TODO Configure timeout
                _generator.flushBuffers();
            }
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletOutputStream#print(java.lang.String)
         */
        public void print(String s) throws IOException
        {
            if (_closed)
                throw new IOException("Closed");
            PrintWriter writer=getPrintWriter(null);
            writer.print(s);
        }

        /* ------------------------------------------------------------ */
        public void sendContent(Object content) throws IOException
        {
            if (_closed)
                throw new IOException("Closed");
            
            if (_generator.getContentAdded() > 0) throw new IllegalStateException("!empty");

            if (content instanceof HttpContent)
            {
                HttpContent c = (HttpContent) content;
                if (c.getContentType() != null && !_responseFields.containsKey(HttpHeaders.CONTENT_TYPE_BUFFER)) _responseFields.add(HttpHeaders.CONTENT_TYPE_BUFFER, c.getContentType());
                if (c.getContentLength() > 0) _responseFields.addLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, c.getContentLength());
                if (c.getLastModified() != null) _responseFields.add(HttpHeaders.LAST_MODIFIED_BUFFER, c.getLastModified());
                if (c.getBuffer() != null) _generator.addContent(c.getBuffer(), HttpGenerator.LAST);
                commitResponse(HttpGenerator.LAST);
            }
            else if (content instanceof Buffer)
            {
                _generator.addContent((Buffer) content, HttpGenerator.LAST);
                commitResponse(HttpGenerator.LAST);
            }

            else
                throw new IllegalArgumentException("unknown content type?");
        }
        
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public class OutputWriter extends Writer
    {
        int _maxChar;
        String _characterEncoding;
        Writer _converter;
        ByteArrayOutputStream2 _bytes;


        public void setCharacterEncoding(String encoding)
        {
            if (_characterEncoding==null || !_characterEncoding.equalsIgnoreCase(encoding))
                _converter=null;
            _characterEncoding=encoding;
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
            if (_bytes==null)
            {   
                _bytes=new ByteArrayOutputStream2(_writeChunk*2);
                
                if (_characterEncoding==null)
                    _maxChar=0x80; // TODO maybe 0x0 if we do not know default encoding is 8859
                else if (StringUtil.__ISO_8859_1.equalsIgnoreCase(_characterEncoding))
                    _maxChar=0x100;
                else if (StringUtil.__UTF8.equalsIgnoreCase(_characterEncoding))
                    _maxChar=0x80;
                else
                    _maxChar=0;
            }
            
            synchronized (_bytes)
            {
                int end=offset+length;
                
                for (int i=offset;i<end; )
                {
                    int next=i+_writeChunk;
                    if (next>end)
                        next=end;
                    
                    for (;i<next; i++)
                    {
                        char c=s.charAt(i);
                        
                        if (c<_maxChar) 
                        {
                            _bytes.writeUnchecked(c);
                        }
                        else
                        {
                            if (_converter==null)
                            {
                                _converter=_characterEncoding==null?new OutputStreamWriter(_bytes):new OutputStreamWriter(_bytes,_characterEncoding);
                            }

                            // write a chunket
                            int i0=i++;
                            i+=_writeChunk/2;
                            if (i>end)
                                i=end;
                            
                            _converter.write(s,i0,i-i0);
                            _converter.flush();
                        }
                    }
                    
                    _out.write(_bytes.getBuf(),0,_bytes.getCount());
                    _bytes.reset();
                }
                
            }
        }
        

        public void write (char[] s,int offset, int length) throws IOException
        {
            if (_bytes==null)
            {
                _bytes=new ByteArrayOutputStream2(_writeChunk*2);
                if (_characterEncoding==null)
                    _maxChar=0; // TODO maybe 0x80 if we know default encoding is 8859
                else if (StringUtil.__ISO_8859_1.equalsIgnoreCase(_characterEncoding))
                    _maxChar=0x100;
                else if (StringUtil.__UTF8.equalsIgnoreCase(_characterEncoding))
                    _maxChar=0x80;
                else
                    _maxChar=0;
            }
            
            synchronized (_bytes)
            {
                int end=offset+length;
                for (int i=offset;i<end; )
                {
                    int next=i+_writeChunk;
                    if (next>end)
                        next=end;
                    
                    for (;i<next; i++)
                    {
                        char c=s[i];
                        
                        if (c<_maxChar) 
                            _bytes.writeUnchecked(c);
                        else
                        {
                            if (_converter==null)
                            {
                                _converter=new OutputStreamWriter(_bytes,_characterEncoding);
                            }

                            // write a chunket
                            int i0=i++;
                            i+=_writeChunk/2;
                            if (i>end)
                                i=end;
                            _converter.write(s,i0,i-i0);
                            _converter.flush();
                        }
                    }
                    
                    byte[] b=_bytes.getBuf();
                    _out.write(_bytes.getBuf(),0,_bytes.getCount());
                    _bytes.reset();
                }
            }
        }

    }


}

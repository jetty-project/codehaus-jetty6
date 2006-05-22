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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.log.Log;
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
    private boolean _expectingContinues;  // TODO use this!
    private boolean _idle=true;

    private HttpURI _uri=new HttpURI();

    private HttpParser _parser;
    private HttpFields _requestFields;
    private Request _request;
    private HttpParser.Input _in;

    private HttpGenerator _generator;
    private HttpFields _responseFields;
    private Response _response;
    private Output _out;
    private OutputWriter _writer;
    private PrintWriter _printWriter;

    int _include;
    
    private Object _associatedObject; // associated object
    
    private transient int _connection = UNKNOWN;
    private transient int _expect = UNKNOWN;
    private transient int _version = UNKNOWN;
    private transient boolean _head = false;
    private transient boolean _host = false;

    public static HttpConnection getCurrentConnection()
    {
        return (HttpConnection) __currentConnection.get();
    }

    /* ------------------------------------------------------------ */
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
        _generator.setSendServerVersion(server.getSendServerVersion());
        _server = server;
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        // _endp.close();  // TODO fix loop
        if (_parser!=null)
            _parser.reset(true);
        if (_generator!=null)
            _generator.reset(true);
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
     * @return The result of calling {@link #getConnector}.{@link Connector#isConfidential(Request) isCondidential}(request), or false
     *  if there is no connector.
     */
    public boolean isConfidential(Request request)
    {
        if (_connector!=null)
            return _connector.isConfidential(request);
        return false;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Find out if the request is INTEGRAL security.
     * @param request
     * @return
     */
    public boolean isIntegral(Request request)
    {
        if (_connector!=null)
            return _connector.isIntegral(request);
        return false;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The {@link EndPoint} for this connection.
     */
    public EndPoint getEndPoint()
    {
        return _endp;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return <code>false</code> (this method is not yet implemented)
     */
    public boolean getResolveNames()
    {
        return _connector.getResolveNames();
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
     * @return The input stream for this connection. The stream will be created if it does not already exist.
     */
    public ServletInputStream getInputStream()
    {
        if (_in == null) _in = new HttpParser.Input(_parser,_connector.getMaxIdleTime());
        return _in;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The output stream for this connection. The stream will be created if it does not already exist.
     */
    public ServletOutputStream getOutputStream()
    {
        if (_out == null) _out = new Output();
        return _out;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return A {@link PrintWriter} wrapping the {@link #getOutputStream output stream}. The writer is created if it
     *    does not already exist.
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
        // Loop while more in buffer
        boolean more_in_buffer =true; // assume true until proven otherwise
        while (more_in_buffer)
        {
            _idle=false;
            try
            {
                __currentConnection.set(this);
                
                Continuation continuation = _request.getContinuation();
                if (continuation != null && continuation.isPending())
                {
                    Log.debug("resume continuation {}",continuation);
                    if (_request.getMethod()==null)
                        throw new IllegalStateException();
                    handlerRequest();
                }
                else
                {
                    // If we are not ended then parse available
                    if (!_parser.isState(HttpParser.STATE_END)) 
                        _parser.parseAvailable();
                    
                    // Do we have more writting to do?
                    if (_generator.isState(HttpGenerator.STATE_FLUSHING) || _generator.isState(HttpGenerator.STATE_CONTENT)) 
                        _generator.flushBuffers();
                }
            }
            catch (HttpException e)
            {
                Log.debug(e);
                _generator.sendError(e.getStatus(), e.getReason(), e.toString(), true);
                throw e;
            }
            finally
            {
                __currentConnection.set(null);
                
                Buffer header = _parser.getHeaderBuffer();
                
                if (_parser.isState(HttpParser.STATE_END) && _generator.isState(HttpGenerator.STATE_END))
                {
                    _idle=true;
                    
                    _expectingContinues = false; // TODO do something with this!
                    _parser.reset(!more_in_buffer); // TODO maybe only release when low on resources
                    _requestFields.clear();
                    _request.recycle();
                    
                    _generator.reset(!more_in_buffer); // TODO maybe only release when low on resources
                    _responseFields.clear();
                    _response.recycle();
                }

                more_in_buffer = header!=null && (header.length()>0);
                
                Continuation continuation = _request.getContinuation();
                if (continuation != null && continuation.isPending())
                {
                    break;
                }
            }
        }
        
    }

    /* ------------------------------------------------------------ */
    private void handlerRequest() throws IOException
    {
        if (_server != null)
        {
            boolean retry = false;
            boolean error = false;
            String threadName=null;
            try
            {
                // TODO try to do this lazily or more efficiently
                String info=URIUtil.canonicalPath(_uri.getDecodedPath());
                if (info==null)
                    throw new HttpException(400);
                _request.setPathInfo(info);
                
                if (_out!=null)
                    _out.reopen();
                
                if (Log.isDebugEnabled())
                {
                    threadName=Thread.currentThread().getName();
                    Thread.currentThread().setName(threadName+" - "+_uri);
                }
                
                _connector.customize(_endp, _request);
                
                _server.handle(this);
            }
            catch (RetryRequest r)
            {
                Log.ignore(r);
                retry = true;
            }
            catch (EofException e)
            {
                Log.ignore(e);
                error=true;
            }
            catch (ServletException e)
            {
                Log.warn(e);
                _generator.sendError(500, null, null, true);
            }
            catch (HttpException e)
            {
                Log.debug(e);
                _response.sendError(e.getStatus(), e.getReason());
                throw e;
            }
            finally
            {
                if (threadName!=null)
                    Thread.currentThread().setName(threadName);
                
                if (!retry)
                {
      
                    if (_request.getContinuation()!=null && _request.getContinuation().isPending())
                    {
                        Log.debug("continuation still pending {}");
                        _request.getContinuation().reset();
                    }
                    
                    if (!error) 
                    {
                        if (!_response.isCommitted() && _response.getStatus()<0)
                            _response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        _response.complete();

                        while (!_generator.isComplete() && _endp.isOpen())
                        {
                            _generator.complete();
                            if (!_generator.isComplete() && !_endp.isBlocking())
                            {
                                _endp.blockWritable(_connector.getMaxIdleTime());
                                _generator.complete();
                            }
                        }
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
        if (last) 
            _generator.complete();
    }

    /* ------------------------------------------------------------ */
    public void completeResponse() throws IOException
    {
        if (!_generator.isCommitted())
        {
            _generator.setResponse(_response.getStatus(), _response.getReason());
            _generator.completeHeader(_responseFields, HttpGenerator.LAST);
        }

        _generator.complete();
    }

    /* ------------------------------------------------------------ */
    public void flushResponse() throws IOException
    {
        try
        {
            commitResponse(HttpGenerator.MORE);
            _generator.flushBuffers();
        }
        catch(IOException e)
        {
            throw new EofException(e); // TODO review this?
        }
    }

    /* ------------------------------------------------------------ */
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
    public boolean isIdle()
    {
        return _idle;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class RequestHandler extends HttpParser.EventHandler
    {
        boolean _delayedHandling=false;
        
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
            _delayedHandling=false;

            _request.setTimeStamp(System.currentTimeMillis());
            _request.setMethod(method.toString());

            try
            {
                _uri.parse(uri.array(), uri.getIndex(), uri.length());
                _request.setUri(_uri);
                
                _version = version == null ? HttpVersions.HTTP_0_9_ORDINAL : HttpVersions.CACHE.getOrdinal(version);
                if (_version <= 0) _version = HttpVersions.HTTP_1_0_ORDINAL;
                _request.setProtocol(version.toString());

                _head = method == HttpMethods.HEAD_BUFFER; // depends on method being decached.
            }
            catch (Exception e)
            {
                throw new HttpException(400,null,e);
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
                        _generator.completeHeader(_responseFields, true);
                        _generator.complete();
                        return;
                    }

                    if (_expect != UNKNOWN)
                    {
                        if (_expect == HttpHeaderValues.CONTINUE_ORDINAL)
                        {
                            _expectingContinues = true;
                            
                            // TODO delay sending 100 response until a read is attempted.
                            if (_parser.getHeaderBuffer()==null || _parser.getHeaderBuffer().length()<2)
                            {
                                _generator.setResponse(100, null);
                                _generator.completeHeader(null, true);
                                _generator.complete();
                                _generator.reset(false);
                            }
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

            // Either handle now or wait for first content
            if (_parser.getContentLength()<=0 && !_parser.isChunking())
                handlerRequest();
            else
                _delayedHandling=true;
        }

        /* ------------------------------------------------------------ */
        /*
         * @see org.mortbay.jetty.HttpParser.EventHandler#content(int, org.mortbay.io.Buffer)
         */
        public void content(Buffer ref) throws IOException
        {
            if (_delayedHandling)
            {
                _delayedHandling=false;
                handlerRequest();
            }
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
    public class Output extends HttpGenerator.Output 
    {
        Output()
        {
            super(HttpConnection.this._generator,_connector.getMaxIdleTime());
        }
        
        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#close()
         */
        public void close() throws IOException
        {
            if (_closed)
                return;
            
            if (!isIncluding() && !_generator.isCommitted())
                commitResponse(HttpGenerator.LAST);
            else
                flushResponse();
            
            super.close();
        }

        
        /* ------------------------------------------------------------ */
        /*
         * @see java.io.OutputStream#flush()
         */
        public void flush() throws IOException
        {
            if (!_generator.isCommitted())
                commitResponse(HttpGenerator.MORE);
            super.flush();
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
            
            if (_generator.getContentWritten() > 0) throw new IllegalStateException("!empty");

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
    public class OutputWriter extends HttpGenerator.OutputWriter
    {
        OutputWriter()
        {
            super(HttpConnection.this._out);
        }
    }
}
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
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.io.Connection;
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
public class HttpConnection implements Connection
{
    private static int UNKNOWN = -2;
    private static ThreadLocal __currentConnection = new ThreadLocal();

    private long _timeStamp=System.currentTimeMillis();
    private int _requests;
    
    protected Connector _connector;
    protected EndPoint _endp;
    protected Server _server;
    
    private boolean _expectingContinues;  // TODO use this!

    protected HttpURI _uri=new HttpURI();

    protected Parser _parser;
    protected HttpFields _requestFields;
    protected Request _request;
    protected ServletInputStream _in;

    protected Generator _generator;
    protected HttpFields _responseFields;
    protected Response _response;
    protected Output _out;
    protected OutputWriter _writer;
    protected PrintWriter _printWriter;

    int _include;
    
    private Object _associatedObject; // associated object
    
    private transient int _connection = UNKNOWN;
    private transient int _expect = UNKNOWN;
    private transient int _version = UNKNOWN;
    private transient boolean _head = false;
    private transient boolean _host = false;
    private transient boolean  _delayedHandling=false;

    /* ------------------------------------------------------------ */
    public static HttpConnection getCurrentConnection()
    {
        return (HttpConnection) __currentConnection.get();
    }
    
    /* ------------------------------------------------------------ */
    protected static void setCurrentConnection(HttpConnection connection)
    {
        __currentConnection.set(connection);
    }

    /* ------------------------------------------------------------ */
    /** Constructor
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
    public void destroy()
    {
        if (_parser!=null)
            _parser.reset(true);
        
        if (_generator!=null)
            _generator.reset(true);
        
        if (_requestFields!=null)
            _requestFields.destroy();
        
        if (_responseFields!=null)
            _responseFields.destroy();
        
        _server=null;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the parser used by this connection
     */        
    public Parser getParser()
    {
        return _parser;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the number of requests handled by this connection
     */
    public int getRequests()
    {
        return _requests;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The time this connection was established.
     */
    public long getTimeStamp()
    {
        return _timeStamp;
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
     * @return <code>true</code> if there is a {@link #getConnector() connector} and it considers <code>request</code>
     *         to be {@link Connector#isIntegral(Request) integral}
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
        if (_in == null) _in = new HttpParser.Input(((HttpParser)_parser),_connector.getMaxIdleTime());
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
    public synchronized void handle() throws IOException
    {
        // Loop while more in buffer
        boolean more_in_buffer =true; // assume true until proven otherwise
        int no_progress=0;
        
        while (more_in_buffer && _endp.isOpen())
        {
            try
            {
                setCurrentConnection(this);
                long io=0;
                
                Continuation continuation = _request.getContinuation();
                if (continuation != null && continuation.isPending())
                {
                    Log.debug("resume continuation {}",continuation);
                    if (_request.getMethod()==null)
                        throw new IllegalStateException();
                    handleRequest();
                }
                else
                {
                    // If we are not ended then parse available
                    if (!_parser.isComplete()) 
                        io=_parser.parseAvailable();
                    
                    // Do we have more writting to do?
                    if (_generator.isCommitted() && !_generator.isComplete()) 
                        io+=_generator.flush();
                    
                    if (_endp.isBufferingOutput())
                    {
                        _endp.flush();
                        if (_endp.isBufferingOutput())
                            no_progress=0;
                    }
                    
                    if (io>0)
                        no_progress=0;
                    else if (no_progress++>=2) 
                        return;
                }
            }
            catch (HttpException e)
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug("uri="+_uri);
                    Log.debug("fields="+_requestFields);
                    Log.debug(e);
                }
                _generator.sendError(e.getStatus(), e.getReason(), null, true);
                // TODO.  Need to consider how to really flush this for non-blocking
                
                _parser.reset(true);
                _endp.close();
                throw e;
            }
            finally
            {
                setCurrentConnection(null);
                
                more_in_buffer = _parser.isMoreInBuffer() || _endp.isBufferingInput();  
                
                if (_parser.isComplete() && _generator.isComplete() && !_endp.isBufferingOutput())
                {  
                    if (!_generator.isPersistent())
                    {
                        _parser.reset(true);
                        more_in_buffer=false;
                    }
                    
                    _expectingContinues = false; // TODO do something with this!
                    
                    reset(!more_in_buffer);
                }
                
                Continuation continuation = _request.getContinuation();
                if (continuation != null && continuation.isPending())
                {
                    break;
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    protected void reset(boolean returnBuffers)
    {
        _parser.reset(returnBuffers); // TODO maybe only release when low on resources
        _requestFields.clear();
        _request.recycle();
        
        _generator.reset(returnBuffers); // TODO maybe only release when low on resources
        _responseFields.clear();
        _response.recycle();
        
        _uri.clear();   
    }
    
    /* ------------------------------------------------------------ */
    protected void handleRequest() throws IOException
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
            catch (HttpException e)
            {
                Log.debug(e);
                _request.setHandled(true);
                _response.sendError(e.getStatus(), e.getReason());
                error=true;
            }
            catch (Exception e)
            {
                Log.warn(e);
                _request.setHandled(true);
                _generator.sendError(500, null, null, true);
                error=true;
            }
            catch (Error e)
            {
                Log.warn(e);
                _request.setHandled(true);
                _generator.sendError(500, null, null, true);
                error=true;
            }
            finally
            {   
                if (threadName!=null)
                    Thread.currentThread().setName(threadName);
                
                if (!retry)
                {
                    _requests++;
                    
                    if (_request.getContinuation()!=null && _request.getContinuation().isPending())
                    {
                        Log.debug("continuation still pending {}");
                        _request.getContinuation().reset();
                    }

                    if(_endp.isOpen())
                    {
                        if (error) 
                            _endp.close();
                        else
                        {
                            if (!_response.isCommitted() && !_request.isHandled())
                                _response.sendError(HttpServletResponse.SC_NOT_FOUND);
                            _response.complete();
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
            _generator.flush();
        }
        catch(IOException e)
        {
            throw (e instanceof EofException) ? e:new EofException(e);
        }
    }

    /* ------------------------------------------------------------ */
    public Generator getGenerator()
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
        return _generator.isIdle() && _parser.isIdle() || _delayedHandling;
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
            _delayedHandling=false;

            _request.setTimeStamp(System.currentTimeMillis());
            _request.setMethod(method.toString());

            try
            {
                _uri.parse(uri.array(), uri.getIndex(), uri.length());
                _request.setUri(_uri);

                if (version==null)
                {
                    _request.setProtocol(HttpVersions.HTTP_0_9);
                    _version=HttpVersions.HTTP_0_9_ORDINAL;
                }
                else
                {
                    version= HttpVersions.CACHE.get(version);
                    _version = HttpVersions.CACHE.getOrdinal(version);
                    if (_version <= 0) _version = HttpVersions.HTTP_1_0_ORDINAL;
                    _request.setProtocol(version.toString());
                }

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
                            if (((HttpParser)_parser).getHeaderBuffer()==null || ((HttpParser)_parser).getHeaderBuffer().length()<2)
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
            if (((HttpParser)_parser).getContentLength()<=0 && !((HttpParser)_parser).isChunking())
                handleRequest();
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
                handleRequest();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mortbay.jetty.HttpParser.EventHandler#messageComplete(int)
         */
        public void messageComplete(long contextLength) throws IOException
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
    public class Output extends AbstractGenerator.Output 
    {
        Output()
        {
            super((AbstractGenerator)HttpConnection.this._generator,_connector.getMaxIdleTime());
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
                if (c.getContentType() != null && !_responseFields.containsKey(HttpHeaders.CONTENT_TYPE_BUFFER)) 
                    _responseFields.add(HttpHeaders.CONTENT_TYPE_BUFFER, c.getContentType());
                if (c.getContentLength() > 0) 
                    _responseFields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, c.getContentLength());
                Buffer lm = c.getLastModified();
                if (lm != null) 
                    _responseFields.put(HttpHeaders.LAST_MODIFIED_BUFFER, lm);
                else if (c.getResource()!=null)
                {
                    long lml=c.getResource().lastModified();
                    if (lml!=-1)
                        _responseFields.putDateField(HttpHeaders.LAST_MODIFIED_BUFFER, lml);
                }
                    
                
                content = c.getBuffer();
                if (content==null)
                    content=c.getInputStream();
            }
            
            
            if (content instanceof Buffer)
            {
                _generator.addContent((Buffer) content, HttpGenerator.LAST);
                commitResponse(HttpGenerator.LAST);
            }
            else if (content instanceof InputStream)
            {
                InputStream in = (InputStream)content;
                
                int max = _generator.prepareUncheckedAddContent();
                Buffer buffer = _generator.getUncheckedBuffer();
                
                int len=buffer.readFrom(in,max);
                
                while (len>=0)
                {
                    _generator.completeUncheckedAddContent();
                    _out.flush();
                    
                    max = _generator.prepareUncheckedAddContent();
                    buffer = _generator.getUncheckedBuffer();
                    len=buffer.readFrom(in,max);
                }
                _generator.completeUncheckedAddContent();
                _out.flush();        
            }
            else
                throw new IllegalArgumentException("unknown content type?");
        }     
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public class OutputWriter extends AbstractGenerator.OutputWriter
    {
        OutputWriter()
        {
            super(HttpConnection.this._out);
        }
    }


}

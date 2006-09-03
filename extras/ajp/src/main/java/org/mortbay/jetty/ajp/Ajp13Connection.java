package org.mortbay.jetty.ajp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpStatus;
import org.mortbay.log.Log;
import org.mortbay.util.URIUtil;
import org.mortbay.util.ajax.Continuation;

/**
 * Connection implementation of the Ajp13 protocol. <p/> XXX Refactor to remove
 * duplication of HttpConnection
 * 
 * @author Markus Kobler
 */
public class Ajp13Connection extends HttpConnection
{

    private int _requests;

    private Ajp13HttpURI _uri=new Ajp13HttpURI();

    private Connector _connector;
    private EndPoint _endp;
    private Server _server;
    private int _bufferSize;

    private Ajp13Parser _parser;
    private HttpFields _requestFields;
    private Request _request;
    private Ajp13Generator _generator;
    private HttpFields _responseFields;
    private Response _response;

    private ServletInputStream _in;
    private ServletOutputStream _out;

    private boolean _idle=true;

    private boolean _persistent=true;

    private boolean _sslSecure=false;

    public Ajp13Connection(Connector connector, EndPoint endPoint, Server server, int bufferSize)
    {
        super(connector,endPoint,server);
        _connector=connector;
        _bufferSize=bufferSize;
        _endp=endPoint;
        _parser=new Ajp13Parser(_connector,_endp,new RequestHandler(),_bufferSize);
        _requestFields=super.getRequestFields();
        _request=super.getRequest();
        _generator=new Ajp13Generator(_connector,_endp,_bufferSize);
        _responseFields=super.getResponseFields();
        _response=super.getResponse();
        _server=server;
    }

    public boolean isConfidential(Request request)
    {
        return _sslSecure;
    }

    public boolean isIntegral(Request request)
    {
        return _sslSecure;
    }

    public int getRequests()
    {
        return _requests;
    }

    public boolean isIdle()
    {
        return _idle;
    }

    public ServletInputStream getInputStream()
    {
        if (_in==null)
            _in=new Ajp13Parser.Input(_parser,_connector.getMaxIdleTime());
        return _in;
    }

    // XXX Implement
    public ServletOutputStream getOutputStream()
    {
        throw new UnsupportedOperationException();
    }

    // XXX Implement
    public PrintWriter getPrintWriter(String encoding)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isResponseCommitted()
    {
        return _generator.isCommited();
    }

    // Largly based on HttpConnection.handle()
    public void handle() throws IOException
    {

        // Loop while more in buffer
        boolean more_in_buffer=true; // assume true until proven otherwise
        int no_progress=0;

        while (more_in_buffer&&_endp.isOpen())
        {

            _idle=false;

            try
            {

                setCurrentConnection(this);
                long io=0;

                Continuation continuation=_request.getContinuation();
                if (continuation!=null&&continuation.isPending())
                {

                    Log.debug("resume continuation {}",continuation);
                    if (_request.getMethod()==null)
                        throw new IllegalStateException();
                    handleRequest();

                }
                else
                {

                    if (!_parser.inEndState())
                        io=_parser.parseAvailable();

                    if (io>0)
                        no_progress=0;
                    else if (no_progress++>10) // TODO This is a bit arbitrary
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
                _generator.sendError(e.getStatus(),e.getReason(),null,true);
                _parser.skipBuffer();
                _persistent=false;
                throw e;
            }
            finally
            {

                setCurrentConnection(null);

                more_in_buffer=_parser.isMoreInBuffer();

                if (_parser.inEndState()&&_generator.isComplete())
                {
                    _idle=true;

                    // XXX is relivant any more?
                    // if( !_generator.isPersistent() )
                    // _parser.skipBuffer();

                    reset(!more_in_buffer);

                    _persistent=false;
                }

                if (!_persistent)
                    _endp.close();

                Continuation continuation=_request.getContinuation();
                if (continuation!=null&&continuation.isPending())
                {
                    break;
                }
            }
        }
    }

    // XXX Copy and paste of Super Class
    protected void handleRequest() throws IOException
    {
        if (_server!=null)
        {
            boolean retry=false;
            boolean error=false;
            String threadName=null;
            try
            {
                // TODO try to do this lazily or more efficiently
                String info=URIUtil.canonicalPath(_uri.getDecodedPath());
                if (info==null)
                    throw new HttpException(400);
                _request.setPathInfo(info);

                // if (_out != null)
                // _out.reopen();

                if (Log.isDebugEnabled())
                {
                    threadName=Thread.currentThread().getName();
                    Thread.currentThread().setName(threadName+" - "+_uri);
                }

                _connector.customize(_endp,_request);

                _server.handle(this);
            }
            catch (RetryRequest r)
            {
                Log.ignore(r);
                retry=true;
            }
            catch (EofException e)
            {
                Log.ignore(e);
                error=true;
            }
            catch (ServletException e)
            {
                Log.warn(e);
                _request.setHandled(true);
                _generator.sendError(HttpStatus.ORDINAL_500_Internal_Server_Error,null,null,true);
            }
            catch (HttpException e)
            {
                Log.debug(e);
                _request.setHandled(true);
                _response.sendError(e.getStatus(),e.getReason());
            }
            finally
            {
                if (threadName!=null)
                    Thread.currentThread().setName(threadName);

                if (!retry)
                {
                    _requests++;

                    if (_request.getContinuation()!=null&&_request.getContinuation().isPending())
                    {
                        Log.debug("continuation still pending {}");
                        _request.getContinuation().reset();
                    }

                    if (!error&&_endp.isOpen())
                    {
                        if (!_response.isCommitted()&&!_request.isHandled())
                            _response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        _response.complete();
                    }
                }
            }
        }
    }

    private class RequestHandler implements Ajp13Parser.EventHandler
    {

        public void startForwardRequest() throws IOException
        {
            _uri.clear();
            _sslSecure=false;
            _request.setTimeStamp(System.currentTimeMillis());
            _request.setUri(_uri);
        }

        public void parsedMethod(Buffer method) throws IOException
        {
            Log.debug("AJP13 METHOD '{}'",method);

            _request.setMethod(method.toString());
        }

        public void parsedUri(Buffer uri) throws IOException
        {
            Log.debug("AJP13 URI '{}'",uri);

            _uri.setPathAndParam(uri.toString());
        }

        public void parsedProtocol(Buffer protocol) throws IOException
        {
            Log.debug("AJP13 PROTOCOL '{}'",protocol);

            _request.setProtocol(protocol.toString());
        }

        public void parsedRemoteAddr(Buffer addr) throws IOException
        {
            Log.debug("AJP13 REMOTE ADDR '{}'",addr);

            // XXX Is the remote address used anywhere?
        }

        public void parsedRemoteHost(Buffer name) throws IOException
        {
            Log.debug("AJP13 REMOTE HOST '{}'",name);

            // XXX Is the remote host used anywhere?
        }

        public void parsedServerName(Buffer name) throws IOException
        {
            Log.debug("AJP13 SERVER NAME '{}'",name);

            _uri.setHost(name.toString());
        }

        public void parsedServerPort(int port) throws IOException
        {
            Log.debug("AJP13 SERVER PORT '{}'",new Integer(port));

            _uri.setPort(port);
        }

        public void parsedSslSecure(boolean secure) throws IOException
        {
            Log.debug("AJP13 SSL SECURE {}",new Boolean(secure));

            _sslSecure=secure;
            _uri.setSslSecure(secure);
        }

        public void parsedQueryString(Buffer value) throws IOException
        {
            Log.debug("AJP13 QUERY STRING '{}'",value);

            _uri.setQuery(value.toString());
        }

        public void parsedHeader(Buffer name, Buffer value) throws IOException
        {
            Log.debug("AJP13 Header {}: {}",name,value);

            _requestFields.add(name,value);
        }

        public void parsedRequestAttribute(String key, Buffer value) throws IOException
        {
            Log.debug("AJP13 Header [{}]<{}>",key,value);

            _request.setAttribute(key,value.toString());
        }

        public void headerComplete() throws IOException
        {
        }

    }

}

// ========================================================================
// Copyright 2006-2007 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.Connection;
import org.mortbay.io.EndPoint;
import org.mortbay.io.nio.SelectChannelEndPoint;
import org.mortbay.jetty.HttpGenerator;
import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpParser;
import org.mortbay.jetty.HttpSchemes;
import org.mortbay.jetty.HttpVersions;
import org.mortbay.jetty.client.security.Authorization;
import org.mortbay.jetty.security.SslHttpChannelEndPoint;
import org.mortbay.log.Log;
import org.mortbay.thread.Timeout;

/**
 * 
 * @author Greg Wilkins
 * @author Guillaume Nodet
 */
public class HttpConnection implements Connection
{
    HttpDestination _destination;
    EndPoint _endp;
    HttpGenerator _generator;
    HttpParser _parser;
    boolean _http11 = true;
    Buffer _connectionHeader;
    Buffer _requestContentChunk;
    long _last;
    boolean _requestComplete;
    public String _message;
    public Throwable _throwable;

    /* The current exchange waiting for a response */
    HttpExchange _exchange;
    HttpExchange _pipeline;

    public void dump() throws IOException
    {
        System.err.println("endp=" + _endp + " " + _endp.isBufferingInput() + " " + _endp.isBufferingOutput());
        System.err.println("generator=" + _generator);
        System.err.println("parser=" + _parser.getState() + " " + _parser.isMoreInBuffer());
        System.err.println("exchange=" + _exchange);
        if (_endp instanceof SslHttpChannelEndPoint)
            ((SslHttpChannelEndPoint)_endp).dump();
    }

    Timeout.Task _timeout = new Timeout.Task()
    {
        public void expired()
        {
            HttpExchange ex = null;
            try
            {
                synchronized (HttpConnection.this)
                {
                    ex = _exchange;
                    _exchange = null;
                    if (ex != null)
                        _destination.returnConnection(HttpConnection.this,true);
                }
            }
            catch (Exception e)
            {
                Log.debug(e);
            }
            finally
            {
                try
                {
                    _endp.close();
                }
                catch (IOException e)
                {
                    Log.ignore(e);
                }

                if (ex.getStatus() < HttpExchange.STATUS_COMPLETED)
                {
                    ex.setStatus(HttpExchange.STATUS_EXPIRED);
                }
            }
        }

    };

    /* ------------------------------------------------------------ */
    HttpConnection(Buffers buffers, EndPoint endp, int hbs, int cbs)
    {
        _endp = endp;
        _generator = new HttpGenerator(buffers,endp,hbs,cbs);
        _parser = new HttpParser(buffers,endp,new Handler(),hbs,cbs);
    }

    /* ------------------------------------------------------------ */
    public HttpDestination getDestination()
    {
        return _destination;
    }

    /* ------------------------------------------------------------ */
    public void setDestination(HttpDestination destination)
    {
        _destination = destination;
    }

    /* ------------------------------------------------------------ */
    public boolean send(HttpExchange ex) throws IOException
    {
        // _message =
        // Thread.currentThread().getName()+": Generator instance="+_generator
        // .hashCode()+" state= "+_generator.getState()+" _exchange="+_exchange;
        _throwable = new Throwable();
        synchronized (this)
        {
            if (_exchange != null)
            {
                if (_pipeline != null)
                    throw new IllegalStateException(this + " PIPELINED!!!  _exchange=" + _exchange);
                _pipeline = ex;
                return true;
            }

            if (!_endp.isOpen())
                return false;

            ex.setStatus(HttpExchange.STATUS_WAITING_FOR_COMMIT);
            _exchange = ex;

            if (_endp.isBlocking())
                this.notify();
            else
            {
                SelectChannelEndPoint scep = (SelectChannelEndPoint)_endp;
                scep.scheduleWrite();
            }

            if (!_endp.isBlocking())
                _destination.getHttpClient().schedule(_timeout);

            return true;
        }
    }

    /* ------------------------------------------------------------ */
    public void handle() throws IOException
    {
        int no_progress = 0;
        long flushed = 0;

        boolean failed = false;
        while (_endp.isBufferingInput() || _endp.isOpen())
        {
            synchronized (this)
            {
                while (_exchange == null)
                {
                    if (_endp.isBlocking())
                    {
                        try
                        {
                            this.wait();
                        }
                        catch (InterruptedException e)
                        {
                            throw new InterruptedIOException();
                        }
                    }
                    else
                    {
                        // Hopefully just space?
                        _parser.fill();
                        _parser.skipCRLF();
                        if (_parser.isMoreInBuffer())
                        {
                            Log.warn("unexpected data");
                            _endp.close();
                        }

                        return;
                    }
                }
            }
            if (_exchange.getStatus() == HttpExchange.STATUS_WAITING_FOR_COMMIT)
            {
                no_progress = 0;
                commitRequest();
            }

            try
            {
                long io = 0;
                _endp.flush();

                if (_generator.isComplete())
                {
                    if (!_requestComplete)
                    {
                        _requestComplete = true;
                        _exchange.getEventListener().onRequestComplete();
                    }
                }
                else
                {
                    // Write as much of the request as possible
                    synchronized (this)
                    {
                        if (_exchange == null)
                            continue;
                        flushed = _generator.flush();
                        io += flushed;
                    }

                    if (!_generator.isComplete())
                    {
                        InputStream in = _exchange.getRequestContentSource();
                        if (in != null)
                        {
                            if (_requestContentChunk == null || _requestContentChunk.length() == 0)
                            {
                                _requestContentChunk = _exchange.getRequestContentChunk();
                                if (_requestContentChunk != null)
                                    _generator.addContent(_requestContentChunk,false);
                                else
                                    _generator.complete();
                                io += _generator.flush();
                            }
                        }
                        else
                            _generator.complete();
                    }
                }
                

                // If we are not ended then parse available
                if (!_parser.isComplete() && _generator.isCommitted())
                {
                    long filled = _parser.parseAvailable();
                    io += filled;
                }

                if (io > 0)
                    no_progress = 0;
                else if (no_progress++ >= 2 && !_endp.isBlocking())
                {
                    // SSL may need an extra flush as it may have made "no progress" while actually doing a handshake.
                    if (_endp instanceof SslHttpChannelEndPoint && !_generator.isComplete() && !_generator.isEmpty())
                    {
                        if (_generator.flush()>0)
                            continue;
                    }
                    return;
                }
            }
            catch (IOException e)
            {
                synchronized (this)
                {
                    if (_exchange != null)
                    {
                        _exchange.getEventListener().onException(e);
                        _exchange.setStatus(HttpExchange.STATUS_EXCEPTED);
                    }
                }
                failed = true;
                throw e;
            }
            finally
            {
                boolean complete = false;
                boolean close = failed; // always close the connection on error
                if (!failed)
                {
                    // are we complete?
                    if (_generator.isComplete())
                    {
                        if (!_requestComplete)
                        {
                            _requestComplete = true;
                            _exchange.getEventListener().onRequestComplete();
                        }

                        // we need to return the HttpConnection to a state that
                        // it can be reused or closed out
                        if (_parser.isComplete())
                        {
                            _destination.getHttpClient().cancel(_timeout);
                            complete = true;
                        }
                    }
                }

                if (complete || failed)
                {
                    synchronized (this)
                    {
                        if (!close)
                            close = shouldClose();
                            
                        reset(true);
                        no_progress = 0;
                        flushed = -1;
                        if (_exchange != null)
                        {
                            _exchange = null;

                            if (_pipeline == null)
                            {
                                _destination.returnConnection(this,close);
                                if (close)
                                    return;
                            }
                            else
                            {
                                if (close)
                                {
                                    _destination.returnConnection(this,close);
                                    _destination.send(_pipeline);
                                    _pipeline = null;
                                    return;
                                }

                                HttpExchange ex = _pipeline;
                                _pipeline = null;

                                send(ex);
                            }
                        }
                    }
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isIdle()
    {
        synchronized (this)
        {
            return _exchange == null;
        }
    }

    /* ------------------------------------------------------------ */
    public EndPoint getEndPoint()
    {
        return _endp;
    }

    /* ------------------------------------------------------------ */
    private void commitRequest() throws IOException
    {
        synchronized (this)
        {
            if (_exchange.getStatus() != HttpExchange.STATUS_WAITING_FOR_COMMIT)
                throw new IllegalStateException();

            _exchange.setStatus(HttpExchange.STATUS_SENDING_REQUEST);
            _generator.setVersion(_exchange._version);

            String uri = _exchange._uri;
            if (_destination.isProxied() && uri.startsWith("/"))
            {
                // TODO suppress port 80 or 443
                uri = (_destination.isSecure()?HttpSchemes.HTTPS:HttpSchemes.HTTP) + "://" + _destination.getAddress().getHost() + ":"
                        + _destination.getAddress().getPort() + uri;
                Authorization auth = _destination.getProxyAuthentication();
                if (auth != null)
                    auth.setCredentials(_exchange);
            }

            _generator.setRequest(_exchange._method,uri);

            if (_exchange._version >= HttpVersions.HTTP_1_1_ORDINAL)
            {
                if (!_exchange._requestFields.containsKey(HttpHeaders.HOST_BUFFER))
                    _exchange._requestFields.add(HttpHeaders.HOST_BUFFER,_destination.getHostHeader());
            }

            if (_exchange._requestContent != null)
            {
                _exchange._requestFields.putLongField(HttpHeaders.CONTENT_LENGTH,_exchange._requestContent.length());
                _generator.completeHeader(_exchange._requestFields,false);
                _generator.addContent(_exchange._requestContent,true);
            }
            else if (_exchange._requestContentSource != null)
            {
                _generator.completeHeader(_exchange._requestFields,false);
                int available = _exchange._requestContentSource.available();
                if (available > 0)
                {
                    // TODO deal with any known content length

                    // TODO reuse this buffer!
                    byte[] buf = new byte[available];
                    int length = _exchange._requestContentSource.read(buf);
                    _generator.addContent(new ByteArrayBuffer(buf,0,length),false);
                }
            }
            else
            {
                _exchange._requestFields.remove(HttpHeaders.CONTENT_LENGTH); // TODO
                // :
                // should
                // not
                // be
                // needed
                _generator.completeHeader(_exchange._requestFields,true);
            }

            _exchange.setStatus(HttpExchange.STATUS_WAITING_FOR_RESPONSE);
        }
    }

    /* ------------------------------------------------------------ */
    protected void reset(boolean returnBuffers) throws IOException
    {
        _requestComplete = false;
        _connectionHeader = null;
        _parser.reset(returnBuffers);
        _generator.reset(returnBuffers);
        _http11 = true;
    }

    /* ------------------------------------------------------------ */
    private boolean shouldClose()
    {
        if (_connectionHeader!=null)
        {
            if (HttpHeaderValues.CLOSE_BUFFER.equals(_connectionHeader))
                return true;
            if (HttpHeaderValues.KEEP_ALIVE_BUFFER.equals(_connectionHeader))
                return false;
        }
        return !_http11;
    }

    /* ------------------------------------------------------------ */
    private class Handler extends HttpParser.EventHandler
    {
        @Override
        public void startRequest(Buffer method, Buffer url, Buffer version) throws IOException
        {
            // System.out.println( method.toString() + "///" + url.toString() +
            // "///" + version.toString() );
            // TODO validate this is acceptable, the <!DOCTYPE goop was coming
            // out here
            // throw new IllegalStateException();
        }

        @Override
        public void startResponse(Buffer version, int status, Buffer reason) throws IOException
        {
            _http11 = HttpVersions.HTTP_1_1_BUFFER.equals(version);
            _exchange.getEventListener().onResponseStatus(version,status,reason);
            _exchange.setStatus(HttpExchange.STATUS_PARSING_HEADERS);
        }

        @Override
        public void parsedHeader(Buffer name, Buffer value) throws IOException
        {
            if (HttpHeaders.CACHE.getOrdinal(name) == HttpHeaders.CONNECTION_ORDINAL)
            {
                _connectionHeader = HttpHeaderValues.CACHE.lookup(value);
            }
            _exchange.getEventListener().onResponseHeader(name,value);
        }

        @Override
        public void headerComplete() throws IOException
        {
            _exchange.setStatus(HttpExchange.STATUS_PARSING_CONTENT);
        }

        @Override
        public void content(Buffer ref) throws IOException
        {
            _exchange.getEventListener().onResponseContent(ref);
        }

        @Override
        public void messageComplete(long contextLength) throws IOException
        {
            _exchange.setStatus(HttpExchange.STATUS_COMPLETED);
        }
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "HttpConnection@" + hashCode() + "//" + _destination.getAddress().getHost() + ":" + _destination.getAddress().getPort();
    }

    /* ------------------------------------------------------------ */
    public String toDetailString()
    {
        return toString() + " ex=" + _exchange + " " + _timeout.getAge();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the last
     */
    public long getLast()
    {
        return _last;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param last
     *            the last to set
     */
    public void setLast(long last)
    {
        _last = last;
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        _endp.close();
    }

}
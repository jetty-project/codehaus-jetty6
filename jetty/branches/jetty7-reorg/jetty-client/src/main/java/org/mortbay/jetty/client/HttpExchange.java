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
import java.net.InetSocketAddress;

import org.mortbay.jetty.http.HttpFields;
import org.mortbay.jetty.http.HttpHeaders;
import org.mortbay.jetty.http.HttpMethods;
import org.mortbay.jetty.http.HttpVersions;
import org.mortbay.jetty.io.Buffer;
import org.mortbay.jetty.io.ByteArrayBuffer;
import org.mortbay.jetty.io.BufferCache.CachedBuffer;
import org.mortbay.jetty.server.HttpSchemes;
import org.mortbay.jetty.server.HttpURI;
import org.mortbay.jetty.util.log.Log;


/**
 * An HTTP client API that encapsulates Exchange with a HTTP server.
 *
 * This object encapsulates:<ul>
 * <li>The HTTP server. (see {@link #setAddress(InetSocketAddress)} or {@link #setURL(String)})
 * <li>The HTTP request method, URI and HTTP version (see {@link #setMethod(String)}, {@link #setURI(String)}, and {@link #setVersion(int)}
 * <li>The Request headers (see {@link #addRequestHeader(String, String)} or {@link #setRequestHeader(String, String)})
 * <li>The Request content (see {@link #setRequestContent(Buffer)} or {@link #setRequestContentSource(InputStream)})
 * <li>The status of the exchange (see {@link #getStatus()})
 * <li>Callbacks to handle state changes (see the onXxx methods such as {@link #onRequestComplete()} or {@link #onResponseComplete()})
 * <li>The ability to intercept callbacks (see {@link #setEventListener(HttpEventListener)}
 * </ul>
 *
 * The HttpExchange class is intended to be used by a developer wishing to have close asynchronous
 * interaction with the the exchange.  Typically a developer will extend the HttpExchange class with a derived
 * class that implements some or all of the onXxx callbacks.  There are also some predefined HttpExchange subtypes
 * that can be used as a basis (see {@link ContentExchange} and {@link CachedExchange}.
 *
 * <p>Typically the HttpExchange is passed to a the {@link HttpClient#send(HttpExchange)} method, which in
 * turn selects a {@link HttpDestination} and calls it's {@link HttpDestination#send(HttpExchange), which
 * then creates or selects a {@link HttpConnection} and calls its {@link HttpConnection#send(HttpExchange).
 * A developer may wish to directly call send on the destination or connection if they wish to bypass
 * some handling provided (eg Cookie handling in the HttpDestination).
 *
 * <p>In some circumstances, the HttpClient or HttpDestination may wish to retry a HttpExchange (eg. failed
 * pipeline request, authentication retry or redirection).  In such cases, the HttpClient and/or HttpDestination
 * may insert their own HttpExchangeListener to intercept and filter the call backs intended for the
 * HttpExchange.
 *
 * @author gregw
 * @author Guillaume Nodet
 */
public class HttpExchange
{
    public static final int STATUS_START = 0;
    public static final int STATUS_WAITING_FOR_CONNECTION = 1;
    public static final int STATUS_WAITING_FOR_COMMIT = 2;
    public static final int STATUS_SENDING_REQUEST = 3;
    public static final int STATUS_WAITING_FOR_RESPONSE = 4;
    public static final int STATUS_PARSING_HEADERS = 5;
    public static final int STATUS_PARSING_CONTENT = 6;
    public static final int STATUS_COMPLETED = 7;
    public static final int STATUS_EXPIRED = 8;
    public static final int STATUS_EXCEPTED = 9;

    Address _address;
    String _method = HttpMethods.GET;
    Buffer _scheme = HttpSchemes.HTTP_BUFFER;
    int _version = HttpVersions.HTTP_1_1_ORDINAL;
    String _uri;
    int _status = STATUS_START;
    HttpFields _requestFields = new HttpFields();
    Buffer _requestContent;
    InputStream _requestContentSource;
    Buffer _requestContentChunk;
    boolean _retryStatus = false;


    /**
     * boolean controlling if the exchange will have listeners autoconfigured by
     * the destination
     */
    boolean _configureListeners = true;


    private HttpEventListener _listener = new Listener();

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    // methods to build request

    /* ------------------------------------------------------------ */
    public int getStatus()
    {
        return _status;
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated
     */
    public void waitForStatus(int status) throws InterruptedException
    {
        synchronized (this)
        {
            while (_status < status)
            {
                this.wait();
            }
        }
    }


    public int waitForDone () throws InterruptedException
    {
        synchronized (this)
        {
            while (!isDone(_status))
                this.wait();
        }
        return _status;
    }




    /* ------------------------------------------------------------ */
    public void reset()
    {
        setStatus(STATUS_START);
    }

    /* ------------------------------------------------------------ */
    void setStatus(int status)
    {
        synchronized (this)
        {
            _status = status;
            this.notifyAll();

            try
            {
                switch (status)
                {
                    case STATUS_WAITING_FOR_CONNECTION:
                        break;

                    case STATUS_WAITING_FOR_COMMIT:
                        break;

                    case STATUS_SENDING_REQUEST:
                        break;

                    case HttpExchange.STATUS_WAITING_FOR_RESPONSE:
                        getEventListener().onRequestCommitted();
                        break;

                    case STATUS_PARSING_HEADERS:
                        break;

                    case STATUS_PARSING_CONTENT:
                        getEventListener().onResponseHeaderComplete();
                        break;

                    case STATUS_COMPLETED:
                        getEventListener().onResponseComplete();
                        break;

                    case STATUS_EXPIRED:
                        getEventListener().onExpire();
                        break;

                }
            }
            catch (IOException e)
            {
                Log.warn(e);
            }
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isDone (int status)
    {
        return ((status == STATUS_COMPLETED) || (status == STATUS_EXPIRED) || (status == STATUS_EXCEPTED));
    }

    /* ------------------------------------------------------------ */
    public HttpEventListener getEventListener()
    {
        return _listener;
    }

    /* ------------------------------------------------------------ */
    public void setEventListener(HttpEventListener listener)
    {
        _listener=listener;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param url Including protocol, host and port
     */
    public void setURL(String url)
    {
        HttpURI uri = new HttpURI(url);
        String scheme = uri.getScheme();
        if (scheme != null)
        {
            if (HttpSchemes.HTTP.equalsIgnoreCase(scheme))
                setScheme(HttpSchemes.HTTP_BUFFER);
            else if (HttpSchemes.HTTPS.equalsIgnoreCase(scheme))
                setScheme(HttpSchemes.HTTPS_BUFFER);
            else
                setScheme(new ByteArrayBuffer(scheme));
        }

        int port = uri.getPort();
        if (port <= 0)
            port = "https".equalsIgnoreCase(scheme)?443:80;

        setAddress(new Address(uri.getHost(),port));

        String completePath = uri.getCompletePath();
        if (completePath == null)
            completePath = "/";
        
        setURI(completePath);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param address
     */
    public void setAddress(Address address)
    {
        _address = address;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public Address getAddress()
    {
        return _address;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param scheme
     */
    public void setScheme(Buffer scheme)
    {
        _scheme = scheme;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public Buffer getScheme()
    {
        return _scheme;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param version as integer, 9, 10 or 11 for 0.9, 1.0 or 1.1
     */
    public void setVersion(int version)
    {
        _version = version;
    }

    /* ------------------------------------------------------------ */
    public void setVersion(String version)
    {
        CachedBuffer v = HttpVersions.CACHE.get(version);
        if (v == null)
            _version = 10;
        else
            _version = v.getOrdinal();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public int getVersion()
    {
        return _version;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param method
     */
    public void setMethod(String method)
    {
        _method = method;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public String getMethod()
    {
        return _method;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public String getURI()
    {
        return _uri;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param uri
     */
    public void setURI(String uri)
    {
        _uri = uri;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     */
    public void addRequestHeader(String name, String value)
    {
        getRequestFields().add(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     */
    public void addRequestHeader(Buffer name, Buffer value)
    {
        getRequestFields().add(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     */
    public void setRequestHeader(String name, String value)
    {
        getRequestFields().put(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     */
    public void setRequestHeader(Buffer name, Buffer value)
    {
        getRequestFields().put(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param value
     */
    public void setRequestContentType(String value)
    {
        getRequestFields().put(HttpHeaders.CONTENT_TYPE_BUFFER,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public HttpFields getRequestFields()
    {
        return _requestFields;
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    // methods to commit and/or send the request

    /* ------------------------------------------------------------ */
    /**
     * @param requestContent
     */
    public void setRequestContent(Buffer requestContent)
    {
        _requestContent = requestContent;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param in
     */
    public void setRequestContentSource(InputStream in)
    {
        _requestContentSource = in;
    }

    /* ------------------------------------------------------------ */
    public InputStream getRequestContentSource()
    {
        return _requestContentSource;
    }

    /* ------------------------------------------------------------ */
    public Buffer getRequestContentChunk() throws IOException
    {
        synchronized (this)
        {
            if (_requestContentChunk == null)
                _requestContentChunk = new ByteArrayBuffer(4096); // TODO configure
            else
            {
                if (_requestContentChunk.hasContent())
                    throw new IllegalStateException();
                _requestContentChunk.clear();
            }

            int read = _requestContentChunk.capacity();
            int length = _requestContentSource.read(_requestContentChunk.array(),0,read);
            if (length >= 0)
            {
                _requestContentChunk.setPutIndex(length);
                return _requestContentChunk;
            }
            return null;
        }
    }

    /* ------------------------------------------------------------ */
    public Buffer getRequestContent()
    {
        return _requestContent;
    }

    public boolean getRetryStatus()
    {
        return _retryStatus;
    }

    public void setRetryStatus( boolean retryStatus )
    {
        _retryStatus = retryStatus;
    }

    /* ------------------------------------------------------------ */
    /** Cancel this exchange
     * Currently this implementation does nothing.
     */
    public void cancel()
    {

    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "HttpExchange@" + hashCode() + "=" + _method + "//" + _address.getHost() + ":" + _address.getPort() + _uri + "#" + _status;
    }



    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    // methods to handle response
    protected void onRequestCommitted() throws IOException
    {
    }

    protected void onRequestComplete() throws IOException
    {
    }

    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
    }

    protected void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
    }

    protected void onResponseHeaderComplete() throws IOException
    {
    }

    protected void onResponseContent(Buffer content) throws IOException
    {
    }

    protected void onResponseComplete() throws IOException
    {
    }

    protected void onConnectionFailed(Throwable ex)
    {
        Log.warn("CONNECTION FAILED on " + this,ex);
    }

    protected void onException(Throwable ex)
    {

        Log.warn("EXCEPTION on " + this,ex);
    }

    protected void onExpire()
    {
        Log.debug("EXPIRED " + this);
    }

    protected void onRetry() throws IOException
    {}

    /**
     * true of the exchange should have listeners configured for it by the destination
     *
     * false if this is being managed elsewhere
     *
     * @return
     */
    public boolean configureListeners()
    {
        return _configureListeners;
    }

    public void setConfigureListeners(boolean autoConfigure )
    {
        this._configureListeners = autoConfigure;
    }

    private class Listener implements HttpEventListener
    {
        public void onConnectionFailed(Throwable ex)
        {
            HttpExchange.this.onConnectionFailed(ex);
        }

        public void onException(Throwable ex)
        {
            HttpExchange.this.onException(ex);
        }

        public void onExpire()
        {
            HttpExchange.this.onExpire();
        }

        public void onRequestCommitted() throws IOException
        {
            HttpExchange.this.onRequestCommitted();
        }

        public void onRequestComplete() throws IOException
        {
            HttpExchange.this.onRequestComplete();
        }

        public void onResponseComplete() throws IOException
        {
            HttpExchange.this.onResponseComplete();
        }

        public void onResponseContent(Buffer content) throws IOException
        {
            HttpExchange.this.onResponseContent(content);
        }

        public void onResponseHeader(Buffer name, Buffer value) throws IOException
        {
            HttpExchange.this.onResponseHeader(name,value);
        }

        public void onResponseHeaderComplete() throws IOException
        {
            HttpExchange.this.onResponseHeaderComplete();
        }

        public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
        {
            HttpExchange.this.onResponseStatus(version,status,reason);
        }

        public void onRetry()
        {
            HttpExchange.this.setRetryStatus( true );
            try
            {
                HttpExchange.this.onRetry();
            }
            catch (IOException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * @deprecated use {@link org.mortbay.jetty.client.CachedExchange}
     *
     */
    public static class CachedExchange extends org.mortbay.jetty.client.CachedExchange
    {
        public CachedExchange(boolean cacheFields)
        {
            super(cacheFields);
        }
    }

    /**
     * @deprecated use {@link org.mortbay.jetty.client.ContentExchange}
     *
     */
    public static class ContentExchange extends org.mortbay.jetty.client.ContentExchange
    {

    }



}

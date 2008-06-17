package org.mortbay.jetty.client;

import java.io.IOException;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.HttpFields;

/**
 * An exchange that caches response status and fields for later use.
 * 
 * @author gregw
 *
 */
public class CachedExchange extends HttpExchange
{
    int _responseStatus;
    HttpFields _responseFields;

    public CachedExchange(boolean cacheFields)
    {
        if (cacheFields)
            _responseFields = new HttpFields();
    }

    /* ------------------------------------------------------------ */
    public int getResponseStatus()
    {
        if (_status < HttpExchange.STATUS_PARSING_HEADERS)
            throw new IllegalStateException("Response not received");
        return _responseStatus;
    }

    /* ------------------------------------------------------------ */
    public HttpFields getResponseFields()
    {
        if (_status < HttpExchange.STATUS_PARSING_CONTENT)
            throw new IllegalStateException("Headers not complete");
        return _responseFields;
    }

    /* ------------------------------------------------------------ */
    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        super.onResponseStatus(version,status,reason);
        _responseStatus = status;
    }

    /* ------------------------------------------------------------ */
    protected void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
        super.onResponseHeader(name,value);
        if (_responseFields != null)
            _responseFields.add(name,value);
    }

}
package org.mortbay.jetty.client;

import java.io.IOException;

import org.mortbay.io.Buffer;

public class HttpEventListenerWrapper implements HttpEventListener
{
    HttpEventListener _listener;
    boolean _delegating;

    public HttpEventListenerWrapper()
    {
        _listener=null;
        _delegating=false;
    }
    
    public HttpEventListenerWrapper(HttpEventListener eventListener,boolean delegating)
    {
        _listener=eventListener;
        _delegating=delegating;
    }
    
    public HttpEventListener getEventListener()
    {
        return _listener;
    }

    public void setEventListener(HttpEventListener listener)
    {
        _listener = listener;
    }

    public boolean isDelegating()
    {
        return _delegating;
    }

    public void setDelegating(boolean delegating)
    {
        _delegating = delegating;
    }

    
    public void onConnectionFailed(Throwable ex)
    {
        if (_delegating)
            _listener.onConnectionFailed(ex);
    }

    public void onException(Throwable ex)
    {
        if (_delegating)
            _listener.onException(ex);
    }

    public void onExpire()
    {
        if (_delegating)
            _listener.onExpire();
    }

    public void onRequestCommitted() throws IOException
    {
        if (_delegating)
            _listener.onRequestCommitted();
    }

    public void onRequestComplete() throws IOException
    {
        if (_delegating)
            _listener.onRequestComplete();
    }

    public void onResponseComplete() throws IOException
    {
        if (_delegating)
            _listener.onResponseComplete();
    }

    public void onResponseContent(Buffer content) throws IOException
    {
        if (_delegating)
            _listener.onResponseContent(content);
    }

    public void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
        if (_delegating)
            _listener.onResponseHeader(name,value);
    }

    public void onResponseHeaderComplete() throws IOException
    {
        if (_delegating)
            _listener.onResponseHeaderComplete();
    }

    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        if (_delegating)
            _listener.onResponseStatus(version,status,reason);
    }

    public void onRetry()
    {
        if (_delegating)
            _listener.onRetry();
    }
    
    
    
}

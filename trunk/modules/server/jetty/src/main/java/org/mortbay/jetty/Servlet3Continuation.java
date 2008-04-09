package org.mortbay.jetty;

import org.mortbay.util.ajax.Continuation;

public class Servlet3Continuation implements Continuation
{
    Request _request;
    Object _object;
    RetryRequest _retry;
    
    Servlet3Continuation(Request request)
    {
        _request=request;
    }
    
    public Object getObject()
    {
        return _object;
    }

    public boolean isExpired()
    {
        return _request.isTimeout();
    }

    public boolean isNew()
    {
        return _retry==null;
    }

    public boolean isPending()
    {
        return _request.isSuspended() || !_request.isInitial();
    }

    public boolean isResumed()
    {
        return _request.isResumed();
    }

    public void reset()
    {
        _request.reset();
    }

    public void resume()
    {
        System.err.println("Resume");
        _request.resume();
    }

    public void setMutex(Object mutex)
    {
    }

    public void setObject(Object o)
    {
        _object=o;
    }

    public boolean suspend(long timeout)
    {
        System.err.println(_request);
        if (!_request.isInitial()&&(_request.isResumed()||_request.isTimeout()))
            return _request.isResumed();

        _request.suspend(timeout);
        if (_retry==null)
            _retry=new RetryRequest();
        throw _retry;
        
    }

}

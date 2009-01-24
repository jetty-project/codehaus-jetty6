//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import org.mortbay.util.ajax.Continuation;

public class Servlet3Continuation implements Continuation, AsyncListener
{
    AsyncRequest _asyncContextState;
    Request _request;
    Object _object;
    RetryRequest _retry;
    boolean _resumed=false;
    boolean _timeout=false;
    
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
        return _asyncContextState!=null && _timeout;
    }

    public boolean isNew()
    {
        return _retry==null;
    }

    public boolean isPending()
    {
        return _asyncContextState!=null && (_asyncContextState.isSuspended() || !_asyncContextState.isInitial());
    }

    public boolean isResumed()
    {
        return _asyncContextState!=null && _resumed;
    }

    public void reset()
    {
        _resumed=false;
        _timeout=false;
    }

    public void resume()
    {
        if (_asyncContextState==null)
            throw new IllegalStateException();
        _resumed=true;
        _asyncContextState.dispatch();
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
        _asyncContextState=_request;
        if (!_asyncContextState.isInitial()||_resumed||_timeout)
        {
            _resumed=false;
            _timeout=false;
            return _resumed;
        }

        _request.setAsyncTimeout(timeout);
        _request.addAsyncListener(this);
        _request.startAsync();
        _asyncContextState=_request;
        if (_retry==null)
            _retry=new RetryRequest();
        throw _retry;
        
    }

    public void onComplete(AsyncEvent event) throws IOException
    {
        
    }

    public void onTimeout(AsyncEvent event) throws IOException
    {
        _timeout=true;
        _request.dispatch();
    }

}

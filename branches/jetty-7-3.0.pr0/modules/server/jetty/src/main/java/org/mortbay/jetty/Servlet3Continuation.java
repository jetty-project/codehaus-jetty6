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
    AsyncContextState _asyncContextState;
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
        return _asyncContextState!=null && _asyncContextState.isTimeout();
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
        return _asyncContextState!=null && _asyncContextState.isResumed();
    }

    public void reset()
    {
        _request.getAsyncContextState().reset();
    }

    public void resume()
    {
        if (_asyncContextState==null)
            throw new IllegalStateException();
        _asyncContextState.forward();
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
        _asyncContextState=_request.getAsyncContextState();
        if (_asyncContextState!=null &&
            (_asyncContextState.isInitial()||_asyncContextState.isResumed()||_asyncContextState.isTimeout()))
        {
            return _asyncContextState.isResumed();
        }

        _request.setAsyncTimeout(timeout);
        _request.startAsync();
        _asyncContextState=_request.getAsyncContextState();
        if (_retry==null)
            _retry=new RetryRequest();
        throw _retry;
        
    }

    public void onComplete(AsyncEvent event) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onTimeout(AsyncEvent event) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

}

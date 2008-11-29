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

//========================================================================
//$Id: WaitingContinuation.java,v 1.1 2005/11/14 17:45:56 gregwilkins Exp $
//Copyright 2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.util.ajax;

public class WaitingContinuation implements org.mortbay.util.ajax.Continuation
{
    Object _mutex;
    Object _object;
    boolean _waited;
    boolean _new=true;
    boolean _pending=true;
    
    WaitingContinuation(Object mutex)
    {
        _mutex=mutex==null?this:mutex;
    }
    
    public void resume()
    {
        synchronized (_mutex)
        {
            _pending=false;
            _mutex.notify();
        }
    }

    public boolean isNew()
    {
        return _new;
    }

    public boolean suspend(long timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException();
        
        synchronized (_mutex)
        {
            _new=false;
            try
            {
                if (!_waited && _pending && timeout>0)
                {
                    _waited=true;
                    _mutex.wait(timeout);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            boolean timed_out=_pending;
            _pending=false;
            return timed_out;
        }
    }
    
    public boolean isPending()
    {
        return _pending;
    }

    public Object getObject()
    {
        return _object;
    }

    public void setObject(Object object)
    {
        _object = object;
    }

}
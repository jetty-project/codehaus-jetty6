// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.cometd;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.cometd.ClientImpl;
import org.mortbay.thread.Timeout;

/* ------------------------------------------------------------ */
/**
 * Extension of {@link ClientImpl} that uses {@link Continuation}s to
 * resume clients waiting for messages. Continuation clients are used for
 * remote clients and have removed if they are not accessed within
 * an idle timeout (@link {@link SuspendingBayeux#_clientTimer}).
 * 
 * @author gregw
 *
 */
public class SuspendingClient extends ClientImpl
{
    private long _accessed;
    public transient Timeout.Task _timeout; 
    private SuspendingBayeux _bayeux;
    private transient ServletRequest _pollRequest;

    /* ------------------------------------------------------------ */
    protected SuspendingClient(SuspendingBayeux bayeux)
    {
        super(bayeux);
        _bayeux=bayeux;

        if (!isLocal())
        {
            _timeout=new Timeout.Task()
            {
                public void expired()
                {
                    remove(true);
                }
                public String toString()
                {
                    return "T-"+SuspendingClient.this.toString();
                }
            };
            _bayeux.startTimeout(_timeout,getTimeout());
        }
    }


    /* ------------------------------------------------------------ */
    public void setPollRequest(ServletRequest request)
    {
        
        if (request==null)
        {
            synchronized (this)
            {
                if (_pollRequest!=null)
                {
                    if(_pollRequest.isSuspended())
                        _pollRequest.resume(); 
                }
                _pollRequest=null;
                if (_timeout!=null)
                    _bayeux.startTimeout(_timeout,getTimeout());
            }
        }
        else
        {
            synchronized (this)
            {
                if (_pollRequest!=null)
                {
                    if(_pollRequest.isSuspended())
                        _pollRequest.resume(); 
                }
                _pollRequest=request;
                if (_timeout!=null)
                    _bayeux.cancelTimeout(_timeout);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public ServletRequest getPollRequest()
    {
        return _pollRequest;
    }

    /* ------------------------------------------------------------ */
    public void resume()
    {
        synchronized (this)
        {
            if (_pollRequest!=null)
            {
                ((HttpServletResponse)((HttpServletRequest)_pollRequest).getServletResponse()).addHeader("Debug","Resume");
                _pollRequest.resume();
            }
            _pollRequest=null;
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isLocal()
    {
        return false;
    }

    /* ------------------------------------------------------------ */
    public void access()
    {
        synchronized(this)
        {
            // distribute access time in cluster
            _accessed=_bayeux.getNow();
            if (_timeout!=null && _timeout.isScheduled())
            {
                _timeout.reschedule();
            }
        }
    }


    /* ------------------------------------------------------------ */
    public synchronized long lastAccessed()
    {
        return _accessed;
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.ClientImpl#remove(boolean)
     */
    public void remove(boolean wasTimeout) 
    {
        synchronized(this)
        {
            if (!wasTimeout && _timeout!=null)
                _bayeux.cancelTimeout(_timeout);
            _timeout=null;
            super.remove(wasTimeout);
        }   
    }

}
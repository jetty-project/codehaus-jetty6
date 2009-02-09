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

import javax.servlet.AsyncContext;
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
 * an idle timeout (@link {@link AsyncBayeux#_clientTimer}).
 * 
 * @author gregw
 *
 */
public class AsyncClient extends ClientImpl
{
    private long _accessed;
    public transient Timeout.Task _timeout; 
    private AsyncBayeux _bayeux;
    private transient AsyncContext _asyncContext;

    /* ------------------------------------------------------------ */
    protected AsyncClient(AsyncBayeux bayeux)
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
                    return "T-"+AsyncClient.this.toString();
                }
            };
            _bayeux.startTimeout(_timeout,getTimeout());
        }
    }


    /* ------------------------------------------------------------ */
    public void setAsyncContext(AsyncContext context)
    {
        if (context==null)
        {
            synchronized (this)
            {
                if (_asyncContext!=null)
                    _asyncContext.dispatch(); 
                _asyncContext=null;
                if (_timeout!=null)
                    _bayeux.startTimeout(_timeout,getTimeout());
            }
        }
        else
        {
            synchronized (this)
            {
                if (_asyncContext!=null)
                    _asyncContext.dispatch(); 
                _asyncContext=context;
                if (_timeout!=null)
                    _bayeux.cancelTimeout(_timeout);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public AsyncContext getAsyncContext()
    {
        return _asyncContext;
    }

    /* ------------------------------------------------------------ */
    public void resume()
    {
        synchronized (this)
        {
            if (_asyncContext!=null)
                _asyncContext.dispatch();
            _asyncContext=null;
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
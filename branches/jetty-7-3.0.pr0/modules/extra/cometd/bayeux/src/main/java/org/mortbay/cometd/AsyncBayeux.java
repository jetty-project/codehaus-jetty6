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
//========================================================================

package org.mortbay.cometd;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.ClientImpl;
import org.mortbay.thread.Timeout;
import org.mortbay.thread.Timeout.Task;

/* ------------------------------------------------------------ */
/**
 * Extension of Bayeux that uses {@link AsyncClient}s.
 * @author gregw
 *
 */
public class AsyncBayeux extends AbstractBayeux
{
    private static int __id;
    private transient Timer _tick;
    private transient Timeout _timeout;
    private long _now;
    
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.AbstractBayeux#newClient(java.lang.String, dojox.io.cometd.Destination)
     */
    public ClientImpl newRemoteClient()
    {
        return new AsyncClient(this);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.AbstractBayeux#initialize(javax.servlet.ServletContext)
     */
    protected void initialize(ServletContext context)
    {
        super.initialize(context);
        
        _tick=new Timer("SuspendingBayeux-"+__id++, true);
        _timeout=new Timeout(this);
        _timeout.setDuration(getMaxInterval());
    
        _tick.schedule(new TimerTask()
        {
            public void run()
            {
                _timeout.setNow();
                _now=_timeout.getNow();
                _timeout.tick();
            }
        },500L,500L);
    }

    /* ------------------------------------------------------------ */
    long getNow()
    {
        return _now;
    }
    
    /* ------------------------------------------------------------ */
    public void setMaxInterval(long ms)
    {
        _timeout.setDuration(ms);
        super.setMaxInterval(ms);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.AbstractBayeux#initialize(javax.servlet.ServletContext)
     */
    public void destroy()
    {
        _tick.cancel();
    }

    /* ------------------------------------------------------------ */
    void startTimeout(Task timeout,long delay)
    {
        if (delay==0)
            _timeout.schedule(timeout);
        else
            _timeout.schedule(timeout,delay);
    }

    /* ------------------------------------------------------------ */
    public void cancelTimeout(Task timeout)
    {
        if (timeout!=null)
            timeout.cancel();
    }
    
}
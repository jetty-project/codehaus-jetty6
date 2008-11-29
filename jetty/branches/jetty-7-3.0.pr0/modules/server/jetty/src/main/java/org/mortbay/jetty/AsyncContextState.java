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

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mortbay.io.AsyncEndPoint;
import org.mortbay.io.EndPoint;
import org.mortbay.log.Log;
import org.mortbay.thread.Timeout;

public class AsyncContextState implements AsyncContext
{
    // STATES:
    private static final int __IDLE=0;         // Idle request
    private static final int __DISPATCHED=1;   // Request dispatched to filter/servlet
    private static final int __SUSPENDING=2;   // Suspend called, but not yet returned to container
    private static final int __RESUMING=3;     // resumed while dispatched
    private static final int __COMPLETING=4;   // completed while dispatched
    private static final int __SUSPENDED=5;    // Suspended and parked
    private static final int __UNSUSPENDING=6; // Has been scheduled
    private static final int __REDISPATCHED=7; // Request redispatched to filter/servlet
    
    // State table
    //                       __HANDLE      __UNHANDLE       __SUSPEND        __RESUME   
    // IDLE */          {  __DISPATCHED,    __Illegal,      __Illegal,      __Illegal  },    
    // DISPATCHED */    {   __Illegal,         __IDLE,   __SUSPENDING,       __Ignore  }, 
    // SUSPENDING */    {   __Illegal,    __SUSPENDED,      __Illegal,     __RESUMING  },
    // RESUMING */      {   __Illegal,  _REDISPATCHED,      __Ignored,       __Ignore  },
    // COMPLETING */    {   __Illegal,         __IDLE,      __Illegal,       __Illegal },
    // SUSPENDED */     {  __REDISPATCHED,  __Illegal,      __Illegal, __UNSUSPENDING  },
    // UNSUSPENDING */  {  __REDISPATCHED,  __Illegal,      __Illegal,       __Ignore  },
    // REDISPATCHED */  {   __Illegal,         __IDLE,   __SUSPENDING,       __Ignore  },
    
    // State diagram
    //
    //   +----->  IDLE  <---------------------> DISPATCHED
    //   |                                            |
    //   |          +-----+------------> REDISPATCHED |
    //   |          ^     ^                ^      |   |
    //   |          |     |                |      |   |   
    //   |          |     |                |      |   |
    //   |          |     |                |      v   v
    //   |          |    SUSPENDED <----------- SUSPENDING
    //   |          |     |  |             |    |     |
    //   |          |     |  |             |    |     |
    //   |          |     v  |             |    v     |
    //   |     UNSUSPENDING  |            RESUMING    |
    //   |                   |                |       |
    //   |                   v                v       v
    //   +---------------- COMPLETING  <--------------+
    
    
    protected HttpConnection _connection;
    
    protected int _state;
    protected boolean _initial;
    protected boolean _resumed;
    protected boolean _timeout;
    
    protected long _timeoutMs;
    protected final Timeout.Task _timeoutTask;
    

    /* ------------------------------------------------------------ */
    public AsyncContextState(HttpConnection connection)
    {
        _connection=connection;
        _state=__IDLE;
        _initial=true;
        _resumed=false;
            
        _timeoutTask= new Timeout.Task()
        {
            public void expired()
            {
                AsyncContextState.this.expire();
            }
        };
    }

    /* ------------------------------------------------------------ */
    public void setConnection(HttpConnection connection)
    {
        _connection=connection;
    }

    /* ------------------------------------------------------------ */
    public long getTimeout()
    {
        return _timeoutMs;
    } 

    

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isInitial()
     */
    public boolean isInitial()
    {
        synchronized(this)
        {
            return _initial;
        }
    }
       
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isResumed()
     */
    public boolean isResumed()
    {
        synchronized(this)
        {
            return _resumed;
        }
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isSuspended()
     */
    public boolean isSuspended()
    {
        synchronized(this)
        {
            switch(_state)
            {
                case __IDLE:
                case __DISPATCHED:
                case __REDISPATCHED:
                    return false;
                case __SUSPENDING:
                case __RESUMING:
                case __COMPLETING:
                case __SUSPENDED:
                    return true;
                case __UNSUSPENDING:
                default:
                    return false;   
            }
        }
    }

    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isTimeout()
     */
    public boolean isTimeout()
    {
        synchronized(this)
        {
            return _timeout;
        }
    }
    

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#suspend()
     */
    protected void suspend()
    {
        long timeout = 60000L;
        suspend(timeout);
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return getStatusString();
    }

    /* ------------------------------------------------------------ */
    public String getStatusString()
    {
        synchronized (this)
        {
            return
            ((_state==__IDLE)?"IDLE":
                (_state==__DISPATCHED)?"DISPATCHED":
                    (_state==__SUSPENDING)?"SUSPENDING":
                        (_state==__SUSPENDED)?"SUSPENDED":
                            (_state==__RESUMING)?"RESUMING":
                                (_state==__UNSUSPENDING)?"UNSUSPENDING":
                                    (_state==__COMPLETING)?"COMPLETING":
                                        (_state==__REDISPATCHED)?"REDISPATCHED":
                                            ("???"+_state))+
            (_initial?",initial":"")+
            (_resumed?",resumed":"")+
            (_timeout?",timeout":"");
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#resume()
     */
    public void dispatch()
    {
        synchronized (this)
        {
            switch(_state)
            {
                case __DISPATCHED:
                case __REDISPATCHED:
                    throw new IllegalStateException(this.getStatusString());

                case __IDLE:
                    _initial=true;
                    _state=__DISPATCHED;
                    return;

                case __SUSPENDING:
                case __RESUMING:
                    throw new IllegalStateException(this.getStatusString());

                case __COMPLETING:
                    return;

                case __SUSPENDED:
                    cancelTimeout();
                case __UNSUSPENDING:
                    _state=__REDISPATCHED;
                    return;

                default:
                    throw new IllegalStateException(""+_state);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#suspend(long)
     */
    protected void suspend(long timeoutMs)
    {
        synchronized (this)
        {
            switch(_state)
            {
                case __DISPATCHED:
                case __REDISPATCHED:
                    _timeout=false;
                    _resumed=false;
                    _state=__SUSPENDING;
                    _timeoutMs = timeoutMs;
                    return;

                case __IDLE:
                    throw new IllegalStateException(this.getStatusString());

                case __SUSPENDING:
                case __RESUMING:
                    if (timeoutMs<_timeoutMs)
                        _timeoutMs = timeoutMs;
                    return;

                case __COMPLETING:
                case __SUSPENDED:
                case __UNSUSPENDING:
                    throw new IllegalStateException(this.getStatusString());

                default:
                    throw new IllegalStateException(""+_state);
            }

        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @return true if handling is complete, false if the request should 
     * be handled again (eg because of a resume)
     */
    public boolean undispatch()
    {
        synchronized (this)
        {
            switch(_state)
            {
                case __REDISPATCHED:
                    // TODO complete?
                    
                case __DISPATCHED:
                    _state=__IDLE;
                    return true;

                case __IDLE:
                    throw new IllegalStateException(this.getStatusString());

                case __SUSPENDING:
                    _initial=false;
                    _state=__SUSPENDED;
                    scheduleTimeout(); // could block and change state.
                    if (_state==__SUSPENDED || _state==__COMPLETING)
                        return true;
                    _initial=false;
                    _state=__REDISPATCHED;
                    return false; 

                case __RESUMING:
                    _initial=false;
                    _state=__REDISPATCHED;
                    return false; 

                case __COMPLETING:
                    _initial=false;
                    _state=__IDLE;
                    return true;

                case __SUSPENDED:
                case __UNSUSPENDING:
                default:
                    throw new IllegalStateException(this.getStatusString());
            }

        }
    }

    /* ------------------------------------------------------------ */
    protected void resume()
    {
        boolean dispatch=false;
        synchronized (this)
        {
            switch(_state)
            {
                case __REDISPATCHED:
                case __DISPATCHED:
                    _resumed=true;
                    return;
                    
                case __SUSPENDING:
                    _resumed=true;
                    _state=__RESUMING;
                    return;

                case __IDLE:
                case __RESUMING:
                case __COMPLETING:
                    return;
                    
                case __SUSPENDED:
                    dispatch=true;
                    _resumed=true;
                    _state=__UNSUSPENDING;
                    break;
                    
                case __UNSUSPENDING:
                    _resumed=true;
                    return;
                    
                default:
                    throw new IllegalStateException(this.getStatusString());
            }
        }
        
        if (dispatch)
        {
            cancelTimeout();
            scheduleDispatch();
        }
    }


    /* ------------------------------------------------------------ */
    protected void expire()
    {
        // just like resume, except don't set _resumed=true;
        boolean dispatch=false;
        synchronized (this)
        {
            switch(_state)
            {
                case __DISPATCHED:
                case __REDISPATCHED:
                    return;
                    
                case __IDLE:
                    throw new IllegalStateException(this.getStatusString());
                    
                case __SUSPENDING:
                    _timeout=true;
                    _state=__RESUMING;
                    cancelTimeout();
                    return;
                    
                case __RESUMING:
                    return;
                    
                case __COMPLETING:
                    return;
                    
                case __SUSPENDED:
                    dispatch=true;
                    _timeout=true;
                    _state=__UNSUSPENDING;
                    break;
                    
                case __UNSUSPENDING:
                    _timeout=true;
                    return;
                    
                default:
                    throw new IllegalStateException(this.getStatusString());
            }
        }
        if (dispatch)
        {
            scheduleDispatch();
        }
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#complete()
     */
    public void complete()
    {
        // just like resume, except don't set _resumed=true;
        boolean dispatch=false;
        synchronized (this)
        {
            switch(_state)
            {
                case __REDISPATCHED:
                    _state=__COMPLETING;
                    return;
                    
                case __DISPATCHED:
                    throw new IllegalStateException(this.getStatusString());
                    
                case __IDLE:
                    return;
                    
                case __SUSPENDING:
                    _state=__COMPLETING;
                    break;
                    
                case __RESUMING:
                    break;

                case __COMPLETING:
                    return;
                    
                case __SUSPENDED:
                    _state=__COMPLETING;
                    dispatch=true;
                    break;
                    
                case __UNSUSPENDING:
                    return;
                    
                default:
                    throw new IllegalStateException(this.getStatusString());
            }
        }
        
        if (dispatch)
        {
            cancelTimeout();
            scheduleDispatch();
        }
    }


    /* ------------------------------------------------------------ */
    public void reset()
    {
        synchronized (this)
        {
            _state=(_state==__SUSPENDED||_state==__IDLE)?__IDLE:__DISPATCHED;
            _resumed = false;
            _initial = true;
            _timeout = false;
            cancelTimeout();
        }
    }

    /* ------------------------------------------------------------ */
    protected void scheduleDispatch()
    {
        EndPoint endp=_connection.getEndPoint();
        if (!endp.isBlocking())
        {
            ((AsyncEndPoint)endp).dispatch();
        }
    }

    /* ------------------------------------------------------------ */
    protected void scheduleTimeout()
    {
        EndPoint endp=_connection.getEndPoint();
        if (endp.isBlocking())
        {
            synchronized(this)
            {
                long expire_at = System.currentTimeMillis()+_timeoutMs;
                long wait=_timeoutMs;
                while (_timeoutMs>0 && wait>0)
                {
                    try
                    {
                        this.wait(wait);
                    }
                    catch (InterruptedException e)
                    {
                        Log.ignore(e);
                    }
                    wait=expire_at-System.currentTimeMillis();
                }

                if (_timeoutMs>0 && wait<=0)
                    expire();
            }            
        }
        else
            _connection.scheduleTimeout(_timeoutTask,_timeoutMs);
    }

    /* ------------------------------------------------------------ */
    protected void cancelTimeout()
    {
        EndPoint endp=_connection.getEndPoint();
        if (endp.isBlocking())
        {
            synchronized(this)
            {
                _timeoutMs=0;
                this.notifyAll();
            }
        }
        else
            _connection.cancelTimeout(_timeoutTask);
    }

    /* ------------------------------------------------------------ */
    public boolean isCompleting()
    {
        return _state==__COMPLETING;
    }

    /* ------------------------------------------------------------ */
    public boolean isAsyncStarted()
    {
        return _state!=__IDLE && _state!=__DISPATCHED;
    }

    /* ------------------------------------------------------------ */
    public boolean shouldComplete()
    {
        switch(_state)
        {
            case __RESUMING:
            case __SUSPENDED:
            case __SUSPENDING:
            case __UNSUSPENDING:
                return false;
                
            default:
            return true;
        }
    }

    /* ------------------------------------------------------------ */
    public void forward()
    {
        resume();
    }

    /* ------------------------------------------------------------ */
    public void forward(ServletContext context, String path)
    {
        // TODO Auto-generated method stub
        resume();
    }

    /* ------------------------------------------------------------ */
    public void forward(String path)
    {
        // TODO Auto-generated method stub
        resume();
    }

    /* ------------------------------------------------------------ */
    public ServletRequest getRequest()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* ------------------------------------------------------------ */
    public ServletResponse getResponse()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* ------------------------------------------------------------ */
    public void start(Runnable run)
    {
        // TODO Auto-generated method stub
    }
    
}

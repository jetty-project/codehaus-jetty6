//========================================================================
//$Id: Timeout.java,v 1.3 2005/11/11 22:55:41 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.thread;

import org.mortbay.log.Log;


/* ------------------------------------------------------------ */
/** Timeout queue.
 * This class implements a timeout queue for timers that are at least as likely to be cancelled as they are to expire.
 * Unlike the util timeout class, the duration of the timouts is shared by all scheduled tasks and if the duration 
 * is changed, this affects all scheduled tasks.
 * <p>
 * The nested class Task should be extended by users of this class to obtain call back notification of 
 * expiries. 
 * <p>
 * This class is synchronized, but the callback to expired is not called within the synchronized scope.
 * 
 * @author gregw
 *
 */
public class Timeout
{
    private final Object _mutex;
    private long _duration;
    private long _now=System.currentTimeMillis();
    private Task _head=new Task();

    public Timeout()
    {
        _mutex=this;
        _head._timeout=this;
    }
    
    public Timeout(final Object mutex)
    {
        _mutex=mutex==null?this:mutex;
        _head._timeout=this;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the duration.
     */
    public long getDuration()
    {
        return _duration;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param duration The duration to set.
     */
    public void setDuration(long duration)
    {
        _duration = duration;
    }

    /* ------------------------------------------------------------ */
    public void setNow()
    {
        _now=System.currentTimeMillis();
    }
    
    /* ------------------------------------------------------------ */
    public long getNow()
    {
        return _now;
    }

    /* ------------------------------------------------------------ */
    public void setNow(long now)
    {
        _now=now;
    }

    /* ------------------------------------------------------------ */
    public void tick()
    {
        long expiry = _now-_duration;
        
        while (true)
        {
            Task expired=null;
            synchronized (_mutex)
            {
                if (_head._next!=_head && _head._next._timestamp<=expiry)
                {
                    expired=_head._next;
                    expired.unlink();
                    expired._expired=true;
                }
            }
            if (expired!=null)
            {
                try
                {
                    expired.expired();
                }
                catch(Throwable th)
                {
                    Log.warn(Log.EXCEPTION,th);
                }
            }
            else
                break;
        }
    }

    /* ------------------------------------------------------------ */
    public void schedule(Task task)
    {
        schedule(task,0L);
    }
    
    /* ------------------------------------------------------------ */
    public void schedule(Task task,long delay)
    {
        if (task._timeout!=null && task._timeout!=this)
        {
            task.cancel();
        }
        
        synchronized (_mutex)
        {
            if (task._timestamp!=0)
            {
                task.unlink();
                task._timestamp=0;
            }
            task._expired=false;
            task._delay=delay;
            task._timestamp = _now+delay;

            Task last=_head._prev;
            while (last!=_head)
            {
                if (last._timestamp <= task._timestamp)
                    break;
                last=last._prev;
            }
            last.setNext(task);
        }
    }


    /* ------------------------------------------------------------ */
    public void cancelAll()
    {
        synchronized (_mutex)
        {
            _head._next=_head._prev=_head;
            // TODO call a cancel callback?
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isEmpty()
    {
        synchronized (_mutex)
        {
            return _head._next==_head;
        }
    }

    /* ------------------------------------------------------------ */
    public long getTimeToNext()
    {
        synchronized (_mutex)
        {
            if (_head._next==_head)
                return -1;
            long to_next = _duration+_head._next._timestamp-_now;
            return to_next<0?0:to_next;
        }
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());

        synchronized (_mutex)
        {
            Task task = _head._next;
            while (task!=_head)
            {
                buf.append("-->");
                buf.append(task);
                task=task._next;
            }
        }
        
        return buf.toString();
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** Task.
     * The base class for scheduled timeouts.  This class should be
     * extended to implement the {@link #expire()} or {@link #expired()} method, which is called if the
     * timeout expires.
     * 
     * @author gregw
     *
     */
    public static class Task
    {
        Task _next;
        Task _prev;
        Timeout _timeout;
        long _delay;
        long _timestamp=0;
        boolean _expired=false;

        /* ------------------------------------------------------------ */
        public Task()
        {
            _next=_prev=this;
        }

        /* ------------------------------------------------------------ */
        public long getTimestamp()
        {
            return _timestamp;
        }

        /* ------------------------------------------------------------ */
        public long getAge()
        {
            Timeout t = _timeout;
            if (t!=null && t._now!=0 && _timestamp!=0)
                return t._now-_timestamp;
            return 0;
        }

        /* ------------------------------------------------------------ */
        private void unlink()
        {
            _next._prev=_prev;
            _prev._next=_next;
            _next=_prev=this;
            _timeout=null;
            _expired=false;
        }

        /* ------------------------------------------------------------ */
        private void setNext(Task task)
        {
            if (_timeout==null || 
                task._timeout!=null && task._timeout!=_timeout ||    
                task._next!=task)
                throw new IllegalStateException();
            Task next_next = _next;
            _next._prev=task;
            _next=task;
            _next._next=next_next;
            _next._prev=this;   
            _next._timeout=_timeout;
        }
        
        /* ------------------------------------------------------------ */
        /** Cancel the task.
         * Remove the task from the timeout.
         */
        public void cancel()
        {
            if (_timeout!=null)
            {
                synchronized (_timeout._mutex)
                {
                    _timestamp=0;
                    unlink();
                }
            }
        }
        
        /* ------------------------------------------------------------ */
        public boolean isExpired() { return _expired; }

        /* ------------------------------------------------------------ */
        public boolean isScheduled() { return _next!=this; }
        
        /* ------------------------------------------------------------ */
        /** Expire task.
         * This method is called when the timeout expires. It is called 
         * outside of any synchronization scope and may be delayed. 
         * 
         * @see #expire() For a synchronized callback.
         */
        public void expired(){}

    }

}

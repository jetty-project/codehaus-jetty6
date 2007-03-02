// ========================================================================
// Copyright 1996-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mortbay.log.Log;
import org.mortbay.util.LazyList;


/* ------------------------------------------------------------ */
/** An in-memory implementation of SessionManager.
 *
 * @author Greg Wilkins (gregw)
 */
public class HashSessionManager extends AbstractSessionManager
{
    private int _scavengePeriodMs=30000;
    private Thread _scavenger=null;
    protected Map _sessions;
    
    /* ------------------------------------------------------------ */
    public HashSessionManager()
    {
        super();
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#doStart()
     */
    public void doStart() throws Exception
    {
        _sessions=new HashMap();
        super.doStart();

        // Start the session scavenger if we haven't already
        getSessionHandler().getServer().getThreadPool().dispatch(new SessionScavenger());

    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#doStop()
     */
    public void doStop() throws Exception
    {
        super.doStop();
        _sessions.clear();
        _sessions=null;

        // stop the scavenger
        Thread scavenger=_scavenger;
        _scavenger=null;
        if (scavenger!=null)
            scavenger.interrupt();

    }

    /* ------------------------------------------------------------ */
    /**
     * @return seconds
     */
    public int getScavengePeriod()
    {
        return _scavengePeriodMs/1000;
    }

    
    /* ------------------------------------------------------------ */
    public Map getSessionMap()
    {
        return Collections.unmodifiableMap(_sessions);
    }


    /* ------------------------------------------------------------ */
    public int getSessions()
    {
        return _sessions.size();
    }


    /* ------------------------------------------------------------ */
    public void setMaxInactiveInterval(int seconds)
    {
        super.setMaxInactiveInterval(seconds);
        if (_dftMaxIdleSecs>0&&_scavengePeriodMs>_dftMaxIdleSecs*1000)
            setScavengePeriod((_dftMaxIdleSecs+9)/10);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param seconds
     */
    public void setScavengePeriod(int seconds)
    {
        if (seconds==0)
            seconds=60;

        int old_period=_scavengePeriodMs;
        int period=seconds*1000;
        if (period>60000)
            period=60000;
        if (period<1000)
            period=1000;

        if (period!=old_period)
        {
            synchronized (this)
            {
                _scavengePeriodMs=period;
                if (_scavenger!=null)
                    _scavenger.interrupt();
            }
        }
    }
    
    /* -------------------------------------------------------------- */
    /**
     * Find sessions that have timed out and invalidate them. This runs in the
     * SessionScavenger thread.
     */
    private void scavenge()
    {
        //don't attempt to scavenge if we are shutting down
        if (isStopping() || isStopped())
            return;
        
        Thread thread=Thread.currentThread();
        ClassLoader old_loader=thread.getContextClassLoader();
        try
        {
            if (_loader!=null)
                thread.setContextClassLoader(_loader);

            long now=System.currentTimeMillis();

            // Since Hashtable enumeration is not safe over deletes,
            // we build a list of stale sessions, then go back and invalidate
            // them
            Object stale=null;

            synchronized (HashSessionManager.this)
            {
                // For each session
                for (Iterator i=_sessions.values().iterator(); i.hasNext();)
                {
                    Session session=(Session)i.next();
                    long idleTime=session._maxIdleMs;
                    if (idleTime>0&&session._accessed+idleTime<now)
                    {
                        // Found a stale session, add it to the list
                        stale=LazyList.add(stale,session);
                    }
                }
            }

            // Remove the stale sessions
            for (int i=LazyList.size(stale); i-->0;)
            {
                // check it has not been accessed in the meantime
                Session session=(Session)LazyList.get(stale,i);
                long idleTime=session._maxIdleMs;
                if (idleTime>0&&session._accessed+idleTime<System.currentTimeMillis())
                {
                    session.invalidate();
                    int nbsess=this._sessions.size();
                    if (nbsess<this._minSessions)
                        this._minSessions=nbsess;
                }
            }
        }
        finally
        {
            thread.setContextClassLoader(old_loader);
        }
    }
    
    /* ------------------------------------------------------------ */
    protected void addSession(AbstractSessionManager.Session session)
    {
        _sessions.put(session.getClusterId(),session);
    }
    
    /* ------------------------------------------------------------ */
    protected AbstractSessionManager.Session getSession(String idInCluster)
    {
        return (Session)_sessions.get(idInCluster);
    }

    /* ------------------------------------------------------------ */
    protected void invalidateSessions()
    {
        // Invalidate all sessions to cause unbind events
        ArrayList sessions=new ArrayList(_sessions.values());
        for (Iterator i=sessions.iterator(); i.hasNext();)
        {
            Session session=(Session)i.next();
            session.invalidate();
        }
        _sessions.clear();
        
    }

    /* ------------------------------------------------------------ */
    protected AbstractSessionManager.Session newSession(HttpServletRequest request)
    {
        return new Session(request);
    }
    
    /* ------------------------------------------------------------ */
    protected void removeSession(String idInCluster)
    {
        _sessions.remove(idInCluster);
    }
    
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class Session extends AbstractSessionManager.Session
    {
        /* ------------------------------------------------------------ */
        private static final long serialVersionUID=-2134521374206116367L;

        /* ------------------------------------------------------------- */
        protected Session(HttpServletRequest request)
        {
            super(request);
        }
        
        /* ------------------------------------------------------------- */
        public void setMaxInactiveInterval(int secs)
        {
            super.setMaxInactiveInterval(secs);
            if (_maxIdleMs>0&&(_maxIdleMs/10)<_scavengePeriodMs)
                HashSessionManager.this.setScavengePeriod((secs+9)/10);
        }
        
        /* ------------------------------------------------------------ */
        protected Map newAttributeMap()
        {
            return new HashMap(3);
        }
    }
    

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* -------------------------------------------------------------- */
    /** SessionScavenger is a background thread that kills off old sessions */
    class SessionScavenger implements Runnable
    {
        public void run()
        {
            _scavenger=Thread.currentThread();
            String name=Thread.currentThread().getName();
            if (_context!=null)
                Thread.currentThread().setName(name+" - Invalidator - "+_context.getContextPath());
            int period=-1;
            try
            {
                do
                {
                    try
                    {
                        if (period!=_scavengePeriodMs)
                        {
                            if (Log.isDebugEnabled())
                                Log.debug("Session scavenger period = "+_scavengePeriodMs/1000+"s");
                            period=_scavengePeriodMs;
                        }
                        Thread.sleep(period>1000?period:1000);
                        HashSessionManager.this.scavenge();
                    }
                    catch (InterruptedException ex)
                    {
                        continue;
                    }
                    catch (Error e)
                    {
                        Log.warn(Log.EXCEPTION,e);
                    }
                    catch (Exception e)
                    {
                        Log.warn(Log.EXCEPTION,e);
                    }
                }
                while (isStarted());
            }
            finally
            {
                HashSessionManager.this._scavenger=null;
                String exit="Session scavenger exited";
                if (isStarted())
                    Log.warn(exit);
                else
                    Log.debug(exit);
                Thread.currentThread().setName(name);
            }
        }

    } // SessionScavenger

    
}

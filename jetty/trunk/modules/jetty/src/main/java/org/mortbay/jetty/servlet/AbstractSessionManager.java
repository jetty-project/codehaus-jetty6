// ========================================================================
// Copyright 199-2004 Mort Bay Consulting Pty. Ltd.
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
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.HttpOnlyCookie;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.SessionIdManager;
import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

/* ------------------------------------------------------------ */
/**
 * An Abstract implementation of SessionManager. The partial implementation of
 * SessionManager interface provides the majority of the handling required to
 * implement a SessionManager. Concrete implementations of SessionManager based
 * on AbstractSessionManager need only implement the newSession method to return
 * a specialized version of the Session inner class that provides an attribute
 * Map.
 * <p>
 * If the property
 * org.mortbay.jetty.servlet.AbstractSessionManager.23Notifications is set to
 * true, the 2.3 servlet spec notification style will be used.
 * <p>
 * 
 * @author Greg Wilkins (gregw)
 */
public abstract class AbstractSessionManager extends AbstractLifeCycle implements SessionManager
{
    private static final HttpSessionContext __nullSessionContext=new NullSessionContext();

    /* ------------------------------------------------------------ */
    public final static int __distantFuture=60*60*24*7*52*20;

    /* ------------------------------------------------------------ */
    // Setting of max inactive interval for new sessions
    // -1 means no timeout
    private int _dftMaxIdleSecs=-1;
    protected boolean _httpOnly=false;
    protected int _maxSessions=0;
    protected int _minSessions=0;
    private int _scavengePeriodMs=30000;
    private SessionIdManager _sessionIdManager;
    private SessionHandler _sessionHandler;

    private Thread _scavenger=null;
    protected boolean _secureCookies=false;
    protected Object _sessionAttributeListeners;
    protected Object _sessionListeners;
    protected Map _sessions;
    private boolean _usingCookies=true;
    protected ClassLoader _loader;
    protected ContextHandler.Context _context;
    protected String _sessionCookie;
    protected String _sessionURL;
    protected String _sessionURLPrefix;
    protected String _sessionDomain;
    protected String _sessionPath;
    protected int _maxSessionCookieAge=Integer.parseInt(SessionManager.__DefaultMaxAge);
    protected boolean _maxAgeSet=false;

    /* ------------------------------------------------------------ */
    public AbstractSessionManager()
    {
    }

    public void setSessionCookie(String cookieName)
    {
        _sessionCookie=cookieName;
    }

    public String getSessionCookie()
    {
        return _sessionCookie;
    }

    public void setSessionURL(String url)
    {
        _sessionURL=url;
    }

    public String getSessionURL()
    {
        return _sessionURL;
    }

    public String getSessionURLPrefix()
    {
        return _sessionURLPrefix;
    }

    public void setSessionDomain(String domain)
    {
        _sessionDomain=domain;
    }

    public String getSessionDomain()
    {
        return _sessionDomain;
    }

    public void setSessionPath(String path)
    {
        _sessionPath=path;
    }

    public String getSessionPath()
    {
        return _sessionPath;
    }

    public void setMaxSessionCookieAge(int maxCookieAge)
    {
        _maxSessionCookieAge=maxCookieAge;
        _maxAgeSet=true;
    }

    public int getMaxCookieAge()
    {
        return _maxSessionCookieAge;
    }

    /* ------------------------------------------------------------ */
    public void clearEventListeners()
    {
        _sessionAttributeListeners=null;
        _sessionListeners=null;
    }

    /* ------------------------------------------------------------ */
    public void addEventListener(EventListener listener)
    {
        if (listener instanceof HttpSessionAttributeListener)
            _sessionAttributeListeners=LazyList.add(_sessionAttributeListeners,listener);
        if (listener instanceof HttpSessionListener)
            _sessionListeners=LazyList.add(_sessionListeners,listener);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the metaManager used for cross context session management
     */
    public SessionIdManager getMetaManager()
    {
        return _sessionIdManager;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param metaManager
     *            The metaManager used for cross context session management.
     */
    public void setMetaManager(SessionIdManager metaManager)
    {
        _sessionIdManager=metaManager;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the httpOnly.
     */
    public boolean getHttpOnly()
    {
        return _httpOnly;
    }

    /* ------------------------------------------------------------ */
    public HttpSession getHttpSession(String id)
    {
        synchronized (this)
        {
            return (HttpSession)_sessions.get(id);
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @return seconds
     */
    public int getMaxInactiveInterval()
    {
        return _dftMaxIdleSecs;
    }

    /* ------------------------------------------------------------ */
    public int getMaxSessions()
    {
        return _maxSessions;
    }

    /* ------------------------------------------------------------ */
    public int getMinSessions()
    {
        return _minSessions;
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
    /**
     * @return Returns the secureCookies.
     */
    public boolean getSecureCookies()
    {
        return _secureCookies;
    }

    /* ------------------------------------------------------------ */
    public Cookie getSessionCookie(HttpSession session, String contextPath, boolean requestIsSecure)
    {
        if (isUsingCookies())
        {
            Cookie cookie=getHttpOnly()?new HttpOnlyCookie(_sessionCookie,session.getId()):new Cookie(_sessionCookie,session.getId());

            cookie.setPath((contextPath==null||contextPath.length()==0)?"/":contextPath);
            cookie.setMaxAge(_maxSessionCookieAge);
            cookie.setSecure(requestIsSecure&&getSecureCookies());

            // set up the overrides
            if (_sessionDomain!=null)
                cookie.setDomain(_sessionDomain);
            if (_sessionPath!=null)
                cookie.setPath(_sessionPath);

            return cookie;
        }
        return null;
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
    /**
     * @return Returns the usingCookies.
     */
    public boolean isUsingCookies()
    {
        return _usingCookies;
    }

    /* ------------------------------------------------------------ */
    /**
     * Create a new HttpSession for a request
     */
    public HttpSession newHttpSession(HttpServletRequest request)
    {
        Session session=newSession(request);
        session.setMaxInactiveInterval(_dftMaxIdleSecs);
        addSession(session,true);
        return session;
    }

    /* ------------------------------------------------------------ */
    /**
     * Add the session Registers the session with this manager and registers the
     * session ID with the sessionIDManager;
     */
    protected void addSession(Session session, boolean created)
    {
        synchronized (_sessionIdManager)
        {
            _sessionIdManager.addSession(session);
            synchronized (this)
            {
                _sessions.put(session.getId(),session);
                if (_sessions.size()>this._maxSessions)
                    this._maxSessions=_sessions.size();
            }
        }
        
        if (created && _sessionListeners!=null)
        {
            HttpSessionEvent event=new HttpSessionEvent(session);
            for (int i=0; i<LazyList.size(_sessionListeners); i++)
                ((HttpSessionListener)LazyList.get(_sessionListeners,i)).sessionCreated(event);
        }
    }
    

    /* ------------------------------------------------------------ */
    /** Remove session from manager 
     * @param session The session to remove
     * @param invalidate True if {@link HttpSessionListener#sessionDestroyed(HttpSessionEvent)} and
     * {@link SessionIdManager#invalidateAll(String)} should be called.
     */
    protected void removeSession(Session session, boolean invalidate)
    {
        if (invalidate && _sessionListeners!=null)
        {
            HttpSessionEvent event=new HttpSessionEvent(session);
            for (int i=LazyList.size(_sessionListeners); i-->0;)
                ((HttpSessionListener)LazyList.get(_sessionListeners,i)).sessionDestroyed(event);
        }
        
        // Remove session from context and global maps
        synchronized (_sessionIdManager)
        {
            String id=session.getId();
            _sessionIdManager.removeSession(session);
            
            synchronized (this)
            {
                _sessions.remove(id);
            }
            if (invalidate)
                _sessionIdManager.invalidateAll(id);
        }
    }

    /* ------------------------------------------------------------ */
    protected abstract Session newSession(HttpServletRequest request);

    /* ------------------------------------------------------------ */
    public void removeEventListener(EventListener listener)
    {
        if (listener instanceof HttpSessionAttributeListener)
            _sessionAttributeListeners=LazyList.remove(_sessionAttributeListeners,listener);
        if (listener instanceof HttpSessionListener)
            _sessionListeners=LazyList.remove(_sessionListeners,listener);
    }

    /* ------------------------------------------------------------ */
    public void resetStats()
    {
        _minSessions=_sessions.size();
        _maxSessions=_sessions.size();
    }

    /* -------------------------------------------------------------- */
    /**
     * Find sessions that have timed out and invalidate them. This runs in the
     * SessionScavenger thread.
     */
    private void scavenge()
    {
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

            synchronized (AbstractSessionManager.this)
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
    /**
     * @param httpOnly
     *            The httpOnly to set.
     */
    public void setHttpOnly(boolean httpOnly)
    {
        _httpOnly=httpOnly;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param seconds
     */
    public void setMaxInactiveInterval(int seconds)
    {
        _dftMaxIdleSecs=seconds;
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

    /* ------------------------------------------------------------ */
    /**
     * @param secureCookies
     *            The secureCookies to set.
     */
    public void setSecureCookies(boolean secureCookies)
    {
        _secureCookies=secureCookies;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param usingCookies
     *            The usingCookies to set.
     */
    public void setUsingCookies(boolean usingCookies)
    {
        _usingCookies=usingCookies;
    }

    /* ------------------------------------------------------------ */
    public void doStart() throws Exception
    {
        _context=ContextHandler.getCurrentContext();
        _loader=Thread.currentThread().getContextClassLoader();

        if (_sessions==null)
            _sessions=new HashMap();

        if (_sessionIdManager==null)
        {
            Server server=getSessionHandler().getServer();
            synchronized (server)
            {
                _sessionIdManager=server.getSessionIdManager();
                if (_sessionIdManager==null)
                {
                    _sessionIdManager=new HashSessionIdManager();
                    server.setSessionIdManager(_sessionIdManager);
                }
            }
        }
        if (!_sessionIdManager.isStarted())
            _sessionIdManager.start();

        // set up the cookie name if it isn't already
        if (_sessionCookie==null)
        {
            // try the context initParams then the system properties using the
            // defaults
            String str=null;
            if (_context!=null)
                str=_context.getInitParameter(SessionManager.__SessionCookieProperty);
            _sessionCookie=(str==null?SessionManager.__SessionCookieSystemProperty:str);
        }

        // set up the parameter name of the session for urls
        if (_sessionURL==null)
        {
            String str=null;
            if (_context!=null)
                str=_context.getInitParameter(SessionManager.__SessionURLProperty);
            _sessionURL=(str==null?SessionManager.__SessionURLSystemProperty:str);
        }
        // set up the whole session url
        _sessionURLPrefix=";"+_sessionURL+"=";

        // set up the max session cookie age if it isn't already
        if (!_maxAgeSet)
        {
            String str=null;
            if (_context!=null)
                str=_context.getInitParameter(SessionManager.__MaxAgeProperty);
            str=(str==null?SessionManager.__MaxAgeSystemProperty:str);
            _maxSessionCookieAge=Integer.parseInt(str.trim());
            _maxAgeSet=true;
        }

        // set up the session domain if it isn't already
        if (_sessionDomain==null)
        {
            // only try the context initParams
            if (_context!=null)
                _sessionDomain=_context.getInitParameter(SessionManager.__SessionDomainProperty);
        }

        // set up the sessionPath if it isn't already
        if (_sessionPath==null)
        {
            // only the context initParams
            if (_context!=null)
                _sessionPath=_context.getInitParameter(SessionManager.__SessionPathProperty);
        }

        super.doStart();

        // Start the session scavenger if we haven't already
        _sessionHandler.getServer().getThreadPool().dispatch(new SessionScavenger());
    }

    /* ------------------------------------------------------------ */
    public void doStop() throws Exception
    {
        super.doStop();

        // Invalidate all sessions to cause unbind events
        ArrayList sessions=new ArrayList(_sessions.values());
        for (Iterator i=sessions.iterator(); i.hasNext();)
        {
            Session session=(Session)i.next();
            session.invalidate();
        }
        _sessions.clear();

        // stop the scavenger
        Thread scavenger=_scavenger;
        _scavenger=null;
        if (scavenger!=null)
            scavenger.interrupt();

        _loader=null;
    }

    /* ------------------------------------------------------------ */
    public void access(HttpSession session)
    {
        ((Session)session).access();
    }

    /* ------------------------------------------------------------ */
    public void complete(HttpSession session)
    {
    }

    /* ------------------------------------------------------------ */
    public boolean isValid(HttpSession session)
    {
        return ((Session)session).isValid();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the sessionHandler.
     */
    public SessionHandler getSessionHandler()
    {
        return _sessionHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param sessionHandler
     *            The sessionHandler to set.
     */
    public void setSessionHandler(SessionHandler sessionHandler)
    {
        _sessionHandler=sessionHandler;
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public abstract class Session implements HttpSession
    {
        String _id;
        long _created;
        long _accessed;
        boolean _invalid=false;
        long _maxIdleMs=_dftMaxIdleSecs*1000;
        boolean _newSession=true;
        Map _values;

        /* ------------------------------------------------------------- */
        protected Session(HttpServletRequest request)
        {
            _id=_sessionIdManager.newSessionId(request,_created);
            _created=System.currentTimeMillis();
            _accessed=_created;
            if (_dftMaxIdleSecs>=0)
                _maxIdleMs=_dftMaxIdleSecs*1000;
        }

        /* ------------------------------------------------------------ */
        void access()
        {
            _newSession=false;
            _accessed=System.currentTimeMillis();
        }

        /* ------------------------------------------------------------- */
        /** If value implements HttpSessionBindingListener, call valueBound() */
        private void bindValue(java.lang.String name, Object value)
        {
            if (value!=null&&value instanceof HttpSessionBindingListener)
                ((HttpSessionBindingListener)value).valueBound(new HttpSessionBindingEvent(this,name));
        }

        /* ------------------------------------------------------------ */
        public synchronized Object getAttribute(String name)
        {
            if (_invalid)
                throw new IllegalStateException();
            if (_values==null)
                return null;
            return _values.get(name);
        }

        /* ------------------------------------------------------------ */
        public synchronized Enumeration getAttributeNames()
        {
            if (_invalid)
                throw new IllegalStateException();
            List names=_values==null?Collections.EMPTY_LIST:new ArrayList(_values.keySet());
            return Collections.enumeration(names);
        }

        /* ------------------------------------------------------------- */
        public long getCreationTime() throws IllegalStateException
        {
            if (_invalid)
                throw new IllegalStateException();
            return _created;
        }

        /* ------------------------------------------------------------- */
        public String getId() throws IllegalStateException
        {
            return _id;
        }

        /* ------------------------------------------------------------- */
        public long getLastAccessedTime() throws IllegalStateException
        {
            if (_invalid)
                throw new IllegalStateException();
            return _accessed;
        }

        /* ------------------------------------------------------------- */
        public int getMaxInactiveInterval()
        {
            if (_invalid)
                throw new IllegalStateException();
            return (int)(_maxIdleMs/1000);
        }

        /* ------------------------------------------------------------ */
        /*
         * @see javax.servlet.http.HttpSession#getServletContext()
         */
        public ServletContext getServletContext()
        {
            return _context;
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated
         */
        public HttpSessionContext getSessionContext() throws IllegalStateException
        {
            if (_invalid)
                throw new IllegalStateException();
            return __nullSessionContext;
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated As of Version 2.2, this method is replaced by
         *             {@link #getAttribute}
         */
        public Object getValue(String name) throws IllegalStateException
        {
            return getAttribute(name);
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated As of Version 2.2, this method is replaced by
         *             {@link #getAttributeNames}
         */
        public synchronized String[] getValueNames() throws IllegalStateException
        {
            if (_invalid)
                throw new IllegalStateException();
            if (_values==null)
                return new String[0];
            String[] a=new String[_values.size()];
            return (String[])_values.keySet().toArray(a);
        }

        /* ------------------------------------------------------------- */
        public void invalidate() throws IllegalStateException
        {
            if (Log.isDebugEnabled())
                Log.debug("Invalidate session "+getId());
            try
            {
                // remove session from context and invalidate other sessions with same ID.
                removeSession(this,true);
                
                // Notify listeners and unbind values
                synchronized (this)
                {
                    if (_invalid)
                        throw new IllegalStateException();

                    if (_values!=null)
                    {
                        Iterator iter=_values.keySet().iterator();
                        while (iter.hasNext())
                        {
                            String key=(String)iter.next();
                            Object value=_values.get(key);
                            iter.remove();
                            unbindValue(key,value);

                            if (_sessionAttributeListeners!=null)
                            {
                                HttpSessionBindingEvent event=new HttpSessionBindingEvent(this,key,value);

                                for (int i=0; i<LazyList.size(_sessionAttributeListeners); i++)
                                    ((HttpSessionAttributeListener)LazyList.get(_sessionAttributeListeners,i)).attributeRemoved(event);
                            }
                        }
                    }
                }
            }
            finally
            {
                // mark as invalid
                _invalid=true;
            }
        }

        /* ------------------------------------------------------------- */
        public boolean isNew() throws IllegalStateException
        {
            if (_invalid)
                throw new IllegalStateException();
            return _newSession;
        }

        /* ------------------------------------------------------------ */
        boolean isValid()
        {
            return !_invalid;
        }

        /* ------------------------------------------------------------ */
        protected abstract Map newAttributeMap();

        /* ------------------------------------------------------------- */
        /**
         * @deprecated As of Version 2.2, this method is replaced by
         *             {@link #setAttribute}
         */
        public void putValue(java.lang.String name, java.lang.Object value) throws IllegalStateException
        {
            setAttribute(name,value);
        }

        /* ------------------------------------------------------------ */
        public synchronized void removeAttribute(String name)
        {
            if (_invalid)
                throw new IllegalStateException();
            if (_values==null)
                return;

            Object old=_values.remove(name);
            if (old!=null)
            {
                unbindValue(name,old);
                if (_sessionAttributeListeners!=null)
                {
                    HttpSessionBindingEvent event=new HttpSessionBindingEvent(this,name,old);

                    for (int i=0; i<LazyList.size(_sessionAttributeListeners); i++)
                        ((HttpSessionAttributeListener)LazyList.get(_sessionAttributeListeners,i)).attributeRemoved(event);
                }
            }
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated As of Version 2.2, this method is replaced by
         *             {@link #removeAttribute}
         */
        public void removeValue(java.lang.String name) throws IllegalStateException
        {
            removeAttribute(name);
        }

        /* ------------------------------------------------------------ */
        public synchronized void setAttribute(String name, Object value)
        {
            if (value==null)
            {
                removeAttribute(name);
                return;
            }

            if (_invalid)
                throw new IllegalStateException();
            if (_values==null)
                _values=newAttributeMap();
            Object oldValue=_values.put(name,value);

            if (value==null||!value.equals(oldValue))
            {
                unbindValue(name,oldValue);
                bindValue(name,value);

                if (_sessionAttributeListeners!=null)
                {
                    HttpSessionBindingEvent event=new HttpSessionBindingEvent(this,name,oldValue==null?value:oldValue);

                    for (int i=0; i<LazyList.size(_sessionAttributeListeners); i++)
                    {
                        HttpSessionAttributeListener l=(HttpSessionAttributeListener)LazyList.get(_sessionAttributeListeners,i);

                        if (oldValue==null)
                            l.attributeAdded(event);
                        else if (value==null)
                            l.attributeRemoved(event);
                        else
                            l.attributeReplaced(event);
                    }
                }
            }
        }

        /* ------------------------------------------------------------- */
        public void setMaxInactiveInterval(int secs)
        {
            _maxIdleMs=(long)secs*1000;
            if (_maxIdleMs>0&&(_maxIdleMs/10)<_scavengePeriodMs)
                AbstractSessionManager.this.setScavengePeriod((secs+9)/10);
        }

        /* ------------------------------------------------------------- */
        /** If value implements HttpSessionBindingListener, call valueUnbound() */
        private void unbindValue(java.lang.String name, Object value)
        {
            if (value!=null&&value instanceof HttpSessionBindingListener)
                ((HttpSessionBindingListener)value).valueUnbound(new HttpSessionBindingEvent(this,name));
        }

        /* ------------------------------------------------------------- */
        public String toString()
        {
            return this.getClass().getName()+":"+getId()+"@"+hashCode();
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
                        AbstractSessionManager.this.scavenge();
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
                AbstractSessionManager.this._scavenger=null;
                String exit="Session scavenger exited";
                if (isStarted())
                    Log.warn(exit);
                else
                    Log.debug(exit);
                Thread.currentThread().setName(name);
            }
        }

    } // SessionScavenger

    /* ------------------------------------------------------------ */
    /**
     * Null returning implementation of HttpSessionContext
     * 
     * @author Greg Wilkins (gregw)
     */
    public static class NullSessionContext implements HttpSessionContext
    {
        /* ------------------------------------------------------------ */
        private NullSessionContext()
        {
        }

        /* ------------------------------------------------------------ */
        /**
         * @deprecated From HttpSessionContext
         */
        public Enumeration getIds()
        {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        /* ------------------------------------------------------------ */
        /**
         * @deprecated From HttpSessionContext
         */
        public HttpSession getSession(String id)
        {
            return null;
        }
    }

}

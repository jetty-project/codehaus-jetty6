// ========================================================================
// $Id: AbstractSessionManager.java,v 1.5 2005/11/11 22:55:39 gregwilkins Exp $
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
import java.util.Random;

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

import org.mortbay.jetty.HttpOnlyCookie;
import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.log.Log;
import org.mortbay.thread.AbstractLifeCycle;
import org.mortbay.util.LazyList;
import org.mortbay.util.MultiMap;


/* ------------------------------------------------------------ */
/** An Abstract implementation of SessionManager.
 * The partial implementation of SessionManager interface provides
 * the majority of the handling required to implement a
 * SessionManager.  Concrete implementations of SessionManager based
 * on AbstractSessionManager need only implement the newSession method
 * to return a specialized version of the Session inner class that
 * provides an attribute Map.
 * <p>
 * If the property
 * org.mortbay.jetty.servlet.AbstractSessionManager.23Notifications is set to
 * true, the 2.3 servlet spec notification style will be used.
 * <p>
 * @version $Id: AbstractSessionManager.java,v 1.5 2005/11/11 22:55:39 gregwilkins Exp $
 * @author Greg Wilkins (gregw)
 */
public abstract class AbstractSessionManager extends AbstractLifeCycle implements SessionManager
{
    private static final HttpSessionContext __nullSessionContext = new NullSessionContext();
    
    
    /* ------------------------------------------------------------ */
    public final static int __distantFuture = 60*60*24*7*52*20;
    private final static String __NEW_SESSION_ID="org.mortbay.jetty.newSessionId";
    
    /* ------------------------------------------------------------ */
    // Setting of max inactive interval for new sessions
    // -1 means no timeout
    private int _dftMaxIdleSecs = -1;
    protected boolean _httpOnly=false;
    protected int _maxSessions = 0;
    protected int _minSessions = 0;
    protected transient Random _random;
    private int _scavengePeriodMs = 30000;
    private MetaManager _metaManager;
    
    private transient SessionScavenger _scavenger = null;
    protected boolean _secureCookies=false;
    protected transient Object _sessionAttributeListeners;
    protected transient Object _sessionListeners;
    protected transient Map _sessions;
    private boolean _usingCookies=true;
    private String _workerName ;
    protected transient ClassLoader _loader;
    protected transient ContextHandler.Context _context;
    
    /* ------------------------------------------------------------ */
    public AbstractSessionManager()
    {
        this(null);
    }
    
    /* ------------------------------------------------------------ */
    public AbstractSessionManager(Random random)
    {
        _random=random;
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
    public MetaManager getMetaManager() {
        return _metaManager;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param metaManager The metaManager used for cross context session management.
     */
    public void setMetaManager(MetaManager metaManager) {
        _metaManager = metaManager;
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
        synchronized(this)
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
    public int getMaxSessions ()
    {
        return _maxSessions;
    }
    
    /* ------------------------------------------------------------ */
    public int getMinSessions ()
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
    public Cookie getSessionCookie(HttpSession session,String contextPath, boolean requestIsSecure)
    {
        if (isUsingCookies())
        {
            Cookie cookie = getHttpOnly()
                ?new HttpOnlyCookie(SessionManager.__SessionCookie,session.getId())
                :new Cookie(SessionManager.__SessionCookie,session.getId());  
         
            cookie.setPath(contextPath==null?"/":contextPath);
            cookie.setMaxAge(-1);
            cookie.setSecure(requestIsSecure && getSecureCookies());
            
            if (_context!=null)
            {
                String domain=_context.getInitParameter(SessionManager.__SessionDomain);
                String maxAge=_context.getInitParameter(SessionManager.__MaxAge);
                String path=_context.getInitParameter(SessionManager.__SessionPath);
                
                if (path!=null)
                    cookie.setPath(path);
                if (domain!=null)
                    cookie.setDomain(domain);       
                if (maxAge!=null)
                    cookie.setMaxAge(Integer.parseInt(maxAge));
            }
            
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
    public int getSessions ()
    {
        return _sessions.size ();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the workname.
     * If set, the workername is dot appended to the session ID
     * and can be used to assist session affinity in a load balancer.
     * @return String or null
     */
    public String getWorkerName()
    {
        return _workerName;
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
    public HttpSession newHttpSession(HttpServletRequest request)
    {
        Session session = newSession(request);
        session.setMaxInactiveInterval(_dftMaxIdleSecs);
        
        synchronized(_metaManager)
        {
            synchronized(this)
            {
                _sessions.put(session.getId(),session);
                _metaManager.addSession(session);
                if (_sessions.size() > this._maxSessions)
                    this._maxSessions = _sessions.size ();
            }
        }
        
        HttpSessionEvent event=new HttpSessionEvent(session);
        for(int i=0;i<LazyList.size(_sessionListeners);i++)
            ((HttpSessionListener)LazyList.get(_sessionListeners,i)).sessionCreated(event);
        
        if (!(_metaManager instanceof NullMetaManager))
            request.setAttribute(__NEW_SESSION_ID, session.getId());
        return session;
    }
    
    /* ------------------------------------------------------------ */
    protected abstract Session newSession(HttpServletRequest request);
    
    /* ------------------------------------------------------------ */
    /* new Session ID.
     * If the request has a requestedSessionID which is unique, that is used.
     * The session ID is created as a unique random long, represented as in a
     * base between 30 and 36, selected by timestamp.
     * If the request has a jvmRoute attribute, that is appended as a
     * worker tag, else any worker tag set on the manager is appended.
     * @param request 
     * @param created 
     * @return Session ID.
     */
    private String newSessionId(HttpServletRequest request,long created)
    {
        synchronized(_metaManager)
        {
            // A requested session ID can only be used if it is in the global map of
            // ID but not in this contexts map.  Ie it is an ID in use by another context
            // in this server and thus we are doing a cross context dispatch.
            if (!(_metaManager instanceof NullMetaManager))
            {
                String requested_id=(String)request.getAttribute(__NEW_SESSION_ID);
                if (requested_id==null)
                    requested_id=request.getRequestedSessionId();
                if (requested_id !=null && 
                    requested_id!=null && _metaManager.idInUse(requested_id) && !_sessions.containsKey(requested_id))
                return requested_id;
            }
            
            // pick a new unique ID!
            String id=null;
            while (id==null || id.length()==0 || _metaManager.idInUse(id))
            {
                long r = _random.nextLong();
                if (r<0)r=-r;
                id=Long.toString(r,30+(int)(created%7));
                String worker = (String)request.getAttribute("org.mortbay.http.ajp.JVMRoute");
                if (worker!=null)
                    id+="."+worker;
                else if (_workerName!=null)
                    id+="."+_workerName;
            }
            return id;
        }
    }
    
    /* ------------------------------------------------------------ */
    public void removeEventListener(EventListener listener)
    {
        if (listener instanceof HttpSessionAttributeListener)
            _sessionAttributeListeners=LazyList.remove(_sessionAttributeListeners,listener);
        if (listener instanceof HttpSessionListener)
            _sessionListeners=LazyList.remove(_sessionListeners,listener);
    }
    
    /* ------------------------------------------------------------ */
    public void resetStats ()
    {
        _minSessions =  _sessions.size ();
        _maxSessions = _sessions.size ();
    }
    
    /* -------------------------------------------------------------- */
    /** Find sessions that have timed out and invalidate them.
     *  This runs in the SessionScavenger thread.
     */
    private void scavenge()
    {
        Thread thread = Thread.currentThread();
        ClassLoader old_loader = thread.getContextClassLoader();
        try
        {
            if (_loader!=null)
                thread.setContextClassLoader(_loader);
            
            long now = System.currentTimeMillis();
            
            // Since Hashtable enumeration is not safe over deletes,
            // we build a list of stale sessions, then go back and invalidate them
            Object stale=null;
            

            synchronized(AbstractSessionManager.this)
            {
                // For each session
                for (Iterator i = _sessions.values().iterator(); i.hasNext(); )
                {
                    Session session = (Session)i.next();
                    long idleTime = session._maxIdleMs;
                    if (idleTime > 0 && session._accessed + idleTime < now) {
                        // Found a stale session, add it to the list
                        stale=LazyList.add(stale,session);
                    }
                }
            }
            
            // Remove the stale sessions
            for (int i = LazyList.size(stale); i-->0;)
            {
                // check it has not been accessed in the meantime
                Session session=(Session)LazyList.get(stale,i);
                long idleTime = session._maxIdleMs;
                if (idleTime > 0 && session._accessed + idleTime < System.currentTimeMillis())    
                {
                    session.invalidate();
                    int nbsess = this._sessions.size();
                    if (nbsess < this._minSessions)
                        this._minSessions = nbsess;
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
     * @param httpOnly The httpOnly to set.
     */
    public void setHttpOnly(boolean httpOnly)
    {
        _httpOnly = httpOnly;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param seconds 
     */
    public void setMaxInactiveInterval(int seconds)
    {
        _dftMaxIdleSecs = seconds;
        if (_dftMaxIdleSecs>0 && _scavengePeriodMs>_dftMaxIdleSecs*100)
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
        int period = seconds*1000;
        if (period>60000)
            period=60000;
        if (period<1000)
            period=1000;
        
        if (period!=old_period)
        {
            synchronized(this)
            {
                _scavengePeriodMs=period;
                if (_scavenger!=null)
                    _scavenger.interrupt();
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param secureCookies The secureCookies to set.
     */
    public void setSecureCookies(boolean secureCookies)
    {
        _secureCookies = secureCookies;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param usingCookies The usingCookies to set.
     */
    public void setUsingCookies(boolean usingCookies)
    {
        _usingCookies = usingCookies;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the workname.
     * If set, the workername is dot appended to the session ID
     * and can be used to assist session affinity in a load balancer.
     * @param workerName 
     */
    public void setWorkerName(String workerName)
    {
        _workerName = workerName;
    }
    
    /* ------------------------------------------------------------ */
    public void doStart()
    throws Exception
    {
        _context=ContextHandler.getCurrentContext();
        _loader = Thread.currentThread().getContextClassLoader();
        if (_random==null)
        {
            Log.debug("New random session seed");
            _random=new Random();
        }
        else
            if(Log.isDebugEnabled())Log.debug("Initializing random session key: "+_random);
        _random.nextLong();
        
        if (_sessions==null)
            _sessions=new HashMap();
        
        // Start the session scavenger if we haven't already
        if (_scavenger == null)
        {
            _scavenger = new SessionScavenger();
            _scavenger.start();
        }
        
        if (_metaManager==null)
            _metaManager=new NullMetaManager();
        _metaManager.start();
        
        super.doStart();
    }
    
    
    /* ------------------------------------------------------------ */
    public void doStop()
    	throws Exception	
    {
        super.doStop();
        
        // Invalidate all sessions to cause unbind events
        ArrayList sessions = new ArrayList(_sessions.values());
        for (Iterator i = sessions.iterator(); i.hasNext(); )
        {
            Session session = (Session)i.next();
            session.invalidate();
        }
        _sessions.clear();
        
        // stop the scavenger
        SessionScavenger scavenger = _scavenger;
        _scavenger=null;
        if (scavenger!=null)
            scavenger.interrupt();
        
        // TODO when do we stop the meta manager?
        
        _loader=null;
    }
    
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public abstract class Session implements SessionManager.Session
    {
        long _created=System.currentTimeMillis();
        long _accessed=_created;
        String _id;
        boolean _invalid=false;
        long _maxIdleMs = _dftMaxIdleSecs*1000;
        boolean _newSession=true;
        Map _values;
        
        /* ------------------------------------------------------------- */
        protected Session(HttpServletRequest request)
        {
            _id=newSessionId(request,_created);
            if (_dftMaxIdleSecs>=0)
                _maxIdleMs=_dftMaxIdleSecs*1000;
            
        }
        
        /* ------------------------------------------------------------ */
        public void access()
        {
            _newSession=false;
            _accessed=System.currentTimeMillis();
        }
        
        /* ------------------------------------------------------------- */
        /** If value implements HttpSessionBindingListener, call valueBound() */
        private void bindValue(java.lang.String name, Object value)
        {
            if (value!=null && value instanceof HttpSessionBindingListener)
                ((HttpSessionBindingListener)value).valueBound(new HttpSessionBindingEvent(this,name));            
        }
                
        /* ------------------------------------------------------------ */
        public synchronized Object getAttribute(String name)
        {
            if (_invalid) throw new IllegalStateException();
            if (_values==null)
                return null;
            return _values.get(name);
        }
        
        /* ------------------------------------------------------------ */
        public synchronized Enumeration getAttributeNames()
        {
            if (_invalid) throw new IllegalStateException();
            List names = _values==null?Collections.EMPTY_LIST:new ArrayList(_values.keySet());
            return Collections.enumeration(names);
        }
        
        /* ------------------------------------------------------------- */
        public long getCreationTime()
        throws IllegalStateException
        {
            if (_invalid) throw new IllegalStateException();
            return _created;
        }
        
        /* ------------------------------------------------------------- */
        public String getId()
        throws IllegalStateException
        {
            return _id;
        }
        
        /* ------------------------------------------------------------- */
        public long getLastAccessedTime()
        throws IllegalStateException
        {
            if (_invalid) throw new IllegalStateException();
            return _accessed;
        }
        
        /* ------------------------------------------------------------- */
        public int getMaxInactiveInterval()
        {
            if (_invalid) throw new IllegalStateException();
            return (int)(_maxIdleMs / 1000);
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
        public HttpSessionContext getSessionContext()
        throws IllegalStateException
        {
            if (_invalid) throw new IllegalStateException();
            return __nullSessionContext;
        }
        
        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #getAttribute}
         */
        public Object getValue(String name)
        throws IllegalStateException
        {
            return getAttribute(name);
        }
        
        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #getAttributeNames}
         */
        public synchronized String[] getValueNames()
        throws IllegalStateException
        {
            if (_invalid) throw new IllegalStateException();
            if (_values==null)
                return new String[0];
            String[] a = new String[_values.size()];
            return (String[])_values.keySet().toArray(a);
        }
        
        /* ------------------------------------------------------------- */
        public void invalidate() throws IllegalStateException
        {
            if (Log.isDebugEnabled()) Log.debug("Invalidate session "+getId());
            try
            {
                // Notify listeners and unbind values
                synchronized (this)
                {
                    if (_invalid)
                        throw new IllegalStateException();

                    if (_sessionListeners!=null)
                    {
                        HttpSessionEvent event=new HttpSessionEvent(this);
                        for (int i=0; i<LazyList.size(_sessionListeners); i++)
                            ((HttpSessionListener)LazyList.get(_sessionListeners,i)).sessionDestroyed(event);
                    }

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
                // Remove session from context and global maps
                synchronized(_metaManager)
                {
                    String id = getId();
                    synchronized (_sessions)
                    {
                        _invalid=true;
                        _sessions.remove(id);
                    }                        
                    _metaManager.invalidateAll(id);
                }
            }
        }
        
        /* ------------------------------------------------------------- */
        public boolean isNew()
        throws IllegalStateException
        {
            if (_invalid) throw new IllegalStateException();
            return _newSession;
        }
        
        /* ------------------------------------------------------------ */
        public boolean isValid()
        {
            return !_invalid;
        }
        
        /* ------------------------------------------------------------ */
        protected abstract Map newAttributeMap();
        
        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #setAttribute}
         */
        public void putValue(java.lang.String name,
                java.lang.Object value)
        throws IllegalStateException
        {
            setAttribute(name,value);
        }
        
        /* ------------------------------------------------------------ */
        public synchronized void removeAttribute(String name)
        {
            if (_invalid) throw new IllegalStateException();
            if (_values==null)
                return;
            
            Object old=_values.remove(name);
            if (old!=null)
            {
                unbindValue(name, old);
                if (_sessionAttributeListeners!=null)
                {
                    HttpSessionBindingEvent event =
                        new HttpSessionBindingEvent(this,name,old);
                    
                    for(int i=0;i<LazyList.size(_sessionAttributeListeners);i++)
                        ((HttpSessionAttributeListener)LazyList.get(_sessionAttributeListeners,i)).attributeRemoved(event);
                }
            }
        }
        
        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #removeAttribute}
         */
        public void removeValue(java.lang.String name)
        throws IllegalStateException
        {
            removeAttribute(name);
        }
        
        /* ------------------------------------------------------------ */
        public synchronized void setAttribute(String name, Object value)
        {
            if (_invalid) throw new IllegalStateException();
            if (_values==null)
                _values=newAttributeMap();
            Object oldValue = _values.put(name,value);
            
            if (value==null || !value.equals(oldValue))
            {
                unbindValue(name, oldValue);
                bindValue(name, value);
                
                if (_sessionAttributeListeners!=null)
                {
                    HttpSessionBindingEvent event =
                        new HttpSessionBindingEvent(this,name,
                                oldValue==null?value:oldValue);

                    for(int i=0;i<LazyList.size(_sessionAttributeListeners);i++)
                    {
                        HttpSessionAttributeListener l = (HttpSessionAttributeListener)LazyList.get(_sessionAttributeListeners,i);
                        
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
            _maxIdleMs = (long)secs * 1000;
            if (_maxIdleMs>0 && (_maxIdleMs/10)<_scavengePeriodMs)
                AbstractSessionManager.this.setScavengePeriod((secs+9)/10);
        }
        
        /* ------------------------------------------------------------- */
        /** If value implements HttpSessionBindingListener, call valueUnbound() */
        private void unbindValue(java.lang.String name, Object value)
        {
            if (value!=null && value instanceof HttpSessionBindingListener)
                ((HttpSessionBindingListener)value).valueUnbound(new HttpSessionBindingEvent(this,name));
        }
    }
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* -------------------------------------------------------------- */
    /** SessionScavenger is a background thread that kills off old sessions */
    class SessionScavenger extends Thread
    {
        
        SessionScavenger()
        {
            super("SessionScavenger");
            setDaemon(true);
        }
        public void run()
        {
            int period=-1;
            try{
                while (isStarted())
                {
                    try {
                        if (period!=_scavengePeriodMs)
                        {
                            if(Log.isDebugEnabled())Log.debug("Session scavenger period = "+_scavengePeriodMs/1000+"s");
                            period=_scavengePeriodMs;
                        }
                        sleep(period>1000?period:1000);
                        AbstractSessionManager.this.scavenge();
                    }
                    catch (InterruptedException ex){continue;}
                    catch (Error e) {Log.warn(Log.EXCEPTION,e);}
                    catch (Exception e) {Log.warn(Log.EXCEPTION,e);}
                }
            }
            finally
            {
                AbstractSessionManager.this._scavenger=null;
                Log.debug("Session scavenger exited");
            }
        }
        
    }   // SessionScavenger

    /* ------------------------------------------------------------ */
    /** 
     * Null returning implementation of HttpSessionContext
     * @version $Id: AbstractSessionManager.java,v 1.5 2005/11/11 22:55:39 gregwilkins Exp $
     * @author Greg Wilkins (gregw)
     */
    public static class NullSessionContext implements HttpSessionContext
    {
        /* ------------------------------------------------------------ */
        private NullSessionContext(){}
        
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
    
    /* ------------------------------------------------------------ */
    
    public static class SimpleMetaManager extends AbstractLifeCycle implements MetaManager
    {
        MultiMap _sessions;
        
        protected void doStart()
        {
            _sessions=new MultiMap();
        }

        /* ------------------------------------------------------------ */
        protected void doStop()
        {
            if (_sessions!=null)
                _sessions.clear(); // Maybe invalidate?
            _sessions=null;
        }
        
        /* ------------------------------------------------------------ */
        /* 
         * @see org.mortbay.jetty.SessionManager.MetaManager#idInUse(java.lang.String)
         */
        public boolean idInUse(String id) {
            return _sessions.containsKey(id);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see org.mortbay.jetty.SessionManager.MetaManager#addSession(javax.servlet.http.HttpSession)
         */
        public void addSession(HttpSession session) {
            _sessions.add(session.getId(), session);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see org.mortbay.jetty.SessionManager.MetaManager#invalidateAll(java.lang.String)
         */
        public void invalidateAll(String id) {
            
            synchronized(this)
            {
                while(_sessions.containsKey(id))
                {
                    Session session=(Session)_sessions.getValue(id,0);
                    if (session.isValid())
                        session.invalidate();
                    else
                        _sessions.removeValue(id, session);
                }
            }
            
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see org.mortbay.jetty.SessionManager.MetaManager#crossContext()
         */
        public boolean crossContext() {
            return true;
        }
        
    }

    /* ------------------------------------------------------------ */
    public static class NullMetaManager extends AbstractLifeCycle implements MetaManager
    {
        public boolean idInUse(String id) {return false;}
        public void addSession(HttpSession session) {}
        public void invalidateAll(String id) {}
        public boolean crossContext() {
            return true;
        }
    }
    
}

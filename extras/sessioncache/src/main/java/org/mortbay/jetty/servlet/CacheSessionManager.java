package org.mortbay.jetty.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.SessionIdManager;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;


/* ------------------------------------------------------------ */
/**
 * A session manager that uses ehcache in front of a pluggable session store.
 * 
 * THIS IS PRE-ALPHA CODE - only checked in for more developement!
 * 
 * @author nik
 *
 */
public class CacheSessionManager extends AbstractSessionManager implements Serializable
{
    protected String _cacheKey;
    transient CacheSessionIdManager _cacheIdManager;
    transient Ehcache _sessionCache;
    transient Store _sessionStore;

    private int _scavengePeriodMs=30000;
    private TimerTask _scavenger=null;
    
    /* ------------------------------------------------------------ */
    public void doStart() throws Exception
    {
        super.doStart();
        SessionIdManager sid=getIdManager();
        if (!(sid instanceof CacheSessionIdManager))
            throw new IllegalStateException("Non cached ID manager:" + sid);
        _cacheIdManager=(CacheSessionIdManager)sid;
        
        _cacheKey=_context.getContextPath();
        if (_context.getContextHandler().getVirtualHosts()!=null && _context.getContextHandler().getVirtualHosts().length>0)
            _cacheKey=_cacheKey+"@"+_context.getContextHandler().getVirtualHosts()[0];
        _cacheKey=_cacheKey.replace('/','_');
        
        synchronized(_cacheIdManager)
        {
            _sessionCache = _cacheIdManager._cacheManager.getEhcache(_cacheKey);
            if (_sessionCache==null)
            {
                
                // just create a cache with the default settings of the cacheIdManager
                _cacheIdManager._cacheManager.addCache(_cacheKey);
                _sessionCache = _cacheIdManager._cacheManager.getEhcache(_cacheKey);
                
                // XXX Changed my mind about self populating caches.
                // they always create an element - which is not what we want.
                
            }
        }
        
        if (_sessionStore != null)
        {
            _sessionStore.setContext(_context.getContextPath());
        }

        //scavenger thread
        Timer timer = new Timer();
        timer.schedule(new SessionScavenger(), 0, _scavengePeriodMs);
    }

    public void setStore(Store store)
    {
        _sessionStore = store;
    }

    /* ------------------------------------------------------------ */
    public Map getSessionMap()
    {
        // Not supported in this impl
        return null;
    }

    /* ------------------------------------------------------------ */
    public int getSessions()
    {
        // TODO this is not exactly correct and we need to think about the
        // diskstore and sessions not on the node.  Maybe this is only sessions 
        // this node??
        return _sessionCache.getSize();
    }
    
    /* ------------------------------------------------------------ */
    protected void addSession(Session session)
    {
        // OK - changing my mind here.... perhaps the whole 
        // session is in the cache!
        _sessionCache.put(new Element(session.getClusterId(),session));
        
        // TODO - maybe put the Element as a transient in the Session
        // instance, so it can be used for last access times, isValid etc.
    }

    /* ------------------------------------------------------------ */
    protected Session getSession(String idInCluster)
    {
        Element sessionElement = _sessionCache.get(idInCluster);
        Session session = null;

        if (sessionElement==null || sessionElement.isExpired())
        {
            session = (Session)_sessionStore.get(idInCluster);
            if (session == null)
            {
                return null;
            }
            ((EHSession)session).setServletContext(_context);
            sessionElement = new Element(session.getClusterId(), session);
            _sessionCache.put(sessionElement);
        }
        else
        {
            session = (Session)sessionElement.getValue();
        }

        if(session != null)
        {
            EHSession ehSession = (EHSession)session; 
            ehSession.setSessionCacheElement(sessionElement);
        }            

        return session;
    }

    /* ------------------------------------------------------------ */
    protected void invalidateSessions()
    {
        // TODO Auto-generated method stub
    }

    /* ------------------------------------------------------------ */
    protected Session newSession(HttpServletRequest request)
    {
        return new EHSession(request);
    }

    /* ------------------------------------------------------------ */
    protected void removeSession(String idInCluster)
    {
        _sessionCache.remove(idInCluster);
        _sessionStore.remove(idInCluster);
    }

    public void complete(HttpSession session)
    {
        EHSession ehSession = (EHSession)session;
        if (ehSession._dirty)
            _sessionStore.add(ehSession.getClusterId(), (EHSession)session);
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

            synchronized (CacheSessionManager.this)
            {
                List keyList = _sessionCache.getKeys();
                 
                // For each session
                for (Iterator keys = keyList.iterator(); keys.hasNext();)
                {
                    
                    Element e = _sessionCache.getQuiet(keys.next());
                    Session session = (Session)e.getObjectValue();
                    long idleTime=session._maxIdleMs;

                    if (idleTime>0&&session.getLastAccessedTime()+idleTime<now)
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
                if (idleTime>0&&session.getLastAccessedTime()+idleTime<System.currentTimeMillis())
                {
                    session.invalidate();
                    int nbsess=this._sessionCache.getSize();
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
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class EHSession extends AbstractSessionManager.Session
    {
        private boolean _dirty = false;
        transient Element _sessionCacheElement;
        
        /* ------------------------------------------------------------- */
        protected EHSession(HttpServletRequest request)
        {
            super(request);
        }
        
        /* ------------------------------------------------------------ */
        protected Map newAttributeMap()
        {
            return new HashMap(3);
        }
        
        protected void setSessionCacheElement(Element sessionCacheElement)
        {
            _sessionCacheElement = sessionCacheElement;
        }
                
        private void setServletContext(ContextHandler.SContext context)
        {
            _context = context;
        }

        private void readObject(ObjectInputStream in)
        throws ClassNotFoundException, IOException 
        {
            in.defaultReadObject();
            _dirty = false;
        }
        public synchronized void setAttribute(String name, Object value)
        {
            super.setAttribute(name, value);
            _dirty = true;
        }
        
        public long getLastAccessedTime() throws IllegalStateException
        {
            return _sessionCacheElement.getLastAccessTime();
        }
    }

    public interface Store
    {
        Object get(String id);
        
        void add(String id, Serializable serializable);
        
        void remove(String id);
        
        List getKeys();
        
        void setContext(String contextName);
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
                    _scavenger.cancel();
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* -------------------------------------------------------------- */
    /** SessionScavenger is a background thread that kills off old sessions */
    //may be moved to abstractSessionManager
    class SessionScavenger extends TimerTask
    {
        public void run()
        {
            _scavenger=SessionScavenger.this;

            String name=Thread.currentThread().getName();
            if (_context!=null)
                Thread.currentThread().setName(name+" - Invalidator - "+_context.getContextPath());

            try
            {
                if (Log.isDebugEnabled())
                    Log.debug("Session scavenger period = "+_scavengePeriodMs/1000+"s");
                            
                CacheSessionManager.this.scavenge();
            }
            catch (Error e)
            {
                Log.warn(Log.EXCEPTION,e);
            }
            catch (Exception e)
            {
                Log.warn(Log.EXCEPTION,e);
            }

            finally
            {
                CacheSessionManager.this._scavenger=null;
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
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    // TODO remove this test main eventually when not needed for simple testing.
    public static void main(String[] args)
    throws Exception
    {
        String jetty_home=System.getProperty("jetty.home");
        if (jetty_home==null)
            jetty_home="../..";

        Server server = new Server();
        SelectChannelConnector connector=new SelectChannelConnector();  
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});
        server.setSessionIdManager(new CacheSessionIdManager(jetty_home+"/sessions"));

        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();        
        
        WebAppContext[] wah = new WebAppContext[3];
        
        for (int i=0;i<3;i++)
        {
            SessionHandler sessionHandler = new SessionHandler();
            CacheSessionManager cacheSessionManager = new CacheSessionManager();
            cacheSessionManager.setSessionHandler(sessionHandler);
            cacheSessionManager.setStore(new FileStore(jetty_home+"/sessions"));
            sessionHandler.setSessionManager(cacheSessionManager);
        
            wah[i]= new WebAppContext(null,sessionHandler,null,null);
            wah[i].setContextPath("/test"+(i==0?"":(""+i)));
            wah[i].setResourceBase(jetty_home+"/webapps/test");
        }
        
        contexts.setHandlers(wah);
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler()});
        server.setHandler(handlers);

        HashUserRealm hur = new HashUserRealm();
        hur.setName("Test Realm");
        hur.setConfig(jetty_home+"/etc/realm.properties");
        wah[0].getSecurityHandler().setUserRealm(hur);
        
        server.start();
        server.join();
    }
    
}

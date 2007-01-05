package org.mortbay.jetty.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
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
    }

    public void complete(HttpSession session)
    {
        EHSession ehSession = (EHSession)session;
        if (ehSession._dirty)
            _sessionStore.add(ehSession.getClusterId(), (EHSession)session);
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

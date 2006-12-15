package org.mortbay.jetty.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.SessionIdManager;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.webapp.WebAppContext;

import net.sf.ehcache.Cache;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.ehcache.Ehcache;


/* ------------------------------------------------------------ */
/**
 * A session manager that uses ehcache in front of a pluggable session store.
 * 
 * THIS IS PRE-ALPHA CODE - only checked in for more developement!
 * 
 * @author nik
 *
 */
public class CacheSessionManager extends AbstractSessionManager
{
    protected AttributesFactory _mapFactory;
    protected CacheSessionIdManager _cacheIdManager;
    protected SelfPopulatingCache _sessionCache;

    public void doStart() throws Exception
    {
        super.doStart();
        SessionIdManager sid=getIdManager();
        if (!(sid instanceof CacheSessionIdManager))
            throw new IllegalStateException("Non cached ID manager:" + sid);
        _cacheIdManager=(CacheSessionIdManager)sid;
        if (_mapFactory == null)
            _mapFactory = new HashAttributesFactory();
        
        //set time to live and time to idle to 600 for now
        
        Ehcache cache = new Cache(_context.getContextPath().replaceAll("/",""), 1000, MemoryStoreEvictionPolicy.LRU, 
            true, null, false, 600, 600, true, 200, null, null);
        
        //TODO discuss how to implement CacheEntryFactory
        _cacheIdManager._cacheManager.addCache(cache);
        _sessionCache = new SelfPopulatingCache(_cacheIdManager._cacheManager.getCache(_context.getContextPath().replaceAll("/","")), null);
    }
    
    protected AbstractSessionManager.Session newSession(HttpServletRequest request)
    {
        return new Session(request);
    }

    protected void setSessionAttributeMapFactory(AttributesFactory mapFactory)
    {
        _mapFactory = mapFactory;
    }

    public interface AttributesFactory
    {
        Map create(String id, Ehcache cache);
    }
    
    class Session extends AbstractSessionManager.Session
    {

        protected Session(HttpServletRequest request)
        {
            super(request);
        }
        
        protected Map newAttributeMap()
        {
            // TODO - Change this so that a Cache holds a map of session ID to hashmap
            // so that Cache elements are sessions rather than elements.
            // The cache should be a write through self populating cache.
            //
           return _mapFactory.create(_clusterId,_sessionCache);
        }
    }

    class SessionCacheEntryFactory implements CacheEntryFactory
    {
        public synchronized Object createEntry(Object key)
        {
            return null;
        }        
    }

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
            cacheSessionManager.setSessionAttributeMapFactory(new FileAttributesFactory());
            cacheSessionManager.setSessionHandler(sessionHandler);
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
        // System.setProperty("DEBUG","true");
        
        server.start();
        server.join();
    }

}

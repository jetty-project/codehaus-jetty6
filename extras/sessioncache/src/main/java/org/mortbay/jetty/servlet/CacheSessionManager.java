package org.mortbay.jetty.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

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
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
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
public class CacheSessionManager extends AbstractSessionManager
{
    protected AttributesFactory _mapFactory;
    protected CacheSessionIdManager _cacheIdManager;

    public void doStart() throws Exception
    {
        super.doStart();
        SessionIdManager sid=getIdManager();
        if (!(sid instanceof CacheSessionIdManager))
            throw new IllegalStateException("Non cached ID manager:" + sid);
        _cacheIdManager=(CacheSessionIdManager)sid;
        if (_mapFactory == null)
            _mapFactory = new HashAttributesFactory();
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
        Map create(String id, String context);
    }
    
    class Session extends AbstractSessionManager.Session
    {
        private Cache _attributeCache;

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
            _attributeCache = new Cache(_clusterId,1000,true,true,100,200);
            _cacheIdManager._cacheManager.addCache(_attributeCache);
            _attributeCache = _cacheIdManager._cacheManager.getCache(_clusterId);
            
           return _mapFactory.create(_clusterId, _context.getContextPath());
        }
        
        public synchronized void setAttribute(String name, Object value)
        {
            // TODO - should not need to do this... 
            // The session map should be directly updated and then ehcache informed that the
            // value has changed (if it does not auto detect ????)
            
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

            if (oldValue != null)
            {
                _attributeCache.put(new Element(name,(Serializable)value));
            }
            if (oldValue==null || !value.equals(oldValue)) 
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

        public synchronized Object getAttribute(String name)
        {
            // TODO - this should just be a direct get from the session hashmap.
            
            if (_invalid)
                throw new IllegalStateException();
            if (_values==null)
                return null;
            Element attribute = _attributeCache.get(name);
            if (attribute != null) 
            {
                return attribute.getValue();
            }
            else
            {
                Object value = _values.get(name);
                _attributeCache.put(new Element(name,(Serializable)value));
                return value;
            }
        }

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

                    if (_attributeCache!=null)
                    {
                        Iterator iter=_attributeCache.getKeys().iterator();
                        while (iter.hasNext())
                        {
                            String key=(String)iter.next();
                            Element attribute=_attributeCache.get(key);
                            _attributeCache.remove(key);
                            unbindValue(key,attribute);

                            if (_sessionAttributeListeners!=null)
                            {
                                HttpSessionBindingEvent event=new HttpSessionBindingEvent(this,key,attribute);

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
                _cacheIdManager._cacheManager.removeCache(_clusterId);
                _invalid=true;
            }
        }

        public synchronized void removeAttribute(String name)
        {
            if (_invalid)
                throw new IllegalStateException();
            if (_values==null)
                return;

            Object old=_values.remove(name);
            boolean unbound = false;
            if (old!=null)
            {
                unbindValue(name,old);
                if (_sessionAttributeListeners!=null)
                {
                    unbound = true;
                    HttpSessionBindingEvent event=new HttpSessionBindingEvent(this,name,old);

                    for (int i=0; i<LazyList.size(_sessionAttributeListeners); i++)
                        ((HttpSessionAttributeListener)LazyList.get(_sessionAttributeListeners,i)).attributeRemoved(event);
                }
            }

            old = _attributeCache.get(name);
            if (old!=null)
            {
                unbindValue(name,old);
                if (_sessionAttributeListeners!=null && !unbound)
                {
                    HttpSessionBindingEvent event=new HttpSessionBindingEvent(this,name,old);

                    for (int i=0; i<LazyList.size(_sessionAttributeListeners); i++)
                        ((HttpSessionAttributeListener)LazyList.get(_sessionAttributeListeners,i)).attributeRemoved(event);
                }
            }
            _attributeCache.remove(name);
        }


    }

    
    // TODO remove this test main eventually when not needed for simple testing.
    public static void main(String[] args)
    throws Exception
    {
        Server server = new Server();
        SelectChannelConnector connector=new SelectChannelConnector();  
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});
        server.setSessionIdManager(new CacheSessionIdManager());

        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        String jetty_home=System.getProperty("jetty.home");
        if (jetty_home==null)
            jetty_home="../..";
        
        
        WebAppContext[] wah = new WebAppContext[3];
        
        for (int i=0;i<3;i++)
        {
            SessionHandler sessionHandler = new SessionHandler();
            CacheSessionManager cacheSessionManager = new CacheSessionManager();
            cacheSessionManager.setSessionAttributeMapFactory(new FileAttributesFactory(jetty_home+"/sessions"));
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

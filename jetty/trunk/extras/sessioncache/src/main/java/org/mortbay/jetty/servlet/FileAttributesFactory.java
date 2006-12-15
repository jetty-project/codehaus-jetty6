package org.mortbay.jetty.servlet;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.mortbay.jetty.servlet.CacheSessionManager.AttributesFactory;

public class FileAttributesFactory implements AttributesFactory
{
    // TODO replace this with ehcache disk store.
    
    private Ehcache _cache;

    public Map create(String id, Ehcache cache)
    {
        _cache = cache;
        // TODO Auto-generated method stub
        return new FileSessionAttributeMap(id);
    }

    class FileSessionAttributeMap implements Map
    {
        private String _clusterId;
        public FileSessionAttributeMap(String clusterId)
        {
            _clusterId = clusterId;
            putAttributeMap(new HashMap());
            _cache.flush();
        }
        
        private Map getAttributeMap()
        {
            Element element = _cache.get(_clusterId);
            return (Map)element.getObjectValue();
        }
        
        private void putAttributeMap(Map attributeMap)
        {
            _cache.put(new Element(_clusterId,attributeMap));
        }
        
        public void clear()
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
            attributeMap.clear();
            putAttributeMap(attributeMap);
        }

        public boolean containsKey(Object obj)
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
                return attributeMap.containsKey(obj);
            return false;
        }

        public boolean containsValue(Object obj)
        {
            Map attributeMap =getAttributeMap();
            if (attributeMap != null)
                return attributeMap.containsValue(obj);
            return false;
        }

        public Set entrySet()
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
                attributeMap.entrySet();
            return null;
        }

        public Object get(Object obj)
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
                return attributeMap.get(obj);
            return null;
        }

        public boolean isEmpty()
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
                attributeMap.isEmpty();
            return true;
        }

        public Set keySet()
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
                return attributeMap.keySet();
            return null;
        }

        public Object put(Object key, Object value)
        {
            Map attributeMap = getAttributeMap();
            Object obj = null;
            if (attributeMap != null)
            {
                obj = attributeMap.put(key, value);
            }
            putAttributeMap(attributeMap);
            return obj;
        }

        public void putAll(Map map)
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
            {
                attributeMap.putAll(map);
            }
            putAttributeMap(attributeMap);
        }

        public Object remove(Object obj)
        {
            Map attributeMap = getAttributeMap();
            Object object = null;
            if (attributeMap != null)
            {
                object = attributeMap.remove(obj);
            }
            putAttributeMap(attributeMap);
            return object;
        }

        public int size()
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
                return attributeMap.size();
            return 0;
        }

        public Collection values()
        {
            Map attributeMap = getAttributeMap();
            if (attributeMap != null)
                return attributeMap.values();
            return null;
        }
    }
}
package org.mortbay.jetty.servlet;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Ehcache;

import org.mortbay.jetty.servlet.CacheSessionManager.AttributesFactory;

public class HashAttributesFactory implements AttributesFactory
{

    public Map create(String id, Ehcache cach)
    {
        // TODO Auto-generated method stub
        return new HashMap(3);
    }

}

// ========================================================================
// $Id: ResourceCache.java,v 1.3 2005/11/11 22:55:39 gregwilkins Exp $
// Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.resource.ResourceFactory;


/* ------------------------------------------------------------ */
/** 
 * @version $Id: ResourceCache.java,v 1.3 2005/11/11 22:55:39 gregwilkins Exp $
 * @author Greg Wilkins
 */
public class ResourceCache extends AbstractLifeCycle implements Serializable
{   
    private int _maxCachedFileSize =254*1024;
    private int _maxCachedFiles=1024;
    private int _maxCacheSize =4096*1024;
    
    protected transient Map _cache;
    protected transient int _cacheSize;
    protected transient int _cachedFiles;
    protected transient Entry _mostRecentlyUsed;
    protected transient Entry _leastRecentlyUsed;


    /* ------------------------------------------------------------ */
    /** Constructor.
     */
    public ResourceCache()
    {}

    
    /* ------------------------------------------------------------ */
    public int getMaxCachedFileSize()
    {
        return _maxCachedFileSize;
    }

    /* ------------------------------------------------------------ */
    public void setMaxCachedFileSize(int maxCachedFileSize)
    {
        _maxCachedFileSize = maxCachedFileSize;
        _cache.clear();
    }

    /* ------------------------------------------------------------ */
    public int getMaxCacheSize()
    {
        return _maxCacheSize;
    }

    /* ------------------------------------------------------------ */
    public void setMaxCacheSize(int maxCacheSize)
    {
        _maxCacheSize = maxCacheSize;
        _cache.clear();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the maxCachedFiles.
     */
    public int getMaxCachedFiles()
    {
        return _maxCachedFiles;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param maxCachedFiles The maxCachedFiles to set.
     */
    public void setMaxCachedFiles(int maxCachedFiles)
    {
        _maxCachedFiles = maxCachedFiles;
    }
    
    /* ------------------------------------------------------------ */
    public void flushCache()
    {
        _cache.clear();
        System.gc();
    }

    /* ------------------------------------------------------------ */
    /** Get a Entry from the cache.
     * Get either a valid entry object or create a new one if possible.
     * When an non-new entry is found, the thread will wait until a
     * non-null value is set on the entry.  It is the responsibility of the
     * thread that creates an entry to set the value. 
     *
     * @param pathInContext
     * @param resource
     * @param value associated value
     * @exception IOException
     */
    public Entry lookup(String pathInContext, ResourceFactory factory)
    {
        if (Log.isDebugEnabled()) Log.debug("lookup {}",pathInContext);
        
        Entry entry=null;
        boolean newEntry=false;
        
        // Look up cache operations
        synchronized(_cache)
        {
            // Look for it in the cache
            entry = (Entry)_cache.get(pathInContext);
            if (entry!=null)
            {
                if (entry!=null && !entry.isValid())
                    entry=null;
                else
                    if (Log.isDebugEnabled()) Log.debug("CACHE HIT: {}",entry);
            }

            if (entry==null)
            {
                Resource resource=factory.getResource(pathInContext);
                if (resource==null)
                    return null;
                
                long len = resource.length();
                if (resource.exists())
                {
                    // Is it badly named?
                    if (resource.isDirectory())
                        return null;
                    
                    // Is it cacheable?
                    if (len>0 && len<_maxCachedFileSize && len<_maxCacheSize)
                    {
                        int needed=_maxCacheSize-(int)len;
                        while(_cacheSize>needed || (_maxCachedFiles>0 && _cachedFiles>_maxCachedFiles))
                            _leastRecentlyUsed.invalidate();
                        
                        if(Log.isDebugEnabled())Log.debug("CACHED: {}",resource);
                        entry= new Entry(pathInContext,resource);
                        newEntry=true;
                    }
                }
            }
        }
        
        if (!newEntry && entry!=null)
        {
            synchronized(entry)
            {
                try
                {
                    while(entry.getKey()!=null && entry.getValue()==null)
                        entry.wait();
                }
                catch(InterruptedException e){}
            }
        }
        return entry; 
    }


    /* ------------------------------------------------------------ */
    public synchronized void doStart()
        throws Exception
    {
        _cache=new HashMap();
        _cacheSize=0;
        _cachedFiles=0;
    }

    /* ------------------------------------------------------------ */
    /** Stop the context.
     */
    public void doStop()
        throws InterruptedException
    {
        if (_cache!=null)
            _cache.clear();
        _cache=null;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** MetaData associated with a context Resource.
     */
    public class Entry
    {
        String _key;
        Resource _resource;
        Object _value;
        long _lastModified;
        Entry _prev;
        Entry _next;

        Entry(String pathInContext, Resource resource)
        {
            _key=pathInContext;
            _resource=resource;
            _lastModified=resource.lastModified();

            _next=_mostRecentlyUsed;
            _mostRecentlyUsed=this;
            if (_next!=null)
                _next._prev=this;
            _prev=null;
            if (_leastRecentlyUsed==null)
                _leastRecentlyUsed=this;

            _cache.put(_key,this);
            _cacheSize+=_resource.length();
            _cachedFiles++;
        }

        public String getKey()
        {
            return _key;
        }
        
        public Resource getResource()
        {
            return _resource;
        }
        
        public Object getValue()
        {
            return _value;
        }
        
        public void setValue(Object value)
        {
            synchronized(this)
            {
                _value=value;
                if (value!=null)
                {
                    if (value instanceof Value)
                        ((Value)value).validate();
                    this.notifyAll();
                }
            }
        }
        

        /* ------------------------------------------------------------ */
        boolean isValid()
        {
            if (_lastModified==_resource.lastModified())
            {
                if (_mostRecentlyUsed!=this)
                {
                    Entry tp = _prev;
                    Entry tn = _next;

                    _next=_mostRecentlyUsed;
                    _mostRecentlyUsed=this;
                    if (_next!=null)
                        _next._prev=this;
                    _prev=null;

                    if (tp!=null)
                        tp._next=tn;
                    if (tn!=null)
                        tn._prev=tp;

                    if (_leastRecentlyUsed==this && tp!=null)
                        _leastRecentlyUsed=tp;
                }
                return true;
            }

            invalidate();
            return false;
        }

        public void invalidate()
        {
            synchronized(this)
            {
                // Invalidate it
                _cache.remove(_key);
                _key=null;
                _cacheSize=_cacheSize-(int)_resource.length();
                _cachedFiles--;
                
                if (_mostRecentlyUsed==this)
                    _mostRecentlyUsed=_next;
                else
                    _prev._next=_next;
                
                if (_leastRecentlyUsed==this)
                    _leastRecentlyUsed=_prev;
                else
                    _next._prev=_prev;
                
                _prev=null;
                _next=null;
                _resource=null;
                
                if (_value==null)
                    this.notifyAll();
                else if (_value instanceof Value)
                    ((Value)_value).invalidate();
                    
                _value=null;
            }
        }
    }
    
    public interface Value
    {
        public void validate();
        public void invalidate();
    }

}

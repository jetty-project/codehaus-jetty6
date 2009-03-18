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

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;

import org.mortbay.jetty.util.log.Log;

/* --------------------------------------------------------------------- */
/** 
 * 
 */
public class FilterHolder extends Holder
{    
    /* ------------------------------------------------------------ */
    private transient Filter _filter;
    private transient Config _config;
        
    /* ---------------------------------------------------------------- */
    /** Constructor for Serialization.
     */
    public FilterHolder()
    {
    }   
    
    /* ---------------------------------------------------------------- */
    /** Constructor for Serialization.
     */
    public FilterHolder(Class filter)
    {
        super (filter);
    }

    /* ---------------------------------------------------------------- */
    /** Constructor for existing filter.
     */
    public FilterHolder(Filter filter)
    {
        setFilter(filter);
    }
    
    /* ------------------------------------------------------------ */
    public void doStart()
        throws Exception
    {
        super.doStart();
        
        if (!javax.servlet.Filter.class
            .isAssignableFrom(_class))
        {
            String msg = _class+" is not a javax.servlet.Filter";
            super.stop();
            throw new IllegalStateException(msg);
        }

        if (_filter==null)
            _filter=(Filter)newInstance();
        
        _filter = getServletHandler().customizeFilter(_filter);
        
        _config=new Config();
        _filter.init(_config);
    }

    /* ------------------------------------------------------------ */
    public void doStop()
    {      
        if (_filter!=null)
        {
            try
            {
                destroyInstance(_filter);
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }
        if (!_extInstance)
            _filter=null;
        
        _config=null;
        super.doStop();   
    }

    /* ------------------------------------------------------------ */
    public void destroyInstance (Object o)
    throws Exception
    {
        if (o==null)
            return;
        Filter f = (Filter)o;
        f.destroy();
        getServletHandler().customizeFilterDestroy(f);
    }

    /* ------------------------------------------------------------ */
    public synchronized void setFilter(Filter filter)
    {
        _filter=filter;
        _extInstance=true;
        setHeldClass(filter.getClass());
        if (getName()==null)
            setName(filter.getClass().getName());
    }
    
    /* ------------------------------------------------------------ */
    public Filter getFilter()
    {
        return _filter;
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return getName();
    }
    

    public FilterRegistration getRegistration()
    {
        return new Registration();
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class Registration extends HolderRegistration implements FilterRegistration
    {
        /* ------------------------------------------------------------ */
        public boolean addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames)
        {
            illegalStateIfContextStarted();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterHolder(FilterHolder.this);
            mapping.setServletNames(servletNames);
            mapping.setDispatcherTypes(dispatcherTypes);
            if (isMatchAfter)
                _servletHandler.addFilterMapping(mapping);
            else
                _servletHandler.prependFilterMapping(mapping);

            return true;
        }

        public boolean addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns)
        {
            illegalStateIfContextStarted();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterHolder(FilterHolder.this);
            mapping.setPathSpecs(urlPatterns);
            mapping.setDispatcherTypes(dispatcherTypes);
            if (isMatchAfter)
                _servletHandler.addFilterMapping(mapping);
            else
                _servletHandler.prependFilterMapping(mapping);
            return true;
        }

    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    class Config extends HolderConfig implements FilterConfig
    {
        /* ------------------------------------------------------------ */
        public String getFilterName()
        {
            return _name;
        }
    }
}






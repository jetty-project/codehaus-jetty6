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

import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.mortbay.jetty.Handler;

/* --------------------------------------------------------------------- */
/** 
 * @author Greg Wilkins
 */
public class FilterHolder
    extends Holder
{

    /* ------------------------------------------------------------ */
    /** Dispatch type from name
     */
    public static int dispatch(String type)
    {
        if ("request".equalsIgnoreCase(type))
            return Handler.REQUEST;
        if ("forward".equalsIgnoreCase(type))
            return Handler.FORWARD;
        if ("include".equalsIgnoreCase(type))
            return Handler.INCLUDE;
        if ("error".equalsIgnoreCase(type))
            return Handler.ERROR;
        throw new IllegalArgumentException(type);
    }
    
    /* ------------------------------------------------------------ */
    private transient Filter _filter;
    private transient Config _config;
        
    /* ---------------------------------------------------------------- */
    /** Constructor for Serialization.
     */
    public FilterHolder()
    {
    }
    
    /* ------------------------------------------------------------ */
    public void doStart()
        throws Exception
    {
        super.doStart();
        
        if (!javax.servlet.Filter.class
            .isAssignableFrom(_class))
        {
            super.stop();
            throw new IllegalStateException(_class+" is not a javax.servlet.Filter");
        }

        _filter=(Filter)newInstance();
        _config=new Config();
        _filter.init(_config);
    }

    /* ------------------------------------------------------------ */
    public void doStop()
    {
        if (_filter!=null)
            _filter.destroy();
        _filter=null;
        _config=null;
        super.doStop();   
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
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    class Config implements FilterConfig
    {
        /* ------------------------------------------------------------ */
        public String getFilterName()
        {
            return _name;
        }

        /* ------------------------------------------------------------ */
        public ServletContext getServletContext()
        {
            return _servletHandler.getServletContext();
        }
        
        /* -------------------------------------------------------- */
        public String getInitParameter(String param)
        {
            return FilterHolder.this.getInitParameter(param);
        }
    
        /* -------------------------------------------------------- */
        public Enumeration getInitParameterNames()
        {
            return FilterHolder.this.getInitParameterNames();
        }
    }
    


}





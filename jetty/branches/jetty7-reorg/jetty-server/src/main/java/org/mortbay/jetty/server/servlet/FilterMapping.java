//========================================================================
//$Id: FilterMapping.java,v 1.2 2005/11/01 11:42:53 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.server.servlet;

import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.mortbay.jetty.http.PathMap;
import org.mortbay.jetty.server.Handler;


public class FilterMapping
{
    /** Dispatch types */
    public static final int DEFAULT=0;
    public static final int REQUEST=1;
    public static final int FORWARD=2;
    public static final int INCLUDE=4;
    public static final int ERROR=8;
    public static final int ASYNC=16;
    public static final int ALL=31;
    

    /* ------------------------------------------------------------ */
    /** Dispatch type from name
     */
    public static int dispatch(String type)
    {
        if ("request".equalsIgnoreCase(type))
            return REQUEST;
        if ("forward".equalsIgnoreCase(type))
            return FORWARD;
        if ("include".equalsIgnoreCase(type))
            return INCLUDE;
        if ("error".equalsIgnoreCase(type))
            return ERROR;
        if ("async".equalsIgnoreCase(type))
            return ASYNC;
        throw new IllegalArgumentException(type);
    }
    
    /* ------------------------------------------------------------ */
    /** Dispatch type from name
     */
    public static int dispatch(DispatcherType type)
    {
    	switch(type)
    	{
    	  case REQUEST:
    		  return REQUEST;
    	  case ASYNC:
    		  return ASYNC;
    	  case FORWARD:
    		  return FORWARD;
    	  case INCLUDE:
    		  return INCLUDE;
    	  case ERROR:
    		  return ERROR;
    	}
        throw new IllegalArgumentException(type.toString());
    }

    
    /* ------------------------------------------------------------ */
    /** Dispatch type from name
     */
    public static DispatcherType dispatch(int type)
    {
    	switch(type)
    	{
    	  case REQUEST:
    		  return DispatcherType.REQUEST;
    	  case ASYNC:
    		  return DispatcherType.ASYNC;
    	  case FORWARD:
    		  return DispatcherType.FORWARD;
    	  case INCLUDE:
    		  return DispatcherType.INCLUDE;
    	  case ERROR:
    		  return DispatcherType.ERROR;
    	}
        throw new IllegalArgumentException(""+type);
    }
	

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    
	
    private int _dispatches=DEFAULT;
    private String _filterName;
    private transient FilterHolder _holder;
    private String[] _pathSpecs;
    private String[] _servletNames;

    /* ------------------------------------------------------------ */
    public FilterMapping()
    {}
    
    /* ------------------------------------------------------------ */
    /** Check if this filter applies to a path.
     * @param path The path to check or null to just check type
     * @param type The type of request: __REQUEST,__FORWARD,__INCLUDE, __ASYNC or __ERROR.
     * @return True if this filter applies
     */
    boolean appliesTo(String path, int type)
    {
        if (appliesTo(type))
        {
            for (int i=0;i<_pathSpecs.length;i++)
                if (_pathSpecs[i]!=null &&  PathMap.match(_pathSpecs[i], path,true))
                    return true;
        }

        return false;
    }
    
    /* ------------------------------------------------------------ */
    /** Check if this filter applies to a particular dispatch type.
     * @param type The type of request:
     *      {@link Handler#REQUEST}, {@link Handler#FORWARD}, {@link Handler#INCLUDE} or {@link Handler#ERROR}.
     * @return <code>true</code> if this filter applies
     */
    boolean appliesTo(int type)
    {
    	if (_dispatches==0)
    		return type==REQUEST || type==ASYNC && _holder.isAsyncSupported();
        return (_dispatches&type)!=0;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the dispatches.
     */
    public int getDispatches()
    {
        return _dispatches;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the filterName.
     */
    public String getFilterName()
    {
        return _filterName;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the holder.
     */
    FilterHolder getFilterHolder()
    {
        return _holder;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the pathSpec.
     */
    public String[] getPathSpecs()
    {
        return _pathSpecs;
    }

    /* ------------------------------------------------------------ */
    public void setDispatcherTypes(EnumSet<DispatcherType> dispatcherTypes) 
    {
        _dispatches=DEFAULT;
        if (dispatcherTypes.contains(DispatcherType.ERROR)) 
            _dispatches|=ERROR;
        if (dispatcherTypes.contains(DispatcherType.FORWARD)) 
            _dispatches|=FORWARD;
        if (dispatcherTypes.contains(DispatcherType.INCLUDE)) 
            _dispatches|=INCLUDE;
        if (dispatcherTypes.contains(DispatcherType.REQUEST)) 
            _dispatches|=REQUEST;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param dispatches The dispatches to set.
     * @see Handler#DEFAULT
     * @see Handler#REQUEST
     * @see Handler#ERROR
     * @see Handler#FORWARD
     * @see Handler#INCLUDE
     */
    public void setDispatches(int dispatches)
    {
        _dispatches = dispatches;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param filterName The filterName to set.
     */
    public void setFilterName(String filterName)
    {
        _filterName = filterName;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param holder The holder to set.
     */
    void setFilterHolder(FilterHolder holder)
    {
        _holder = holder;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param pathSpecs The Path specifications to which this filter should be mapped. 
     */
    public void setPathSpecs(String[] pathSpecs)
    {
        _pathSpecs = pathSpecs;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param pathSpec The pathSpec to set.
     */
    public void setPathSpec(String pathSpec)
    {
        _pathSpecs = new String[]{pathSpec};
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the servletName.
     */
    public String[] getServletNames()
    {
        return _servletNames;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param servletNames Maps the {@link #setFilterName(String) named filter} to multiple servlets
     * @see #setServletName
     */
    public void setServletNames(String[] servletNames)
    {
        _servletNames = servletNames;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param servletName Maps the {@link #setFilterName(String) named filter} to a single servlet
     * @see #setServletNames
     */
    public void setServletName(String servletName)
    {
        _servletNames = new String[]{servletName};
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "(F="+_filterName+","+(_pathSpecs==null?"[]":Arrays.asList(_pathSpecs).toString())+","+(_servletNames==null?"[]":Arrays.asList(_servletNames).toString())+","+_dispatches+")"; 
    }

}

// ========================================================================
// Copyright 199-2004 Mort Bay Consulting Pty. Ltd.
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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.WrappedHandler;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;
import org.mortbay.util.MultiException;
import org.mortbay.util.MultiMap;
import org.mortbay.util.URIUtil;


/* --------------------------------------------------------------------- */
/** Servlet HttpHandler.
 * This handler maps requests to servlets that implement the
 * javax.servlet.http.HttpServlet API.
 * <P>
 * This handler does not implement the full J2EE features and is intended to
 * be used when a full web application is not required.  Specifically filters
 * and request wrapping are not supported.
 * <P>
 * If a SessionManager is not added to the handler before it is
 * initialized, then a HashSessionManager with a standard
 * java.util.Random generator is created.
 * <P>
 * @see org.mortbay.jetty.servlet.WebAppContext
 * @author Greg Wilkins
 */
public class ServletHandler extends WrappedHandler
{
    private static String __AllowString="GET, HEAD, POST, OPTIONS, TRACE";

    /* ------------------------------------------------------------ */
    public static final String __DEFAULT_SERVLET="default";
    public static final String __J_S_CONTEXT_TEMPDIR="javax.servlet.context.tempdir";
    public static final String __J_S_ERROR_EXCEPTION="javax.servlet.error.exception";
    public static final String __J_S_ERROR_EXCEPTION_TYPE="javax.servlet.error.exception_type";
    public static final String __J_S_ERROR_MESSAGE="javax.servlet.error.message";
    public static final String __J_S_ERROR_REQUEST_URI="javax.servlet.error.request_uri";
    public static final String __J_S_ERROR_SERVLET_NAME="javax.servlet.error.servlet_name";
    public static final String __J_S_ERROR_STATUS_CODE="javax.servlet.error.status_code";
    
    /* ------------------------------------------------------------ */
    private static final boolean __Slosh2Slash=File.separatorChar=='\\';

    
    
    /* ------------------------------------------------------------ */
    private ContextHandler _contextHandler;
    private ContextHandler.Context _servletContext;
    private FilterHolder[] _filterHolders;
    private FilterMapping[] _filterMappings;
    private boolean _filterChainsCached=true;
    
    private ServletHolder[] _servlets;
    private ServletMapping[] _servletMappings;
    
    private boolean _initializeAtStart=true;
    
    private transient Map _filterNameMap;
    private transient List _filterPathMappings;
    private transient MultiMap _filterNameMappings;
    
    private transient Map _servletNameMap;
    private transient PathMap _servletPathMap;
    
    protected transient HashMap _chainCache[];
    protected transient HashMap _namedChainCache[];


    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public ServletHandler()
    {
    }

    /* ----------------------------------------------------------------- */
    protected synchronized void doStart()
        throws Exception
    {
        _servletContext=ContextHandler.getCurrentContext();
        _contextHandler=_servletContext.getContextHandler();
        
        updateMappings();
        
        if (isInitializeAtStart())
            initialize();

        if(_filterChainsCached && _filterHolders!=null && _filterHolders.length>0)
        {
            _chainCache=     new HashMap[]{null,new HashMap(),new HashMap(),null,new HashMap(),null,null,null,new HashMap()};
            _namedChainCache=new HashMap[]{null,null,new HashMap(),null,new HashMap(),null,null,null,new HashMap()};
        }
        super.doStart();
    }   
    
    /* ----------------------------------------------------------------- */
    protected synchronized void doStop()
        throws Exception
    {
        super.doStop();
        
        // Stop filters
        if (_filterHolders!=null)
        {
            for (int i=_filterHolders.length; i-->0;)
            {
                try { _filterHolders[i].stop(); }catch(Exception e){Log.warn(Log.EXCEPTION,e);}
            }
        }
        
        // Stop servlets
        if (_servlets!=null)
        {
            for (int i=_servlets.length; i-->0;)
            {
                try { _servlets[i].stop(); }catch(Exception e){Log.warn(Log.EXCEPTION,e);}
            }
        }

        _filterNameMap=null;
        _filterPathMappings=null;
        _filterNameMappings=null;
        _servletNameMap=null;
        _servletPathMap=null;
        _chainCache=null;
        _namedChainCache=null;
    }

    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the contextLog.
     */
    public Object getContextLog()
    {
        return null;
    }
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the filterMappings.
     */
    public FilterMapping[] getFilterMappings()
    {
        return _filterMappings;
    }
    
    /* ------------------------------------------------------------ */
    /** Get Filters.
     * @return Array of defined servlets
     */
    public FilterHolder[] getFilters()
    {
        return _filterHolders;
    }
    
    /* ------------------------------------------------------------ */
    /** ServletHolder matching path.
     * @param pathInContext Path within _context.
     * @return PathMap Entries pathspec to ServletHolder
     */
    private Map.Entry getHolderEntry(String pathInContext)
    {
        return _servletPathMap.getMatch(pathInContext);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param ipath
     * @return
     */
    public RequestDispatcher getRequestDispatcher(String uriInContext)
    {
        if (uriInContext == null)
            return null;

        if (!uriInContext.startsWith("/"))
            return null;
        
        try
        {
            String query=null;
            int q=0;
            if ((q=uriInContext.indexOf('?'))>0)
            {
                query=uriInContext.substring(q+1);
                uriInContext=uriInContext.substring(0,q);
            }
            if ((q=uriInContext.indexOf(';'))>0)
                uriInContext=uriInContext.substring(0,q);

            String pathInContext=URIUtil.canonicalPath(URIUtil.decodePath(uriInContext));
            String uri=URIUtil.addPaths(_contextHandler.getContextPath(), uriInContext);
            return new Dispatcher(_contextHandler, uri, pathInContext, query);
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
        return null;
    }

    /* ------------------------------------------------------------ */
    public ServletContext getServletContext()
    {
        return _servletContext;
    }
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the servletMappings.
     */
    public ServletMapping[] getServletMappings()
    {
        return _servletMappings;
    }
        
    /* ------------------------------------------------------------ */
    /** Get Servlets.
     * @return Array of defined servlets
     */
    public ServletHolder[] getServlets()
    {
        return _servlets;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public boolean handle(String target, HttpServletRequest request,HttpServletResponse response, int type)
         throws IOException
    {
        if (!isStarted())
            return false;

        // Get the base requests
        Request base_request=(request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
        String old_servlet_path=null;
        String old_path_info=null;

        
        try
        {
            old_servlet_path=base_request.getServletPath();
            old_path_info=base_request.getPathInfo();

            ServletHolder servlet_holder=null;
            FilterChain chain=null;
            
            // find the servlet
            if (target.startsWith("/"))
            {
                // Look for the servlet by path
                Map.Entry map=getHolderEntry(target);
                if (map!=null)
                {
                    servlet_holder=(ServletHolder)map.getValue();
                    if(Log.isDebugEnabled())Log.debug("servlet="+servlet_holder);
                    
                    String servlet_path_spec=(String)map.getKey(); 
                    String servlet_path=PathMap.pathMatch(servlet_path_spec,target);
                    String path_info=PathMap.pathInfo(servlet_path_spec,target);
                    
                    if (type==INCLUDE)
                    {
                        base_request.setAttribute(Dispatcher.__INCLUDE_SERVLET_PATH,servlet_path);
                        base_request.setAttribute(Dispatcher.__INCLUDE_PATH_INFO, path_info);
                    }
                    else
                    {
                        base_request.setServletPath(servlet_path);
                        base_request.setPathInfo(path_info);
                    }
                    
                    if (servlet_holder!=null && _filterMappings!=null && _filterMappings.length>0)
                        chain=getFilterChain(type, target, servlet_holder);
                }      
            }
            else
            {
                // look for a servlet by name!
                servlet_holder=(ServletHolder)_servletNameMap.get(target);
                if (servlet_holder!=null && _filterMappings!=null && _filterMappings.length>0)
                    chain=getFilterChain(type, null,servlet_holder);
            }

            if (Log.isDebugEnabled()) 
            {
                Log.debug("chain="+chain);
                Log.debug("servelet holder="+servlet_holder);
            }
            
            // Do the filter/handling thang
            if (chain!=null)
                chain.doFilter(request, response);
            else if (servlet_holder != null)
                servlet_holder.handle(request,response);
            else
                notFound(request, response);
        }
        catch(RetryRequest e)
        {
            throw e;
        }
        catch(Exception e)
        {
            Throwable th=e;
            while (th instanceof ServletException)
            {
                Throwable cause=((ServletException)th).getRootCause();
                if (cause==th || cause==null)
                    break;
                th=cause;
            }
            
            
            /* TODO
            if (th instanceof HttpException)
                throw (HttpException)th;
            if (th instanceof EOFException)
                throw (IOException)th;
            else */ 
            if (Log.isDebugEnabled() || !( th instanceof java.io.IOException))
            {
                Log.warn(request.getRequestURI()+": ",th);
                if(Log.isDebugEnabled())
                {
                    Log.warn(request.getRequestURI()+": ",th);
                    Log.debug(request.toString());
                }
            }
            // TODO clean up
            Log.warn(request.getRequestURI(), th);
            
            // TODO httpResponse.getHttpConnection().forceClose();
            if (!response.isCommitted())
            {
                request.setAttribute(ServletHandler.__J_S_ERROR_EXCEPTION_TYPE,th.getClass());
                request.setAttribute(ServletHandler.__J_S_ERROR_EXCEPTION,th);
                if (th instanceof UnavailableException)
                {
                    UnavailableException ue = (UnavailableException)th;
                    if (ue.isPermanent())
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,th.getMessage());
                    else
                        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,th.getMessage());
                }
                else
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,th.getMessage());
            }
            else
                if(Log.isDebugEnabled())Log.debug("Response already committed for handling "+th);
        }
        catch(Error e)
        {   
            Log.warn("Error for "+request.getRequestURI(),e);
            if(Log.isDebugEnabled())Log.debug(request.toString());
            
            // TODO httpResponse.getHttpConnection().forceClose();
            if (!response.isCommitted())
            {
                request.setAttribute(ServletHandler.__J_S_ERROR_EXCEPTION_TYPE,e.getClass());
                request.setAttribute(ServletHandler.__J_S_ERROR_EXCEPTION,e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
            }
            else
                if(Log.isDebugEnabled())Log.debug("Response already committed for handling ",e);
        }
        finally
        {
            if (type!=INCLUDE)
            base_request.setServletPath(old_servlet_path);
            base_request.setPathInfo(old_path_info); 
        }
        return true;
    }

    /* ------------------------------------------------------------ */
    private FilterChain getFilterChain(int requestType, String pathInContext, ServletHolder servletHolder) 
    {
        String key=pathInContext==null?servletHolder.getName():pathInContext;
        
        if (_filterChainsCached && _chainCache!=null)
        {
            synchronized(this)
            {
                if(_chainCache[requestType].containsKey(key))
                    return (FilterChain)_chainCache[requestType].get(key);
            }
        }
        
        
        // Build list of filters
        Object filters= null;
    
        
        // Path filters
        if (pathInContext!=null && _filterPathMappings!=null)
        {
            for (int i= 0; i < _filterPathMappings.size(); i++)
            {
                FilterMapping mapping = (FilterMapping)_filterPathMappings.get(i);
                if (mapping.appliesTo(pathInContext, requestType))
                    filters= LazyList.add(filters, mapping.getFilterHolder());
            }
        }

        // Servlet name filters
        if (servletHolder != null && _filterNameMappings!=null && _filterNameMappings.size() > 0)
        {
            // Servlet name filters
            if (_filterNameMappings.size() > 0)
            {
                Object o= _filterNameMappings.get(servletHolder.getName());
                for (int i=0; i<LazyList.size(o);i++)
                {
                    FilterMapping mapping = (FilterMapping)LazyList.get(o,i);
                    if (mapping.appliesTo(requestType))
                        filters=LazyList.add(filters,mapping.getFilterHolder());
                }
                
                o= _filterNameMappings.get("*");
                for (int i=0; i<LazyList.size(o);i++)
                {
                    FilterMapping mapping = (FilterMapping)LazyList.get(o,i);
                    if (mapping.appliesTo(requestType))
                        filters=LazyList.add(filters,mapping.getFilterHolder());
                }
            }
        }
        
        if (filters==null)
            return null;
        
        FilterChain chain = null;
        if (_filterChainsCached)
        {
            synchronized(this)
            {
                if (LazyList.size(filters) > 0)
                    chain= new CachedChain(filters, servletHolder);
                _chainCache[requestType].put(key,chain);
            }
        }
        else if (LazyList.size(filters) > 0)
            chain = new Chain(filters, servletHolder);
    
        return chain;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the initializeAtStart.
     */
    public boolean isInitializeAtStart()
    {
        return _initializeAtStart;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param initializeAtStart The initializeAtStart to set.
     */
    public void setInitializeAtStart(boolean initializeAtStart)
    {
        _initializeAtStart = initializeAtStart;
    }
    
    /* ------------------------------------------------------------ */
    /** Initialize filters and load-on-startup servlets.
     * Called automatically from start if autoInitializeServlet is true.
     */
    public void initialize()
        throws Exception
    {
        MultiException mx = new MultiException();

        // Start filters
        if (_filterHolders!=null)
        {
            for (int i=_filterHolders.length; i-->0;)
                _filterHolders[i].start();
        }
        
        if (_servlets!=null)
        {
            
            // Sort and Initialize servlets
            ServletHolder[] servlets = (ServletHolder[])_servlets.clone();
            Arrays.sort(servlets);
            for (int i=0; i<servlets.length; i++)
            {
                try
                {
                    if (servlets[i].getClassName()==null && servlets[i].getForcedPath()!=null)
                    {
                        ServletHolder forced_holder = (ServletHolder)_servletPathMap.match(servlets[i].getForcedPath());
                        if (forced_holder==null)
                        {    
                            mx.add(new IllegalStateException("No servlet for "+servlets[i].getForcedPath()));
                            continue;
                        }
                        servlets[i].setClassName(forced_holder.getClassName());
                    }
                    
                    servlets[i].start();
                }
                catch(Exception e)
                {
                    Log.debug(Log.EXCEPTION,e);
                    mx.add(e);
                }
            } 
            mx.ifExceptionThrow();  
        }
    }
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the filterChainsCached.
     */
    public boolean isFilterChainsCached()
    {
        return _filterChainsCached;
    }
    
    /* ------------------------------------------------------------ */
    protected synchronized void updateMappings()
    {   
        // Map servlet names to holders
        if (_servlets==null)
        {
            _servletNameMap=null;
        }
        else
        {   
            HashMap nm = new HashMap();
            
            // update the maps
            for (int i=0;i<_servlets.length;i++)
            {
                nm.put(_servlets[i].getName(),_servlets[i]);
                _servlets[i].setServletHandler(this);
            }
            _servletNameMap=nm;
        }

        // Map servlet paths to holders
        if (_servletMappings==null || _servletNameMap==null)
        {
            _servletPathMap=null;
        }
        else
        {
            PathMap pm = new PathMap();
            
            // update the maps
            for (int i=0;i<_servletMappings.length;i++)
            {
                ServletHolder servlet_holder = (ServletHolder)_servletNameMap.get(_servletMappings[i].getServletName());
                if (servlet_holder==null)
                    throw new IllegalStateException("No such servlet: "+_servletMappings[i].getServletName());
                else if (_servletMappings[i].getPathSpecs()!=null)
                {
                    String[] pathSpecs = _servletMappings[i].getPathSpecs();
                    for (int j=0;j<pathSpecs.length;j++)
                        if (pathSpecs[j]!=null)
                            pm.put(pathSpecs[j],servlet_holder);
                }
            }
            
            _servletPathMap=pm;
        }
        
        
        
        // update filter name map
        if (_filterHolders==null)
        {
            _filterNameMap=null;
        }
        else
        {   
            HashMap nm = new HashMap();
            for (int i=0;i<_filterHolders.length;i++)
            {
                nm.put(_filterHolders[i].getName(),_filterHolders[i]);
                _filterHolders[i].setServletHandler(this);
            }
            _filterNameMap=nm;
        }

        // update filter mappings
        if (_filterMappings==null)
        {
            _filterPathMappings=null;
            _filterNameMappings=null;
        }
        else 
        {
            _filterPathMappings=new ArrayList();
            _filterNameMappings=new MultiMap();
            for (int i=0;i<_filterMappings.length;i++)
            {
                FilterHolder filter_holder = (FilterHolder)_filterNameMap.get(_filterMappings[i].getFilterName());
                if (filter_holder==null)
                    throw new IllegalStateException("No filter named "+_filterMappings[i].getFilterName());
                _filterMappings[i].setFilterHolder(filter_holder);    
                if (_filterMappings[i].getPathSpecs()!=null)
                    _filterPathMappings.add(_filterMappings[i]);
                
                if (_filterMappings[i].getServletNames()!=null)
                {
                    String[] names=_filterMappings[i].getServletNames();
                    for (int j=0;j<names.length;j++)
                    {
                        if (names[j]!=null)
                            _filterNameMappings.add(names[j], _filterMappings[i]);  
                    }
                }
            }
        }

        if (Log.isDebugEnabled()) 
        {
            Log.debug("filterNameMap="+_filterNameMap);
            Log.debug("pathFilters="+_filterPathMappings);
            Log.debug("servletFilterMap="+_filterNameMappings);
            Log.debug("servletPathMap="+_servletPathMap);
            Log.debug("servletNameMap="+_servletNameMap);
        }
        
        
        try
        {
            if (isStarted())
                initialize();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    /* ------------------------------------------------------------ */
    protected void notFound(HttpServletRequest request,
                  HttpServletResponse response)
        throws IOException
    {
        if(Log.isDebugEnabled())Log.debug("Not Found "+request.getRequestURI());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        
    }
    /* ------------------------------------------------------------ */
    /**
     * @param contextLog The contextLog to set.
     */
    public void setContextLog(Object contextLog)
    {
        // TODO
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param filterChainsCached The filterChainsCached to set.
     */
    public void setFilterChainsCached(boolean filterChainsCached)
    {
        _filterChainsCached = filterChainsCached;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param filterMappings The filterMappings to set.
     */
    public void setFilterMappings(FilterMapping[] filterMappings)
    {
        _filterMappings = filterMappings;
        if (isStarted())
            updateMappings();
    }
    
    /* ------------------------------------------------------------ */
    /** Get Filters.
     * @return Array of defined servlets
     */
    public synchronized void setFilters(FilterHolder[] holders)
    {
        _filterHolders=holders;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param servletMappings The servletMappings to set.
     */
    public void setServletMappings(ServletMapping[] servletMappings)
    {
        _servletMappings = servletMappings;
        if (isStarted())
            updateMappings();
    }
    
    /* ------------------------------------------------------------ */
    /** Get Servlets.
     * @return Array of defined servlets
     */
    public synchronized void setServlets(ServletHolder[] holders)
    {
        _servlets=holders;
        updateMappings();
    }


    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class CachedChain implements FilterChain
    {
        FilterHolder _filterHolder;
        CachedChain _next;
        ServletHolder _servletHolder;

        /* ------------------------------------------------------------ */
        CachedChain(Object filters, ServletHolder servletHolder)
        {
            if (LazyList.size(filters)>0)
            {
                _filterHolder=(FilterHolder)LazyList.get(filters, 0);
                filters=LazyList.remove(filters,0);
                _next=new CachedChain(filters,servletHolder);
            }
            else
                _servletHolder=servletHolder;
        }

        /* ------------------------------------------------------------ */
        public void doFilter(ServletRequest request, ServletResponse response) 
            throws IOException, ServletException
        {
            // pass to next filter
            if (_filterHolder!=null)
            {
                if (Log.isDebugEnabled())
                    Log.debug("call filter " + _filterHolder);
                Filter filter= _filterHolder.getFilter();
                filter.doFilter(request, response, _next);
                return;
            }

            // Call servlet
            if (_servletHolder != null)
            {
                if (Log.isDebugEnabled())
                    Log.debug("call servlet " + _servletHolder);
                _servletHolder.handle(request, response);
            }
            else // Not found
                notFound((HttpServletRequest)request, (HttpServletResponse)response);
        }
        
        public String toString()
        {
            if (_filterHolder!=null)
                return _filterHolder+"->"+_next.toString();
            if (_servletHolder!=null)
                return _servletHolder.toString();
            return "null";
        }
    }  
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class Chain implements FilterChain
    {
        int _filter= 0;
        Object _filters;
        ServletHolder _servletHolder;

        /* ------------------------------------------------------------ */
        Chain(Object filters, ServletHolder servletHolder)
        {
            _filters= filters;
            _servletHolder= servletHolder;
        }

        /* ------------------------------------------------------------ */
        public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException
        {
            if (Log.isDebugEnabled()) Log.debug("doFilter " + _filter);

            // pass to next filter
            if (_filter < LazyList.size(_filters))
            {
                FilterHolder holder= (FilterHolder)LazyList.get(_filters, _filter++);
                if (Log.isDebugEnabled()) Log.debug("call filter " + holder);
                Filter filter= holder.getFilter();
                filter.doFilter(request, response, this);
                return;
            }

            // Call servlet
            if (_servletHolder != null)
            {
                if (Log.isDebugEnabled()) Log.debug("call servlet " + _servletHolder);
                _servletHolder.handle(request, response);
            }
            else // Not found
                notFound((HttpServletRequest)request, (HttpServletResponse)response);
        }
        
        public String toString()
        {
            StringBuffer b = new StringBuffer();
            for (int i=0; i<LazyList.size(_filters);i++)
            {
                b.append(LazyList.get(_filters, i).toString());
                b.append("->");
            }
            b.append(_servletHolder);
            return b.toString();
        }
    }


}

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

package org.mortbay.jetty;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.util.Attributes;
import org.mortbay.jetty.util.LazyList;
import org.mortbay.jetty.util.MultiMap;
import org.mortbay.jetty.util.UrlEncoded;

/* ------------------------------------------------------------ */
/** Servlet RequestDispatcher.
 * 
 * @author Greg Wilkins (gregw)
 */
public class Dispatcher implements RequestDispatcher
{
    /** Dispatch include attribute names */
    public final static String __INCLUDE_PREFIX="javax.servlet.include.";
    public final static String __INCLUDE_REQUEST_URI= INCLUDE_REQUEST_URI;
    public final static String __INCLUDE_CONTEXT_PATH= INCLUDE_CONTEXT_PATH;
    public final static String __INCLUDE_SERVLET_PATH= INCLUDE_SERVLET_PATH;
    public final static String __INCLUDE_PATH_INFO= INCLUDE_PATH_INFO;
    public final static String __INCLUDE_QUERY_STRING= INCLUDE_QUERY_STRING;

    /** Dispatch include attribute names */
    public final static String __FORWARD_PREFIX="javax.servlet.forward.";
    public final static String __FORWARD_REQUEST_URI= FORWARD_REQUEST_URI;
    public final static String __FORWARD_CONTEXT_PATH= FORWARD_CONTEXT_PATH;
    public final static String __FORWARD_SERVLET_PATH= FORWARD_SERVLET_PATH;
    public final static String __FORWARD_PATH_INFO= FORWARD_PATH_INFO;
    public final static String __FORWARD_QUERY_STRING= FORWARD_QUERY_STRING;

    /** JSP attributes */
    public final static String __JSP_FILE="org.apache.catalina.jsp_file";

    /* ------------------------------------------------------------ */
    private ContextHandler _contextHandler;
    private String _uri;
    private String _path;
    private String _dQuery;
    private String _named;
    
    /* ------------------------------------------------------------ */
    /**
     * @param contextHandler
     * @param uriInContext
     * @param pathInContext
     * @param query
     */
    public Dispatcher(ContextHandler contextHandler, String uri, String pathInContext, String query)
    {
        _contextHandler=contextHandler;
        _uri=uri;
        _path=pathInContext;
        _dQuery=query;
    }


    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param servletHandler
     * @param name
     */
    public Dispatcher(ContextHandler contextHandler,String name)
        throws IllegalStateException
    {
        _contextHandler=contextHandler;
        _named=name;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        forward(request, response, DispatcherType.FORWARD);
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void error(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        forward(request, response, DispatcherType.ERROR);
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        Request base_request=(request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
        request.removeAttribute(__JSP_FILE); // TODO remove when glassfish 1044 is fixed
        
        // TODO - allow stream or writer????
        
        DispatcherType old_type = base_request.getDispatcherType();
        Attributes old_attr=base_request.getAttributes();
        MultiMap old_params=base_request.getParameters();
        try
        {
            base_request.setDispatcherType(DispatcherType.INCLUDE);
            base_request.getConnection().include();
            if (_named!=null)
                _contextHandler.doHandle(_named, base_request,(HttpServletRequest)request, (HttpServletResponse)response);
            else 
            {
                String query=_dQuery;
                
                if (query!=null)
                {
                    MultiMap parameters=new MultiMap();
                    UrlEncoded.decodeTo(query,parameters,request.getCharacterEncoding());
                    
                    if (old_params!=null && old_params.size()>0)
                    {
                        // Merge parameters.
                        Iterator iter = old_params.entrySet().iterator();
                        while (iter.hasNext())
                        {
                            Map.Entry entry = (Map.Entry)iter.next();
                            String name=(String)entry.getKey();
                            Object values=entry.getValue();
                            for (int i=0;i<LazyList.size(values);i++)
                                parameters.add(name, LazyList.get(values, i));
                        }
                        
                    }
                    base_request.setParameters(parameters);
                }
                
                IncludeAttributes attr = new IncludeAttributes(old_attr); 
                
                attr._requestURI=_uri;
                attr._contextPath=_contextHandler.getContextPath();
                attr._servletPath=null; // set by ServletHandler
                attr._pathInfo=_path;
                attr._query=query;
                
                base_request.setAttributes(attr);
                
                _contextHandler.doHandle(_named==null?_path:_named, base_request,(HttpServletRequest)request, (HttpServletResponse)response);
            }
        }
        finally
        {
            base_request.setAttributes(old_attr);
            base_request.getConnection().included();
            base_request.setParameters(old_params);
            base_request.setDispatcherType(old_type);
        }
    }

    
    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    protected void forward(ServletRequest request, ServletResponse response, DispatcherType dispatch) throws ServletException, IOException
    {
        Request base_request=(request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
        Response base_response=(Response)base_request.getResponse();
        base_response.fwdReset();
        request.removeAttribute(__JSP_FILE); // TODO remove when glassfish 1044 is fixed
        
        String old_uri=base_request.getRequestURI();
        String old_context_path=base_request.getContextPath();
        String old_servlet_path=base_request.getServletPath();
        String old_path_info=base_request.getPathInfo();
        String old_query=base_request.getQueryString();
        Attributes old_attr=base_request.getAttributes();
        MultiMap old_params=base_request.getParameters();
        DispatcherType old_type=base_request.getDispatcherType();
        
        try
        {
            base_request.setDispatcherType(dispatch);
            
            if (_named!=null)
                _contextHandler.doHandle(_named, base_request,(HttpServletRequest)request, (HttpServletResponse)response);
            else 
            {
                String query=_dQuery;
                
                if (query!=null)
                {
                    MultiMap parameters=new MultiMap();
                    UrlEncoded.decodeTo(query,parameters,request.getCharacterEncoding());
                 
                    boolean rewrite_old_query = false;

                    if( old_params == null )
                    {
                        base_request.getParameterNames();    // force parameters to be evaluated
                        old_params = base_request.getParameters();
                    }
                    
                    if (old_params!=null && old_params.size()>0)
                    {
                        // Merge parameters; new parameters of the same name take precedence.
                        Iterator iter = old_params.entrySet().iterator();
                        while (iter.hasNext())
                        {
                            Map.Entry entry = (Map.Entry)iter.next();
                            String name=(String)entry.getKey();
                            
                            if (parameters.containsKey(name))
                            {
                                rewrite_old_query = true;
                            }
                            else
                            {
                                Object values=entry.getValue();
                                for (int i=0;i<LazyList.size(values);i++)
                                {
                                    parameters.add(name, LazyList.get(values, i));
                                }
                            }
                        }
                    }
                    
                    if (old_query != null && old_query.length()>0)
                    {
                        if ( rewrite_old_query )
                        {
                            StringBuilder overridden_query_string = new StringBuilder();
                            MultiMap overridden_old_query = new MultiMap();
                            UrlEncoded.decodeTo(old_query,overridden_old_query,request.getCharacterEncoding());
    
                            MultiMap overridden_new_query = new MultiMap(); 
                            UrlEncoded.decodeTo(query,overridden_new_query,request.getCharacterEncoding());

                            Iterator iter = overridden_old_query.entrySet().iterator();
                            while (iter.hasNext())
                            {
                                Map.Entry entry = (Map.Entry)iter.next();
                                String name=(String)entry.getKey();
                                if(!overridden_new_query.containsKey(name))
                                {
                                    Object values=entry.getValue();
                                    for (int i=0;i<LazyList.size(values);i++)
                                    {
                                        overridden_query_string.append("&"+name+"="+LazyList.get(values, i));
                                    }
                                }
                            }
                            
                            query = query + overridden_query_string;
                        }
                        else 
                        {
                            query=query+"&"+old_query;
                        }
                   }

                    base_request.setParameters(parameters);
                    base_request.setQueryString(query);
                }
                
                ForwardAttributes attr = new ForwardAttributes(old_attr); 
                
                //If we have already been forwarded previously, then keep using the established 
                //original value. Otherwise, this is the first forward and we need to establish the values.
                //Note: the established value on the original request for pathInfo and
                //for queryString is allowed to be null, but cannot be null for the other values.
                if ((String)old_attr.getAttribute(__FORWARD_REQUEST_URI) != null)
                {
                    attr._pathInfo=(String)old_attr.getAttribute(__FORWARD_PATH_INFO);
                    attr._query=(String)old_attr.getAttribute(__FORWARD_QUERY_STRING);
                    attr._requestURI=(String)old_attr.getAttribute(__FORWARD_REQUEST_URI);
                    attr._contextPath=(String)old_attr.getAttribute(__FORWARD_CONTEXT_PATH);
                    attr._servletPath=(String)old_attr.getAttribute(__FORWARD_SERVLET_PATH);
                }
                else
                {
                    attr._pathInfo=old_path_info;
                    attr._query=old_query;
                    attr._requestURI=old_uri;
                    attr._contextPath=old_context_path;
                    attr._servletPath=old_servlet_path;
                }                
   
              
                
                base_request.setRequestURI(_uri);
                base_request.setContextPath(_contextHandler.getContextPath());
                base_request.setAttributes(attr);
                base_request.setQueryString(query);
                
                _contextHandler.doHandle(_path, base_request,(HttpServletRequest)request, (HttpServletResponse)response);
                
                if (base_request.getConnection().getResponse().isWriting())
                {
                    try {response.getWriter().close();}
                    catch(IllegalStateException e) { response.getOutputStream().close(); }
                }
                else
                {
                    try {response.getOutputStream().close();}
                    catch(IllegalStateException e) { response.getWriter().close(); }
                }
            }
        }
        finally
        {
            base_request.setRequestURI(old_uri);
            base_request.setContextPath(old_context_path);
            base_request.setServletPath(old_servlet_path);
            base_request.setPathInfo(old_path_info);
            base_request.setAttributes(old_attr);
            base_request.setParameters(old_params);
            base_request.setQueryString(old_query);
            base_request.setDispatcherType(old_type);
        }
    }


    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class ForwardAttributes implements Attributes
    {
        Attributes _attr;
        
        String _requestURI;
        String _contextPath;
        String _servletPath;
        String _pathInfo;
        String _query;
        
        ForwardAttributes(Attributes attributes)
        {
            _attr=attributes;
        }
        
        /* ------------------------------------------------------------ */
        public Object getAttribute(String key)
        {
            if (Dispatcher.this._named==null)
            {
                if (key.equals(__FORWARD_PATH_INFO))    
                    return _pathInfo;
                if (key.equals(__FORWARD_REQUEST_URI))  
                    return _requestURI;
                if (key.equals(__FORWARD_SERVLET_PATH)) 
                    return _servletPath;
                if (key.equals(__FORWARD_CONTEXT_PATH)) 
                    return _contextPath;
                if (key.equals(__FORWARD_QUERY_STRING)) 
                    return _query;
            }
            
            if (key.startsWith(__INCLUDE_PREFIX))
                return null;
            
            return _attr.getAttribute(key);
        }
        
        /* ------------------------------------------------------------ */
        public Enumeration getAttributeNames()
        {
            HashSet set=new HashSet();
            Enumeration e=_attr.getAttributeNames();
            while(e.hasMoreElements())
            {
                String name=(String)e.nextElement();
                if (!name.startsWith(__INCLUDE_PREFIX) &&
                    !name.startsWith(__FORWARD_PREFIX))
                    set.add(name);
            }
            
            if (_named==null)
            {
                if (_pathInfo!=null)
                    set.add(__FORWARD_PATH_INFO);
                else
                    set.remove(__FORWARD_PATH_INFO);
                set.add(__FORWARD_REQUEST_URI);
                set.add(__FORWARD_SERVLET_PATH);
                set.add(__FORWARD_CONTEXT_PATH);
                if (_query!=null)
                    set.add(__FORWARD_QUERY_STRING);
                else
                    set.remove(__FORWARD_QUERY_STRING);
            }

            return Collections.enumeration(set);
        }
        
        /* ------------------------------------------------------------ */
        public void setAttribute(String key, Object value)
        {
            if (_named==null && key.startsWith("javax.servlet."))
            {
                if (key.equals(__FORWARD_PATH_INFO))         
                    _pathInfo=(String)value;
                else if (key.equals(__FORWARD_REQUEST_URI))  
                    _requestURI=(String)value;
                else if (key.equals(__FORWARD_SERVLET_PATH)) 
                    _servletPath=(String)value;
                else if (key.equals(__FORWARD_CONTEXT_PATH)) 
                    _contextPath=(String)value;
                else if (key.equals(__FORWARD_QUERY_STRING)) 
                    _query=(String)value;
                
                else if (value==null)
                    _attr.removeAttribute(key);
                else
                    _attr.setAttribute(key,value); 
            }
            else if (value==null)
                _attr.removeAttribute(key);
            else
                _attr.setAttribute(key,value);
        }
        
        /* ------------------------------------------------------------ */
        public String toString() 
        {
            return "FORWARD+"+_attr.toString();
        }

        /* ------------------------------------------------------------ */
        public void clearAttributes()
        {
            throw new IllegalStateException();
        }

        /* ------------------------------------------------------------ */
        public void removeAttribute(String name)
        {
            setAttribute(name,null);
        }
    }

    /* ------------------------------------------------------------ */
    private class IncludeAttributes implements Attributes
    {
        Attributes _attr;
        
        String _requestURI;
        String _contextPath;
        String _servletPath;
        String _pathInfo;
        String _query;
        
        IncludeAttributes(Attributes attributes)
        {
            _attr=attributes;
        }
        
        /* ------------------------------------------------------------ */
        /* ------------------------------------------------------------ */
        /* ------------------------------------------------------------ */
        public Object getAttribute(String key)
        {
            if (Dispatcher.this._named==null)
            {
                if (key.equals(__INCLUDE_PATH_INFO))    return _pathInfo;
                if (key.equals(__INCLUDE_SERVLET_PATH)) return _servletPath;
                if (key.equals(__INCLUDE_CONTEXT_PATH)) return _contextPath;
                if (key.equals(__INCLUDE_QUERY_STRING)) return _query;
                if (key.equals(__INCLUDE_REQUEST_URI))  return _requestURI;
            }
            else if (key.startsWith(__INCLUDE_PREFIX)) 
                    return null;
            
            
            return _attr.getAttribute(key);
        }
        
        /* ------------------------------------------------------------ */
        public Enumeration getAttributeNames()
        {
            HashSet set=new HashSet();
            Enumeration e=_attr.getAttributeNames();
            while(e.hasMoreElements())
            {
                String name=(String)e.nextElement();
                if (!name.startsWith(__INCLUDE_PREFIX))
                    set.add(name);
            }
            
            if (_named==null)
            {
                if (_pathInfo!=null)
                    set.add(__INCLUDE_PATH_INFO);
                else
                    set.remove(__INCLUDE_PATH_INFO);
                set.add(__INCLUDE_REQUEST_URI);
                set.add(__INCLUDE_SERVLET_PATH);
                set.add(__INCLUDE_CONTEXT_PATH);
                if (_query!=null)
                    set.add(__INCLUDE_QUERY_STRING);
                else
                    set.remove(__INCLUDE_QUERY_STRING);
            }
            
            return Collections.enumeration(set);
        }
        
        /* ------------------------------------------------------------ */
        public void setAttribute(String key, Object value)
        {
            if (_named==null && key.startsWith("javax.servlet."))
            {
                if (key.equals(__INCLUDE_PATH_INFO))         _pathInfo=(String)value;
                else if (key.equals(__INCLUDE_REQUEST_URI))  _requestURI=(String)value;
                else if (key.equals(__INCLUDE_SERVLET_PATH)) _servletPath=(String)value;
                else if (key.equals(__INCLUDE_CONTEXT_PATH)) _contextPath=(String)value;
                else if (key.equals(__INCLUDE_QUERY_STRING)) _query=(String)value;
                else if (value==null)
                    _attr.removeAttribute(key);
                else
                    _attr.setAttribute(key,value); 
            }
            else if (value==null)
                _attr.removeAttribute(key);
            else
                _attr.setAttribute(key,value);
        }
        
        /* ------------------------------------------------------------ */
        public String toString() 
        {
            return "INCLUDE+"+_attr.toString();
        }

        /* ------------------------------------------------------------ */
        public void clearAttributes()
        {
            throw new IllegalStateException();
        }

        /* ------------------------------------------------------------ */
        public void removeAttribute(String name)
        {
            setAttribute(name,null);
        }
    }
};

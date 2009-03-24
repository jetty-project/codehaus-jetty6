//========================================================================
//$Id: ContextHandler.java,v 1.16 2005/11/17 11:19:45 gregwilkins Exp $
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

package org.mortbay.jetty.server.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.http.MimeTypes;
import org.mortbay.jetty.io.Buffer;
import org.mortbay.jetty.io.HttpException;
import org.mortbay.jetty.server.Dispatcher;
import org.mortbay.jetty.server.Handler;
import org.mortbay.jetty.server.HandlerContainer;
import org.mortbay.jetty.server.HttpConnection;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.util.Attributes;
import org.mortbay.jetty.util.AttributesMap;
import org.mortbay.jetty.util.LazyList;
import org.mortbay.jetty.util.Loader;
import org.mortbay.jetty.util.URIUtil;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.log.Logger;
import org.mortbay.jetty.util.resource.Resource;

/* ------------------------------------------------------------ */
/** ContextHandler.
 * 
 * This handler wraps a call to handle by setting the context and
 * servlet path, plus setting the context classloader.
 * 
 * <p>
 * If the context init parameter "org.mortbay.jetty.servlet.ManagedAttributes"
 * is set to a coma separated list of names, then they are treated as context
 * attribute names, which if set as attributes are passed to the servers Container
 * so that they may be managed with JMX.
 * 
 * @org.apache.xbean.XBean description="Creates a basic HTTP context"
 *
 * 
 *
 */
public class ContextHandler extends HandlerWrapper implements Attributes, Server.Graceful, CompleteHandler
{
    private static ThreadLocal<Context> __context=new ThreadLocal<Context>();
    public static final String MANAGED_ATTRIBUTES = "org.mortbay.jetty.server.servlet.ManagedAttributes";
    
    /* ------------------------------------------------------------ */
    /** Get the current ServletContext implementation.
     * This call is only valid during a call to doStart and is available to
     * nested handlers to access the context.
     * 
     * @return ServletContext implementation
     */
    public static Context getCurrentContext()
    {
        Context context = __context.get();
        return context;
    }

    protected Context _scontext;
    
    private AttributesMap _attributes;
    private AttributesMap _contextAttributes;
    private ClassLoader _classLoader;
    private String _contextPath="/";
    private Map<String,String> _initParams;
    private String _displayName;
    private Resource _baseResource;  
    private MimeTypes _mimeTypes;
    private Map<String,String> _localeEncodingMap;
    private String[] _welcomeFiles;
    private ErrorHandler _errorHandler;
    private String[] _vhosts;
    private Set<String> _connectors;
    private EventListener[] _eventListeners;
    private Logger _logger;
    private boolean _shutdown;
    private boolean _allowNullPathInfo;
    private int _maxFormContentSize=Integer.getInteger("org.mortbay.jetty.server.Request.maxFormContentSize",200000).intValue();
    private boolean _compactPath=false;
    private boolean _aliases=true;

    private Object _contextListeners;
    private Object _contextAttributeListeners;
    private Object _requestListeners;
    private Object _asyncListeners;
    private Object _requestAttributeListeners;
    private Set<String> _managedAttributes;
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public ContextHandler()
    {
        super();
        _scontext=new Context();
        _attributes=new AttributesMap();
        _initParams=new HashMap<String,String>();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    protected ContextHandler(Context context)
    {
        super();
        _scontext=context;
        _attributes=new AttributesMap();
        _initParams=new HashMap<String,String>();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public ContextHandler(String contextPath)
    {
        this();
        setContextPath(contextPath);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public ContextHandler(HandlerContainer parent, String contextPath)
    {
        this();
        setContextPath(contextPath);
        parent.addHandler(this);
    }

    /* ------------------------------------------------------------ */
    public Context getServletContext()
    {
        return _scontext;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the allowNullPathInfo true if /context is not redirected to /context/
     */
    public boolean getAllowNullPathInfo()
    {
        return _allowNullPathInfo;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param allowNullPathInfo  true if /context is not redirected to /context/
     */
    public void setAllowNullPathInfo(boolean allowNullPathInfo)
    {
        _allowNullPathInfo=allowNullPathInfo;
    }

    /* ------------------------------------------------------------ */
    public void setServer(Server server)
    {
        if (_errorHandler!=null)
        {
            Server old_server=getServer();
            if (old_server!=null && old_server!=server)
                old_server.getContainer().update(this, _errorHandler, null, "error",true);
            super.setServer(server); 
            if (server!=null && server!=old_server)
                server.getContainer().update(this, null, _errorHandler, "error",true);
            _errorHandler.setServer(server); 
        }
        else
            super.setServer(server); 
    }

    /* ------------------------------------------------------------ */
    /** Set the virtual hosts for the context.
     * Only requests that have a matching host header or fully qualified
     * URL will be passed to that context with a virtual host name.
     * A context with no virtual host names or a null virtual host name is
     * available to all requests that are not served by a context with a
     * matching virtual host name.
     * @param vhosts Array of virtual hosts that this context responds to. A
     * null host name or null/empty array means any hostname is acceptable.
     * Host names may be String representation of IP addresses. Host names may
     * start with '*.' to wildcard one level of names.
     */
    public void setVirtualHosts( String[] vhosts )
    {
        if ( vhosts == null )
        {
            _vhosts = vhosts;
        } 
        else 
        {
            _vhosts = new String[vhosts.length];
            for ( int i = 0; i < vhosts.length; i++ )
                _vhosts[i] = normalizeHostname( vhosts[i]);
        }
    }

    /* ------------------------------------------------------------ */
    /** Get the virtual hosts for the context.
     * Only requests that have a matching host header or fully qualified
     * URL will be passed to that context with a virtual host name.
     * A context with no virtual host names or a null virtual host name is
     * available to all requests that are not served by a context with a
     * matching virtual host name.
     * @return Array of virtual hosts that this context responds to. A
     * null host name or empty array means any hostname is acceptable.
     * Host names may be String representation of IP addresses.
     * Host names may start with '*.' to wildcard one level of names.
     */
    public String[] getVirtualHosts()
    {
        return _vhosts;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @deprecated use {@link #setConnectorNames(String[])} 
     */
    public void setHosts(String[] hosts)
    {
        setConnectorNames(hosts);
    }

    /* ------------------------------------------------------------ */
    /** Get the hosts for the context.
     * @deprecated
     */
    public String[] getHosts()
    {
        return getConnectorNames();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return an array of connector names that this context
     * will accept a request from.
     */
    public String[] getConnectorNames()
    {
        if (_connectors==null || _connectors.size()==0)
            return null;
            
        return (String[])_connectors.toArray(new String[_connectors.size()]);
    }

    /* ------------------------------------------------------------ */
    /** Set the names of accepted connectors.
     * 
     * Names are either "host:port" or a specific configured name for a connector.
     * 
     * @param connectors If non null, an array of connector names that this context
     * will accept a request from.
     */
    public void setConnectorNames(String[] connectors)
    {
        if (connectors==null || connectors.length==0)
            _connectors=null;
        else
            _connectors= new HashSet<String>(Arrays.asList(connectors));
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name)
    {
        return _attributes.getAttribute(name);
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletContext#getAttributeNames()
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames()
    {
        return AttributesMap.getAttributeNamesCopy(_attributes);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the attributes.
     */
    public Attributes getAttributes()
    {
        return _attributes;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the classLoader.
     */
    public ClassLoader getClassLoader()
    {
        return _classLoader;
    }

    /* ------------------------------------------------------------ */
    /**
     * Make best effort to extract a file classpath from the context classloader
     * @return Returns the classLoader.
     */
    public String getClassPath()
    {
        if ( _classLoader==null || !(_classLoader instanceof URLClassLoader))
            return null;
        URLClassLoader loader = (URLClassLoader)_classLoader;
        URL[] urls =loader.getURLs();
        StringBuilder classpath=new StringBuilder();
        for (int i=0;i<urls.length;i++)
        {
            try
            {
                Resource resource = newResource(urls[i]);
                File file=resource.getFile();
                if (file.exists())
                {
                    if (classpath.length()>0)
                        classpath.append(File.pathSeparatorChar);
                    classpath.append(file.getAbsolutePath());
                }
            }
            catch (IOException e)
            {
                Log.debug(e);
            }
        }
        if (classpath.length()==0)
            return null;
        return classpath.toString();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the _contextPath.
     */
    public String getContextPath()
    {
        return _contextPath;
    }
   
    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String name)
    {
        return (String)_initParams.get(name);
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletContext#getInitParameterNames()
     */
    @SuppressWarnings("unchecked")
    public Enumeration getInitParameterNames()
    {
        return Collections.enumeration(_initParams.keySet());
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the initParams.
     */
    public Map<String,String> getInitParams()
    {
        return _initParams;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletContext#getServletContextName()
     */
    public String getDisplayName()
    {
        return _displayName;
    }

    /* ------------------------------------------------------------ */
    public EventListener[] getEventListeners()
    {
        return _eventListeners;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Set the context event listeners.
     * @see ServletContextListener
     * @see ServletContextAttributeListener
     * @see ServletRequestListener
     * @see ServletRequestAttributeListener
     */
    public void setEventListeners(EventListener[] eventListeners)
    {
        _contextListeners=null;
        _contextAttributeListeners=null;
        _requestListeners=null;
        _requestAttributeListeners=null;
        
        _eventListeners=eventListeners;
        
        for (int i=0; eventListeners!=null && i<eventListeners.length;i ++)
        {
            EventListener listener = _eventListeners[i];
            
            if (listener instanceof ServletContextListener)
                _contextListeners= LazyList.add(_contextListeners, listener);
            
            if (listener instanceof ServletContextAttributeListener)
                _contextAttributeListeners= LazyList.add(_contextAttributeListeners, listener);
            
            if (listener instanceof ServletRequestListener)
                _requestListeners= LazyList.add(_requestListeners, listener);
            
            if (listener instanceof ServletRequestAttributeListener)
                _requestAttributeListeners= LazyList.add(_requestAttributeListeners, listener);
        }
    }     

    /* ------------------------------------------------------------ */
    /**
    * Add a context event listeners.
    * @see ServletContextListener
    * @see ServletContextAttributeListener
    * @see ServletRequestListener
    * @see ServletRequestAttributeListener
    */
    public void addEventListener(EventListener listener) 
    {
        setEventListeners((EventListener[])LazyList.addToArray(getEventListeners(), listener, EventListener.class));
    }

    /* ------------------------------------------------------------ */
    /**
     * @return true if this context is accepting new requests
     */
    public boolean isShutdown()
    {
        return !_shutdown;
    }

    /* ------------------------------------------------------------ */
    /** Set shutdown status.
     * This field allows for graceful shutdown of a context. A started context may be put into non accepting state so
     * that existing requests can complete, but no new requests are accepted.
     * @param shutdown true if this context is (not?) accepting new requests
     */
    public void setShutdown(boolean shutdown)
    {
        _shutdown = shutdown;
    }

    /* ------------------------------------------------------------ */
    public Logger getLogger()
    {
        return _logger;
    }
    
    /* ------------------------------------------------------------ */
    public void setLogger(Logger logger)
    {
        _logger=logger;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        if (_contextPath==null)
            throw new IllegalStateException("Null contextPath");
        
        _logger=Log.getLogger(getDisplayName()==null?getContextPath():getDisplayName());
        ClassLoader old_classloader=null;
        Thread current_thread=null;
        Context old_context=null;

        _contextAttributes=new AttributesMap();
        try
        {
            
            // Set the classloader
            if (_classLoader!=null)
            {
                current_thread=Thread.currentThread();
                old_classloader=current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(_classLoader);
            }
            

            if (_mimeTypes==null)
                _mimeTypes=new MimeTypes();
            
            old_context=__context.get();
            __context.set(_scontext);
            
            if (_errorHandler==null)
                setErrorHandler(new ErrorHandler());
            
            // defers the calling of super.doStart()
            startContext();
            
           
        }
        finally
        {
            __context.set(old_context);
            
            // reset the classloader
            if (_classLoader!=null)
            {
                current_thread.setContextClassLoader(old_classloader);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * Extensible startContext.
     * this method is called from {@link ContextHandler#doStart()} instead of a 
     * call to super.doStart().   This allows derived classes to insert additional
     * handling (Eg configuration) before the call to super.doStart by this method
     * will start contained handlers.
     * @see org.mortbay.jetty.Scope.Context
     * @see org.mortbay.jetty.webapp.WebAppContext
     */
    protected void startContext()
    	throws Exception
    {
        super.doStart();

        if (_errorHandler!=null)
            _errorHandler.start();
        
        // Context listeners
        if (_contextListeners != null )
        {
            ServletContextEvent event= new ServletContextEvent(_scontext);
            for (int i= 0; i < LazyList.size(_contextListeners); i++)
            {
                ((ServletContextListener)LazyList.get(_contextListeners, i)).contextInitialized(event);
            }
        }

        String managedAttributes = _initParams.get(MANAGED_ATTRIBUTES);
        if (managedAttributes!=null)
        {
            _managedAttributes=new HashSet<String>();
            String[] attributes = managedAttributes.toString().split(",");
            for (String s : attributes)
                _managedAttributes.add(s);

            Enumeration e = _scontext.getAttributeNames();
            while(e.hasMoreElements())
            {
                String name = (String)e.nextElement();
                Object value = _scontext.getAttribute(name);
                setManagedAttribute(name,value);
            }
        }       
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        ClassLoader old_classloader=null;
        Thread current_thread=null;

        Context old_context=__context.get();
        __context.set(_scontext);
        try
        {
            // Set the classloader
            if (_classLoader!=null)
            {
                current_thread=Thread.currentThread();
                old_classloader=current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(_classLoader);
            }
            
            super.doStop();
            
            // Context listeners
            if (_contextListeners != null )
            {
                ServletContextEvent event= new ServletContextEvent(_scontext);
                for (int i=LazyList.size(_contextListeners); i-->0;)
                {
                    ((ServletContextListener)LazyList.get(_contextListeners, i)).contextDestroyed(event);
                }
            }

            if (_errorHandler!=null)
                _errorHandler.stop();
            
            Enumeration e = _scontext.getAttributeNames();
            while(e.hasMoreElements())
            {
                String name = (String)e.nextElement();
                setManagedAttribute(name,null);
            }
        }
        finally
        {
            __context.set(old_context);
            // reset the classloader
            if (_classLoader!=null)
                current_thread.setContextClassLoader(old_classloader);
        }

        if (_contextAttributes!=null)
            _contextAttributes.clearAttributes();
        _contextAttributes=null;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.server.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void handle(String target, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {   
        Request baseRequest=(request instanceof Request)?(Request)request:HttpConnection.getCurrentConnection().getRequest();
        DispatcherType dispatch=request.getDispatcherType();
        
        if( !isStarted() || _shutdown || (DispatcherType.REQUEST.equals(dispatch) && baseRequest.isHandled()))
            return;

        // Check the vhosts
        if (_vhosts!=null && _vhosts.length>0)
        {
            String vhost = normalizeHostname( request.getServerName());

            boolean match=false;
            
            // TODO non-linear lookup
            for (int i=0;!match && i<_vhosts.length;i++)
            {
                String contextVhost = _vhosts[i];
                if(contextVhost==null) continue;
                if(contextVhost.startsWith("*.")) {
                    // wildcard only at the beginning, and only for one additional subdomain level
                    match=contextVhost.regionMatches(true,2,vhost,vhost.indexOf(".")+1,contextVhost.length()-2);
                } else
                    match=contextVhost.equalsIgnoreCase(vhost);
            }
            if (!match)
                return;
        }
        
        // Check the connector
        if (_connectors!=null && _connectors.size()>0)
        {
            String connector=HttpConnection.getCurrentConnection().getConnector().getName();
            if (connector==null || !_connectors.contains(connector))
                return;
        }
                
        if (_compactPath)
            target=URIUtil.compactPath(target);
            
        if (target.startsWith(_contextPath))
        {
            if (_contextPath.length()==target.length() && _contextPath.length()>1 &&!_allowNullPathInfo)
            {
                // context request must end with /
                baseRequest.setHandled(true);
                if (request.getQueryString()!=null)
                    response.sendRedirect(URIUtil.addPaths(request.getRequestURI(),URIUtil.SLASH)+"?"+request.getQueryString());
                else 
                    response.sendRedirect(URIUtil.addPaths(request.getRequestURI(),URIUtil.SLASH));
                return;
            }
        }
        else
        {
            // Not for this context!
            return;
        }
        
        doHandle(target,baseRequest,request,response);
    }        
        
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.server.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {   
        boolean new_context=false;
        Context old_context=null;
        String old_context_path=null;
        String old_servlet_path=null;
        String old_path_info=null;
        ClassLoader old_classloader=null;
        Thread current_thread=null;
        String pathInfo=null;

        DispatcherType dispatch=request.getDispatcherType();
        
        old_context=baseRequest.getContext();
        
        // Are we already in this context?
        if (old_context!=_scontext)
        {
            new_context=true;
            
            // check the target.
            if (DispatcherType.REQUEST.equals(dispatch) || DispatcherType.ASYNC.equals(dispatch))
            {
                if (target.length()>_contextPath.length())
                {
                    if (_contextPath.length()>1)
                        target=target.substring(_contextPath.length());
                    pathInfo=target;
                }
                else if (_contextPath.length()==1)
                {
                    target=URIUtil.SLASH;
                    pathInfo=URIUtil.SLASH;
                }
                else
                {
                    target=URIUtil.SLASH;
                    pathInfo=null;
                }
            }
        }
        
        try
        {
            old_context_path=baseRequest.getContextPath();
            old_servlet_path=baseRequest.getServletPath();
            old_path_info=baseRequest.getPathInfo();
            
            // Update the paths
            baseRequest.setContext(_scontext);
            if (!DispatcherType.INCLUDE.equals(dispatch) && target.startsWith("/"))
            {
                if (_contextPath.length()==1)
                    baseRequest.setContextPath("");
                else
                    baseRequest.setContextPath(_contextPath);
                baseRequest.setServletPath(null);
                baseRequest.setPathInfo(pathInfo);
            }

            ServletRequestEvent event=null;
            if (new_context)
            {
                // Set the classloader
                if (_classLoader!=null)
                {
                    current_thread=Thread.currentThread();
                    old_classloader=current_thread.getContextClassLoader();
                    current_thread.setContextClassLoader(_classLoader);
                }
                
                // Handle the REALLY SILLY request events!
                baseRequest.setRequestListeners(_requestListeners);
                if (_requestAttributeListeners!=null)
                {
                    final int s=LazyList.size(_requestAttributeListeners);
                    for(int i=0;i<s;i++)
                        baseRequest.addEventListener(((EventListener)LazyList.get(_requestAttributeListeners,i)));
                }
            }
            
            // Handle the request
            try
            {
                if (DispatcherType.REQUEST.equals(dispatch) && isProtectedTarget(target))
                    throw new HttpException(HttpServletResponse.SC_NOT_FOUND);
                
                Handler handler = getHandler();
                if (handler!=null)
                    handler.handle(target, request, response);
            }
            catch(HttpException e)
            {
                Log.debug(e);
                response.sendError(e.getStatus(), e.getReason());
            }
            finally
            {
                // Handle more REALLY SILLY request events!
                if (new_context)
                {
                    baseRequest.takeRequestListeners();
                    if (_requestAttributeListeners!=null)
                    {
                        for(int i=LazyList.size(_requestAttributeListeners);i-->0;)
                            baseRequest.removeEventListener(((EventListener)LazyList.get(_requestAttributeListeners,i)));
                    }
                }
            }
        }
        finally
        {
            if (old_context!=_scontext)
            {
                // reset the classloader
                if (_classLoader!=null)
                {
                    current_thread.setContextClassLoader(old_classloader);
                }
                
                // reset the context and servlet path.
                baseRequest.setContext(old_context);
                baseRequest.setContextPath(old_context_path);
                baseRequest.setServletPath(old_servlet_path);
                baseRequest.setPathInfo(old_path_info); 
            }
        }
    }    
    
    /* ------------------------------------------------------------ */
    /* Handle a runnable in this context
     */
    public void handle(Runnable runnable)
    {   
        ClassLoader old_classloader=null;
        Thread current_thread=null;
        try
        {
            // Set the classloader
            if (_classLoader!=null)
            {
                current_thread=Thread.currentThread();
                old_classloader=current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(_classLoader);
            }
            
            runnable.run();
        }
        finally
        {
            if (old_classloader!=null)
            {
                current_thread.setContextClassLoader(old_classloader);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** Check the target.
     * Called by {@link #handle(String, HttpServletRequest, HttpServletResponse)} when a
     * target within a context is determined.  If the target is protected, 404 is returned.
     * The default implementation always returns false.
     * @see org.mortbay.jetty.webapp.WebAppContext#isProtectedTarget(String)
     */
    /* ------------------------------------------------------------ */
    protected boolean isProtectedTarget(String target)
    { 
        return false;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name)
    {
        setManagedAttribute(name,null);
        _attributes.removeAttribute(name);
    }

    /* ------------------------------------------------------------ */
    /* Set a context attribute.
     * Attributes set via this API cannot be overriden by the ServletContext.setAttribute API.
     * Their lifecycle spans the stop/start of a context.  No attribute listener events are 
     * triggered by this API.
     * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value)
    {
        setManagedAttribute(name,value);
        _attributes.setAttribute(name,value);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(Attributes attributes)
    {
        if (attributes instanceof AttributesMap)
        {
            _attributes = (AttributesMap)attributes;
            Enumeration e = _attributes.getAttributeNames();
            while (e.hasMoreElements())
            {
                String name = (String)e.nextElement();
                setManagedAttribute(name,attributes.getAttribute(name));
            }
        }
        else
        {
            _attributes=new AttributesMap();
            Enumeration e = attributes.getAttributeNames();
            while (e.hasMoreElements())
            {
                String name = (String)e.nextElement();
                Object value=attributes.getAttribute(name);
                setManagedAttribute(name,value);
                _attributes.setAttribute(name,value);
            }
        }
    }

    /* ------------------------------------------------------------ */
    public void clearAttributes()
    {
        Enumeration e = _attributes.getAttributeNames();
        while (e.hasMoreElements())
        {
            String name = (String)e.nextElement();
            setManagedAttribute(name,null);
        }
        _attributes.clearAttributes();
    }

    /* ------------------------------------------------------------ */
    private void setManagedAttribute(String name, Object value)
    {   
        if (_managedAttributes!=null && _managedAttributes.contains(name))
        {
            Object o =_scontext.getAttribute(name);
            if (o!=null)
                getServer().getContainer().removeBean(o);
            if (value!=null)
                getServer().getContainer().addBean(value);
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param classLoader The classLoader to set.
     */
    public void setClassLoader(ClassLoader classLoader)
    {
        _classLoader = classLoader;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param contextPath The _contextPath to set.
     */
    public void setContextPath(String contextPath)
    {
        if (contextPath!=null && contextPath.length()>1 && contextPath.endsWith("/"))
            throw new IllegalArgumentException("ends with /");
        _contextPath = contextPath;
        
        if (getServer()!=null && (getServer().isStarting() || getServer().isStarted()))
        {
            Handler[] contextCollections = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
            for (int h=0;contextCollections!=null&& h<contextCollections.length;h++)
                ((ContextHandlerCollection)contextCollections[h]).mapContexts();
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param initParams The initParams to set.
     */
    public void setInitParams(Map<String,String> initParams)
    {
        if (initParams == null)
            return;
        _initParams = new HashMap<String,String>(initParams);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param servletContextName The servletContextName to set.
     */
    public void setDisplayName(String servletContextName)
    {
        _displayName = servletContextName;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the resourceBase.
     */
    public Resource getBaseResource()
    {
        if (_baseResource==null)
            return null;
        return _baseResource;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the base resource as a string.
     */
    public String getResourceBase()
    {
        if (_baseResource==null)
            return null;
        return _baseResource.toString();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param base The resourceBase to set.
     */
    public void setBaseResource(Resource base) 
    {
        _baseResource=base;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param resourceBase The base resource as a string.
     */
    public void setResourceBase(String resourceBase) 
    {
        try
        {
            setBaseResource(newResource(resourceBase));
        }
        catch (Exception e)
        {
            Log.warn(e);
            throw new IllegalArgumentException(resourceBase);
        }
    }
    /* ------------------------------------------------------------ */
    /**
     * @return True if alias checking is performed on resources.
     */
    public boolean isAliases()
    {
        return _aliases;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param aliases  alias checking performed on resources.
     */
    public void setAliases(boolean aliases)
    {
        _aliases = aliases;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the mimeTypes.
     */
    public MimeTypes getMimeTypes()
    {
        return _mimeTypes;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param mimeTypes The mimeTypes to set.
     */
    public void setMimeTypes(MimeTypes mimeTypes)
    {
        _mimeTypes = mimeTypes;
    }

    /* ------------------------------------------------------------ */
    /**
     */
    public void setWelcomeFiles(String[] files) 
    {
        _welcomeFiles=files;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The names of the files which the server should consider to be welcome files in this context.
     * @see <a href="http://jcp.org/aboutJava/communityprocess/final/jsr154/index.html">The Servlet Specification</a>
     * @see #setWelcomeFiles
     */
    public String[] getWelcomeFiles() 
    {
        return _welcomeFiles;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the errorHandler.
     */
    public ErrorHandler getErrorHandler()
    {
        return _errorHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param errorHandler The errorHandler to set.
     */
    public void setErrorHandler(ErrorHandler errorHandler)
    {
        if (errorHandler!=null)
            errorHandler.setServer(getServer());
        if (getServer()!=null)
            getServer().getContainer().update(this, _errorHandler, errorHandler, "errorHandler",true);
        _errorHandler = errorHandler;
    }
    
    /* ------------------------------------------------------------ */
    public int getMaxFormContentSize()
    {
        return _maxFormContentSize;
    }
    
    /* ------------------------------------------------------------ */
    public void setMaxFormContentSize(int maxSize)
    {
        _maxFormContentSize=maxSize;
    }


    /* ------------------------------------------------------------ */
    /**
     * @return True if URLs are compacted to replace multiple '/'s with a single '/'
     */
    public boolean isCompactPath()
    {
        return _compactPath;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param compactPath True if URLs are compacted to replace multiple '/'s with a single '/'
     */
    public void setCompactPath(boolean compactPath)
    {
        _compactPath=compactPath;
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        
        return this.getClass().getName()+"@"+Integer.toHexString(hashCode())+"{"+getContextPath()+","+getBaseResource()+"}";
    }

    /* ------------------------------------------------------------ */
    public synchronized Class<?> loadClass(String className)
        throws ClassNotFoundException
    {
        if (className==null)
            return null;
        
        if (_classLoader==null)
            return Loader.loadClass(this.getClass(), className);

        return _classLoader.loadClass(className);
    }
    

    /* ------------------------------------------------------------ */
    public void addLocaleEncoding(String locale,String encoding)
    {
        if (_localeEncodingMap==null)
            _localeEncodingMap=new HashMap<String,String>();
        _localeEncodingMap.put(locale, encoding);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Get the character encoding for a locale. The full locale name is first
     * looked up in the map of encodings. If no encoding is found, then the
     * locale language is looked up. 
     *
     * @param locale a <code>Locale</code> value
     * @return a <code>String</code> representing the character encoding for
     * the locale or null if none found.
     */
    public String getLocaleEncoding(Locale locale)
    {
        if (_localeEncodingMap==null)
            return null;
        String encoding = (String)_localeEncodingMap.get(locale.toString());
        if (encoding==null)
            encoding = (String)_localeEncodingMap.get(locale.getLanguage());
        return encoding;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     */
    public Resource getResource(String path) throws MalformedURLException
    {
        if (path==null || !path.startsWith(URIUtil.SLASH))
            throw new MalformedURLException(path);
        
        if (_baseResource==null)
            return null;

        try
        {
            path=URIUtil.canonicalPath(path);
            Resource resource=_baseResource.addPath(path);
            
            if (_aliases && resource.getAlias()!=null)
            {
                if (resource.exists())
                    Log.warn("Aliased resource: "+resource+"~="+resource.getAlias());
                else if (Log.isDebugEnabled())
                    Log.debug("Aliased resource: "+resource+"~="+resource.getAlias());
                return null;
            }
            
            return resource;
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
                    
        return null;
    }

    /* ------------------------------------------------------------ */
    /** Convert URL to Resource
     * wrapper for {@link Resource#newResource(URL)} enables extensions to 
     * provide alternate resource implementations.
     */
    public Resource newResource(URL url) throws IOException
    {
        return Resource.newResource(url);
    }

    /* ------------------------------------------------------------ */
    /** Convert URL to Resource
     * wrapper for {@link Resource#newResource(String)} enables extensions to 
     * provide alternate resource implementations.
     */
    public Resource newResource(String url) throws IOException
    {
        return Resource.newResource(url);
    }

    /* ------------------------------------------------------------ */
    /* 
     */
    public Set<String> getResourcePaths(String path)
    {           
        try
        {
            path=URIUtil.canonicalPath(path);
            Resource resource=getResource(path);
            
            if (resource!=null && resource.exists())
            {
                if (!path.endsWith(URIUtil.SLASH))
                    path=path+URIUtil.SLASH;
                
                String[] l=resource.list();
                if (l!=null)
                {
                    HashSet<String> set = new HashSet<String>();
                    for(int i=0;i<l.length;i++)
                        set.add(path+l[i]);
                    return set;
                }   
            }
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
        return Collections.emptySet();
    }



    /* ------------------------------------------------------------ */
    private String normalizeHostname( String host )
    {
        if ( host == null )
            return null;
        
        if ( host.endsWith( "." ) )
            return host.substring( 0, host.length() -1);
      
            return host;
    }

    public void complete(Request request)
    {
        // TODO Auto-generated method stub
        
    }
    
    /* ------------------------------------------------------------ */
    /** Context.
     * <p>
     * A partial implementation of  {@link javax.servlet.ServletContext}.
     * A complete implementation is provided by the derived {@link org.mortbay.jetty.servlet.ServletContextHandler.Context}.   
     * </p>
     * 
     *
     */
    public class Context implements ServletContext
    {
        /* ------------------------------------------------------------ */
        protected Context()
        {
        }

        /* ------------------------------------------------------------ */
        public ContextHandler getContextHandler()
        {
            // TODO reduce visibility of this method
            return ContextHandler.this;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getContext(java.lang.String)
         */
        public ServletContext getContext(String uripath)
        {
            // TODO this is a very poor implementation!
            // TODO move this to Server
            ContextHandler context=null;
            Handler[] handlers = getServer().getChildHandlersByClass(ContextHandler.class);
            for (int i=0;i<handlers.length;i++)
            {
                if (handlers[i]==null || !handlers[i].isStarted())
                    continue;
                ContextHandler ch = (ContextHandler)handlers[i];
                String context_path=ch.getContextPath();
                if (uripath.equals(context_path) || (uripath.startsWith(context_path)&&uripath.charAt(context_path.length())=='/'))
                {
                    if (context==null || context_path.length()>context.getContextPath().length())
                        context=ch;
                }
            }
            
            if (context!=null)
                return context._scontext;
            return null;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getMajorVersion()
         */
        public int getMajorVersion()
        {
            return 3;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
         */
        public String getMimeType(String file)
        {
            if (_mimeTypes==null)
                return null;
            Buffer mime = _mimeTypes.getMimeByExtension(file);
            if (mime!=null)
                return mime.toString();
            return null;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getMinorVersion()
         */
        public int getMinorVersion()
        {
            return 0;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
         */
        public RequestDispatcher getNamedDispatcher(String name)
        {
            return null;
        }
        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
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
                String uri=URIUtil.addPaths(getContextPath(), uriInContext);
                ContextHandler context=ContextHandler.this;
                return new Dispatcher(context,uri, pathInContext, query);
            }
            catch(Exception e)
            {
                Log.ignore(e);
            }
            return null;
        }
        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
         */
        public String getRealPath(String path)
        {
            if(path==null)
                return null;
            if(path.length()==0)
                path = URIUtil.SLASH;
            else if(path.charAt(0)!='/')
                path = URIUtil.SLASH + path;
                
            try
            {
                Resource resource=ContextHandler.this.getResource(path);
                if(resource!=null)
                {
                    File file = resource.getFile();
                    if (file!=null)
                        return file.getCanonicalPath();
                }
            }
            catch (Exception e)
            {
                Log.ignore(e);
            }
            
            return null;
        }

        /* ------------------------------------------------------------ */
        public URL getResource(String path) throws MalformedURLException
        {
            Resource resource=ContextHandler.this.getResource(path);
            if (resource!=null && resource.exists())
                return resource.getURL();
            return null;
        }
        
        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
         */
        public InputStream getResourceAsStream(String path)
        {
            try
            {
                URL url=getResource(path);
                if (url==null)
                    return null;
                return url.openStream();
            }
            catch(Exception e)
            {
                Log.ignore(e);
                return null;
            }
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
         */
        public Set getResourcePaths(String path)
        {            
            return ContextHandler.this.getResourcePaths(path);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getServerInfo()
         */
        public String getServerInfo()
        {
            return "jetty/"+Server.getVersion();
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getServlet(java.lang.String)
         */
        public Servlet getServlet(String name) throws ServletException
        {
            return null;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getServletNames()
         */
        @SuppressWarnings("unchecked")
        public Enumeration getServletNames()
        {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getServlets()
         */
        @SuppressWarnings("unchecked")
        public Enumeration getServlets()
        {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
         */
        public void log(Exception exception, String msg)
        {
            _logger.warn(msg,exception);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#log(java.lang.String)
         */
        public void log(String msg)
        {
            _logger.info(msg, null, null);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
         */
        public void log(String message, Throwable throwable)
        {
            _logger.warn(message,throwable);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
         */
        public String getInitParameter(String name)
        {
            return ContextHandler.this.getInitParameter(name);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getInitParameterNames()
         */
        @SuppressWarnings("unchecked")
        public Enumeration getInitParameterNames()
        {
            return ContextHandler.this.getInitParameterNames();
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
         */
        public synchronized Object getAttribute(String name)
        {
            Object o = ContextHandler.this.getAttribute(name);
            if (o==null && _contextAttributes!=null)
                o=_contextAttributes.getAttribute(name);
            return o;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getAttributeNames()
         */
        @SuppressWarnings("unchecked")
        public synchronized Enumeration getAttributeNames()
        {
            HashSet<String> set = new HashSet<String>();
            if (_contextAttributes!=null)
            {
            	Enumeration<String> e = _contextAttributes.getAttributeNames();
            	while(e.hasMoreElements())
            		set.add(e.nextElement());
            }
            Enumeration<String> e = _attributes.getAttributeNames();
            while(e.hasMoreElements())
                set.add(e.nextElement());
            
            return Collections.enumeration(set);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
         */
        public synchronized void setAttribute(String name, Object value)
        {
            
            if (_contextAttributes==null)
            {
            	// Set it on the handler
            	ContextHandler.this.setAttribute(name, value);
                return;
            }

            setManagedAttribute(name,value);
            Object old_value=_contextAttributes==null?null:_contextAttributes.getAttribute(name);
            
            if (value==null)
                _contextAttributes.removeAttribute(name);
            else
                _contextAttributes.setAttribute(name,value);
            
            if (_contextAttributeListeners!=null)
            {
                ServletContextAttributeEvent event =
                    new ServletContextAttributeEvent(_scontext,name, old_value==null?value:old_value);

                for(int i=0;i<LazyList.size(_contextAttributeListeners);i++)
                {
                    ServletContextAttributeListener l = (ServletContextAttributeListener)LazyList.get(_contextAttributeListeners,i);
                    
                    if (old_value==null)
                        l.attributeAdded(event);
                    else if (value==null)
                        l.attributeRemoved(event);
                    else
                        l.attributeReplaced(event);
                }
            }
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
         */
        public synchronized void removeAttribute(String name)
        {
            setManagedAttribute(name,null);
            
            if (_contextAttributes==null)
            {
            	// Set it on the handler
            	_attributes.removeAttribute(name);
                return;
            }
            
            Object old_value=_contextAttributes.getAttribute(name);
            _contextAttributes.removeAttribute(name);
            if (old_value!=null)
            {
                if (_contextAttributeListeners!=null)
                {
                    ServletContextAttributeEvent event =
                        new ServletContextAttributeEvent(_scontext,name, old_value);

                    for(int i=0;i<LazyList.size(_contextAttributeListeners);i++)
                        ((ServletContextAttributeListener)LazyList.get(_contextAttributeListeners,i)).attributeRemoved(event);
                }
            }
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getServletContextName()
         */
        public String getServletContextName()
        {
            String name = ContextHandler.this.getDisplayName();
            if (name==null)
                name=ContextHandler.this.getContextPath();
            return name;
        }

        /* ------------------------------------------------------------ */
        public String getContextPath()
        {
            if ((_contextPath != null) && _contextPath.equals(URIUtil.SLASH))
                return "";
            
            return _contextPath;
        }

        /* ------------------------------------------------------------ */
        public String toString()
        {
            return "ServletContext@"+Integer.toHexString(hashCode())+"{"+(getContextPath().equals("")?URIUtil.SLASH:getContextPath())+","+getBaseResource()+"}";
        }

        /* ------------------------------------------------------------ */
        public boolean setInitParameter(String name, String value)
        {
            if (ContextHandler.this.getInitParameter(name)!=null)
                return false;
            ContextHandler.this.getInitParams().put(name,value);
            return true;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.Class)
         */
        public FilterRegistration addFilter(String filterName, Class<? extends Filter> filterClass)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.String)
         */
        public FilterRegistration addFilter(String filterName, String className)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.Class)
         */
        public ServletRegistration addServlet(String servletName, Class<? extends Servlet> servletClass)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.String)
         */
        public ServletRegistration addServlet(String servletName, String className)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#findFilterRegistration(java.lang.String)
         */
        public FilterRegistration findFilterRegistration(String filterName)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#findServletRegistration(java.lang.String)
         */
        public ServletRegistration findServletRegistration(String servletName)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#getDefaultSessionTrackingModes()
         */
        public EnumSet<SessionTrackingMode> getDefaultSessionTrackingModes()
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#getEffectiveSessionTrackingModes()
         */
        public EnumSet<SessionTrackingMode> getEffectiveSessionTrackingModes()
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#getSessionCookieConfig()
         */
        public SessionCookieConfig getSessionCookieConfig()
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#setSessionTrackingModes(java.util.EnumSet)
         */
        public void setSessionTrackingModes(EnumSet<SessionTrackingMode> sessionTrackingModes)
        {
            Log.warn("Use servlet Context");
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, javax.servlet.Filter)
         */
        public FilterRegistration addFilter(String filterName, Filter filter)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, javax.servlet.Servlet)
         */
        public ServletRegistration addServlet(String servletName, Servlet servlet)
        {
            Log.warn("Use servlet Context");
            return null;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#setSessionTrackingModes(java.util.Set)
         */
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
        {
            // TODO Auto-generated method stub
            Log.warn("Not implemented");
            
        }

        
    }

}

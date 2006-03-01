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

package org.mortbay.jetty.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;
import org.mortbay.resource.Resource;
import org.mortbay.util.Attributes;
import org.mortbay.util.AttributesMap;
import org.mortbay.util.LazyList;
import org.mortbay.util.Loader;
import org.mortbay.util.URIUtil;


/* ------------------------------------------------------------ */
/** ContextHandler.
 * 
 * This handler wraps a call to handle by setting the context and
 * servlet path, plus setting the context classloader.
 * 
 * @org.apache.xbean.XBean description="Creates a basic HTTP context"
 *
 * @author gregw
 *
 */
public class ContextHandler extends WrappedHandler implements Attributes
{
    private static ThreadLocal __context=new ThreadLocal();
    
    /* ------------------------------------------------------------ */
    /** Get the current ServletContext implementation.
     * This call is only valid during a call to doStart and is available to
     * nested handlers to access the context.
     * 
     * @return ServletContext implementation
     */
    public static Context getCurrentContext()
    {
        Context context = (Context)__context.get();
        if (context==null)
            throw new IllegalStateException("Only valid during call to doStart()");
        return context;
    }
    
    private Attributes _attributes;
    private Attributes _contextAttributes;
    private ClassLoader _classLoader;
    private Context _context;
    private String _contextPath;
    private HashMap _initParams;
    private String _displayName;
    private String _docRoot;
    private Resource _baseResource;  
    private MimeTypes _mimeTypes;
    private Map _localeEncodingMap;
    private String[] _welcomeFiles;
    private ErrorHandler _errorHandler;
    private String[] _hosts;
    private String[] _vhosts;
    private EventListener[] _eventListeners;
    Logger logger;

    private Object _contextListeners;
    private Object _contextAttributeListeners;
    private Object _requestListeners;
    private Object _requestAttributeListeners;
    
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public ContextHandler()
    {
        super();
        _context=new Context();
        _attributes=new AttributesMap();
        _initParams=new HashMap();
    }
    

    /* ------------------------------------------------------------ */
    public void setServer(Server server)
    {
        if (getServer()!=null && getServer()!=server)
            getServer().getContainer().update(this, _errorHandler, null, "error");
        
        if (server!=null && getServer()!=server)
            server.getContainer().update(this, null, _errorHandler, "error");
        
        super.setServer(server);
        if (_errorHandler!=null)
            _errorHandler.setServer(server);  
    }

    /* ------------------------------------------------------------ */
    /** Set the virtual hosts for the context.
     * Only requests that have a matching host header or fully qualified
     * URL will be passed to that context with a virtual host name.
     * A context with no virtual host names or a null virtual host name is
     * available to all requests that are not served by a context with a
     * matching virtual host name.
     * @param hosts Array of virtual hosts that this context responds to. A
     * null host name or null/empty array means any hostname is acceptable.
     * Host names may String representation of IP addresses.
     */
    public void setVirtualHosts(String[] vhosts)
    {
        _vhosts=vhosts;
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
     */
    public String[] getVirtualHosts()
    {
        return _vhosts;
    }

    /* ------------------------------------------------------------ */
    /** Set the hosts for the context.
     * Set the real hosts that this context will accept requests for.
     * If not null or empty, then only requests from server for hosts
     * in this array are accepted by this context. 
     */
    public void setHosts(String[] hosts)
    {
        _hosts=hosts;
    }

    /* ------------------------------------------------------------ */
    /** Get the hosts for the context.
     */
    public String[] getHosts()
    {
        return _hosts;
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
    public Enumeration getAttributeNames()
    {
        return _attributes.getAttributeNames();
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
    public Enumeration getInitParameterNames()
    {
        return Collections.enumeration(_initParams.keySet());
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the initParams.
     */
    public HashMap getInitParams()
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
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        logger=Log.getLogger(getDisplayName()==null?getContextPath():getDisplayName());
        ClassLoader old_classloader=null;
        Thread current_thread=null;
        Object old_context=null;
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
            __context.set(_context);
            
            if (_errorHandler==null)
                _errorHandler=new ErrorHandler();
            
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
    protected void startContext()
    	throws Exception
    {
        super.doStart();

        // Context listeners
        if (_contextListeners != null )
        {
            ServletContextEvent event= new ServletContextEvent(_context);
            for (int i= 0; i < LazyList.size(_contextListeners); i++)
            {
                ((ServletContextListener)LazyList.get(_contextListeners, i)).contextInitialized(event);
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

        Object old_context=__context.get();
        __context.set(_context);
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
                ServletContextEvent event= new ServletContextEvent(_context);
                for (int i= 0; i < LazyList.size(_contextListeners); i++)
                {
                    ((ServletContextListener)LazyList.get(_contextListeners, i)).contextDestroyed(event);
                }
            }
        }
        finally
        {
            __context.set(old_context);
            // reset the classloader
            if (_classLoader!=null)
                current_thread.setContextClassLoader(old_classloader);
        }

        _contextAttributes.clearAttributes();
        _contextAttributes=null;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException
    {
        boolean handled=false;
        boolean new_context=false;
        Request base_request=null;
        Context old_context=null;
        String old_context_path=null;
        String old_servlet_path=null;
        String old_path_info=null;
        ClassLoader old_classloader=null;
        Thread current_thread=null;
        
        
        base_request=(request instanceof Request)?(Request)request:HttpConnection.getCurrentConnection().getRequest();
        old_context=base_request.getContext();
        
        // Are we already in this context?
        if (old_context!=_context)
        {
            new_context=true;
            
            // Check the vhosts
            if (_vhosts!=null && _vhosts.length>0)
            {
                String vhost=request.getServerName();
                boolean match=false;
                // TODO consider non-linear lookup
                for (int i=0;!match && i<_vhosts.length;i++)
                    match=_vhosts[i]!=null && _vhosts[i].equalsIgnoreCase(vhost);
                if (!match)
                    return false;
            }
            
            // Check the real hosts
            if (_hosts!=null && _hosts.length>0)
            {
                String host=request.getLocalName();
                boolean match=false;
                // TODO consider non-linear lookup
                for (int i=0;!match && i<_hosts.length;i++)
                    match=_hosts[i]!=null && _hosts[i].equalsIgnoreCase(host);
                if (!match)
                    return false;
            }
            
            // Nope - so check the target.
            if (dispatch==REQUEST)
            {
                if (target.equals(_contextPath))
                {
                    target=_contextPath;
                    if (!target.endsWith("/"))
                    {
                        response.sendRedirect(target+"/");
                        return true;
                    }
                }
                else if (target.startsWith(_contextPath) && (_contextPath.length()==1 || target.charAt(_contextPath.length())=='/'))
                {
                    if (_contextPath.length()>1)
                        target=target.substring(_contextPath.length());
                }
                else 
                {
                    // Not for this context!
                    return false;
                }
            }
        }
        
        try
        {
            old_context_path=base_request.getContextPath();
            old_servlet_path=base_request.getServletPath();
            old_path_info=base_request.getPathInfo();
            
            // Update the paths
            base_request.setContext(_context);
            if (dispatch!=INCLUDE && target.startsWith("/"))
            {
                if (_contextPath.length()==1)
                    base_request.setContextPath("");
                else
                    base_request.setContextPath(_contextPath);
                base_request.setServletPath(null);
                base_request.setPathInfo(target);
            }
            
            // Set the classloader
            if (_classLoader!=null)
            {
                current_thread=Thread.currentThread();
                old_classloader=current_thread.getContextClassLoader();
                current_thread.setContextClassLoader(_classLoader);
            }
            
            // Handle the REALLY SILLY request events!
            ServletRequestEvent event=null;
            if (new_context)
            {
                if (_requestListeners!=null)
                {
                    event = new ServletRequestEvent(_context,request);
                    for(int i=0;i<LazyList.size(_requestListeners);i++)
                        ((ServletRequestListener)LazyList.get(_requestListeners,i)).requestInitialized(event);
                }
                for(int i=0;i<LazyList.size(_requestAttributeListeners);i++)
                    base_request.addEventListener(((ServletRequestListener)LazyList.get(_requestAttributeListeners,i)));
            }
            
            // Handle the request
            try
            {
                handled = getHandler().handle(target, request, response, dispatch);
            }
            finally
            {
                // Handle more REALLY SILLY request events!
                if (new_context)
                {
                    for(int i=0;i<LazyList.size(_requestListeners);i++)
                        ((ServletRequestListener)LazyList.get(_requestListeners,i)).requestDestroyed(event);
                    
                    for(int i=0;i<LazyList.size(_requestAttributeListeners);i++)
                        base_request.removeEventListener(((ServletRequestListener)LazyList.get(_requestAttributeListeners,i)));
                }
            }
        }
        finally
        {
            if (old_context!=_context)
            {
                // reset the classloader
                if (_classLoader!=null)
                {
                    current_thread.setContextClassLoader(old_classloader);
                }
                
                // reset the context and servlet path.
                base_request.setContext(old_context);
                base_request.setContextPath(old_context_path);
                base_request.setServletPath(old_servlet_path);
                base_request.setPathInfo(old_path_info); 
            }
        }
        
        return handled;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name)
    {
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
        _attributes.setAttribute(name,value);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(Attributes attributes)
    {
        _attributes = attributes;
    }

    /* ------------------------------------------------------------ */
    public void clearAttributes()
    {
        _attributes.clearAttributes();
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
     * @param _contextPath The _contextPath to set.
     */
    public void setContextPath(String contextPath)
    {
        _contextPath = contextPath;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param initParams The initParams to set.
     */
    public void setInitParams(HashMap initParams)
    {
        _initParams = initParams;
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
     * @param resourceBase The resourceBase to set.
     */
    public void setBaseResource(Resource base) 
    {
        _baseResource=base;
        _docRoot=null;
        
        try
        {
            File file=_baseResource.getFile();
            if (file!=null)
            {
                _docRoot=file.getCanonicalPath();
                if (_docRoot.endsWith(File.pathSeparator))
                    _docRoot=_docRoot.substring(0,_docRoot.length()-1);
            }
        }
        catch (Exception e)
        {
            Log.warn(e);
            throw new IllegalArgumentException(base.toString());
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @param resourceBase The base resource as a string.
     */
    public void setResourceBase(String resourceBase) 
    {
        try
        {
            setBaseResource(Resource.newResource(resourceBase));
        }
        catch (Exception e)
        {
            Log.warn(e);
            throw new IllegalArgumentException(resourceBase);
        }
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
     * @return
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
        if (_errorHandler!=null)
            _errorHandler.setServer(null);
        if (getServer()!=null)
            getServer().getContainer().update(this, _errorHandler, errorHandler, "errorHandler");
        _errorHandler = errorHandler;
        if (_errorHandler!=null)
            _errorHandler.setServer(getServer());
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "ContextHandler@"+Integer.toHexString(hashCode())+"{"+getContextPath()+","+getBaseResource()+"}";
    }

    /* ------------------------------------------------------------ */
    public synchronized Class loadClass(String className)
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
            _localeEncodingMap=new HashMap();
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
        if (path==null || !path.startsWith("/"))
            throw new MalformedURLException(path);
        
        if (_baseResource==null)
            return null;

        try
        {
            path=URIUtil.canonicalPath(path);
            Resource resource=_baseResource.addPath(path);
            return resource;
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
                    
        return null;
    }


    /* ------------------------------------------------------------ */
    /* 
     */
    public Set getResourcePaths(String path)
    {           
        try
        {
            path=URIUtil.canonicalPath(path);
            Resource resource=getResource(path);
            
            if (resource!=null && resource.exists())
            {
                String[] l=resource.list();
                if (l!=null)
                {
                    HashSet set = new HashSet();
                    for(int i=0;i<l.length;i++)
                        set.add(path+"/"+l[i]);
                    return set;
                }   
            }
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
        return Collections.EMPTY_SET;
    }

    
    /* ------------------------------------------------------------ */
    /** Context.
     * @author gregw
     *
     */
    public class Context implements ServletContext
    {
        /* ------------------------------------------------------------ */
        private Context()
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
            Handler[] handlers = getServer().getAllHandlers();
            for (int i=0;i<handlers.length;i++)
            {
                if (handlers[i]==null || !handlers[i].isStarted() || !(handlers[i] instanceof ContextHandler))
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
                return context._context;
            return null;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getMajorVersion()
         */
        public int getMajorVersion()
        {
            return 2;
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
            return 5;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
         */
        public RequestDispatcher getNamedDispatcher(String name)
        {
            return new Dispatcher(ContextHandler.this, name);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
         */
        public String getRealPath(String path)
        {
            if (_docRoot==null)
                return null;
            
            if (path==null)
                return null;
            path=URIUtil.canonicalPath(path);
            
            if (!path.startsWith("/"))
                path="/"+path;
            if (File.separatorChar!='/')
                path =path.replace('/', File.separatorChar);
            
            return _docRoot+path;
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
                return new Dispatcher(ContextHandler.this, uri, pathInContext, query);
            }
            catch(Exception e)
            {
                Log.ignore(e);
            }
            return null;
        }

        /* ------------------------------------------------------------ */
        /* 
         */
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
            return "Jetty-6.0";
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
        public Enumeration getServletNames()
        {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getServlets()
         */
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
            logger.warn(msg,exception);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#log(java.lang.String)
         */
        public void log(String msg)
        {
            logger.info(msg, null, null);
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
         */
        public void log(String message, Throwable throwable)
        {
            logger.warn(message,throwable);
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
            if (o==null)
                o=_contextAttributes.getAttribute(name);
            return o;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getAttributeNames()
         */
        public synchronized Enumeration getAttributeNames()
        {
            HashSet set = new HashSet();
            Enumeration e = _contextAttributes.getAttributeNames();
            while(e.hasMoreElements())
                set.add(e.nextElement());
            e = ContextHandler.this.getAttributeNames();
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
            Object old_value=_contextAttributes==null?null:_contextAttributes.getAttribute(name);
            
            if (value==null)
                _contextAttributes.removeAttribute(name);
            else
                _contextAttributes.setAttribute(name,value);
            
            if (_contextAttributeListeners!=null)
            {
                ServletContextAttributeEvent event =
                    new ServletContextAttributeEvent(_context,name, old_value==null?value:old_value);

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
            Object old_value=_contextAttributes.getAttribute(name);
            _contextAttributes.removeAttribute(name);
            if (old_value!=null)
            {
                if (_contextAttributeListeners!=null)
                {
                    ServletContextAttributeEvent event =
                        new ServletContextAttributeEvent(_context,name, old_value);

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
        /**
         * @return Returns the _contextPath.
         */
        public String getContextPath()
        {
            return _contextPath;
        }


        /* ------------------------------------------------------------ */
        public String toString()
        {
            return "ServletContext@"+Integer.toHexString(hashCode())+"{"+getContextPath()+","+getBaseResource()+"}";
        }

    }

}

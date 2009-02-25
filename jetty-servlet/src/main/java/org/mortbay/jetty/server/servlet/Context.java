//========================================================================
//Copyright 2004-2009 Mort Bay Consulting Pty. Ltd.
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

import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.mortbay.jetty.server.Dispatcher;
import org.mortbay.jetty.server.Handler;
import org.mortbay.jetty.server.HandlerContainer;
import org.mortbay.jetty.server.handler.ContextHandler;
import org.mortbay.jetty.server.handler.ErrorHandler;
import org.mortbay.jetty.server.handler.HandlerWrapper;
import org.mortbay.jetty.server.session.SessionHandler;
import org.mortbay.jetty.util.Loader;


/* ------------------------------------------------------------ */
/** Servlet Context.
 * This conveniance extention to the ContextHandler allows for
 * simple construction of a context with ServletHandler and optionally
 * session and security handlers, et.<pre>
 *   new ServletContext("/context",Context.SESSIONS|Context.NO_SECURITY);
 * </pre>
 * <p/>
 * This class should have been called ServletContext, but this would have
 * cause confusion with {@link ServletContext}.
 */
public class Context extends ContextHandler
{   
    public final static int SESSIONS=1;
    public final static int SECURITY=2;
    public final static int NO_SESSIONS=0;
    public final static int NO_SECURITY=0;
    
    protected String _defaultSecurityHandlerClass="org.mortbay.jetty.security.ConstraintSecurityHandler";
    protected HandlerWrapper _securityHandler;
    protected ServletHandler _servletHandler;
    protected SessionHandler _sessionHandler;
    protected int _options;
    
    /* ------------------------------------------------------------ */
    public Context()
    {
        this(null,null,null,null,null);
    }
    
    /* ------------------------------------------------------------ */
    public Context(int options)
    {
        this(null,null,options);
    }
    
    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath)
    {
        this(parent,contextPath,null,null,null,null);
    }
    
    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath, int options)
    {
        this(parent,contextPath,null,null,null,null);
        _options=options;
    }
    
    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath, boolean sessions, boolean security)
    {
        this(parent,contextPath,(sessions?SESSIONS:0)|(security?SECURITY:0));
    }

    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, SessionHandler sessionHandler, HandlerWrapper securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {   
        this(parent,null,sessionHandler,securityHandler,servletHandler,errorHandler);
    }

    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath, SessionHandler sessionHandler, HandlerWrapper securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {   
        super((ContextHandler.SContext)null);
        _scontext = new SContext();
        _sessionHandler = sessionHandler;
        _securityHandler = securityHandler;
        _servletHandler = servletHandler;
            
        if (errorHandler!=null)
            setErrorHandler(errorHandler);

        if (contextPath!=null)
            setContextPath(contextPath);

        if (parent!=null)
            parent.addHandler(this);
    }    

    
    /* ------------------------------------------------------------ */
    /** Get the defaultSecurityHandlerClass.
     * @return the defaultSecurityHandlerClass
     */
    public String getDefaultSecurityHandlerClass()
    {
        return _defaultSecurityHandlerClass;
    }

    /* ------------------------------------------------------------ */
    /** Set the defaultSecurityHandlerClass.
     * @param defaultSecurityHandlerClass the defaultSecurityHandlerClass to set
     */
    public void setDefaultSecurityHandlerClass(String defaultSecurityHandlerClass)
    {
        _defaultSecurityHandlerClass = defaultSecurityHandlerClass;
    }

    /* ------------------------------------------------------------ */
    protected SessionHandler newSessionHandler()
    {
        return new SessionHandler();
    }
    
    /* ------------------------------------------------------------ */
    protected HandlerWrapper newSecurityHandler()
    {
        try
        {
            Class<?> l = Loader.loadClass(Context.class,_defaultSecurityHandlerClass);
            return (HandlerWrapper)l.newInstance();
        }
        catch(Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /* ------------------------------------------------------------ */
    protected ServletHandler newServletHandler()
    {
        return new ServletHandler();
    }

    /* ------------------------------------------------------------ */
    /**
     * Finish constructing handlers and link them together.
     * 
     * @see org.mortbay.jetty.server.handler.ContextHandler#startContext()
     */
    protected void startContext() throws Exception
    {
        // force creation of missing handlers.
        getSessionHandler();
        getSecurityHandler();
        getServletHandler();
        
        Handler handler = _servletHandler;
        if (_securityHandler!=null)
        {
            _securityHandler.setHandler(handler);
            handler=_securityHandler;
        }
        
        if (_sessionHandler!=null)
        {
            _sessionHandler.setHandler(handler);
            handler=_sessionHandler;
        }
        
        setHandler(handler);
        
    	super.startContext();

    	// OK to Initialize servlet handler now
    	if (_servletHandler != null && _servletHandler.isStarted())
    		_servletHandler.initialize();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the securityHandler.
     */
    public Handler getSecurityHandler()
    {
        if (_securityHandler==null && (_options&SECURITY)!=0 && !isStarted()) 
            _securityHandler=newSecurityHandler();
        
        return _securityHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the servletHandler.
     */
    public ServletHandler getServletHandler()
    {
        if (_servletHandler==null && !isStarted()) 
            _servletHandler=newServletHandler();
        return _servletHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the sessionHandler.
     */
    public SessionHandler getSessionHandler()
    {
        if (_sessionHandler==null && (_options&SESSIONS)!=0 && !isStarted()) 
            _sessionHandler=newSessionHandler();
        return _sessionHandler;
    }

    /* ------------------------------------------------------------ */
    /** conveniance method to add a servlet.
     */
    public ServletHolder addServlet(String className,String pathSpec)
    {
        return _servletHandler.addServletWithMapping(className, pathSpec);
    }

    /* ------------------------------------------------------------ */
    /** conveniance method to add a servlet.
     */
    public ServletHolder addServlet(Class servlet,String pathSpec)
    {
        return _servletHandler.addServletWithMapping(servlet.getName(), pathSpec);
    }
    
    /* ------------------------------------------------------------ */
    /** conveniance method to add a servlet.
     */
    public void addServlet(ServletHolder servlet,String pathSpec)
    {
        _servletHandler.addServletWithMapping(servlet, pathSpec);
    }

    /* ------------------------------------------------------------ */
    /** conveniance method to add a filter
     */
    public void addFilter(FilterHolder holder,String pathSpec,int dispatches)
    {
        _servletHandler.addFilterWithMapping(holder,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /** convenience method to add a filter
     */
    public FilterHolder addFilter(Class filterClass,String pathSpec,int dispatches)
    {
        return _servletHandler.addFilterWithMapping(filterClass,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /** convenience method to add a filter
     */
    public FilterHolder addFilter(String filterClass,String pathSpec,int dispatches)
    {
        return _servletHandler.addFilterWithMapping(filterClass,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param sessionHandler The sessionHandler to set.
     */
    public void setSessionHandler(SessionHandler sessionHandler)
    {
        if (isStarted())
            throw new IllegalStateException("STARTED");
        
        _sessionHandler = sessionHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param securityHandler The {@link org.mortbay.jetty.server.handler.SecurityHandler} to set on this context.
     */
    public void setSecurityHandler(HandlerWrapper securityHandler)
    {
        if (isStarted())
            throw new IllegalStateException("STARTED");
        
        _securityHandler = securityHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param servletHandler The servletHandler to set.
     */
    public void setServletHandler(ServletHandler servletHandler)
    {
        if (isStarted())
            throw new IllegalStateException("STARTED");
        
        _servletHandler = servletHandler;
    }

    /* ------------------------------------------------------------ */
    public class SContext extends ContextHandler.SContext
    {

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
         */
        public RequestDispatcher getNamedDispatcher(String name)
        {
            ContextHandler context=org.mortbay.jetty.server.servlet.Context.this;
            if (_servletHandler==null || _servletHandler.getServlet(name)==null)
                return null;
            return new Dispatcher(context, name);
        }



        /* ------------------------------------------------------------ */
        public FilterRegistration addFilter(String filterName, String className)
        {
            if (!isStarting())
                    throw new IllegalStateException();
            
            final ServletHandler handler = Context.this.getServletHandler();
            final FilterHolder holder= handler.newFilterHolder();
            holder.setClassName(className);
            holder.setName(filterName);
            handler.addFilter(holder);
            
            return new FilterRegistration()
            {
                public void setInitParameter(String name, String value)
                {
                    holder.setInitParameter(name,value);
                }

                public void setAsyncSupported(boolean isAsyncSupported)
                {
                    holder.setAsyncSupported(isAsyncSupported);
                }

                public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames)
                {
                    FilterMapping mapping = new FilterMapping();
                    mapping.setFilterHolder(holder);
                    mapping.setDispatcherTypes(dispatcherTypes);
                    mapping.setServletNames(servletNames);
                    
                    if (isMatchAfter)
                        handler.addFilterMapping(mapping);
                    else
                        handler.prependFilterMapping(mapping);
                }

                public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns)
                {
                    FilterMapping mapping = new FilterMapping();
                    mapping.setFilterHolder(holder);
                    mapping.setDispatcherTypes(dispatcherTypes);
                    mapping.setPathSpecs(urlPatterns);
                    
                    if (isMatchAfter)
                        handler.addFilterMapping(mapping);
                    else
                        handler.prependFilterMapping(mapping);
                }

                public void setDescription(String description)
                {   
                }

                public void setInitParameters(Map<String, String> initParameters)
                {
                    holder.setInitParameters(initParameters);
                }
            };
        }

        /* ------------------------------------------------------------ */
        public ServletRegistration addServlet(String servletName, String className)
        {
            if (!isStarting())
                throw new IllegalStateException();

            final ServletHandler handler = Context.this.getServletHandler();
            final ServletHolder holder= handler.newServletHolder();
            holder.setClassName(className);
            holder.setName(servletName);
            handler.addServlet(holder);

            return new ServletRegistration()
            {
                public void setAsyncSupported(boolean isAsyncSupported)
                {
                    holder.setAsyncSupported(isAsyncSupported);
                }

                public void setLoadOnStartup(int loadOnStartup)
                {
                    holder.setInitOrder(loadOnStartup);
                }

                public void setInitParameter(String name, String value)
                {
                    holder.setInitParameter(name,value);
                }

                public void addMapping(String... urlPatterns)
                {
                    ServletMapping mapping = new ServletMapping();
                    mapping.setServletName(holder.getName());
                    mapping.setPathSpecs(urlPatterns);
                    handler.addServletMapping(mapping);
                }

                public void setDescription(String description)
                {
                }

                public void setInitParameters(Map<String, String> initParameters)
                {
                    holder.setInitParameters(initParameters);
                }
            };
            
        }

        /* ------------------------------------------------------------ */
        public void addFilterMappingForServletNames(String filterName, EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames)
        {
            if (!isStarting())
                throw new IllegalStateException();
            ServletHandler handler = Context.this.getServletHandler();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterName(filterName);
            mapping.setServletNames(servletNames);
            mapping.setDispatcherTypes(dispatcherTypes);
            handler.addFilterMapping(mapping);
        }

        /* ------------------------------------------------------------ */
        public void addFilterMappingForUrlPatterns(String filterName, EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns)
        {
            if (!isStarting())
                throw new IllegalStateException();
            ServletHandler handler = Context.this.getServletHandler();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterName(filterName);
            mapping.setPathSpecs(urlPatterns);
            mapping.setDispatcherTypes(dispatcherTypes);
            handler.addFilterMapping(mapping);
        }

        /* ------------------------------------------------------------ */
        public void addServletMapping(String servletName, String[] urlPatterns)
        {
            if (!isStarting())
                throw new IllegalStateException();
            ServletHandler handler = Context.this.getServletHandler();
            ServletHolder holder= handler.newServletHolder();
            holder.setName(servletName);
            handler.addServlet(holder);
        }
        


    }
}

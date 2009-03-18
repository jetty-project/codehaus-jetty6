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

package org.mortbay.jetty.servlet;

import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.server.Dispatcher;
import org.mortbay.jetty.server.Handler;
import org.mortbay.jetty.server.HandlerContainer;
import org.mortbay.jetty.server.handler.ContextHandler;
import org.mortbay.jetty.server.handler.ErrorHandler;
import org.mortbay.jetty.server.session.SessionHandler;
import org.mortbay.jetty.util.log.Log;


/* ------------------------------------------------------------ */
/** Servlet Context.
 * This extension to the ContextHandler allows for
 * simple construction of a context with ServletHandler and optionally
 * session and security handlers, et.<pre>
 *   new ServletContext("/context",Context.SESSIONS|Context.NO_SECURITY);
 * </pre>
 * <p/>
 * This class should have been called ServletContext, but this would have
 * cause confusion with {@link ServletContext}.
 */
public class ServletContextHandler extends ContextHandler
{   
    public final static int SESSIONS=1;
    public final static int SECURITY=2;
    public final static int NO_SESSIONS=0;
    public final static int NO_SECURITY=0;
    
    protected Class<? extends SecurityHandler> _defaultSecurityHandlerClass=org.mortbay.jetty.security.ConstraintSecurityHandler.class;
    protected SessionHandler _sessionHandler;
    protected SecurityHandler _securityHandler;
    protected ServletHandler _servletHandler;
    protected int _options;
    
    /* ------------------------------------------------------------ */
    public ServletContextHandler()
    {
        this(null,null,null,null,null);
    }
    
    /* ------------------------------------------------------------ */
    public ServletContextHandler(int options)
    {
        this(null,null,options);
    }
    
    /* ------------------------------------------------------------ */
    public ServletContextHandler(HandlerContainer parent, String contextPath)
    {
        this(parent,contextPath,null,null,null,null);
    }
    
    /* ------------------------------------------------------------ */
    public ServletContextHandler(HandlerContainer parent, String contextPath, int options)
    {
        this(parent,contextPath,null,null,null,null);
        _options=options;
    }
    
    /* ------------------------------------------------------------ */
    public ServletContextHandler(HandlerContainer parent, String contextPath, boolean sessions, boolean security)
    {
        this(parent,contextPath,(sessions?SESSIONS:0)|(security?SECURITY:0));
    }

    /* ------------------------------------------------------------ */
    public ServletContextHandler(HandlerContainer parent, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {   
        this(parent,null,sessionHandler,securityHandler,servletHandler,errorHandler);
    }

    /* ------------------------------------------------------------ */
    public ServletContextHandler(HandlerContainer parent, String contextPath, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {   
        super((ContextHandler.Context)null);
        _scontext = new Context();
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
    public Class<? extends SecurityHandler> getDefaultSecurityHandlerClass()
    {
        return _defaultSecurityHandlerClass;
    }

    /* ------------------------------------------------------------ */
    /** Set the defaultSecurityHandlerClass.
     * @param defaultSecurityHandlerClass the defaultSecurityHandlerClass to set
     */
    public void setDefaultSecurityHandlerClass(Class<? extends SecurityHandler> defaultSecurityHandlerClass)
    {
        _defaultSecurityHandlerClass = defaultSecurityHandlerClass;
    }

    /* ------------------------------------------------------------ */
    protected SessionHandler newSessionHandler()
    {
        return new SessionHandler();
    }
    
    /* ------------------------------------------------------------ */
    protected SecurityHandler newSecurityHandler()
    {
        try
        {
            return (SecurityHandler)_defaultSecurityHandlerClass.newInstance();
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
    public SecurityHandler getSecurityHandler()
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
        return getServletHandler().addServletWithMapping(className, pathSpec);
    }

    /* ------------------------------------------------------------ */
    /** conveniance method to add a servlet.
     */
    public ServletHolder addServlet(Class<? extends Servlet> servlet,String pathSpec)
    {
        return getServletHandler().addServletWithMapping(servlet.getName(), pathSpec);
    }
    
    /* ------------------------------------------------------------ */
    /** conveniance method to add a servlet.
     */
    public void addServlet(ServletHolder servlet,String pathSpec)
    {
        getServletHandler().addServletWithMapping(servlet, pathSpec);
    }

    /* ------------------------------------------------------------ */
    /** conveniance method to add a filter
     */
    public void addFilter(FilterHolder holder,String pathSpec,int dispatches)
    {
        getServletHandler().addFilterWithMapping(holder,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /** convenience method to add a filter
     */
    public FilterHolder addFilter(Class<? extends Filter> filterClass,String pathSpec,int dispatches)
    {
        return getServletHandler().addFilterWithMapping(filterClass,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /** convenience method to add a filter
     */
    public FilterHolder addFilter(String filterClass,String pathSpec,int dispatches)
    {
        return getServletHandler().addFilterWithMapping(filterClass,pathSpec,dispatches);
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
    public void setSecurityHandler(SecurityHandler securityHandler)
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
    public class Context extends ContextHandler.Context
    {

        /* ------------------------------------------------------------ */
        /* 
         * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
         */
        public RequestDispatcher getNamedDispatcher(String name)
        {
            ContextHandler context=org.mortbay.jetty.servlet.ServletContextHandler.this;
            if (_servletHandler==null || _servletHandler.getServlet(name)==null)
                return null;
            return new Dispatcher(context, name);
        }


        /* ------------------------------------------------------------ */
        public void addFilterMappingForServletNames(String filterName, EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames)
        {
            if (isStarted())
                throw new IllegalStateException();
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterName(filterName);
            mapping.setServletNames(servletNames);
            mapping.setDispatcherTypes(dispatcherTypes);
            handler.addFilterMapping(mapping);
        }

        /* ------------------------------------------------------------ */
        public void addServletMapping(String servletName, String[] urlPatterns)
        {
            if (isStarted())
                throw new IllegalStateException();
            ServletHandler handler = ServletContextHandler.this.getServletHandler();
            ServletHolder holder= handler.newServletHolder();
            holder.setName(servletName);
            handler.addServlet(holder);
        }
        
        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.Class)
         */
        public FilterRegistration addFilter(String filterName, Class<? extends Filter> filterClass)
        {
            if (isStarted())
                throw new IllegalStateException();

            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final FilterHolder holder= handler.newFilterHolder();
            holder.setName(filterName);
            holder.setHeldClass(filterClass);
            handler.addFilter(holder);
            return holder.getRegistration();
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.String)
         */
        public FilterRegistration addFilter(String filterName, String className)
        {
            if (isStarted())
                throw new IllegalStateException();

            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final FilterHolder holder= handler.newFilterHolder();
            holder.setName(filterName);
            holder.setClassName(className);
            handler.addFilter(holder);
            return holder.getRegistration();
        }


        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, javax.servlet.Filter)
         */
        public FilterRegistration addFilter(String filterName, Filter filter)
        {
            if (isStarted())
                throw new IllegalStateException();

            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final FilterHolder holder= handler.newFilterHolder();
            holder.setName(filterName);
            holder.setFilter(filter);
            handler.addFilter(holder);
            return holder.getRegistration();
        }
        
        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.Class)
         */
        public ServletRegistration addServlet(String servletName, Class<? extends Servlet> servletClass)
        {
            if (!isStarting())
                throw new IllegalStateException();

            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final ServletHolder holder= handler.newServletHolder();
            holder.setName(servletName);
            holder.setHeldClass(servletClass);
            handler.addServlet(holder);
            return holder.getRegistration();
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.String)
         */
        public ServletRegistration addServlet(String servletName, String className)
        {
            if (!isStarting())
                throw new IllegalStateException();

            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final ServletHolder holder= handler.newServletHolder();
            holder.setName(servletName);
            holder.setClassName(className);
            handler.addServlet(holder);
            return holder.getRegistration();
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, javax.servlet.Servlet)
         */
        public ServletRegistration addServlet(String servletName, Servlet servlet)
        {
            if (!isStarting())
                throw new IllegalStateException();

            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final ServletHolder holder= handler.newServletHolder();
            holder.setName(servletName);
            holder.setServlet(servlet);
            handler.addServlet(holder);
            return holder.getRegistration();
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#findFilterRegistration(java.lang.String)
         */
        public FilterRegistration findFilterRegistration(String filterName)
        {
            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final FilterHolder holder=handler.getFilter(filterName);
            return (holder==null)?null:holder.getRegistration();
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#findServletRegistration(java.lang.String)
         */
        public ServletRegistration findServletRegistration(String servletName)
        {
            final ServletHandler handler = ServletContextHandler.this.getServletHandler();
            final ServletHolder holder=handler.getServlet(servletName);
            return (holder==null)?null:holder.getRegistration();
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#getDefaultSessionTrackingModes()
         */
        public EnumSet<SessionTrackingMode> getDefaultSessionTrackingModes()
        {
            return SessionHandler.DEFAULT_TRACKING;
        }

        /* ------------------------------------------------------------ */
        /**
         * @see javax.servlet.ServletContext#getEffectiveSessionTrackingModes()
         */
        public EnumSet<SessionTrackingMode> getEffectiveSessionTrackingModes()
        {
            Log.warn("Not Implemented");
            return null;
        }
        


    }
}

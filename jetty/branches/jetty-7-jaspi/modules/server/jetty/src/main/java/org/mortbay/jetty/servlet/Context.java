//========================================================================
//$Id: WebAppContext.java,v 1.5 2005/11/16 22:02:45 gregwilkins Exp $
//Copyright 2004-2006 Mort Bay Consulting Pty. Ltd.
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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import org.mortbay.jetty.Dispatcher;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.handler.SecurityHandler;
import org.mortbay.util.Loader;
import org.mortbay.util.Loader;


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
    
    protected SecurityHandler _securityHandler;
    protected ServletHandler _servletHandler;
    protected SessionHandler _sessionHandler;
    
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
        this(parent,contextPath,((options&SESSIONS)!=0)?new SessionHandler():null,((options&SECURITY)!=0)?newSecurityHandler():null,null,null);
    }
    
    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath, boolean sessions, boolean security)
    {
        this(parent,contextPath,(sessions?SESSIONS:0)|(security?SECURITY:0));
    }

    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {   
        this(parent,null,sessionHandler,securityHandler,servletHandler,errorHandler);
    }

    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {   
        super((ContextHandler.SContext)null);
        _scontext = new SContext();
        _sessionHandler = sessionHandler;
        _securityHandler = securityHandler;
        _servletHandler = servletHandler!=null?servletHandler:new ServletHandler();
        
        if (_sessionHandler!=null)
        {
            setHandler(_sessionHandler);
            
            if (securityHandler!=null)
            {
                _sessionHandler.setHandler(_securityHandler);
                _securityHandler.setHandler(_servletHandler);
            }
            else
            {
                _sessionHandler.setHandler(_servletHandler);
            }
        }
        else if (_securityHandler!=null)
        {
            setHandler(_securityHandler);
            _securityHandler.setHandler(_servletHandler);
        }
        else
        {
            setHandler(_servletHandler);
        }
            
        if (errorHandler!=null)
            setErrorHandler(errorHandler);

        if (contextPath!=null)
            setContextPath(contextPath);

        if (parent!=null)
            parent.addHandler(this);
    }    

    /* ------------------------------------------------------------ */
    //TODO jaspi doesn't this kinda suck?
    static SecurityHandler newSecurityHandler()
    {
        try
        {
            Class<?> l = Loader.loadClass(Context.class,"org.mortbay.jetty.security.ConstraintSecurityHandler");
            return (SecurityHandler)l.newInstance();
        }
        catch(Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.mortbay.jetty.handler.ContextHandler#startContext()
     */
    protected void startContext() throws Exception
    {
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
        return _securityHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the servletHandler.
     */
    public ServletHandler getServletHandler()
    {
        return _servletHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the sessionHandler.
     */
    public SessionHandler getSessionHandler()
    {
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
    /** conveniance method to add a filter
     */
    public FilterHolder addFilter(Class filterClass,String pathSpec,int dispatches)
    {
        return _servletHandler.addFilterWithMapping(filterClass,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /** conveniance method to add a filter
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
        if (_sessionHandler==sessionHandler)
            return;
        
        if (_sessionHandler!=null)
            _sessionHandler.setHandler(null);

        _sessionHandler = sessionHandler;
        
        setHandler(_sessionHandler);
        
        if (_securityHandler!=null)
            _sessionHandler.setHandler(_securityHandler);
        else if (_servletHandler!=null)
            _sessionHandler.setHandler(_servletHandler);
            
        
    }

    /* ------------------------------------------------------------ */
    /**
     * @param securityHandler The {@link org.mortbay.jetty.handler.SecurityHandler} to set on this context.
     */
    public void setSecurityHandler(SecurityHandler securityHandler)
    {
        if(_securityHandler==securityHandler)
            return;
                    
        if (_securityHandler!=null)
            _securityHandler.setHandler(null);
        
        _securityHandler = securityHandler;
        
        if (_securityHandler==null)
        {
            if (_sessionHandler!=null)
                _sessionHandler.setHandler(_servletHandler);
            else 
                setHandler(_servletHandler);
        }
        else
        {
            if (_sessionHandler!=null)
                _sessionHandler.setHandler(_securityHandler);
            else 
                setHandler(_securityHandler);

            if (_servletHandler!=null)
                _securityHandler.setHandler(_servletHandler);
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @param servletHandler The servletHandler to set.
     */
    public void setServletHandler(ServletHandler servletHandler)
    {
        if (_servletHandler==servletHandler)
            return;
        
        _servletHandler = servletHandler;

        if (_securityHandler!=null)
            _securityHandler.setHandler(_servletHandler);
        else if (_sessionHandler!=null)
            _sessionHandler.setHandler(_servletHandler);
        else 
            setHandler(_servletHandler);
        
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
            ContextHandler context=org.mortbay.jetty.servlet.Context.this;
            if (_servletHandler==null || _servletHandler.getServlet(name)==null)
                return null;
            return new Dispatcher(context, name);
        }



        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.jetty.handler.ContextHandler.SContext#addFilter(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
         */
        public void addFilter(String filterName, String description, String className, Map<String, String> initParameters)
        {
        	if (!isStarting())
        		throw new IllegalStateException();
        	
            ServletHandler handler = Context.this.getServletHandler();
            FilterHolder holder= handler.newFilterHolder();
            holder.setClassName(className);
            holder.setName(filterName);
            holder.setInitParameters(initParameters);
            handler.addFilter(holder);
        }

        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.jetty.handler.ContextHandler.SContext#addFilterMapping(java.lang.String, java.lang.String[], java.lang.String[], java.util.EnumSet, boolean)
         */
        public void addFilterMapping(String filterName, String[] urlPatterns, String[] servletNames, EnumSet<DispatcherType> dispatcherTypes,
                boolean isMatchAfter)
        {
        	if (!isStarting())
        		throw new IllegalStateException();
            ServletHandler handler = Context.this.getServletHandler();
            FilterMapping mapping = new FilterMapping();
            mapping.setFilterName(filterName);
            mapping.setPathSpecs(urlPatterns);
            mapping.setServletNames(servletNames);
            
            int dispatches=mapping.getDispatches();
            if (dispatcherTypes.contains(DispatcherType.ERROR)) 
                dispatches|=ERROR;
            if (dispatcherTypes.contains(DispatcherType.FORWARD)) 
                dispatches|=FORWARD;
            if (dispatcherTypes.contains(DispatcherType.INCLUDE)) 
                dispatches|=INCLUDE;
            if (dispatcherTypes.contains(DispatcherType.REQUEST)) 
                dispatches|=REQUEST;
            mapping.setDispatches(dispatches);
            
            handler.addFilterMapping(mapping);
        }

        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.jetty.handler.ContextHandler.SContext#addServlet(java.lang.String, java.lang.String, java.lang.String, java.util.Map, int)
         */
        public void addServlet(String servletName, String description, String className, Map<String, String> initParameters, int loadOnStartup)
        {
        	if (!isStarting())
        		throw new IllegalStateException();
            ServletHandler handler = Context.this.getServletHandler();
            ServletHolder holder= handler.newServletHolder();
            holder.setClassName(className);
            holder.setName(servletName);
            holder.setInitParameters(initParameters);
            holder.setInitOrder(loadOnStartup);
            handler.addServlet(holder);
        }

        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.jetty.handler.ContextHandler.SContext#addServletMapping(java.lang.String, java.lang.String[])
         */
        public void addServletMapping(String servletName, String[] urlPattern)
        {
        	if (!isStarting())
        		throw new IllegalStateException();
            ServletHandler handler = Context.this.getServletHandler();
            ServletMapping mapping = new ServletMapping();
            mapping.setPathSpecs(urlPattern);
            handler.addServletMapping(mapping);
        }   
    }
}

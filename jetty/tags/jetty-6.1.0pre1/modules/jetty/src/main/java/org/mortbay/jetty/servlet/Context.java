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

import javax.servlet.ServletContext;

import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.security.SecurityHandler;


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
    public Context(HandlerContainer parent, String contextPath)
    {
        this(parent,null,null,null,null);
        setContextPath(contextPath);
    }
    
    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath, int options)
    {
        this(parent,((options&SESSIONS)!=0)?new SessionHandler():null,((options&SECURITY)!=0)?new SecurityHandler():null,null,null);
        setContextPath(contextPath);
    }
    
    /* ------------------------------------------------------------ */
    public Context(HandlerContainer parent, String contextPath, boolean sessions, boolean security)
    {
        this(parent,contextPath,(sessions?SESSIONS:0)|(security?SECURITY:0));
    }

    /* ------------------------------------------------------------ */
    /**
     */
    public Context(HandlerContainer parent, SessionHandler sessionHandler,SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {
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
        

        if (parent!=null)
        {
            parent.addHandler(this);
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
    public void addFilter(Class filterClass,String pathSpec,int dispatches)
    {
        _servletHandler.addFilterWithMapping(filterClass,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /** conveniance method to add a filter
     */
    public void addFilter(String filterClass,String pathSpec,int dispatches)
    {
        _servletHandler.addFilterWithMapping(filterClass,pathSpec,dispatches);
    }
}

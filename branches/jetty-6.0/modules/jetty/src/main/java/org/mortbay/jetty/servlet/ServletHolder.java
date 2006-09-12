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

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.log.Log;



/* --------------------------------------------------------------------- */
/** Servlet Instance and Context Holder.
 * Holds the name, params and some state of a javax.servlet.Servlet
 * instance. It implements the ServletConfig interface.
 * This class will organise the loading of the servlet when needed or
 * requested.
 *
 * @author Greg Wilkins
 */
public class ServletHolder extends Holder
    implements Comparable
{
    /* ---------------------------------------------------------------- */
    
    private int _initOrder;
    private boolean _initOnStartup=false;
    private Map _roleMap; 
    private String _forcedPath;
    private String _runAs;
    private UserRealm _realm;    
    
    private transient Stack _servlets;
    private transient Servlet _servlet;
    private transient Config _config;
    private transient long _unavailable;
    private transient UnavailableException _unavailableEx;

    
    /* ---------------------------------------------------------------- */
    /** Constructor .
     */
    public ServletHolder()
    {}
    
    /* ---------------------------------------------------------------- */
    /** Constructor for existing servlet.
     */
    public ServletHolder(Servlet servlet)
    {
        setServlet(servlet);
    }

    /* ---------------------------------------------------------------- */
    /** Constructor for existing servlet.
     */
    public ServletHolder(Class servlet)
    {
        super(servlet);
    }
    
    /* ------------------------------------------------------------ */
    public synchronized void setServlet(Servlet servlet)
    {
        if (servlet==null || servlet instanceof SingleThreadModel)
            throw new IllegalArgumentException();

        _extInstance=true;
        _servlet=servlet;
        setHeldClass(servlet.getClass());
        if (getName()==null)
            setName(servlet.getClass().getName());
    }
    
    /* ------------------------------------------------------------ */
    public int getInitOrder()
    {
        return _initOrder;
    }

    /* ------------------------------------------------------------ */
    /** Set the initialize order.
     * Holders with order<0, are initialized on use. Those with
     * order>=0 are initialized in increasing order when the handler
     * is started.
     */
    public void setInitOrder(int order)
    {
        _initOnStartup=true;
        _initOrder = order;
    }

    /* ------------------------------------------------------------ */
    /** Comparitor by init order.
     */
    public int compareTo(Object o)
    {
        if (o instanceof ServletHolder)
        {
            ServletHolder sh= (ServletHolder)o;
            if (sh==this)
                return 0;
            if (sh._initOrder<_initOrder)
                return 1;
            if (sh._initOrder>_initOrder)
                return -1;
            
            int c=(_className!=null && sh._className!=null)?_className.compareTo(sh._className):0;
            if (c==0)
                c=_name.compareTo(sh._name);
            if (c==0)
                c=this.hashCode()>o.hashCode()?1:-1;
            return c;
        }
        return 1;
    }

    /* ------------------------------------------------------------ */
    public boolean equals(Object o)
    {
        return compareTo(o)==0;
    }

    /* ------------------------------------------------------------ */
    public int hashCode()
    {
        return _name.hashCode();
    }

    /* ------------------------------------------------------------ */
    /** Link a user role.
     * Translate the role name used by a servlet, to the link name
     * used by the container.
     * @param name The role name as used by the servlet
     * @param link The role name as used by the container.
     */
    public synchronized void setUserRoleLink(String name,String link)
    {
        if (_roleMap==null)
            _roleMap=new HashMap();
        _roleMap.put(name,link);
    }
    
    /* ------------------------------------------------------------ */
    /** get a user role link.
     * @param name The name of the role
     * @return The name as translated by the link. If no link exists,
     * the name is returned.
     */
    public String getUserRoleLink(String name)
    {
        if (_roleMap==null)
            return name;
        String link=(String)_roleMap.get(name);
        return (link==null)?name:link;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param role Role name that is added to UserPrincipal when this servlet
     * is called. 
     */
    public void setRunAs(String role)
    {
        _runAs=role;
    }
    
    /* ------------------------------------------------------------ */
    public String getRunAs()
    {
        return _runAs;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the forcedPath.
     */
    public String getForcedPath()
    {
        return _forcedPath;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param forcedPath The forcedPath to set.
     */
    public void setForcedPath(String forcedPath)
    {
        _forcedPath = forcedPath;
    }
    
    /* ------------------------------------------------------------ */
    public void doStart()
        throws Exception
    {
        _unavailable=0;
        super.doStart();
        
        if (!javax.servlet.Servlet.class
            .isAssignableFrom(_class))
        {
            Exception ex = new IllegalStateException("Servlet "+_class+
                                            " is not a javax.servlet.Servlet");
            super.stop();
            throw ex;
        }        

        _config=new Config();
        
        if (_runAs!=null)
            _realm=((SecurityHandler)(ContextHandler.getCurrentContext()
                    .getContextHandler().getChildHandlerByClass(SecurityHandler.class))).getUserRealm();
        
        if (javax.servlet.SingleThreadModel.class
            .isAssignableFrom(_class))
            _servlets=new Stack();

        if (_initOnStartup)
        {
            if (_servlet==null)
                _servlet=(Servlet)newInstance();
            try
            {
                initServlet(_servlet,_config);
            }
            catch(Throwable e)
            {
                _servlet=null;
                _config=null;
                if (e instanceof Exception)
                    throw (Exception) e;
                else if (e instanceof Error)
                    throw (Error)e;
                else
                    throw new ServletException(e);
            }            
            
            if(_servlets != null)
            {
                _servlets.push(_servlet);
            }
        }  
    }

    /* ------------------------------------------------------------ */
    public void doStop()
    {
        Principal user=null;
        try
        {
            // Handle run as
            if (_runAs!=null && _realm!=null)
                user=_realm.pushRole(null,_runAs);
                
            if (!_extInstance)
            {
                if (_servlet!=null)
                    _servlet.destroy();
                _servlet=null;
            }
            
            while (_servlets!=null && _servlets.size()>0)
            {
                Servlet s = (Servlet)_servlets.pop();
                s.destroy();
            }
            _config=null;
        }
        finally
        {
            super.doStop();
            // pop run-as role
            if (_runAs!=null && _realm!=null && user!=null)
                _realm.popRole(user); 
        }
    }


    /* ------------------------------------------------------------ */
    /** Get the servlet.
     * @return The servlet
     */
    public synchronized Servlet getServlet()
        throws ServletException
    {
        // Handle previous unavailability
        if (_unavailable!=0)
        {
            if (_unavailable<0 || _unavailable>0 && System.currentTimeMillis()<_unavailable)
                throw _unavailableEx;
            _unavailable=0;
            _unavailableEx=null;
        }
        
        try
        {
            if (_servlets!=null)
            {
                Servlet servlet=null;
                if (_servlets.size()==0)
                {
                    servlet= (Servlet)newInstance();
                    if (_config==null)
                    	_config=new Config();
                    initServlet(servlet,_config);
                }
                else
                    servlet = (Servlet)_servlets.pop();

                return servlet;
            }
            
            if (_servlet==null)
            {
                _servlet=(Servlet)newInstance();
                if (_config==null)
                	_config=new Config();
                initServlet(_servlet,_config);
            }
        
            return _servlet;
        }
        catch(UnavailableException e)
        {
            _servlet=null;
            _config=null;
            return makeUnavailable(e);
        }
        catch(ServletException e)
        {
            _servlet=null;
            _config=null;
            throw e;
        }
        catch(Throwable e)
        {
            _servlet=null;
            _config=null;
            throw new ServletException("init",e);
        }    
    }

    /* ------------------------------------------------------------ */
    private Servlet makeUnavailable(UnavailableException e) 
      throws UnavailableException 
    {
        _unavailableEx=e;
        _unavailable=-1;
        if (e.isPermanent())   
            _unavailable=-1;
        else
        {
            if (_unavailableEx.getUnavailableSeconds()>0)
                _unavailable=System.currentTimeMillis()+1000*_unavailableEx.getUnavailableSeconds();
            else
                _unavailable=System.currentTimeMillis()+5000; // TODO configure
        }
        
        throw _unavailableEx;
    }

    /* ------------------------------------------------------------ */
    private void initServlet(Servlet servlet, ServletConfig config) 
    	throws ServletException
    {
        Principal user=null;
        try
        {
            // Handle run as
            if (_runAs!=null && _realm!=null)
                user=_realm.pushRole(null,_runAs);
            servlet.init(config);
        }
        finally
        {
            // pop run-as role
            if (_runAs!=null && _realm!=null && user!=null)
                _realm.popRole(user);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Service a request with this servlet.
     */
    public void handle(ServletRequest request,
                       ServletResponse response)
        throws ServletException,
               UnavailableException,
               IOException
    {
        if (_class==null)
            throw new UnavailableException("Servlet Not Initialized");
        
        Servlet servlet=(!_initOnStartup||_servlets!=null)?getServlet():_servlet;
        if (servlet==null)
            throw new UnavailableException("Could not instantiate "+_class);

        // Service the request
        boolean servlet_error=true;
        Principal user=null;
        Request base_request=null;
        try
        {
            // Handle aliased path
            if (_forcedPath!=null)
                // TODO complain about poor naming to the Jasper folks
                request.setAttribute("org.apache.catalina.jsp_file",_forcedPath);

            // Handle run as
            if (_runAs!=null && _realm!=null)
            {
                base_request=HttpConnection.getCurrentConnection().getRequest();
                user=_realm.pushRole(base_request.getUserPrincipal(),_runAs);
                base_request.setUserPrincipal(user);
            }
            
            servlet.service(request,response);
            servlet_error=false;
        }
        catch(UnavailableException e)
        {
            if (_servlets!=null && servlet!=null)
                try{stop();}catch(Exception e2){Log.ignore(e2);}
            makeUnavailable(e);
        }
        finally
        {
            // pop run-as role
            if (_runAs!=null && _realm!=null && user!=null && base_request!=null)
            {
                user=_realm.popRole(user);
                base_request.setUserPrincipal(user);
            }

            // Handle error params.
            if (servlet_error)
                request.setAttribute("javax.servlet.error.servlet_name",getName());

            // Return to singleThreaded pool
            synchronized(this)
            {
                if (_servlets!=null && servlet!=null)
                    _servlets.push(servlet);
            }
        }
    }

 
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    class Config implements ServletConfig
    {   
        /* -------------------------------------------------------- */
        public String getServletName()
        {
            return getName();
        }
        
        /* -------------------------------------------------------- */
        public ServletContext getServletContext()
        {
            return _servletHandler.getServletContext();
        }

        /* -------------------------------------------------------- */
        public String getInitParameter(String param)
        {
            return ServletHolder.this.getInitParameter(param);
        }
    
        /* -------------------------------------------------------- */
        public Enumeration getInitParameterNames()
        {
            return ServletHolder.this.getInitParameterNames();
        }
    }

}






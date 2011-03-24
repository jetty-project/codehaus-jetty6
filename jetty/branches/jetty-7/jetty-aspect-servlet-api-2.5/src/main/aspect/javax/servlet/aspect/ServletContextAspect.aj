package javax.servlet.aspect;
//========================================================================
//$Id:$
//Copyright 2011 Webtide, LLC
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

import javax.servlet.ServletContext;
import javax.servlet.aspect.ServletContextAttributePermission;
import javax.servlet.aspect.ServletContextParameterPermission;
import javax.servlet.aspect.ServletContextPathPermission;
import javax.servlet.aspect.ServletContextPermission;
import javax.servlet.aspect.ServletDeprecationException;

/**
 * Aspect for handling calls related to ServletContext
 *
 * Permissions:
 *  org.mortbay.jetty.aspect.servlets.ServletContextAttributePermission "<attribute-name>" "read"
 *  org.mortbay.jetty.aspect.servlets.ServletContextAttributePermission "<attribute-name>" "write"
 *  org.mortbay.jetty.aspect.servlets.ServletContextAttributePermission "<attribute-name>" "all"
 *  org.mortbay.jetty.aspect.servlets.ServletContextParameterPermission "<attribute-name>" "read"
 *  org.mortbay.jetty.aspect.servlets.ServletContextParameterPermission "<attribute-name>" "write"
 *  org.mortbay.jetty.aspect.servlets.ServletContextParameterPermission "<attribute-name>" "all"
 *  org.mortbay.jetty.aspect.servlets.ServletContextPathPermission "<uri-path>"
 *  org.mortbay.jetty.aspect.servlets.ServletContextPermission "getDispatcher"
 *
 */
public aspect ServletContextAspect
{

    /**
     * permission must be granted to get servlet attributes
     * 
     * @param sc
     * @param name
     */
    pointcut checkGetAttribute(ServletContext sc, String name) : target(sc) && args(name) && call(public Object ServletContext.getAttribute(String));

    before(ServletContext sc, String name): checkGetAttribute(sc, name)
    {

        System.out.println("ASPECT: checking getAttribute: " + name);
        
        SecurityManager sm = System.getSecurityManager();

        if (sm != null)
        {
            sm.checkPermission(new ServletContextAttributePermission(name,"read"));
        }
    }

    /**
     * permission must be granted to set servlet attributes
     * 
     * @param sc
     * @param name
     * @param o
     */
    pointcut checkSetAttribute(ServletContext sc, String name, Object o) : target(sc) && args(name, o) && call(public void setAttribute(String, Object));

    before(ServletContext sc, String name, Object o): checkSetAttribute(sc, name, o)
    {

        SecurityManager sm = System.getSecurityManager();

        if (sm != null)
        {
            sm.checkPermission(new ServletContextAttributePermission(name,"write"));
        }
    }

    /**
     * permission must be granted to remove servlet attributes
     * 
     * @param sc
     * @param name
     */
    pointcut checkRemoveAttribute(ServletContext sc, String name) : target(sc) && args(name) && call(public void removeAttribute(String));

    before(ServletContext sc, String name): checkRemoveAttribute(sc, name)
    {

        SecurityManager sm = System.getSecurityManager();

        if (sm != null)
        {
            sm.checkPermission(new ServletContextAttributePermission(name,"write"));
        }
    }

    /**
     * permission must be granted for servlets to get init parameters
     * 
     * @param sc
     * @param name
     */
    pointcut checkGetInitParameter(ServletContext sc, String name) : target(sc) && args(name) && call(public String getInitParameter(String));

    before(ServletContext sc, String name): checkGetInitParameter(sc, name)
    {

        SecurityManager sm = System.getSecurityManager();

        if (sm != null)
        {
            sm.checkPermission(new ServletContextParameterPermission(name,"read"));
        }
    }

    /**
     * permission must be granted to get named or request dispatchers
     * 
     * @param sc
     * @param name
     */
    pointcut checkGetDispatcher(ServletContext sc, String name) : target(sc) && args(name) && call(public * *Dispatcher(String));

    before(ServletContext sc, String name): checkGetDispatcher(sc, name)
    {

        SecurityManager sm = System.getSecurityManager();

        if (sm != null)
        {
            sm.checkPermission(new ServletContextPermission("getDispatcher"));
        }
    }

    /**
     * permission must be granted to get servlet contexts based on a uri
     * 
     * @param sc
     * @param uripath
     */
    pointcut checkGetContext(ServletContext sc, String uripath) : target(sc) && args(uripath) && call(public * getContext(String));

    before(ServletContext sc, String uripath): checkGetContext(sc, uripath)
    {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null)
        {
            sm.checkPermission(new ServletContextPathPermission(uripath));
        }
    }

    /**
     * access to deprecated methods is blocked
     * 
     * @param sc
     */
    pointcut blockGetServlets(ServletContext sc) : target(sc) && call(public * getServlets());

    before(ServletContext sc): blockGetServlets(sc)
    {
        throw new ServletDeprecationException("Servlet Method Deprecated: getServlets() was deprecated in servlet-api-2.0 with no replacement");
    }

    /**
     * access to deprecated methods is blocked
     * 
     * @param sc
     */
    pointcut blockGetServlet(ServletContext sc) : target(sc) && call(public * getServlet(..));

    before(ServletContext sc): blockGetServlet(sc)
    {
        throw new ServletDeprecationException("Servlet Method Deprecated: getServlet(String) was deprecated in servlet-api-2.0 with no replacement");
    }

    /**
     * access to deprecated methods is blocked
     * 
     * @param sc
     */
    pointcut blockGetServletNames(ServletContext sc) : target(sc) && call(public * getServletNames());

    before(ServletContext sc): blockGetServletNames(sc)
    {
        throw new ServletDeprecationException("Servlet Method Deprecated: getServletNames() was deprecated in servlet-api-2.1 with no replacement");
    }
}

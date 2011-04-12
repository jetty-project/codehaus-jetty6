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

import java.io.IOException;
import java.security.AccessController; 
import java.security.PrivilegedAction;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.aspect.Constants;
import javax.servlet.aspect.ServletPermission;
import javax.servlet.http.HttpServlet;

import org.aspectj.lang.ProceedingJoinPoint;


/**
 * Aspect for handling calls related to Servlet
 *
 * Permissions:
 *  org.mortbay.jetty.aspect.servlets.ServletPermission "getServletInfo"
 *  org.mortbay.jetty.aspect.servlets.ServletPermission "destroy"
 *
 */
public aspect ServletAspect
{   
    /**
     * permission must be granted to get servlet info
     * 
     * @param s
     */
    pointcut checkGetServletInfo() : execution(public String Servlet+.getServletInfo());
    
    before(): checkGetServletInfo()
    {
        SecurityManager sm = System.getSecurityManager();
        
        if ( sm != null )
        {
            sm.checkPermission( new ServletPermission(Constants.GET_SERVLET_INFO) );
        }        
    }
    
    /**
     * permission must be granted to call Servlet.destroy
     * 
     * @param s
     */
    pointcut checkDestroy() : execution(public void Servlet+.destroy());
    
    before(): checkDestroy()
    {
        SecurityManager sm = System.getSecurityManager();
        
        if ( sm != null )
        {
            sm.checkPermission( new ServletPermission(Constants.DESTROY) );
        }       
    }
}

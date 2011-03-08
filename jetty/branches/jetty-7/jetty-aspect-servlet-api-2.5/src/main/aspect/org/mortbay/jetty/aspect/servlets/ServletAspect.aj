package org.mortbay.jetty.aspect.servlets;
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

import javax.servlet.Servlet;

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
    pointcut checkGetServletInfo(Servlet s) : target(s) && call(public String getServletInfo());
    
    before( Servlet s ): checkGetServletInfo(s)
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
    pointcut checkDestroy(Servlet s) : target(s) && call(public void destroy());
    
    before( Servlet s ): checkDestroy(s)
    {
        SecurityManager sm = System.getSecurityManager();
        
        if ( sm != null )
        {
            sm.checkPermission( new ServletPermission(Constants.DESTROY) );
        }       
    }
}

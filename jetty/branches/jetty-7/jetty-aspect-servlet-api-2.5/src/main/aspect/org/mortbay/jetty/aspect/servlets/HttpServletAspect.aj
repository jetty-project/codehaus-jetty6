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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;


/**
 * Aspect for handling calls related to Servlet
 *
 * Permissions:
 *  org.mortbay.jetty.aspect.servlets.ServletPermission "getServletInfo"
 *  org.mortbay.jetty.aspect.servlets.ServletPermission "destroy"
 *
 */
public aspect HttpServletAspect
{   
    
//    pointcut blessService(HttpServlet s, ServletRequest req, ServletResponse res) : );
    
    void around (final ServletRequest req, final ServletResponse res) : target(HttpServlet) && args(req, res) && call(* service(..) throws *) 
    {            
        AccessController.doPrivileged(new PrivilegedAction()
        {
            public Object run()
            {
                proceed(req,res);
                
                return null;
            }
        });
    }
    
}

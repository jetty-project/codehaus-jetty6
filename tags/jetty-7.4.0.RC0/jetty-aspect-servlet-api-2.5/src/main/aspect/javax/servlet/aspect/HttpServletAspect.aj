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
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.Servlet;


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
    /**
     * wrap the main entry point into the servlet service() method with an around aspect
     * 
     * inside we run priviledged so we isolate permissions between userspace (webapps) and
     * jetty itself. 
     *     
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    void around(final ServletRequest req, final ServletResponse res) throws ServletException, IOException : args(req, res) && execution(* HttpServlet.service(..) throws *) 
    {
        try
        {
            AccessController.doPrivileged(new PrivilegedExceptionAction()
            {
                public Object run() throws ServletException, IOException
                {
                    proceed(req,res);

                    return null;
                }
            });
        }
        catch (PrivilegedActionException pae)
        {
            // PAE is a checked exception so it can only be ServletException or IOException
            if (pae.getException() instanceof ServletException)
            {
                throw (ServletException)pae.getException();
            }
            throw (IOException)pae.getException();
        }
    }
    
    
}

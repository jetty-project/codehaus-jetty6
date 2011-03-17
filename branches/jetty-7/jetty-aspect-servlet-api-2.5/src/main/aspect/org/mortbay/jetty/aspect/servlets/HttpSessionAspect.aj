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

import javax.servlet.http.HttpSession;

/**
 * Aspect for handling calls related to HttpSession
 *
 * Permission:
 *  org.mortbay.jetty.aspect.servlets.ServletHttpSessionPermission
 */
public aspect HttpSessionAspect
{ 
    /**
     * permission must be granted to interact with any public session method
     * 
     * @param c
     */
    pointcut checkHttpSessionsEnabled(HttpSession c) : target(c) && call(public * *(..));
    
    before( HttpSession c ): checkHttpSessionsEnabled(c)
    {
        
        SecurityManager sm = System.getSecurityManager();
        
        if ( sm != null )
        {
            sm.checkPermission( new ServletHttpSessionPermission() ); 
        }            
    }  
}

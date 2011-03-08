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

import javax.servlet.http.Cookie;

/**
 * Aspect for handling calls related to Cookie
 *
 * Permission:
 *   org.mortbay.jetty.aspect.servlets.ServletCookiePermission
 */
public aspect CookieAspect
{    
    /**
     * permission must be granted to create cookies
     */
    before() : call(Cookie.new(..))
    {
        SecurityManager sm = System.getSecurityManager();
        
        if ( sm != null )
        {
            sm.checkPermission( new ServletCookiePermission() );
        }            
    }  
    
    /**
     * if a cookie exists, permission must be granted to call any public methods on it
     * 
     * @param c
     */
    pointcut checkCookiesEnabled(Cookie c) : target(c) && call(public * *(..));

    before( Cookie c ) : checkCookiesEnabled(c)
    {
        SecurityManager sm = System.getSecurityManager();
        
        if ( sm != null )
        {
            sm.checkPermission( new ServletCookiePermission() );
        }    
    }   
}

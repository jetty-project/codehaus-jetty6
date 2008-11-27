//========================================================================
//$Id: PojoFilterTest.java 3363 2008-07-22 13:40:59Z janb $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package com.acme;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.annotation.FilterMapping;
import javax.servlet.http.annotation.ServletFilter;

@ServletFilter(filterMapping = @FilterMapping(urlPattern = { "/ttt/*" }))
public class PojoFilterTest
{
    public void doFilter (HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws java.io.IOException, javax.servlet.ServletException
    {
        HttpSession session = request.getSession(true);
        
        session.setAttribute("action", Boolean.TRUE);
        chain.doFilter(request, response);
    }
}

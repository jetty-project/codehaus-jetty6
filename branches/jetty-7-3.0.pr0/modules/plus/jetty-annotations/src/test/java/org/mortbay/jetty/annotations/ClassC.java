// ========================================================================
//$Id$
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
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
package org.mortbay.jetty.annotations;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.annotation.FilterMapping;
import javax.servlet.http.annotation.InitParam;
import javax.servlet.http.annotation.Servlet;
import javax.servlet.http.annotation.jaxrs.POST;
import javax.servlet.http.annotation.jaxrs.GET;
import javax.servlet.http.annotation.ServletFilter;


@Servlet(urlMappings = { "/foo/*", "/bah/*" }, name="CServlet", initParams={@InitParam(name="x", value="y")})
@ServletFilter(filterName="CFilter", filterMapping=@FilterMapping(dispatcherTypes={DispatcherType.REQUEST}, urlPattern = {"/*"}), initParams={@InitParam(name="a", value="99")})
@RunAs("admin")
public class ClassC
{
    @Resource (mappedName="foo")
    private Double foo;
    
    @PreDestroy
    public void pre ()
    {
        
    }
    
    @PostConstruct
    public void post()
    {
        
    }
    
    @GET()
    @POST()
    public void anything (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.getWriter().println("<h1>Pojo Servlet</h1>");
        response.getWriter().println("Acting like a Servlet.");
    }
    
    
    public void doFilter (HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws java.io.IOException, javax.servlet.ServletException
    {
        HttpSession session = request.getSession(true);
        String val = request.getParameter("action");
        if (val!=null)
            session.setAttribute("action", val);
        chain.doFilter(request, response);
    }
}

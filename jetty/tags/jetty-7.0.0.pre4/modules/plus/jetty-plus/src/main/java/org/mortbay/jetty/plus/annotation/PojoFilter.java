// ========================================================================
// $Id$
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.annotation;

import java.io.IOException;
import java.lang.reflect.Method;


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PojoFilter implements Filter, PojoWrapper
{
    private Object _pojo;
    private Method _doFilterMethod;
    private static final Class[] __params = new Class[] {HttpServletRequest.class, HttpServletResponse.class, FilterChain.class};
    
    public PojoFilter (Object pojo)
    {
        if (pojo == null)
            throw new IllegalArgumentException ("Pojo is null");
        
        _pojo=pojo;
        
        try
        {
            _doFilterMethod = _pojo.getClass().getDeclaredMethod("doFilter", __params);
        }
        catch (Exception e)
        {
            throw new IllegalStateException (e);
        }

    }
    
    public Object getPojo()
    {
        return _pojo;
    }
    
    public void destroy()
    {
       //TODO???? Should try to find a destroy method on the pojo?
    }

    
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {   
        try
        {
            _doFilterMethod.invoke(_pojo, new Object[]{req, resp, chain});
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    public void init(FilterConfig arg0) throws ServletException
    {
        // TODO ???? Should try to find an init() method on the pojo?
    }


}

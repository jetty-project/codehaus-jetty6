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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PojoServlet extends HttpServlet implements PojoWrapper
{
    private Object _pojo;
    private String _deleteMethodName;
    private Method _deleteMethod;
    private String _putMethodName;
    private Method _putMethod;
    private String _headMethodName;
    private Method _headMethod;
    private String _postMethodName;
    private Method _postMethod;
    private String _getMethodName;
    private Method _getMethod;
    private static final Class[] __params = new Class[]{HttpServletRequest.class, HttpServletResponse.class};
    
    public PojoServlet (Object pojo)
    {
        if (pojo==null)
            throw new IllegalArgumentException("Pojo is null");
        
        _pojo=pojo;    
    }

    public Object getPojo()
    {
        return _pojo;
    }
    public void setDeleteMethodName (String name)
    {
        _deleteMethodName = name;
    }
    public String getDeleteMethodName ()
    {
        return _deleteMethodName;
    }
    public void setPutMethodName (String name)
    {
        _putMethodName = name;
    }
    public String getPutMethodName ()
    {
        return _putMethodName;
    }
    public void setHeadMethodName (String name)
    {
        _headMethodName = name;
    }

    public String getHeadMethodName ()
    {
        return _headMethodName;
    }
    public void setPostMethodName (String name)
    {
        _postMethodName = name;
    }
    public String getPostMethodName ()
    {
        return _postMethodName;
    }
    
    public void setGetMethodName (String name)
    {
        _getMethodName = name;
    }
    public String getGetMethodName ()
    {
        return _getMethodName;
    }
    
  
    public void init() throws ServletException
    {
        
        try
        {
            if (_getMethodName != null)
                _getMethod = _pojo.getClass().getDeclaredMethod(_getMethodName, __params);
            if (_postMethodName != null)
                _postMethod = _pojo.getClass().getDeclaredMethod(_postMethodName, __params);
            if (_headMethodName != null)
                _headMethod = _pojo.getClass().getDeclaredMethod(_headMethodName, __params);
            if (_putMethodName != null)
                _putMethod = _pojo.getClass().getDeclaredMethod(_putMethodName, __params);
            if (_deleteMethodName != null)
                _deleteMethod = _pojo.getClass().getDeclaredMethod(_deleteMethodName, __params);          
        }
        catch (NoSuchMethodException e)
        {
           throw new ServletException (e);
        }
        super.init();
    }


    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        invoke (_deleteMethod, req, resp);
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        invoke(_getMethod,req, resp);
    }


    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        invoke(_headMethod, req, resp);
    }


    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        invoke(_postMethod, req, resp);
    }


    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        invoke(_putMethod, req, resp);
    }

    private void invoke (Method method, HttpServletRequest req, HttpServletResponse resp)
    throws ServletException
    {
        if (method == null)
            throw new ServletException ("No method");

        try
        {
            method.invoke(_pojo, new Object[]{req, resp});
        }
        catch (IllegalAccessException e)
        {
            throw new ServletException (e);
        }
        catch (InvocationTargetException e)
        {
            throw new ServletException (e);
        }
    }

}

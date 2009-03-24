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

import java.lang.reflect.Method;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class PojoContextListener implements ServletContextListener, PojoWrapper
{
    private Object _pojo;
    private Method _contextDestroyedMethod;
    private Method _contextInitializedMethod;
    private static final Class[] __params = new Class[]{ServletContextEvent.class};
    
    public PojoContextListener(Object pojo)
    throws IllegalArgumentException
    {
        if (pojo==null)
            throw new IllegalArgumentException("Pojo is null");

        _pojo = pojo;
        try
        {
            _contextDestroyedMethod = _pojo.getClass().getDeclaredMethod("contextDestroyed", __params);
            _contextInitializedMethod = _pojo.getClass().getDeclaredMethod("contextInitialized", __params);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException (e.getLocalizedMessage());   
        }
    }
    
    public Object getPojo()
    {
        return _pojo;
    }

    public void contextDestroyed(ServletContextEvent event)
    {
        try
        {
            _contextDestroyedMethod.invoke(_pojo, new Object[]{event});
        }
        catch (Exception e)
        {
            event.getServletContext().log("Error invoking contextInitialized", e);
        }

    }

    public void contextInitialized(ServletContextEvent event)
    {
        try
        {
            _contextInitializedMethod.invoke(_pojo, new Object[]{event});
        }
        catch (Exception e)
        {
            event.getServletContext().log("Error invoking contextInitialized", e);
        }
    }

}

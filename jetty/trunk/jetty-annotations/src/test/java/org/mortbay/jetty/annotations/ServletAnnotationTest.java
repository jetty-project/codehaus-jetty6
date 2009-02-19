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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import junit.framework.TestCase;

import org.mortbay.jetty.plus.annotation.Injection;
import org.mortbay.jetty.plus.annotation.InjectionCollection;
import org.mortbay.jetty.plus.annotation.LifeCycleCallback;
import org.mortbay.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.mortbay.jetty.plus.annotation.PojoFilter;
import org.mortbay.jetty.plus.annotation.PojoServlet;
import org.mortbay.jetty.plus.annotation.RunAs;
import org.mortbay.jetty.plus.annotation.RunAsCollection;
import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.server.servlet.FilterHolder;
import org.mortbay.jetty.server.servlet.FilterMapping;
import org.mortbay.jetty.server.servlet.ServletHolder;
import org.mortbay.jetty.server.servlet.ServletMapping;
import org.mortbay.jetty.webapp.WebAppContext;

public class ServletAnnotationTest extends TestCase
{ 
   
    public void tearDown()
    throws Exception
    {
        InitialContext ic = new InitialContext();
        Context comp = (Context)ic.lookup("java:comp");
        comp.destroySubcontext("env");
    }
    
    public void testAnnotations() throws Exception
    {
        Server server = new Server();
        WebAppContext wac = new WebAppContext();
        wac.setServer(server);
        
        InitialContext ic = new InitialContext();
        Context comp = (Context)ic.lookup("java:comp");
        Context env = null;
        try
        {
            env = (Context)comp.lookup("env");
        }
        catch (NameNotFoundException e)
        {
            env = comp.createSubcontext("env");
        }
          
        org.mortbay.jetty.plus.jndi.EnvEntry foo = new org.mortbay.jetty.plus.jndi.EnvEntry("foo", new Double(1000.00), false);
        List servlets = new ArrayList();
        List filters = new ArrayList();
        List listeners = new ArrayList();
        List servletMappings = new ArrayList();
        List filterMappings = new ArrayList();
        
        List classes = new ArrayList();
        classes.add("org.mortbay.jetty.annotations.ClassC");
       
        AnnotationFinder finder = new AnnotationFinder();
        finder.find (classes, 
                new ClassNameResolver()
        {

            public boolean isExcluded(String name)
            {
                return false;
            }

            public boolean shouldOverride(String name)
            {
                return true;
            }
            
        });
  
        
        RunAsCollection runAs = new RunAsCollection();
        InjectionCollection injections = new InjectionCollection();
        LifeCycleCallbackCollection callbacks = new LifeCycleCallbackCollection();
        
        AnnotationProcessor processor = new AnnotationProcessor (wac, finder, runAs, injections, callbacks, 
                servlets, filters, listeners, servletMappings, filterMappings);
        processor.process();
        
        
        assertEquals(1, servlets.size());
        ServletHolder sholder = (ServletHolder)servlets.get(0);
        assertEquals("CServlet", sholder.getName());
        assertTrue(sholder.getServlet() instanceof PojoServlet);
        PojoServlet ps  = (PojoServlet)sholder.getServlet();
        assertEquals("anything", ps.getGetMethodName());
        assertEquals("anything", ps.getPostMethodName());
        Map sinitparams = sholder.getInitParameters();
        assertEquals(1, sinitparams.size());
        assertTrue(sinitparams.containsKey("x"));
        assertTrue(sinitparams.containsValue("y"));
        assertEquals(1, filters.size());
        FilterHolder fholder = (FilterHolder)filters.get(0);
        assertTrue(fholder.getFilter() instanceof PojoFilter);
        
        Map finitparams = fholder.getInitParameters();
        assertEquals(1, finitparams.size());
        assertTrue(finitparams.containsKey("a"));
        assertTrue(finitparams.containsValue("99"));
        assertEquals(1, servletMappings.size());
        ServletMapping smap = (ServletMapping)servletMappings.get(0);
        assertEquals("CServlet", smap.getServletName());
        assertEquals(2, smap.getPathSpecs().length);
        assertEquals(1, filterMappings.size());
        FilterMapping fmap = (FilterMapping)filterMappings.get(0);
        assertEquals("CFilter", fmap.getFilterName());
        assertEquals(1, fmap.getPathSpecs().length);
        
        List<Injection> fieldInjections = injections.getFieldInjections(ClassC.class);
        assertNotNull(fieldInjections);
        assertEquals(1, fieldInjections.size());  
        
        RunAs ra = runAs.getRunAs(sholder);
        assertNotNull(ra);
        assertEquals("admin", ra.getRoleName());
        
        List predestroys = callbacks.getPreDestroyCallbacks(sholder.getServlet());
        assertNotNull(predestroys);
        assertEquals(1, predestroys.size());
        LifeCycleCallback cb = (LifeCycleCallback)predestroys.get(0);
        assertTrue(cb.getTarget().equals(ClassC.class.getDeclaredMethod("pre", new Class[]{})));
        
        List postconstructs = callbacks.getPostConstructCallbacks(sholder.getServlet());
        assertNotNull(postconstructs);
        assertEquals(1, postconstructs.size());
        cb = (LifeCycleCallback)postconstructs.get(0);
        assertTrue(cb.getTarget().equals(ClassC.class.getDeclaredMethod("post", new Class[]{})));
    }

}

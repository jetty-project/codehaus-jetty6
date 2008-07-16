//========================================================================
//$Id$
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.mortbay.jetty.annotations.resources.ResourceA;
import org.mortbay.jetty.annotations.resources.ResourceB;
import org.mortbay.jetty.plus.annotation.Injection;
import org.mortbay.jetty.plus.annotation.InjectionCollection;
import org.mortbay.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.mortbay.jetty.plus.annotation.RunAsCollection;
import org.mortbay.jetty.plus.naming.NamingEntry;


import junit.framework.TestCase;

/**
 * TestAnnotationInheritance
 *
 *
 */
public class TestAnnotationInheritance extends TestCase
{
    List<String> classNames = new ArrayList<String>();
    
   
    public void tearDown ()
    {
        classNames.clear();
    }
    
    public void testInheritance ()
    throws Exception
    {        
        NamingEntry.setScope(NamingEntry.SCOPE_WEBAPP);
        classNames.add(ClassA.class.getName());
        classNames.add(ClassB.class.getName());
        AnnotationFinder finder = new AnnotationFinder(Thread.currentThread().getContextClassLoader(), classNames);    
       
        List<Class<?>> classes = finder.getClassesForAnnotation(Sample.class);
        assertEquals(2, classes.size());
        
        //check methods
        //List methods = collection.getMethods();
        List<Method> methods = finder.getMethodsForAnnotation(Sample.class);
        
        assertTrue(methods!=null);
        assertFalse(methods.isEmpty());
        /*
        assertEquals(methods.size(), 4);
        Method m = ClassB.class.getDeclaredMethod("a", new Class[] {});       
        assertTrue(methods.indexOf(m) >= 0);
        Sample s = (Sample)m.getAnnotation(Sample.class);
        assertEquals(51, s.value());
        m = ClassA.class.getDeclaredMethod("a", new Class[] {});
        assertTrue(methods.indexOf(m) < 0); //check overridden public scope superclass method not in there
        
        m = ClassA.class.getDeclaredMethod("b", new Class[] {});
        assertTrue(methods.indexOf(m) >= 0);      
        
        m = ClassB.class.getDeclaredMethod("c", new Class[] {});
        assertTrue(methods.indexOf(m) >= 0);
        m = ClassA.class.getDeclaredMethod("c", new Class[] {});
        assertTrue(methods.indexOf(m) < 0); //check overridden superclass package scope method not in there
        
        m = ClassA.class.getDeclaredMethod("d", new Class[] {});
        assertTrue(methods.indexOf(m) >= 0);
        
        //check fields
        List fields = collection.getFields();
        assertFalse(fields.isEmpty());
        assertEquals(1, fields.size());
        
        Field f = ClassA.class.getDeclaredField("m");
        assertTrue(fields.indexOf(f) >= 0);
        
      */
        NamingEntry.setScope(NamingEntry.SCOPE_CONTAINER);
    }
    
    
    
    
    public void testResourceAnnotations ()
    throws Exception
    {
        InitialContext ic = new InitialContext();
        Context comp = (Context)ic.lookup("java:comp");
        Context env = comp.createSubcontext("env");
        
        org.mortbay.jetty.plus.naming.EnvEntry resourceA = new org.mortbay.jetty.plus.naming.EnvEntry("resA", new Integer(1000));
        org.mortbay.jetty.plus.naming.EnvEntry resourceB = new org.mortbay.jetty.plus.naming.EnvEntry("resB", new Integer(2000));
        
        NamingEntry.setScope(NamingEntry.SCOPE_WEBAPP);
        classNames.add(ResourceA.class.getName());
        classNames.add(ResourceB.class.getName());
        AnnotationFinder finder = new AnnotationFinder(Thread.currentThread().getContextClassLoader(), classNames);
       
        List<Class<?>> annotatedClasses = finder.getClassesForAnnotation(Resource.class);      
        List<Method> annotatedMethods = finder.getMethodsForAnnotation(Resource.class);
        List<Field>  annotatedFields = finder.getFieldsForAnnotation(Resource.class);
        assertNotNull(annotatedClasses);
        assertEquals(0, annotatedClasses.size());
        assertEquals(3, annotatedMethods.size());
        assertEquals(6, annotatedFields.size());
        
        InjectionCollection injections = new InjectionCollection();
        LifeCycleCallbackCollection callbacks = new LifeCycleCallbackCollection();
        RunAsCollection runAses = new RunAsCollection();
        AnnotationProcessor processor = new AnnotationProcessor(finder, runAses, injections, callbacks);
        //process with all the specific annotations turned into injections, callbacks etc
        processor.process();
        
        //processing classA should give us these jndi name bindings:
        // java:comp/env/myf
        // java:comp/env/org.mortbay.jetty.annotations.resources.ResourceA/g
        // java:comp/env/mye
        // java:comp/env/org.mortbay.jetty.annotations.resources.ResourceA/h
        // java:comp/env/resA
        // java:comp/env/org.mortbay.jetty.annotations.resources.ResourceB/f
        // java:comp/env/org.mortbay.jetty.annotations.resources.ResourceA/n
        // 
        assertEquals(resourceB.getObjectToBind(), env.lookup("myf"));
        assertEquals(resourceA.getObjectToBind(), env.lookup("mye"));
        assertEquals(resourceA.getObjectToBind(), env.lookup("resA"));
        assertEquals(resourceA.getObjectToBind(), env.lookup("org.mortbay.jetty.annotations.resources.ResourceA/g")); 
        assertEquals(resourceA.getObjectToBind(), env.lookup("org.mortbay.jetty.annotations.resources.ResourceA/h"));
        assertEquals(resourceB.getObjectToBind(), env.lookup("org.mortbay.jetty.annotations.resources.ResourceB/f"));
        assertEquals(resourceB.getObjectToBind(), env.lookup("org.mortbay.jetty.annotations.resources.ResourceA/n"));
        
        //we should have Injections
        assertNotNull(injections);
        
        List<Injection> fieldInjections = injections.getFieldInjections(ResourceB.class);
        assertNotNull(fieldInjections);
        
        Iterator itor = fieldInjections.iterator();
        System.err.println("Field injections:");
        while (itor.hasNext())
        {
            System.err.println(itor.next());
        }
        //only 1 field injection because the other has no Resource mapping
        assertEquals(1, fieldInjections.size());
        
        fieldInjections = injections.getFieldInjections(ResourceA.class);
        assertNotNull(fieldInjections);
        assertEquals(4, fieldInjections.size());
        
        
        List<Injection> methodInjections = injections.getMethodInjections(ResourceB.class);
        itor = methodInjections.iterator();
        System.err.println("Method injections:");
        while (itor.hasNext())
            System.err.println(itor.next());
        
        assertNotNull(methodInjections);
        assertEquals(0, methodInjections.size());
        
        methodInjections = injections.getMethodInjections(ResourceA.class);
        assertNotNull(methodInjections);
        assertEquals(3, methodInjections.size());
        
        //test injection
        ResourceB binst = new ResourceB();
        injections.inject(binst);
        
        //check injected values
        Field f = ResourceB.class.getDeclaredField ("f");
        f.setAccessible(true);
        assertEquals(resourceB.getObjectToBind() , f.get(binst));
        
        //@Resource(mappedName="resA") //test the default naming scheme but using a mapped name from the environment
        f = ResourceA.class.getDeclaredField("g"); 
        f.setAccessible(true);
        assertEquals(resourceA.getObjectToBind(), f.get(binst));
        
        //@Resource(name="resA") //test using the given name as the name from the environment
        f = ResourceA.class.getDeclaredField("j");
        f.setAccessible(true);
        assertEquals(resourceA.getObjectToBind(), f.get(binst));
        
        //@Resource(mappedName="resB") //test using the default name on an inherited field
        f = ResourceA.class.getDeclaredField("n"); 
        f.setAccessible(true);
        assertEquals(resourceB.getObjectToBind(), f.get(binst));
    }

}

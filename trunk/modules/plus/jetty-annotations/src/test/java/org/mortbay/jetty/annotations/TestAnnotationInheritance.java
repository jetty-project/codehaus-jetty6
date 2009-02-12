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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.annotations.resources.ResourceA;
import org.mortbay.jetty.annotations.resources.ResourceB;
import org.mortbay.jetty.plus.annotation.Injection;
import org.mortbay.jetty.plus.annotation.InjectionCollection;
import org.mortbay.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.mortbay.jetty.plus.annotation.RunAsCollection;
import org.mortbay.jetty.plus.jndi.NamingEntry;
import org.mortbay.jetty.webapp.WebAppContext;


import junit.framework.TestCase;

/**
 * TestAnnotationInheritance
 *
 *
 */
public class TestAnnotationInheritance extends TestCase
{
    List<String> classNames = new ArrayList<String>();
    
   
    public void tearDown () throws Exception
    {
        classNames.clear();
        InitialContext ic = new InitialContext();
        Context comp = (Context)ic.lookup("java:comp");
        comp.destroySubcontext("env");
    }
    
    public void testInheritance ()
    throws Exception
    {        
        classNames.add(ClassA.class.getName());
        classNames.add(ClassB.class.getName());
        
        AnnotationFinder finder = new AnnotationFinder();
        finder.find(classNames, new ClassNameResolver () 
        {
            public boolean isExcluded(String name)
            {
                return false;
            }

            public boolean shouldOverride(String name)
            {
                return false;
            }       
        });    
       
        List<Class<?>> classes = finder.getClassesForAnnotation(Sample.class);
        assertEquals(2, classes.size());
        
        //check methods
        //List methods = collection.getMethods();
        List<Method> methods = finder.getMethodsForAnnotation(Sample.class);
        
        assertTrue(methods!=null);
        assertFalse(methods.isEmpty());
    }
    
    
    public void testExclusions()
    throws Exception
    {
        AnnotationFinder finder = new AnnotationFinder();
        finder.find(ClassA.class.getName(), new ClassNameResolver()
        {
            public boolean isExcluded(String name)
            {
                return true;
            }

            public boolean shouldOverride(String name)
            {
                return false;
            }       
        });
        assertTrue(finder.getClassesForAnnotation(Sample.class).isEmpty());
        
        finder.find (ClassA.class.getName(), new ClassNameResolver()
        {
            public boolean isExcluded(String name)
            {
                return false;
            }

            public boolean shouldOverride(String name)
            {
                return false;
            }        
        });
        assertEquals(1, finder.getClassesForAnnotation(Sample.class).size());
    }
    
    
    public void testResourceAnnotations ()
    throws Exception
    {
        Server server = new Server();
        WebAppContext wac = new WebAppContext();
        wac.setServer(server);
        
        InitialContext ic = new InitialContext();
        Context comp = (Context)ic.lookup("java:comp");
        Context env = comp.createSubcontext("env");
        
        org.mortbay.jetty.plus.jndi.EnvEntry resourceA = new org.mortbay.jetty.plus.jndi.EnvEntry(server, "resA", new Integer(1000), false);
        org.mortbay.jetty.plus.jndi.EnvEntry resourceB = new org.mortbay.jetty.plus.jndi.EnvEntry(server, "resB", new Integer(2000), false);
        

        classNames.add(ResourceA.class.getName());
        classNames.add(ResourceB.class.getName());
        AnnotationFinder finder = new AnnotationFinder();
        finder.find(classNames, new ClassNameResolver()
        {
            public boolean isExcluded(String name)
            {
                return false;
            }

            public boolean shouldOverride(String name)
            {
                return false;
            }       
        });
       
        List<Class<?>> resourcesClasses = finder.getClassesForAnnotation(Resources.class);
        assertNotNull(resourcesClasses);
        assertEquals(1, resourcesClasses.size());
        
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
        AnnotationProcessor processor = new AnnotationProcessor(wac, finder, runAses, injections, callbacks, 
                Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
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

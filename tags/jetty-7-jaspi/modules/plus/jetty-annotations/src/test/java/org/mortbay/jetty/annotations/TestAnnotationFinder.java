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

package org.mortbay.jetty.annotations;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

public class TestAnnotationFinder extends TestCase
{
    
    public void testNormalize()
    {
        assertEquals("org.mortbay.test.Foo", AnnotationFinder.normalize("Lorg/mortbay/test/Foo;"));
        assertEquals("org.mortbay.test.Foo.Bar", AnnotationFinder.normalize("org/mortbay/test/Foo$Bar.class"));
    }
    
    public void testConvertType ()
    throws Exception
    {
       
    }
    
    
    public void testSampleAnnotation ()
    throws Exception
    {      
        long start = System.currentTimeMillis();
        
        String[] classNames = new String[]{"org.mortbay.jetty.annotations.ClassA"};
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
        long end = System.currentTimeMillis();
        
        System.err.println("Time to parse class: "+((end-start)));
        
        start = System.currentTimeMillis();
        List<Class<?>> classes = finder.getClassesForAnnotation(Sample.class);
        end = System.currentTimeMillis(); 
        System.err.println("Time to find classes matching annotation: "+((end-start)));
        
        assertNotNull(classes);
        assertEquals(1, classes.size());
        assertTrue(classes.contains(org.mortbay.jetty.annotations.ClassA.class));
        
        start = System.currentTimeMillis();
        List<Method> methods = finder.getMethodsForAnnotation(Sample.class); 
        end = System.currentTimeMillis();    
        System.err.println("Time to find methods matching annotation : "+((end-start)));
        
        assertNotNull(methods);
        assertEquals (5, methods.size());

        Method a = ClassA.class.getDeclaredMethod("a", new Class[]{Array.newInstance(Integer.class, 0).getClass()});
        Method b = ClassA.class.getDeclaredMethod("b", new Class[]{Array.newInstance(ClassA.Foo.class, 0).getClass()});
        Method c = ClassA.class.getDeclaredMethod("c", new Class[]{Array.newInstance(Integer.TYPE, 0).getClass()});
        Method d = ClassA.class.getDeclaredMethod("d", new Class[]{Integer.TYPE, String.class});
        Method l = ClassA.class.getDeclaredMethod("l", new Class[]{});
        
        assertTrue(methods.contains(a));
        assertTrue(methods.contains(b));
        assertTrue(methods.contains(c));
        assertTrue(methods.contains(d));
        assertTrue(methods.contains(l));
        
        start = System.currentTimeMillis();
        List<Field> fields = finder.getFieldsForAnnotation(Sample.class);
        end = System.currentTimeMillis();
        System.err.println("Time to find fields matching annotation : "+((end-start)));
        
        assertNotNull(fields);
        assertEquals(1, fields.size());
        
        Field m = ClassA.class.getDeclaredField("m");
        assertTrue(fields.contains(m)); 
    } 
}

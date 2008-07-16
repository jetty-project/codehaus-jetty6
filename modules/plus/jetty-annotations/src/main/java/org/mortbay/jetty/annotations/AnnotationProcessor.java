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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.RunAs;
import javax.naming.NamingException;
import javax.servlet.Servlet;

import org.mortbay.jetty.plus.annotation.Injection;
import org.mortbay.jetty.plus.annotation.InjectionCollection;
import org.mortbay.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.mortbay.jetty.plus.annotation.PostConstructCallback;
import org.mortbay.jetty.plus.annotation.PreDestroyCallback;
import org.mortbay.jetty.plus.annotation.RunAsCollection;
import org.mortbay.log.Log;
import org.mortbay.util.IntrospectionUtil;

public class AnnotationProcessor
{
    AnnotationFinder _finder;
    ClassLoader _loader;
    RunAsCollection _runAs;
    InjectionCollection _injections;
    LifeCycleCallbackCollection _callbacks;
    private static Class[] __envEntryTypes = 
        new Class[] {String.class, Character.class, Integer.class, Boolean.class, Double.class, Byte.class, Short.class, Long.class, Float.class};
   
    public AnnotationProcessor(AnnotationFinder finder, RunAsCollection runAs, InjectionCollection injections, LifeCycleCallbackCollection callbacks)
    {
        _finder=finder;
        _runAs=runAs;
        _injections=injections;
        _callbacks=callbacks;
    }
    
    
    public void process ()
    throws Exception
    { 
        processRunAsAnnotations();
        processLifeCycleCallbackAnnotations();
        processResourcesAnnotations();
        processResourceAnnotations();
    }
    
    public void processRunAsAnnotations ()
    throws Exception
    {
        for (Class clazz:_finder.getClassesForAnnotation(RunAs.class))
        {
            //if this implements javax.servlet.Servlet check for run-as
            if (Servlet.class.isAssignableFrom(clazz))
            { 
                RunAs runAs = (RunAs)clazz.getAnnotation(RunAs.class);
                if (runAs != null)
                {
                    String role = runAs.value();
                    if (role != null)
                    {
                        org.mortbay.jetty.plus.annotation.RunAs ra = new org.mortbay.jetty.plus.annotation.RunAs();
                        ra.setTargetClass(clazz);
                        ra.setRoleName(role);
                        _runAs.add(ra);
                    }
                }
            }
        } 
    }
    
    
    public void processLifeCycleCallbackAnnotations()
    throws Exception
    {
        processPostConstructAnnotations();
        processPreDestroyAnnotations();
    }

    private void processPostConstructAnnotations ()
    throws Exception
    {
        //      TODO: check that the same class does not have more than one
        for (Method m:_finder.getMethodsForAnnotation(PostConstruct.class))
        {

            if (m.getParameterTypes().length != 0)
                throw new IllegalStateException(m+" has parameters");
            if (m.getReturnType() != Void.TYPE)
                throw new IllegalStateException(m+" is not void");
            if (m.getExceptionTypes().length != 0)
                throw new IllegalStateException(m+" throws checked exceptions");
            if (Modifier.isStatic(m.getModifiers()))
                throw new IllegalStateException(m+" is static");

            PostConstructCallback callback = new PostConstructCallback();
            callback.setTargetClass(m.getDeclaringClass());
            callback.setTarget(m);
            _callbacks.add(callback);
        }
    }

    public void processPreDestroyAnnotations ()
    throws Exception
    {
        //TODO: check that the same class does not have more than one

        for (Method m: _finder.getMethodsForAnnotation(PreDestroy.class))
        {
            if (m.getParameterTypes().length != 0)
                throw new IllegalStateException(m+" has parameters");
            if (m.getReturnType() != Void.TYPE)
                throw new IllegalStateException(m+" is not void");
            if (m.getExceptionTypes().length != 0)
                throw new IllegalStateException(m+" throws checked exceptions");
            if (Modifier.isStatic(m.getModifiers()))
                throw new IllegalStateException(m+" is static");

            PreDestroyCallback callback = new PreDestroyCallback(); 
            callback.setTargetClass(m.getDeclaringClass());
            callback.setTarget(m);
            _callbacks.add(callback);
        }
    }
    
    

    public void processResourcesAnnotations ()
    throws Exception
    {
        List<Class<?>> classes = _finder.getClassesForAnnotation(Resources.class);
        for (Class<?> clazz:classes)
        {
            //Handle Resources annotation - add namespace entries
            Resources resources = (Resources)clazz.getAnnotation(Resources.class);
            if (resources == null)
                continue;

            Resource[] resArray = resources.value();
            if (resArray==null||resArray.length==0)
                continue;

            for (int j=0;j<resArray.length;j++)
            {

                String name = resArray[j].name();
                String mappedName = resArray[j].mappedName();
                Resource.AuthenticationType auth = resArray[j].authenticationType();
                Class type = resArray[j].type();
                boolean shareable = resArray[j].shareable();

                if (name==null || name.trim().equals(""))
                    throw new IllegalStateException ("Class level Resource annotations must contain a name (Common Annotations Spec Section 2.3)");
                //TODO don't ignore the shareable, auth etc etc

                //make it optional to use the mappedName to represent the JNDI name of the resource in
                //the runtime environment. If present the mappedName would represent the JNDI name set
                //for a Resource entry in jetty.xml or jetty-env.xml.
                org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(name, mappedName);
            }
        }
    }
    
    
    public void processResourceAnnotations ()
    throws Exception
    {
        processClassResourceAnnotations();
        processMethodResourceAnnotations();
        processFieldResourceAnnotations();
    }
    
    
    public void processClassResourceAnnotations ()
    throws Exception
    {
        List<Class<?>> classes = _finder.getClassesForAnnotation(Resource.class);
        for (Class<?> clazz:classes)
        {
            //Handle Resource annotation - add namespace entries
            Resource resource = (Resource)clazz.getAnnotation(Resource.class);
            if (resource == null)
                continue;

            String name = resource.name();
            String mappedName = resource.mappedName();
            Resource.AuthenticationType auth = resource.authenticationType();
            Class type = resource.type();
            boolean shareable = resource.shareable();

            if (name==null || name.trim().equals(""))
                throw new IllegalStateException ("Class level Resource annotations must contain a name (Common Annotations Spec Section 2.3)");

            //TODO don't ignore the shareable, auth etc etc
            org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(name,mappedName);
        }
    }
    

    public void processMethodResourceAnnotations ()
    throws Exception
    {
        //Get all methods that have a Resource annotation
        List<Method> methods = _finder.getMethodsForAnnotation(javax.annotation.Resource.class);

        for (Method m: methods)
        {
            /*
             * Commons Annotations Spec 2.3
             * " The Resource annotation is used to declare a reference to a resource.
             *   It can be specified on a class, methods or on fields. When the 
             *   annotation is applied on a field or method, the container will 
             *   inject an instance of the requested resource into the application 
             *   when the application is initialized... Even though this annotation 
             *   is not marked Inherited, if used all superclasses MUST be examined 
             *   to discover all uses of this annotation. All such annotation instances 
             *   specify resources that are needed by the application. Note that this 
             *   annotation may appear on private fields and methods of the superclasses. 
             *   Injection of the declared resources needs to happen in these cases as 
             *   well, even if a method with such an annotation is overridden by a subclass."
             *  
             *  Which IMHO, put more succinctly means "If you find a @Resource on any method
             *  or field, inject it!".
             */
            Resource resource = (Resource)m.getAnnotation(Resource.class);
            if (resource == null)
                continue;

            //JavaEE Spec 5.2.3: Method cannot be static
            if (Modifier.isStatic(m.getModifiers()))
                throw new IllegalStateException(m+" cannot be static");


            // Check it is a valid javabean 
            if (!IntrospectionUtil.isJavaBeanCompliantSetter(m))
                throw new IllegalStateException(m+" is not a java bean compliant setter method");

            //default name is the javabean property name
            String name = m.getName().substring(3);
            name = name.substring(0,1).toLowerCase()+name.substring(1);
            name = m.getDeclaringClass().getCanonicalName()+"/"+name;
            //allow default name to be overridden
            name = (resource.name()!=null && !resource.name().trim().equals("")? resource.name(): name);
            //get the mappedName if there is one
            String mappedName = (resource.mappedName()!=null && !resource.mappedName().trim().equals("")?resource.mappedName():null);

            Class type = m.getParameterTypes()[0];

            //get other parts that can be specified in @Resource
            Resource.AuthenticationType auth = resource.authenticationType();
            boolean shareable = resource.shareable();

            //if @Resource specifies a type, check it is compatible with setter param
            if ((resource.type() != null) 
                    && 
                    !resource.type().equals(Object.class)
                    &&
                    (!IntrospectionUtil.isTypeCompatible(type, resource.type(), false)))
                throw new IllegalStateException("@Resource incompatible type="+resource.type()+ " with method param="+type+ " for "+m);

            //check if an injection has already been setup for this target by web.xml
            Injection webXmlInjection = _injections.getInjection(m.getDeclaringClass(), m);
            if (webXmlInjection == null)
            {
                try
                {
                    org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(name, mappedName);

                    Log.debug("Bound "+(mappedName==null?name:mappedName) + " as "+ name);
                    //   Make the Injection for it
                    Injection injection = new Injection();
                    injection.setTargetClass(m.getDeclaringClass());
                    injection.setJndiName(name);
                    injection.setMappingName(mappedName);
                    injection.setTarget(m);
                    _injections.add(injection);
                }
                catch (NamingException e)
                {  
                    //if this is an env-entry type resource and there is no value bound for it, it isn't
                    //an error, it just means that perhaps the code will use a default value instead
                    // JavaEE Spec. sec 5.4.1.3
                    if (!isEnvEntryType(type))
                        throw new IllegalStateException(e);
                }
            }
            else
            {
                //if an injection is already set up for this name, then the types must be compatible
                //JavaEE spec sec 5.2.4

                Object value = webXmlInjection.lookupInjectedValue();
                if (!IntrospectionUtil.isTypeCompatible(type, value.getClass(), false))
                    throw new IllegalStateException("Type of field="+type+" is not compatible with Resource type="+value.getClass());
            }
        }
    }


    public void processFieldResourceAnnotations ()
    throws Exception
    {
        //Get all fields that have a Resource annotation
        List<Field> fields = _finder.getFieldsForAnnotation(Resource.class);
        for (Field f: fields)
        {
            Resource resource = (Resource)f.getAnnotation(Resource.class);
            if (resource == null)
                continue;

            //JavaEE Spec 5.2.3: Field cannot be static
            if (Modifier.isStatic(f.getModifiers()))
                throw new IllegalStateException(f+" cannot be static");

            //JavaEE Spec 5.2.3: Field cannot be final
            if (Modifier.isFinal(f.getModifiers()))
                throw new IllegalStateException(f+" cannot be final");

            //work out default name
            String name = f.getDeclaringClass().getCanonicalName()+"/"+f.getName();
            //allow @Resource name= to override the field name
            name = (resource.name()!=null && !resource.name().trim().equals("")? resource.name(): name);

            //get the type of the Field
            Class type = f.getType();
            //if @Resource specifies a type, check it is compatible with field type
            if ((resource.type() != null)
                    && 
                    !resource.type().equals(Object.class)
                    &&
                    (!IntrospectionUtil.isTypeCompatible(type, resource.type(), false)))
                throw new IllegalStateException("@Resource incompatible type="+resource.type()+ " with field type ="+f.getType());

            //get the mappedName if there is one
            String mappedName = (resource.mappedName()!=null && !resource.mappedName().trim().equals("")?resource.mappedName():null);
            //get other parts that can be specified in @Resource
            Resource.AuthenticationType auth = resource.authenticationType();
            boolean shareable = resource.shareable();
            //check if an injection has already been setup for this target by web.xml
            Injection webXmlInjection = _injections.getInjection(f.getDeclaringClass(), f);
            if (webXmlInjection == null)
            {
                try
                {
                    //Check there is a JNDI entry for this annotation 
                    org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(name, mappedName);
                    Log.debug("Bound "+(mappedName==null?name:mappedName) + " as "+ name);
                    //   Make the Injection for it if the binding succeeded
                    Injection injection = new Injection();
                    injection.setTargetClass(f.getDeclaringClass());
                    injection.setJndiName(name);
                    injection.setMappingName(mappedName);
                    injection.setTarget(f);
                    _injections.add(injection); 
                }
                catch (NamingException e)
                {
                    //if this is an env-entry type resource and there is no value bound for it, it isn't
                    //an error, it just means that perhaps the code will use a default value instead
                    // JavaEE Spec. sec 5.4.1.3
                    if (!isEnvEntryType(type))
                        throw new IllegalStateException(e);
                }
            }
            else
            {
                //if an injection is already set up for this name, then the types must be compatible
                //JavaEE spec sec 5.2.4
                Object value = webXmlInjection.lookupInjectedValue();
                if (!IntrospectionUtil.isTypeCompatible(type, value.getClass(), false))
                    throw new IllegalStateException("Type of field="+type+" is not compatible with Resource type="+value.getClass());
            }
        }
    }


    private static boolean isEnvEntryType (Class type)
    {
        boolean result = false;
        for (int i=0;i<__envEntryTypes.length && !result;i++)
        {
            result = (type.equals(__envEntryTypes[i]));
        }
        return result;
    }
}

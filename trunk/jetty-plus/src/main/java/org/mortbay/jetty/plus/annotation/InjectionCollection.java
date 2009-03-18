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

package org.mortbay.jetty.plus.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mortbay.jetty.util.log.Log;

/**
 * InjectionCollection
 *
 *
 */
public class InjectionCollection
{
    private HashMap<Class<?>, List<Injection>> fieldInjectionsMap = new HashMap<Class<?>, List<Injection>>();//map of classname to field injections
    private HashMap<Class<?>, List<Injection>> methodInjectionsMap = new HashMap<Class<?>, List<Injection>>();//map of classname to method injections
    
    
    public void add (Injection injection)
    {
        if ((injection==null) || (injection.getTarget()==null) || (injection.getTargetClass()==null)) 
            return;
        
        if (Log.isDebugEnabled())
            Log.debug("Adding injection for class="+injection.getTargetClass()+ " on a "+injection.getTarget());
        Map<Class<?>, List<Injection>> injectionsMap = null;
        if (injection.getTarget() instanceof Field)
            injectionsMap = fieldInjectionsMap;
        if (injection.getTarget() instanceof Method)
            injectionsMap = methodInjectionsMap;
        
        List<Injection> injections = (List<Injection>)injectionsMap.get(injection.getTargetClass());
        if (injections==null)
        {
            injections = new ArrayList<Injection>();
            injectionsMap.put(injection.getTargetClass(), injections);
        }
        
        injections.add(injection);
    }

    public List<Injection> getFieldInjections (Class<?> clazz)
    {
        if (clazz==null)
            return null;
        List<Injection> list = (List<Injection>)fieldInjectionsMap.get(clazz);
        if (list == null)
            list = Collections.emptyList();
        return list;
    }
    
    public List<Injection>  getMethodInjections (Class<?> clazz)
    {
        if (clazz==null)
            return null;
        List<Injection> list = (List<Injection>)methodInjectionsMap.get(clazz);
        if (list == null)
            list = Collections.emptyList();
        return list;
    }
 
    public List<Injection>  getInjections (Class<?> clazz)
    {
        if (clazz==null)
            return null;
        
        List<Injection>  results = new ArrayList<Injection> ();
        results.addAll(getFieldInjections(clazz));
        results.addAll(getMethodInjections(clazz));
        return results;
    }
    
    public Injection getInjection (Class<?> clazz, Member member)
    {
        if (clazz==null)
            return null;
        if (member==null)
            return null;
        Map<Class<?>, List<Injection>> map = null;
        if (member instanceof Field)
            map = fieldInjectionsMap;
        else if (member instanceof Method)
            map = methodInjectionsMap;
        
        if (map==null)
            return null;

        List<Injection>  injections = (List<Injection>)map.get(clazz);
        Injection injection = null;
        for (int i=0;injections!=null && i<injections.size() && injection==null;i++)
        {
            Injection candidate = (Injection)injections.get(i);
            if (candidate.getTarget().equals(member))
                injection = candidate;
        }
        return injection;
    }
    
    
    public void inject (Object injectable)
    throws Exception
    {
        if (injectable==null)
            return;
        
        //Get all injections pertinent to the Object by
        //looking at it's class hierarchy
        Class<?> clazz = injectable.getClass();
        
        
        if (injectable instanceof PojoWrapper)
        { 
            injectable = ((PojoWrapper)injectable).getPojo();
            clazz = injectable.getClass();
        }
      
        
        while (clazz != null)
        {
            //Do field injections
            List<Injection> injections = getFieldInjections(clazz);
            for (Injection i : injections)
                i.inject(injectable);

            //Do method injections
            injections = getMethodInjections(clazz);
            for (Injection i : injections)
                i.inject(injectable);
            
            clazz = clazz.getSuperclass();
        }
    }
}

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortbay.log.Log;

/**
 * InjectionCollection
 *
 *
 */
public class InjectionCollection
{
    private HashMap fieldInjectionsMap = new HashMap();
    private HashMap methodInjectionsMap = new HashMap();
    
    
    public void add (Injection injection)
    {
        if ((injection==null) || (injection.getTarget()==null) || (injection.getClassName()==null)) 
            return;
        
        Log.debug("Adding injection for class="+injection.getClassName()+ " on a "+injection.getTarget().getClass());
        Map injectionsMap = null;
        if (injection.getTarget() instanceof Field)
            injectionsMap = fieldInjectionsMap;
        if (injection.getTarget() instanceof Method)
            injectionsMap = methodInjectionsMap;
        
        List injections = (List)injectionsMap.get(injection.getClassName());
        if (injections==null)
        {
            injections = new ArrayList();
            injectionsMap.put(injection.getClassName(), injections);
        }
        
        injections.add(injection);
    }

    
 
    
    public void inject (Object injectable)
    throws Exception
    {
        if (injectable==null)
            return;

        //TODO: ensure that overridden methods and fields are correctly visible
        
        ArrayList injections = new ArrayList();
        injections.addAll(getMatchingInjections (injectable.getClass().getDeclaredFields(), 
                (List)fieldInjectionsMap.get(injectable.getClass().getName())));

        injections.addAll(getMatchingInjections (injectable.getClass().getDeclaredMethods(), 
                (List)methodInjectionsMap.get(injectable.getClass().getName())));
        
        Iterator itor = injections.iterator();
        while (itor.hasNext())
            ((Injection)itor.next()).inject(injectable);
    }
    
    
    
    public List getMatchingInjections (Member[] members, List injections)
    {
        if ((injections==null) || (members==null))
            return Collections.EMPTY_LIST;
        
        List results = new ArrayList();

        Iterator itor = injections.iterator();
        while (itor.hasNext())
        {
            Injection injection = (Injection)itor.next();
            //find the member in the injectable matching
            boolean found = false;
            for (int i=0;i<members.length && !found; i++)
            {
                if (members[i].equals(injection.getTarget()))
                {
                    found = true;
                    results.add(injection);
                }
            }
        }
        return results;
    }
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mortbay.jetty.util.log.Log;


/**
 * LifeCycleCallbackCollection
 *
 *
 */
public class LifeCycleCallbackCollection
{
    private HashMap postConstructCallbacksMap = new HashMap();
    private HashMap preDestroyCallbacksMap = new HashMap();
    
    
 
    
    
    /**
     * Add a Callback to the list of callbacks.
     * 
     * @param callback
     */
    public void add (LifeCycleCallback callback)
    {
        if ((callback==null) || (callback.getTargetClass()==null) || (callback.getTarget()==null))
            return;

        if (Log.isDebugEnabled())
            Log.debug("Adding callback for class="+callback.getTargetClass()+ " on "+callback.getTarget());
        Map map = null;
        if (callback instanceof PreDestroyCallback)
            map = preDestroyCallbacksMap;
        if (callback instanceof PostConstructCallback)
            map = postConstructCallbacksMap;

        if (map == null)
            throw new IllegalArgumentException ("Unsupported lifecycle callback type: "+callback);

     
        List callbacks = (List)map.get(callback.getTargetClass());
        if (callbacks==null)
        {
            callbacks = new ArrayList();
            map.put(callback.getTargetClass(), callbacks);
        }
       
        //don't add another callback for exactly the same method
        if (!callbacks.contains(callback))
            callbacks.add(callback);
    }

    public List getPreDestroyCallbacks (Object o)
    {
        if (o == null)
            return null;
        
        Class clazz = o.getClass();
        
        if (o instanceof PojoWrapper)
        {
            o = ((PojoWrapper)o).getPojo();
            clazz = o.getClass();
        }
    
        return (List)preDestroyCallbacksMap.get(clazz);
    }
    
    public List getPostConstructCallbacks (Object o)
    {
        if (o == null)
            return null;
        
        Class clazz = o.getClass();
        
        if (o instanceof PojoWrapper)
        {
            o = ((PojoWrapper)o).getPojo();
            clazz = o.getClass();
        }
    
        return (List)postConstructCallbacksMap.get(clazz);
    }
    
    /**
     * Call the method, if one exists, that is annotated with PostConstruct
     * or with &lt;post-construct&gt; in web.xml
     * @param o the object on which to attempt the callback
     * @throws Exception
     */
    public void callPostConstructCallback (Object o)
    throws Exception
    {
        if (o == null)
            return;
        
        Class clazz = o.getClass();
        
        if (o instanceof PojoWrapper)
        {
            o = ((PojoWrapper)o).getPojo();
            clazz = o.getClass();
        }
        List callbacks = (List)postConstructCallbacksMap.get(clazz);
        
        if (callbacks == null)
            return;
        
        for (int i=0;i<callbacks.size();i++)
            ((LifeCycleCallback)callbacks.get(i)).callback(o);
    }
    
    
    /**
     * Call the method, if one exists, that is annotated with PreDestroy
     * or with &lt;pre-destroy&gt; in web.xml
     * @param o the object on which to attempt the callback
     */
    public void callPreDestroyCallback (Object o)
    throws Exception
    {
        if (o == null)
            return;
        
        Class clazz = o.getClass();
        
        if (o instanceof PojoWrapper)
        {
            o = ((PojoWrapper)o).getPojo();
            clazz = o.getClass();
        }
    
        List callbacks = (List)preDestroyCallbacksMap.get(clazz);
        if (callbacks == null)
            return;
        
        for (int i=0;i<callbacks.size();i++)
            ((LifeCycleCallback)callbacks.get(i)).callback(o);
    }
}

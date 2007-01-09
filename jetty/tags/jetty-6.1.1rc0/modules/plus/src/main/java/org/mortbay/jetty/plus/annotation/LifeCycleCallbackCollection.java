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

import java.util.HashMap;
import java.util.Map;

import org.mortbay.log.Log;


/**
 * LifeCycleCallbackCollection
 *
 *
 */
public class LifeCycleCallbackCollection
{
    private HashMap postConstructCallbacksMap = new HashMap();
    private HashMap preDestroyCallbacksMap = new HashMap();
    
    
    public void add (LifeCycleCallback callback)
    {
        if ((callback==null) || (callback.getClassName()==null) || (callback.getTarget()==null))
            return;

        Log.debug("Adding callback for class="+callback.getClassName()+ " of type "+callback.getClass().getName());
        Map map = null;
        if (callback instanceof PreDestroyCallback)
            map = preDestroyCallbacksMap;
        if (callback instanceof PostConstructCallback)
            map = postConstructCallbacksMap;

        if (map == null)
            throw new IllegalArgumentException ("Unsupported lifecycle callback type: "+callback);

        //check that there is not already a callback of the same type
        //already registered for this class
        LifeCycleCallback existing = (LifeCycleCallback)map.get(callback.getClassName());
        if ( existing != null)
            throw new IllegalStateException("Callback already registered for class "+callback.getClassName() + " on method "+existing.getTarget().getName());
        map.put(callback.getClassName(), callback);
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
        
        LifeCycleCallback callback = (LifeCycleCallback)postConstructCallbacksMap.get(o.getClass().getName());
        
        Log.debug("Got callback="+callback+" for class: "+o.getClass().getName());
        if (callback == null)
            return;
        
        callback.callback(o);
    }
    
    
    /**
     * Call the method, if one exists, that is annotated with PreDestroy
     * or with &lt;pre-destroy&gt; in web.xml
     * @param o the object on which to attempt the callback
     */
    public void callPreDestroyCallback (Object o)
    {
        if (o == null)
            return;

        PreDestroyCallback callback = (PreDestroyCallback)preDestroyCallbacksMap.get(o.getClass().getName());
        Log.debug("Got callback="+callback+" for class: "+o.getClass().getName());
        if (callback == null)
            return;
        callback.callback(o);
    }
}

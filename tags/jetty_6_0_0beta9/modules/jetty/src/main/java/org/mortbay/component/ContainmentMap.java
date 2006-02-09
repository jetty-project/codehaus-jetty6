//========================================================================
//Copyright 2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.component;

import java.util.Map;
import java.util.WeakHashMap;

import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

public class ContainmentMap implements Container.Listener
{
    static ContainmentMap instance;
    Map _map = new WeakHashMap();
    
    public void add(Container.Event event)
    {
        Map p = (Map)_map.get(event.getParent());
        if (p==null)
        {
            p = new WeakHashMap();
            _map.put(event.getParent(), p);
        }
        
        Object r = p.get(event.getRelationship());
        r=LazyList.add(r,event.getChild());
        p.put(event.getRelationship(),r);

    }
    
    public void remove(Container.Event event)
    {
        Map p = (Map)_map.get(event.getParent());
        if (p!=null)
        {
            Object r = p.get(event.getRelationship());
            r=LazyList.remove(r,event.getChild());
            if (r!=null)
                p.put(event.getRelationship(),r);
            else
                p.remove(event.getRelationship());
            if (p.size()==0)
                _map.remove(event.getParent());
        }
    }

    public static void dump()
    {
        Log.debug(instance._map.toString());
    }
}

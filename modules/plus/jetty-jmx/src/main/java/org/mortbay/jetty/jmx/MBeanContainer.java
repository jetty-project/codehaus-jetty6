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

package org.mortbay.jetty.jmx;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.loading.PrivateMLet;

import org.mortbay.jetty.util.MultiMap;
import org.mortbay.jetty.util.TypeUtil;
import org.mortbay.jetty.util.component.AbstractLifeCycle;
import org.mortbay.jetty.util.component.Container;
import org.mortbay.jetty.util.component.Container.Relationship;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.log.Logger;

public class MBeanContainer extends AbstractLifeCycle implements Container.Listener
{
    private final MBeanServer _server;
    private final WeakHashMap _beans = new WeakHashMap();
    private final HashMap _unique = new HashMap();
    private String _domain = null;
    private MultiMap _relations = new MultiMap();
    

    public synchronized ObjectName findMBean(Object object)
    {
        ObjectName bean = (ObjectName)_beans.get(object);
        return bean==null?null:bean; 
    }

    public synchronized Object findBean(ObjectName oname)
    {
        for (Iterator iter = _beans.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iter.next();
            ObjectName bean = (ObjectName)entry.getValue();
            if (bean.equals(oname))
                return entry.getKey();
        }
        return null;
    }

    public MBeanContainer(MBeanServer server)
    {
        this._server = server;
    }
    
    public MBeanServer getMBeanServer()
    {
        return _server;
    }
    
    public void setDomain (String domain)
    {
        _domain =domain;
    }
    
    public String getDomain()
    {
        return _domain;
    }
    
    public void doStart()
    {
    }

    public synchronized void add(Relationship relationship)
    {   
        ObjectName parent=(ObjectName)_beans.get(relationship.getParent());
        if (parent==null)
        {
            addBean(relationship.getParent());
            parent=(ObjectName)_beans.get(relationship.getParent());
        }
        
        ObjectName child=(ObjectName)_beans.get(relationship.getChild());
        if (child==null)
        {
            addBean(relationship.getChild());
            child=(ObjectName)_beans.get(relationship.getChild());
        }
        
        if (parent!=null && child!=null)
            _relations.add(parent,relationship);
        
        
    }

    public synchronized void remove(Relationship relationship)
    {
        ObjectName parent=(ObjectName)_beans.get(relationship.getParent());
        ObjectName child=(ObjectName)_beans.get(relationship.getChild());
        if (parent!=null && child!=null)
            _relations.removeValue(parent,relationship);
    }

    public synchronized void removeBean(Object obj)
    {
        ObjectName bean=(ObjectName)_beans.remove(obj);

        if (bean!=null)
        {
            List r=_relations.getValues(bean);
            if (r!=null && r.size()>0)
            {
                Log.debug("Unregister {}", r);
                Iterator iter = new ArrayList(r).iterator();
                while (iter.hasNext())
                {
                    Relationship rel = (Relationship)iter.next();
                    rel.getContainer().update(rel.getParent(),rel.getChild(),null,rel.getRelationship(),true);
                }
            }
            
            try
            {
                _server.unregisterMBean(bean);
                Log.debug("Unregistered {}", bean);
            }
            catch (javax.management.InstanceNotFoundException e)
            {
                Log.ignore(e);
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }
    }
    
    public synchronized void addBean(Object obj)
    {
        try
        {
            if (obj == null || _beans.containsKey(obj))
                return;
            
            Object mbean = ObjectMBean.mbeanFor(obj);
            if (mbean == null)
                return;

            ObjectName oname = null;
            if (mbean instanceof ObjectMBean)
            {
                ((ObjectMBean) mbean).setMBeanContainer(this);
                oname = ((ObjectMBean)mbean).getObjectName();
            }
            
            //no override mbean object name, so make a generic one
            if (oname == null)
            {
                String type=obj.getClass().getName().toLowerCase();
                int dot = type.lastIndexOf('.');
                if (dot >= 0)
                    type = type.substring(dot + 1);
                
                String name=null;
                if (mbean instanceof ObjectMBean)
                {
                    name = ((ObjectMBean)mbean).getObjectNameBasis();
                    if (name!=null)
                    {
                        name=name.replace('\\','/');
                        if (name.endsWith("/"))
                            name=name.substring(0,name.length()-1);

                        int slash=name.lastIndexOf('/',name.length()-1);
                        if (slash>0)
                            name=name.substring(slash+1);
                        dot=name.lastIndexOf('.');
                        if (dot>0)
                            name=name.substring(0,dot);

                        name=name.replace(':','_').replace('*','_').replace('?','_').replace('=','_').replace(',','_').replace(' ','_');
                    }
                }
                
                String basis=(name!=null&&name.length()>1)?("type="+type+",name="+name):("type="+type);
                
                Integer count = (Integer) _unique.get(basis);
                count = TypeUtil.newInteger(count == null ? 0 : (1 + count.intValue()));
                _unique.put(basis, count);

                //if no explicit domain, create one
                String domain = _domain;
                if (domain==null)
                    domain = obj.getClass().getPackage().getName();

                oname = ObjectName.getInstance(domain+":"+basis+",id="+count);
            }
            
            ObjectInstance oinstance = _server.registerMBean(mbean, oname);
            Log.debug("Registered {}" , oinstance.getObjectName());
            _beans.put(obj, oinstance.getObjectName());

        }
        catch (Exception e)
        {
            Log.warn("bean: "+obj,e);
        }
    }
    
    public void doStop()
    {
        while (_beans.size()>0)
            removeBean(_beans.keySet().iterator().next());
    }
    
    private class ShutdownHook extends Thread
    {
        private final ObjectName mletName;
        private final ObjectName adaptorName;
        private final ObjectName processorName;

        public ShutdownHook(ObjectName mletName, ObjectName adaptorName, ObjectName processorName)
        {
            this.mletName = mletName;
            this.adaptorName = adaptorName;
            this.processorName = processorName;
        }

        public void run()
        {
            halt();
            unregister(processorName);
            unregister(adaptorName);
            unregister(mletName);
        }

        private void halt()
        {
            try
            {
                _server.invoke(adaptorName, "stop", null, null);
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }

        private void unregister(ObjectName objectName)
        {
            try
            {
                _server.unregisterMBean(objectName);
                Log.debug("Unregistered " + objectName);
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }
    }
    
}

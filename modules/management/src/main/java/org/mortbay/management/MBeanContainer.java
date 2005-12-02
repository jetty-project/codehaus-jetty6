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

package org.mortbay.management;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.mortbay.component.Container;
import org.mortbay.component.Container.Event;
import org.mortbay.log.Log;
import org.mortbay.util.TypeUtil;

public class MBeanContainer implements Container.Listener
{
    // TODO try to think of a way to do this without statics
    private static MBeanContainer __instance;
    
    private MBeanServer _server;
    private String _domain;
    private WeakHashMap _beans = new WeakHashMap();
    private HashMap _unique = new HashMap();
    
    public static synchronized void enable(String domain, int port)
    {
        if (__instance==null)
            Container.addEventListener(__instance=new MBeanContainer(domain,port));
    }
    
    public static ObjectName findMBean(Object object)
    {
        if (__instance==null)
            return null;
        ObjectName oname=(ObjectName)__instance._beans.get(object);
        if (oname==null)
            return null;
        return oname;
    }

    public static Object findBean(ObjectName oname)
    {
        if (__instance==null)
            return null;
        for(Iterator iter=__instance._beans.entrySet().iterator();iter.hasNext();)
        {
            Map.Entry entry=(Map.Entry)iter.next();
            if (entry.getValue().equals(oname))
                return entry.getKey();
        }
        return null;
    }


    private MBeanContainer(String domain, int port)
    {
        _domain=domain;
        
        List servers = MBeanServerFactory.findMBeanServer(null);
        if (servers!=null)
        {
            for (Iterator iter=servers.iterator();iter.hasNext();)   
            {
                MBeanServer server = (MBeanServer)iter.next();
                if (_server==null || server.getDefaultDomain()!=null && server.getDefaultDomain().equals(_domain))
                    _server=server;
            }
        }
        if (_server==null)
            _server=MBeanServerFactory.createMBeanServer(_domain);
        
        if (port>0)
        {
            try
            {
                Log.warn("HttpAdaptor for mx4j is not secure");
                ObjectName name = new ObjectName("mx4j:name=HttpAdaptor");
                _server.createMBean("mx4j.tools.adaptor.http.HttpAdaptor", name, null);
                _server.setAttribute(name, new Attribute("Port", new Integer(port)));
                _server.setAttribute(name, new Attribute("Host", "localhost"));
                
                ObjectName processorName = new ObjectName("mx4j:name=XSLTProcessor");
                _server.createMBean("mx4j.tools.adaptor.http.XSLTProcessor", processorName, null);
                _server.setAttribute(name, new Attribute("ProcessorName", processorName));
                
                _server.invoke(name, "start", null, null);
            }
            catch(Exception e)
            {
                Log.warn(e);
            }
        }
    }

    
    public void add(Event event)
    {
        addBean(event.getParent());
        addBean(event.getChild());
        
    }

    public void remove(Event event)
    {
        ObjectName oname=findMBean(event.getChild());
        if (oname!=null)
        {
            try
            {
                _server.unregisterMBean(oname);
                Log.info("unregistered "+oname);
            }
            catch(Exception e)
            {
                Log.warn(e);
            }
        }
    }
    
    private synchronized void addBean(Object bean)
    {
        try
        {
            if (bean==null || _beans.containsKey(bean))
                return;
            Object mbean=ObjectMBean.mbeanFor(bean);
            if (mbean==null)
                return;
            
            String name=bean.getClass().getName().toLowerCase();
            int dot = name.lastIndexOf('.');
            if (dot>=0)
                name=name.substring(dot+1);
            Integer count=(Integer)_unique.get(name);
            count=TypeUtil.newInteger(count==null?0:(1+count.intValue()));
            _unique.put(name, count);
            
            ObjectName oname=new ObjectName(_domain+":"+name+"="+count);
            
            ObjectInstance oinstance = _server.registerMBean(mbean, oname);
            Log.info("registered "+oinstance.getObjectName());
            _beans.put(bean,oinstance.getObjectName());
            
        }
        catch(Exception e)
        {
            Log.warn(e);
        }
    }

}

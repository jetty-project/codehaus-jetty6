package org.jboss.jetty;

import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.mortbay.component.Container.Listener;
import org.mortbay.component.Container.Relationship;
import org.mortbay.management.ObjectMBean;
import org.mortbay.util.TypeUtil;

public class JBossMBeanContainer implements Listener {

	private MBeanServer _server;
	private static String _domain = "org.jboss";
    private final HashMap _unique = new HashMap();

	public JBossMBeanContainer(MBeanServer server)
	{
		this._server = server;
	}
	
	public void addBean(Object bean) 
	{
        try {
			Object mbean = ObjectMBean.mbeanFor(bean);
			String name = bean.getClass().getName().toLowerCase();
			int dot = name.lastIndexOf('.');
			if (dot >= 0)
			    name = name.substring(dot + 1);
			Integer count = (Integer) _unique.get(name);
			count = TypeUtil.newInteger(count == null ? 0 : (1 + count.intValue()));
			_unique.put(name, count);

			ObjectName oname = ObjectName.getInstance(_domain+":type="+name+",id="+count);
			_server.registerMBean(mbean, oname);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void removeBean(Object bean) 
	{
		
	}

	public void add(Relationship relationship) 
	{
	
	}

	public void remove(Relationship relationship) 
	{
	
	}

}

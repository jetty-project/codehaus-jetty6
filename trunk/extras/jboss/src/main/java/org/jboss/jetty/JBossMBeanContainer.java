package org.jboss.jetty;

import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.mortbay.component.Container.Listener;
import org.mortbay.component.Container.Relationship;
import org.mortbay.management.MBeanContainer;
import org.mortbay.management.ObjectMBean;
import org.mortbay.util.TypeUtil;

public class JBossMBeanContainer extends MBeanContainer
{
	public JBossMBeanContainer(MBeanServer server)
	{
		super(server);
        setDomain("org.jboss.web");
	}
	
	public void start ()
    {
     //do nothing - the superclass does initialization of stuff we don't want 
    }

}

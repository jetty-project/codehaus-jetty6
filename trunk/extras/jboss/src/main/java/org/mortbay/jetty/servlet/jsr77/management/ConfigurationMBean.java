//========================================================================
//$Id$
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.servlet.jsr77.management;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.management.ObjectMBean;
import org.mortbay.log.Log;


/**
 * 
 * ConfigurationMBean
 *
 * @author janb
 * @version $Revision$ $Date$
 *
 */
public class ConfigurationMBean extends ObjectMBean implements MBeanRegistration
{
    private Map jsr77MBeanMap = new HashMap();
    private Configuration _config;
    private MBeanServer _mbeanServer = null;
    private ObjectName _objectName = null;
    private static HashMap _objectId = new HashMap();
    public ConfigurationMBean(Object obj) 
    {
    	super(obj);
    	_config = (Configuration)obj;
	}
    
    public ObjectName preRegister(MBeanServer server, ObjectName objName) throws Exception 
    {
        if (_objectName==null)
        {
            try
            {
                //jboss.jetty domain (temporarily hard coded)
                String objectName = "jboss.jetty:";

                String className = _config.getClass().getName();
                className=className.substring(0,className.length()-5);
                objectName+=className+"=";
                
                try
                {
                    while(true)
                    {
                        Integer id=(Integer)_objectId.get(objectName);
                        if (id==null)
                            id=new Integer(0);
                        objName=new ObjectName(objectName+id);
                        id=new Integer(id.intValue()+1);
                        _objectId.put(objectName,id);
                        
                        // If no server, this must be unique
                        if (server==null)
                            break;
                        
                        // Otherwise let's check it is unique
                        // if not found then it is unique
                        if (!server.isRegistered(objName))
                            break;
                    }
                }
                catch(Exception e)
                {
                    Log.warn(e);
                }
            }
            catch(Exception e)
            {
            	Log.warn(e);
            }
        }

        if(Log.isDebugEnabled())Log.debug("preRegister "+_objectName+" -> "+objName);
        
        _objectName=objName;

        return _objectName;
    }
    
    public void preDeregister() throws Exception 
    {
    	
    }
    
    /**postRegister
     * Register the other jsr77 mbeans
     * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
     */
    
    public void postRegister(Boolean ok)
    {
        try
        {
            defineJsr77MBeans();
        }
        catch (Exception e)
        {
            Log.warn(e);
        }
    }
    
    /**postDeregister
     * Deregister also all of the jsr77 mbeans we were responsible for 
     * registering.
     * @see javax.management.MBeanRegistration#postDeregister()
     */
    public void postDeregister ()
    {
        Iterator itor = jsr77MBeanMap.entrySet().iterator();
        while (itor.hasNext())
        {
            try
            {
                Map.Entry entry = (Map.Entry)itor.next();
                
                _mbeanServer.unregisterMBean((ObjectName)entry.getValue());
            }
            catch (Exception e)
            {
                Log.warn (e);
            }
        }
    }
    

    
    /**defineJsr77MBeans
     * Make and register an mbean for each of the jsr77 servlet stats
     * @throws Exception
     */
    private void defineJsr77MBeans ()
    throws Exception
    {
        WebAppContext context = _config.getWebAppContext();       
        ServletHolder[] servlets =  context.getServletHandler().getServlets();
        for (int i=0; null!=servlets && i<servlets.length;i++)
        {
            Jsr77ServletHolderMBean mbean = new Jsr77ServletHolderMBean(servlets[i]);
//            mbean.setBaseObjectName(getBaseObjectName().toString());
            ObjectName oname = _mbeanServer.registerMBean(mbean,null).getObjectName();
            jsr77MBeanMap.put (servlets[i].getName(), oname);
        }
    }
    
 
}

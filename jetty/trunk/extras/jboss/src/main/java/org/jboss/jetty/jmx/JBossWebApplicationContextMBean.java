// ========================================================================
// $Id: JBossWebApplicationContextMBean.java,v 1.5 2004/09/27 14:33:43 janb Exp $
// Copyright 1999-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.jboss.jetty.jmx;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

import org.jboss.jetty.JBossWebApplicationContext;
import org.mortbay.jetty.webapp.management.WebAppContextMBean;;

/* ------------------------------------------------------------ */
/** JBoss Web Application MBean.
 *
 * @version $Revision: 1.5 $
 * @author Jules Gosnell (jules)
 */
public class
  JBossWebApplicationContextMBean
  extends WebAppContextMBean
{
    
    private JBossWebApplicationContext _jbwac = null;
    
  /* ------------------------------------------------------------ */
  /** Constructor.
   * @exception MBeanException
   * @exception InstanceNotFoundException
   */
  public
    JBossWebApplicationContextMBean(Object managedObject)
    throws MBeanException
    {
	  super(managedObject);
      _jbwac=(JBossWebApplicationContext)managedObject;
      _jbwac.setMBeanPeer(this);
    }

  /* ------------------------------------------------------------ */
//  protected void
//    defineManagedResource()
//    {
//      super.defineManagedResource();
//
//      //         defineAttribute("displayName",false);
//      //         defineAttribute("defaultsDescriptor",true);
//      //         defineAttribute("deploymentDescriptor",false);
//      //         defineAttribute("WAR",true);
//      //         defineAttribute("extractWAR",true);
//    }

//   public void setManagedResource(Object proxyObject, String type)
//     throws MBeanException,
//     RuntimeOperationsException,
//     InstanceNotFoundException,
//     InvalidTargetObjectTypeException
//     {
//       super.setManagedResource(proxyObject, type);
//       _jbwac=(JBossWebApplicationContext)proxyObject;
//       _jbwac.setMBeanPeer(this);
//     }

//   public ObjectName[]
//     getComponentMBeans(Object[] components, Map map)
//     {
//       return super.getComponentMBeans(components, map);
//     }
//   
//   public Set getJsr77ObjectNames ()
//   throws Exception
//   {
//       String webModuleName = _jbwac.getContextPath();
//       ObjectName jettyJsr77Query = new ObjectName (JettyMBean.JBOSS_DOMAIN+":J2EEServer=null,J2EEApplication=null,J2EEWebModule="+(webModuleName.length()==0?"/":webModuleName)+",j2EEType=Servlet,*");
//       
//       return getMBeanServer().queryNames (jettyJsr77Query, null);
//   }
}

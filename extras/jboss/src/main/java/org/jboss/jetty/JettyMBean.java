/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: JettyMBean.java,v 1.3 2004/04/05 20:12:24 gregwilkins Exp $

package org.jboss.jetty;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

import org.mortbay.jetty.management.ServerMBean;

public class JettyMBean
  extends ServerMBean
{
  public static final String JBOSS_DOMAIN = "jboss.jetty";

  static
  {
    //setDefaultDomain (JBOSS_DOMAIN);
  }

  public JettyMBean(Jetty jetty)
    throws MBeanException, InstanceNotFoundException
  {
    super(jetty);
  }
}

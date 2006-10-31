/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: JettyService.java,v 1.8 2004/10/07 22:51:17 janb Exp $

//------------------------------------------------------------------------------

package org.jboss.jetty;

//------------------------------------------------------------------------------

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.web.AbstractWebContainer;
import org.jboss.web.AbstractWebDeployer;
import org.jboss.web.WebApplication;
import org.mortbay.util.MultiException;
import org.w3c.dom.Element;

//------------------------------------------------------------------------------
/**
 * A service to launch jetty from JMX.
 *
 *
 * @jmx:mbean name="jboss.jetty:service=Jetty"
 *            extends="org.jboss.web.AbstractWebContainerMBean"
 *
 * @todo convert to use JMXDoclet...
 *
 * @author <a href="mailto:jules@mortbay.com">Julian Gosnell</a>
 * @version $Revision: 1.8 $
 */

public class JettyService
  extends AbstractWebContainer
  implements JettyServiceMBean, MBeanRegistration
{
  public static final String NAME = "Jetty";

  // delegate to Jetty
  protected MBeanServer _server       = null;
  protected Jetty       _jetty        = null;
  protected JettyMBean  _jettyBean    = null;
  protected Element     _jettyConfig  = null;

  public
    JettyService()
  {
    super();
    _jetty = new Jetty(this);
  }

  //----------------------------------------------------------------------------
  // utils...
  //----------------------------------------------------------------------------

  /**
   * mex should implement a better printStackTrace...
   */
  protected void
    log(MultiException e)
  {
    log.error("multiple exceptions...");
    Iterator iter = e.getThrowables().iterator();
    while (iter.hasNext())
      log.error("exception", (Exception)iter.next());
  }

  
  
  //----------------------------------------------------------------------------

  public ObjectName
    preRegister(MBeanServer server, ObjectName name)
    throws Exception
  {
    super.preRegister(server,name);
    name = getObjectName(server, name);
    _server = server;

    return name;
  }

  public void
    postRegister(Boolean done)
  {
    super.postRegister(done);

    // this must be done before config is read otherwise configs
    // defined therein will not receive MBean peers. Since it must now
    // be done before JMX has a chance to configure us, I'm removing
    // the option not to have these MBeans built...
    try
    {
      _jettyBean    = new JettyMBean(_jetty);
      _server.registerMBean(_jettyBean, null);
    }
    catch (Throwable e)
    {
      log.error("could not create MBean peers", e);
    }

    log.debug("created MBean peers");
  }

  //----------------------------------------------------------------------------
  // 'name' interface
  //----------------------------------------------------------------------------

  public String
    getName()
  {
    return NAME;
  }

  //----------------------------------------------------------------------------
  // 'service' interface
  //----------------------------------------------------------------------------

  public void
    createService()
    throws Exception
  {
    super.createService();
    if( _jettyConfig != null )
      _jetty.setConfigurationElement(_jettyConfig);
  }

  public void
    startService()
    throws Exception
  {
    super.startService();

    try
    {
      _jetty.start();
    }
    catch (MultiException e)
    {
      log(e);
    }
    catch (Exception e)
    {
      log.error("could not start Jetty", e);
    }
  }

  public void
    stopService() throws Exception
  {
    super.stopService();

    try
    {
      _jetty.stop();
    }
    catch (Exception e)
    {
      log.error("could not stop Jetty", e);
    }
  }

  public void
    destroyService()
    throws Exception
  {
    super.destroyService();

    // this is not symmetrical - these things are created in
    // postRegister, not createService()...
    try
    {
      //_jetty.destroy();
      _jetty.stop();
      _jetty    =null;
      _jettyBean=null;
    }
    catch (Throwable e)
    {
      log.error("could not destroy Jetty", e);
    }

  }

  //----------------------------------------------------------------------------
  // 'deploy' interface
  //----------------------------------------------------------------------------

  public void
    performDeploy(WebApplication webApp, String warUrl, WebDescriptorParser parser)
    throws DeploymentException
  {
    _jetty.deploy(webApp, warUrl, parser);
  }

  public void
    performUndeploy(String warUrl)
    throws DeploymentException
  {
    _jetty.undeploy(warUrl);
  }

  //----------------------------------------------------------------------------
  // Jetty properties - delegated directly...
  //----------------------------------------------------------------------------

  //----------------------------------------
  // class loader delegation policy property
  //----------------------------------------

  /**
   * @param loaderCompliance if true, Jetty delegates class loading
   *to parent class loader first, false implies servlet spec 2.3 compliance
   * @jmx:managed-attribute
   */
  public void
    setJava2ClassLoadingCompliance(boolean loaderCompliance)
  {
    if (log.isDebugEnabled())
      log.debug("set Java2ClassLoadingCompliance to "+ loaderCompliance);
   super.setJava2ClassLoadingCompliance(loaderCompliance);
    _jetty.setJava2ClassLoadingCompliance(loaderCompliance);
  }

  /**
   * @return true if Java2 style class loading delegation, false if
   *servlet2.3 spec compliance
   * @jmx:managed-attribute
   */
  public boolean
    getJava2ClassLoadingCompliance()
  {
    return _jetty.getJava2ClassLoadingCompliance();
  }

  //----------------------------------------------------------------------------

  /**
   * @jmx:managed-attribute
   */
  public boolean
    getUnpackWars()
  {
    return _jetty.getUnpackWars();
  }

  /**
   * @jmx:managed-attribute
   */
  public void
    setUnpackWars(boolean unpackWars)
  {
    if (log.isDebugEnabled())
      log.debug("set UnpackWars to "+unpackWars);

    _jetty.setUnpackWars(unpackWars);
  }

  //----------------------------------------------------------------------------

  /**
   * @jmx:managed-attribute
   */
  public boolean
    getSupportJSR77()
  {
    return _jetty.getSupportJSR77();
  }

  /**
   * @jmx:managed-attribute
   */
  public void
    setSupportJSR77(boolean supportJSR77)
  {
    if (log.isDebugEnabled())
      log.debug("set SupportJSR77 to "+supportJSR77);

    _jetty.setSupportJSR77(supportJSR77);
  }

  //----------------------------------------------------------------------------

  /**
   * @jmx:managed-attribute
   */
  public String
    getWebDefaultResource()
  {
    return _jetty.getWebDefaultResource();
  }

  /**
   * @jmx:managed-attribute
   */
  public void
    setWebDefaultResource(String webDefaultResource)
  {
    if (log.isDebugEnabled())
      log.debug("set WebDefaultResource to "+webDefaultResource);

    _jetty.setWebDefaultResource(webDefaultResource);
  }

  //----------------------------------------------------------------------------
  /** Get the extended Jetty configuration XML fragment
   * @jmx:managed-attribute
   * @return Jetty XML fragment embedded in jboss-service.xml
   */

  public Element
    getConfig()
  {
    //return _jetty.getConfigurationElement();
     return _jettyConfig;
  }

  /** Configure Jetty
   * @param configElement XML fragment from jboss-service.xml
   * @jmx:managed-attribute
   */
  public void
    setConfig(Element configElement)
  {
    log.debug("Saving Configuration to xml fragment");
    this._jettyConfig = configElement;
    // Don't apply this now as this element can be set more than during init
    //_jetty.setConfigurationElement (configElement);
  }

  //----------------------------------------------------------------------------

  /**
   * @jmx:managed-attribute
   */
  public String
    getSubjectAttributeName()
  {
    return _jetty.getSubjectAttributeName();
  }

  /**
   * @jmx:managed-attribute
   */
  public void
    setSubjectAttributeName(String subjectAttributeName)
  {
    if (log.isDebugEnabled())
      log.debug("set SubjectAttributeName to "+subjectAttributeName);

    _jetty.setSubjectAttributeName(subjectAttributeName);
  }

  //----------------------------------------------------------------------------
  // Hackery to integrate with recent changes to AbstractWebContainer...

  public class
    JettyDeployer
    extends AbstractWebDeployer
  {
    protected DeploymentInfo _deploymentInfo;

    public
      JettyDeployer(DeploymentInfo di)
    {
      _deploymentInfo=di;
    }

    public void
      init(Object containerConfig)
      throws Exception
    {
      //TODO - do better job of passing in config from AbstractWebContainer
      setLenientEjbLink(JettyService.this.getLenientEjbLink ());     
      setServer(JettyService.this._server); 
      
      //Avoiding a code branch for the sake of a single method, 
      //use reflection to set the defaultSecurityDomain if the
      //version of JBoss we have bee compiled against supports it.
      
      try
      {
          Method method = AbstractWebContainer.class.getDeclaredMethod("getDefaultSecurityDomain", new Class[0]);
          String defaultSecurityDomain = (String)method.invoke (JettyService.this, new Object[0]);
          method = AbstractWebDeployer.class.getDeclaredMethod("setDefaultSecurityDomain", new Class[]{String.class});
          method.invoke(this, new Object[]{defaultSecurityDomain});
      }
      catch (Exception e)
      {
          //ignore - it means the currently executing version of jboss does not support this method
          log.info("Getter/setter for DefaultSecurityDomain not available in this version of JBoss");
      }
    }


    public void
    performDeploy(WebApplication webApp, String warUrl, WebDescriptorParser parser)
    throws DeploymentException
    {
      JettyService.this.performDeploy(webApp, warUrl, parser);
    }

    public void
      performUndeploy(String warUrl, WebApplication wa)
      throws DeploymentException
    {
      JettyService.this.performUndeploy(warUrl);
    }
  }

  public AbstractWebDeployer
    getDeployer(DeploymentInfo di)
    throws Exception
  {
    JettyDeployer deployer = new JettyDeployer(di);
    deployer.init(null);
    return deployer;
  }
}

/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: JettyService.java,v 1.8 2004/10/07 22:51:17 janb Exp $

//------------------------------------------------------------------------------

package org.jboss.jetty;


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
 * JettyService
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

public class JettyService extends AbstractWebContainer implements
        JettyServiceMBean, MBeanRegistration
{
    public static final String NAME = "Jetty";

    protected MBeanServer _server = null;
    protected Jetty _jetty = null;
    protected Element _jettyConfig = null;
    protected boolean _supportJSR77;
    protected String _webDefaultResource;
    
    
    /**
     * ConfigurationData
     *
     * Holds info that the jboss API sets on the
     * AbstractWebContainer but is needed by the
     * AbstractWebDeployer.
     */
    public static class ConfigurationData
    {
        private boolean _loaderCompliance;
        private boolean _unpackWars;
        private boolean _lenientEjbLink;
        private String _subjectAttributeName;
        private String _defaultSecurityDomain;
        private boolean _acceptNonWarDirs;
        private String _webDefaultResource;
        private boolean _supportJSR77;
        
        /**
         * @return the _webDefaultResource
         */
        public String getWebDefaultResource()
        {
            return _webDefaultResource;
        }

        /**
         * @param defaultResource the _webDefaultResource to set
         */
        public void setWebDefaultResource(String defaultResource)
        {
            _webDefaultResource = defaultResource;
        }

        public void setJava2ClassLoadingCompliance(boolean loaderCompliance)
        {
           _loaderCompliance=loaderCompliance;
        }

        public boolean getJava2ClassLoadingCompliance()
        {
            return _loaderCompliance;
        }
       
        public boolean getUnpackWars()
        {
            return _unpackWars;
        }

        public void setUnpackWars(boolean unpackWars)
        {
            _unpackWars=unpackWars;
        }
        
        public void setLenientEjbLink (boolean lenientEjbLink)
        {
            _lenientEjbLink=lenientEjbLink;
        }
        
        public boolean getLenientEjbLink()
        {
            return _lenientEjbLink;
        }

        public String getSubjectAttributeName()
        {
            return _subjectAttributeName;
        }

        /**
         * @jmx:managed-attribute
         */
        public void setSubjectAttributeName(String subjectAttributeName)
        {
            _subjectAttributeName=subjectAttributeName;
        }

        /**
         * @return the _defaultSecurityDomain
         */
        public String getDefaultSecurityDomain()
        {
            return _defaultSecurityDomain;
        }

        /**
         * @param securityDomain the _defaultSecurityDomain to set
         */
        public void setDefaultSecurityDomain(String securityDomain)
        {
            _defaultSecurityDomain = securityDomain;
        }

        /**
         * @return the _acceptNonWarDirs
         */
        public boolean getAcceptNonWarDirs()
        {
            return _acceptNonWarDirs;
        }

        /**
         * @param nonWarDirs the _acceptNonWarDirs to set
         */
        public void setAcceptNonWarDirs(boolean nonWarDirs)
        {
            _acceptNonWarDirs = nonWarDirs;
        }

        /**
         * @return the _supportJSR77
         */
        public boolean getSupportJSR77()
        {
            return _supportJSR77;
        }

        /**
         * @param _supportjsr77 the _supportJSR77 to set
         */
        public void setSupportJSR77(boolean _supportjsr77)
        {
            _supportJSR77 = _supportjsr77;
        }
    }

    
    
    /** 
     * Constructor
     */
    public JettyService()
    {
        super();
        _jetty = new Jetty(this);
    }



    /**
     * Log a jetty MultiException 
     */
    protected void log(MultiException e)
    {
        log.error("multiple exceptions...");
        Iterator iter = e.getThrowables().iterator();
        while (iter.hasNext())
            log.error("exception", (Exception) iter.next());
    }



    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception
    {
        super.preRegister(server, name);
        name = getObjectName(server, name);
        _server = server;

        return name;
    }

    public void postRegister(Boolean done)
    {
        super.postRegister(done);

        // this must be done before config is read otherwise configs
        // defined therein will not receive MBean peers. Since it must now
        // be done before JMX has a chance to configure us, I'm removing
        // the option not to have these MBeans built...
        try
        {
            log.info("Setting up mbeanlistener on Jetty");
            _jetty.getContainer().addEventListener(new JBossMBeanContainer(_server));
        }
        catch (Throwable e)
        {
            StackTraceElement[] ste = e.getStackTrace();
            for (int i = 0; i < ste.length; i++)
            {
                log.error(ste[i].toString());
            }
            log.error("could not create MBean peers", e);
        }

        log.debug("created MBean peers");
    }

    // ----------------------------------------------------------------------------
    // 'name' interface
    // ----------------------------------------------------------------------------

    public String getName()
    {
        return NAME;
    }

    // ----------------------------------------------------------------------------
    // 'service' interface
    // ----------------------------------------------------------------------------

    public void createService() throws Exception
    {
        super.createService();
        if (_jettyConfig != null) _jetty.setConfigurationElement(_jettyConfig);
    }

    public void startService() throws Exception
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

    public void stopService() throws Exception
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

    public void destroyService() throws Exception
    {
        super.destroyService();

        // this is not symmetrical - these things are created in
        // postRegister, not createService()...
        try
        {
            // _jetty.destroy();
            _jetty.stop();
            _jetty = null;
        }
        catch (Throwable e)
        {
            log.error("could not destroy Jetty", e);
        }

    }



    /**
     * Old deployment method from AbstractWebContainer.
     * 
     * TODO remove this?
     * @param webApp
     * @param warUrl
     * @param parser
     * @throws DeploymentException
     */
    public void performDeploy(WebApplication webApp, String warUrl,
            WebDescriptorParser parser) throws DeploymentException
    {
        //TODO: backwards compatibility
        throw new UnsupportedOperationException("Backward compatibility not implemented");
    }

    /**
     * Old undeploy method from AbstractWebContainer.
     * 
     * TODO remove?
     * @param warUrl
     * @throws DeploymentException
     */
    public void performUndeploy(String warUrl) throws DeploymentException
    {
        //TODO backwards compatibility
        throw new UnsupportedOperationException("Backward compatibility not implemented");
    }

    /**
     * @jmx:managed-attribute
     */
    public boolean getSupportJSR77()
    {
        return _supportJSR77;
    }

    /**
     * @jmx:managed-attribute
     */
    public void setSupportJSR77(boolean supportJSR77)
    {
        if (log.isDebugEnabled())
            log.debug("set SupportJSR77 to " + supportJSR77);

        _supportJSR77=supportJSR77;
    }

    /**
     * Get the custom webdefault.xml file.
     * @jmx:managed-attribute
     */
    public String getWebDefaultResource()
    {
        return _webDefaultResource;
    }

    /**
     * Set a custom webdefault.xml file.
     * @jmx:managed-attribute
     */
    public void setWebDefaultResource(String webDefaultResource)
    {
        if (log.isDebugEnabled())
            log.debug("set WebDefaultResource to " + webDefaultResource);

        _webDefaultResource=webDefaultResource;
    }


    /**
     * Get the extended Jetty configuration XML fragment
     * 
     * @jmx:managed-attribute
     * @return Jetty XML fragment embedded in jboss-service.xml
     */

    public Element getConfigurationElement()
    {
        return _jettyConfig;
    }

    /**
     * Configure Jetty
     * 
     * @param configElement XML fragment from jboss-service.xml
     * @jmx:managed-attribute
     */
    public void setConfigurationElement(Element configElement)
    {
        log.debug("Saving Configuration to xml fragment");
        this._jettyConfig = configElement;
        // Don't apply this now as this element can be set more than during init
        // _jetty.setConfigurationElement (configElement);
    }

    
    /** 
     * @see org.jboss.web.AbstractWebContainer#getDeployer(org.jboss.deployment.DeploymentInfo)
     */
    public AbstractWebDeployer getDeployer(DeploymentInfo di) throws Exception
    {
        JettyDeployer deployer = new JettyDeployer(di);
        deployer.setHandlerCollection(_jetty.getContextHandlerCollection());
        ConfigurationData configData = new ConfigurationData();
        configData.setAcceptNonWarDirs(getAcceptNonWarDirs());
        configData.setJava2ClassLoadingCompliance(getJava2ClassLoadingCompliance());
        configData.setLenientEjbLink(getLenientEjbLink());
        configData.setSubjectAttributeName(getSubjectAttributeName());
        configData.setSupportJSR77(getSupportJSR77());
        configData.setUnpackWars(getUnpackWars());
        configData.setWebDefaultResource(getWebDefaultResource());
        //defaultSecurityDomain was added at a certain point, so do it
        //this way so we have backwards compatibility
        try
        {
            Method method = AbstractWebContainer.class.getDeclaredMethod(
                    "getDefaultSecurityDomain", new Class[0]);
            String defaultSecurityDomain = (String) method.invoke(
                    JettyService.this, new Object[0]);
            configData.setDefaultSecurityDomain(getDefaultSecurityDomain());
        }
        catch (Exception e)
        {
            // ignore - it means the currently executing version of jboss
            // does not support this method
            log.info("Getter/setter for DefaultSecurityDomain not available in this version of JBoss");
        }
        deployer.setServer(_server);
        deployer.init(configData);
        return deployer;
    }

}

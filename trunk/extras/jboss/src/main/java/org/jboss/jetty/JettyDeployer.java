//========================================================================
//$Id$
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
// This is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as
// published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this software; if not, write to the Free
// Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// 02110-1301 USA, or see the FSF site: http://www.fsf.org.
//========================================================================

package org.jboss.jetty;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.web.AbstractWebDeployer;
import org.jboss.web.WebApplication;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
import org.mortbay.j2ee.session.Manager;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.util.MultiException;

/**
 * JettyDeployer
 *
 * Implementation of the jboss AbstractWebDeployer
 * for deploying webapps to jetty.
 */
public class JettyDeployer extends AbstractWebDeployer
{
    protected static final Logger _log = Logger.getLogger("org.jboss.jetty");

    protected DeploymentInfo _deploymentInfo;
    protected HandlerCollection _contexts;
    protected JettyService.ConfigurationData  _configData;
    protected SessionManager _distributableSessionManagerPrototype;
    protected boolean _forceDistributable = false;

    /**
     * use Hashtable because is is synchronised
     */
    Hashtable _deployed = new Hashtable();

    public JettyDeployer(DeploymentInfo di)
    {
        _deploymentInfo = di;
    }

    public void setHandlerCollection (HandlerCollection contexts)
    {
        _contexts = contexts;
    }

    public HandlerCollection getHandlerCollection ()
    {
        return _contexts;
    }

    public void init(Object containerConfig) throws Exception
    {
        _configData = (JettyService.ConfigurationData)containerConfig;          
        setLenientEjbLink(_configData.getLenientEjbLink());
        setDefaultSecurityDomain(_configData.getDefaultSecurityDomain());
        setJava2ClassLoadingCompliance(_configData.getJava2ClassLoadingCompliance());
        setUnpackWars(_configData.getUnpackWars());
    }

    public void performDeploy(WebApplication webApp, String warUrl, WebDescriptorParser parser) throws DeploymentException
    {
    	log.debug("webapp="+webApp);
        String contextPath = webApp.getMetaData().getContextRoot();
        try
        {
            webApp.setURL(new URL(warUrl));
        	log.debug("set url webapp="+webApp);

            // check whether the context already exists... - a bit hacky,
            // could be nicer...
            boolean found = false;
            Handler[] installed=_contexts.getChildHandlersByClass(ContextHandler.class);
            for (int i=0; (i<installed.length && !found); i++)
            {
                ContextHandler c=(ContextHandler)installed[i];                   
                found = contextPath.equals(c.getContextPath());

            }
            if (found)
                _log.warn("A WebApplication is already deployed in context '" + contextPath
                        + "' - proceed at your own risk.");

        	log.debug("before deploy webapp="+webApp);

            // deploy the WebApp
            JBossWebAppContext app = new JBossWebAppContext(parser, webApp, warUrl);
            app.setContextPath(contextPath);
            if (_configData.getSupportJSR77())
                app.setConfigurationClasses (new String[]{"org.mortbay.jetty.webapp.WebInfConfiguration","org.jboss.jetty.JBossWebXmlConfiguration", "org.mortbay.jetty.webapp.JettyWebXmlConfiguration",  "org.mortbay.jetty.webapp.TagLibConfiguration"/*,"org.mortbay.jetty.servlet.jsr77.Configuration"*/});
            else
                app.setConfigurationClasses (new String[]{ "org.mortbay.jetty.webapp.WebInfConfiguration","org.jboss.jetty.JBossWebXmlConfiguration", "org.mortbay.jetty.webapp.JettyWebXmlConfiguration",  "org.mortbay.jetty.webapp.TagLibConfiguration"});
          
            Manager manager = (Manager) getDistributableSessionManagerPrototype();
            if (manager != null)
            {
                app.setDistributableSessionManager((Manager) manager.clone());
                if (getForceDistributable())
                    app.setDistributable(true);
            }

            // configure whether the context is to flatten the classes in
            // the WAR or not
            app.setExtractWAR(getUnpackWars());
            app.setParentLoaderPriority(getJava2ClassLoadingCompliance());

            // if a different webdefault.xml file has been provided, use it
            // if it exists
            if (_configData.getWebDefaultResource() != null)
            {
                try
                {
                  URL url = getClass().getClassLoader().getResource(_configData.getWebDefaultResource());
                  String fixedUrl = (fixURL(url.toString()));
                  app.setDefaultsDescriptor(fixedUrl);
                  if (_log.isDebugEnabled())
                      _log.debug("webdefault specification is: " + _configData.getWebDefaultResource());
                }
                catch (Exception e)
                {
                    _log.error("Could not find resource: " + _configData.getWebDefaultResource()+" using default", e);
                }
            }

            Iterator hosts = webApp.getMetaData().getVirtualHosts();
            List hostList = new ArrayList();

            while(hosts.hasNext())
                hostList.add((String)hosts.next());

            app.setVirtualHosts((String[])hostList.toArray(new String[hostList.size()]));

            // Add the webapp
            _contexts.addHandler(app);

            // keep track of deployed contexts for undeployment
            _deployed.put(warUrl, app);

            try
            {
                // finally start the app
                app.start();
                
                //get all the jsr77 mbeans
                
                //first check that there is an mbean for the webapp itself
                ObjectName webAppMBean = new ObjectName(_configData.getMBeanDomain() + ":J2EEServer=none,J2EEApplication=none,J2EEWebModule="+app.getUniqueName());
                if (server.isRegistered(webAppMBean))
                    _deploymentInfo.deployedObject = webAppMBean;
                else
                    throw new IllegalStateException("No mbean registered for webapp at "+app.getUniqueName());
                
                //now get all the mbeans that represent servlets and set them on the 
                //deployment info so they will be found by the jsr77 management system
                ObjectName servletQuery = new ObjectName
                (_configData.getMBeanDomain() + ":J2EEServer=none,J2EEApplication=none,J2EEWebModule="+app.getUniqueName()+ ",j2eeType=Servlet,*");
                Iterator iterator = server.queryNames(servletQuery, null).iterator();
                while (iterator.hasNext())
                {
                    _deploymentInfo.mbeans.add((ObjectName) iterator.next());
                }

                _log.info("successfully deployed " + warUrl + " to " + contextPath);
            }
            catch (MultiException me)
            {
                _log.warn("problem deploying " + warUrl + " to " + contextPath);
                for (int i = 0; i < me.size(); i++)
                {
                    Exception e = (Exception)me.getThrowable(i);
                    _log.warn(e, e);
                }
            }

        }
        catch (DeploymentException e)
        {
            _log.error("Undeploying on start due to error", e);
            performUndeploy(warUrl, webApp);
            throw e;
        }
        catch (Exception e)
        {
            _log.error("Undeploying on start due to error", e);
            performUndeploy(warUrl, webApp);
            throw new DeploymentException(e);
        }
    }


    public void performUndeploy(String warUrl, WebApplication wa) throws DeploymentException
    {
        // find the WebApp Context in the repository
        JBossWebAppContext app = (JBossWebAppContext) _deployed.get(warUrl);

        if (app == null)
        {
            _log.warn("app (" + warUrl + ") not currently deployed");
        }
        else
        {
            try
            {
                app.stop();
                _contexts.removeHandler(app);
                app = null;
                _log.info("Successfully undeployed " + warUrl);
            }
            catch (Exception e)
            {
                throw new DeploymentException(e);
            }
            finally
            {
                _deployed.remove(warUrl);
            }
        }

    }
    
    // work around broken JarURLConnection caching...
    static String fixURL(String url)
    {
        // Get the separator of the JAR URL and the file reference
        int index = url.indexOf('!');
        if (index >= 0)
        {
            index = url.lastIndexOf('/', index);
        }
        else
        {
            index = url.lastIndexOf('/');
        }
        // Now add a "./" before the JAR file to add a different path
        if (index >= 0)
        {
            return url.substring(0, index) + "/." + url.substring(index);
        }
        else
        {
            // Now forward slash found then there is severe problem with
            // the URL but here we just ignore it
            return url;
        }
    }
    
    public void setDistributableSessionManagerPrototype(SessionManager manager)
    {
        _distributableSessionManagerPrototype = manager;
    }

    public SessionManager getDistributableSessionManagerPrototype()
    {
        return _distributableSessionManagerPrototype;
    }
    
    public boolean getForceDistributable()
    {
        return _forceDistributable;
    }

    public void setForceDistributable(boolean distributable)
    {
        _forceDistributable = distributable;
    }


}

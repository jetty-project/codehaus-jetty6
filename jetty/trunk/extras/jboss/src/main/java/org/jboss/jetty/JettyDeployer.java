//========================================================================
//$Id$
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.jboss.jetty;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.web.AbstractWebDeployer;
import org.jboss.web.WebApplication;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.util.MultiException;

/**
 * JettyDeployer
 *
 *
 */
public class JettyDeployer extends AbstractWebDeployer
{
    protected static final Logger _log = Logger.getLogger("org.jboss.jetty");

    protected DeploymentInfo _deploymentInfo;
    protected HandlerCollection _contexts;
    protected JettyService.ConfigurationData  _configData;
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
        String contextPath = webApp.getMetaData().getContextRoot();
        try
        {
            webApp.setURL(new URL(warUrl));

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


            // deploy the WebApp
            WebAppContext app = new JBossWebApplicationContext(parser, webApp, warUrl);
            app.setContextPath(contextPath);
            if (_configData.getSupportJSR77())
                app.setConfigurationClasses (new String[]{"org.mortbay.jetty.webapp.WebInfConfiguration","org.jboss.jetty.JBossWebXmlConfiguration", "org.mortbay.jetty.webapp.JettyWebXmlConfiguration",  "org.mortbay.jetty.webapp.TagLibConfiguration"/*,"org.mortbay.jetty.servlet.jsr77.Configuration"*/});
            else
                app.setConfigurationClasses (new String[]{ "org.mortbay.jetty.webapp.WebInfConfiguration","org.jboss.jetty.JBossWebXmlConfiguration", "org.mortbay.jetty.webapp.JettyWebXmlConfiguration",  "org.mortbay.jetty.webapp.TagLibConfiguration"});


//          SessionManager manager = getDistributableSessionManagerPrototype();
//          if (manager != null)
//          {
//          app.setDistributableSessionManager((SessionManager) manager.clone());
//          if (getForceDistributable()) app.setDistributable(true);
//          }

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
        JBossWebApplicationContext app = (JBossWebApplicationContext) _deployed.get(warUrl);

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
}

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

package org.mortbay.jetty.deployer;


import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;

/**
 * Deployer
 *
 * Deployers can deploy webapps to running Servers.
 * 
 * @see HotFileDeployer
 */
public class Deployer
{    
    private Server _server;
    
    
    /**
     * @return the server
     */
    public Server getServer()
    {
        return _server;
    }


    /**
     * @param server the server to set
     */
    public void setServer(Server server)
    {
        _server = server;
    }
    
    /** 
     * Perform a deployment of the webapp to the server instance
     * @see org.mortbay.jetty.deployer.Deployer#deploy(org.mortbay.jetty.webapp.WebAppContext)
     */
    public void deploy(WebAppContext webapp) throws Exception
    {
        if (_server == null)
            throw new IllegalStateException ("No server set for deployer");
        if (webapp != null)
        {
            ContextHandlerCollection contexts = (ContextHandlerCollection)_server.getChildHandlerByClass(ContextHandlerCollection.class);
            contexts.addHandler(webapp);
        }
        Log.info("Webapp "+webapp+" deployed.");
    }
    
    
    /** Perform a deployment of a webapp to a server
     * @param location the dir or war of the webapp
     * @param contextPath the context path for the webapp
     * @param defaults the webdefaults.xml
     * @param configurationClasses configurations to be applied
     * @param extract if true, explode a war
     * @param java2CompliantClassLoader use parent-first a la j2se or child-first a la servlet spec
     * @throws Exception
     */
    public void deploy (String location, String contextPath, String defaults, String[] configurationClasses, boolean extract, boolean java2CompliantClassLoader)
    throws Exception
    {
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);
        webapp.setConfigurationClasses(configurationClasses);
        if (defaults!=null)
            webapp.setDefaultsDescriptor(defaults);
        webapp.setExtractWAR(extract);
        webapp.setWar(location);
        webapp.setParentLoaderPriority(java2CompliantClassLoader);
        deploy (webapp);
    }
    

    /** 
     * Perform an undeployment of the webapp.
     * 
     * @see org.mortbay.jetty.deployer.Deployer#undeploy(org.mortbay.jetty.webapp.WebAppContext)
     */
    public void undeploy(WebAppContext webapp) throws Exception
    {
        if (_server == null)
            throw new IllegalStateException ("No server set for deployer");
        if (webapp == null)
            return;

        webapp.stop();
        ContextHandlerCollection contexts = (ContextHandlerCollection)_server.getChildHandlerByClass(ContextHandlerCollection.class);
        contexts.removeHandler(webapp);

        Log.info ("Webapp "+webapp+" undeployed");
    }
}

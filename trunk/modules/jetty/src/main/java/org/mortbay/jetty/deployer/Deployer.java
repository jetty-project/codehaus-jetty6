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


import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;

/**
 * AbstractDeployer
 *
 * Base class for all types of webapp deployer.
 * 
 * @see HotFileDeployer
 */
public class Deployer extends AbstractLifeCycle
{
    /** 
     * Perform a deployment of the webapp to the server instance
     * @see org.mortbay.jetty.deployer.Deployer#deploy(org.mortbay.jetty.webapp.WebAppContext)
     */
    public void deploy(Server server, WebAppContext webapp) throws Exception
    {
        if (!isStarted())
            throw new IllegalStateException ("Deployer is not started");
        if (server == null)
            throw new IllegalStateException ("No server set for deployer");
        if (webapp != null)
        {
            ContextHandlerCollection contexts = (ContextHandlerCollection)server.getChildHandlerByClass(ContextHandlerCollection.class);
            contexts.addHandler(webapp);
        }
        
        Log.info("Webapp at "+webapp.getContextPath()+" deployed.");
    }

    /** 
     * Perform an undeployment of the webapp.
     * 
     * @see org.mortbay.jetty.deployer.Deployer#undeploy(org.mortbay.jetty.webapp.WebAppContext)
     */
    public void undeploy(Server server, WebAppContext webapp) throws Exception
    {
        if (!isStarted())
            throw new IllegalStateException ("Deployer is not started");       
        if (server == null)
            throw new IllegalStateException ("No server set for deployer");
        if (webapp != null)
        {
            webapp.stop();
            ContextHandlerCollection contexts = (ContextHandlerCollection)server.getChildHandlerByClass(ContextHandlerCollection.class);
            contexts.removeHandler(webapp);
        }
        
        Log.info ("Webapp at "+webapp.getContextPath()+" undeployed");
    }
}

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

import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Deployer
 *
 * Type for a deployer that is able to deploy or undeploy webapps
 * to a jetty server instance.
 */
public interface Deployer extends LifeCycle
{
    
    public void deploy (Server server, WebAppContext webapp) throws Exception;
    
    public void undeploy (Server server, WebAppContext webapp) throws Exception;

}

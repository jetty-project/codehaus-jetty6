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

package org.mortbay.jetty.plugin;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.plugin.util.JettyPluginServer;
import org.mortbay.jetty.plugin.util.JettyPluginWebApplication;
import org.mortbay.jetty.plugin.util.PluginLog;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.start.Main;

/**
 * Jetty6PluginServer
 * 
 * Jetty6 version of a wrapper for the Server class.
 * 
 */
public class Jetty6PluginServer implements JettyPluginServer
{
    public static int DEFAULT_PORT = 8080;
    
    public static long DEFAULT_MAX_IDLE_TIME = 30000L;
    
    private Server server;

    private Connector[] connectors;
    
    private UserRealm[] realms;
    
    
    
    /**
     * @see org.mortbay.jetty.plugin.util.JettyPluginServer#create()
     */
    public Jetty6PluginServer()
    {
        this.server = new Server();
        this.server.setStopAtShutdown(true);
    }

    /**
     * @see org.mortbay.jetty.plugin.util.JettyPluginServer#setConnectors(org.mortbay.jetty.plugin.util.JettyPluginConnector[])
     */
    public void setConnectors(Object[] connectors)
    {
        this.connectors = new Connector[connectors.length];
        for (int i=0; i<connectors.length;i++)
        {
            this.connectors[i] = (Connector)connectors[i];
            PluginLog.getLog().debug("Setting Connector: "+this.connectors[i].getClass().getName()+" on port "+this.connectors[i].getPort());
        }
        this.server.setConnectors(this.connectors);
    }

    /**
     *
     * 
     * @see org.mortbay.jetty.plugin.util.JettyPluginServer#getConnectors()
     */
    public Object[] getConnectors()
    {
        return this.connectors;
    }

    /**
     * 
     * 
     * @see org.mortbay.jetty.plugin.JettyPluginServer#setUserRealms(org.mortbay.jetty.plugin.JettyPluginUserRealm[])
     */
    public void setUserRealms(Object[] realms) throws Exception
    {
        if (realms == null)
            this.realms = null;
        else
        {
            this.realms = new UserRealm[realms.length];
            for (int i=0; i<realms.length;i++)
                this.realms[i] = (UserRealm)realms[i];
        }
        
        this.server.setUserRealms(this.realms);
    }

    /**
     * 
     * @see org.mortbay.jetty.plugin.util.JettyPluginServer#getUserRealms()
     */
    public Object[] getUserRealms()
    {
        return this.realms;
    }


    /**
     * @see org.mortbay.jetty.plugin.util.JettyPluginServer#start()
     */
    public void start() throws Exception
    {
        PluginLog.getLog().info("Starting jetty "+this.server.getClass().getPackage().getImplementationVersion()+" ...");
        this.server.start();
    }

    /**
     * @see org.mortbay.jetty.plugin.util.Proxy#getProxiedObject()
     */
    public Object getProxiedObject()
    { 
        return this.server;
    }

    /**
     * @see org.mortbay.jetty.plugin.util.JettyPluginServer#addWebApplication(java.lang.Object)
     */
    public void addWebApplication(JettyPluginWebApplication webapp) throws Exception
    {
        this.server.addHandler ((Handler)webapp.getProxiedObject());
    }

    /* (non-Javadoc)
     * @see org.mortbay.jetty.plugin.JettyPluginServer#createDefaultConnector()
     */
    public Object createDefaultConnector() throws Exception
    {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector = new SelectChannelConnector();
        connector.setPort(DEFAULT_PORT);
        connector.setMaxIdleTime(DEFAULT_MAX_IDLE_TIME);
        
        return connector;
    }

    /**
     * @see org.mortbay.jetty.plugin.util.JettyPluginServer#createWebApplication()
     */
    public JettyPluginWebApplication createWebApplication() throws Exception
    {
        return new Jetty6PluginWebApplication();
    }



    public void join () throws Exception
    {
        this.server.getThreadPool().join();
    }
}

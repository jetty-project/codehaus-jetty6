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


import java.util.Arrays;

import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.mortbay.jetty.plugin.util.JettyPluginServer;
import org.mortbay.jetty.plugin.util.PluginLog;

/**
 * Jetty6PluginServer
 * 
 * Jetty6 version of a wrapper for the Server class.
 * 
 */
public class Jetty6PluginServer implements JettyPluginServer
{
    public static int DEFAULT_PORT = 8080;
    public static int DEFAULT_MAX_IDLE_TIME = 30000;
    private Server server;
    private ContextHandlerCollection contexts; //the list of ContextHandlers
    HandlerCollection handlers; //the list of lists of Handlers
    private RequestLogHandler requestLogHandler; //the request log handler
    private DefaultHandler defaultHandler; //default handler
    
    private RequestLog requestLog; //the particular request log implementation
    
    
    /**
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#create()
     */
    public Jetty6PluginServer()
    {
        this.server = new Server();
        this.server.setStopAtShutdown(true);
        //make sure Jetty does not use URLConnection caches with the plugin
        Resource.setDefaultUseCaches(false);
    }

    /**
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#setConnectorNames(org.mortbay.jetty.plugin.util.JettyPluginConnector[])
     */
    public void setConnectors(Object[] connectors)
    {
        if (connectors==null || connectors.length==0)
            return;
        
        for (int i=0; i<connectors.length;i++)
        {
            Connector connector = (Connector)connectors[i];
            PluginLog.getLog().debug("Setting Connector: "+connector.getClass().getName()+" on port "+connector.getPort());
            this.server.addConnector(connector);
        }
    }

    
  
    /**
     *
     * 
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#getConnectors()
     */
    public Object[] getConnectors()
    {
        return this.server.getConnectors();
    }

    /**
     * 
     * 
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#setLoginServices(java.Object[])
     */
    public void setUserRealms(Object[] realms) throws Exception
    {
        if (realms == null)
            return;
        
        for (Object o : this.server.getBeans(LoginService.class))
            this.server.removeBean(o);
        
         for (int i=0; i<realms.length;i++)
             this.server.addBean(realms[i]);
    }

    /**
     * 
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#getLoginServices()
     */
    public Object[] getUserRealms()
    {
        return this.server.getBeans(LoginService.class).toArray();
    }

    
    public void setRequestLog (Object requestLog)
    {
        this.requestLog = (RequestLog)requestLog;
    }
    
    public Object getRequestLog ()
    {
        return this.requestLog;
    }

    /**
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#start()
     */
    public void start() throws Exception
    {
        PluginLog.getLog().info("Starting jetty "+this.server.getClass().getPackage().getImplementationVersion()+" ...");
        this.server.start();
    }

    /**
     * @see org.eclipse.jetty.server.plugin.Proxy#getProxiedObject()
     */
    public Object getProxiedObject()
    { 
        return this.server;
    }

    /**
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#addWebApplication(java.lang.Object)
     */
    public void addWebApplication(WebAppContext webapp) throws Exception
    {  
        contexts.addHandler (webapp);
    }

    
    /**
     * Set up the handler structure to receive a webapp.
     * Also put in a DefaultHandler so we get a nice page
     * than a 404 if we hit the root and the webapp's
     * context isn't at root.
     * @throws Exception
     */
    public void configureHandlers () throws Exception 
    {
        this.defaultHandler = new DefaultHandler();
        this.requestLogHandler = new RequestLogHandler();
        if (this.requestLog != null)
            this.requestLogHandler.setRequestLog(this.requestLog);
        
        this.contexts = (ContextHandlerCollection)server.getChildHandlerByClass(ContextHandlerCollection.class);
        if (this.contexts==null)
        {   
            this.contexts = new ContextHandlerCollection();
            this.handlers = (HandlerCollection)server.getChildHandlerByClass(HandlerCollection.class);
            if (this.handlers==null)
            {
                this.handlers = new HandlerCollection();               
                this.server.setHandler(handlers);                            
                this.handlers.setHandlers(new Handler[]{this.contexts, this.defaultHandler, this.requestLogHandler});
            }
            else
            {
                this.handlers.addHandler(this.contexts);
            }
        }  
    }
    
    
    
    
    /**
     * @see org.eclipse.jetty.server.plugin.JettyPluginServer#createDefaultConnector()
     */
    public Object createDefaultConnector(String portnum) throws Exception
    {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector = new SelectChannelConnector();
        int port = ((portnum==null||portnum.equals(""))?DEFAULT_PORT:Integer.parseInt(portnum.trim()));
        connector.setPort(port);
        connector.setMaxIdleTime(DEFAULT_MAX_IDLE_TIME);
        
        return connector;
    }
    
 


    public void join () throws Exception
    {
        this.server.getThreadPool().join();
    }

}

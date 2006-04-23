/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mortbay.jetty.xbean;


import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates an instance of Jetty inside an <a href="http://xbean.org/">XBean</a>
 * configuration file
 * 
 * @org.apache.xbean.XBean element="jetty" rootElement="true" description="Creates an
 *                  embedded Jetty web server with optional web application
 *                  context"
 * 
 * @version $Revision$
 */
public class JettyFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

    private Server server;
    private Handler[] handlers = {};
    private Connector[] connectors = {};
    
    public Object getObject() throws Exception {
        return getServer();
    }

    public Class getObjectType() {
        return Server.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        Server server = getServer();
        Log.info("Starting Jetty Web Server");
        for (int i = 0; i < connectors.length; i++) {
            Connector connector = connectors[i];
            // TODO do we need this?
            connector.setServer(server);
            Log.info("Using Jetty Connector: " + connector);
        }
        server.setConnectors(connectors);

        for (int i = 0; i < handlers.length; i++) {
            Handler handler = handlers[i];
            Log.info("Using Jetty Handler: " + handler);
        }

        HandlerCollection contexts = (HandlerCollection)server.getChildHandlerByClass(ContextHandlerCollection.class);
        if (contexts==null)
            contexts = (HandlerCollection)server.getChildHandlerByClass(HandlerCollection.class);
        if (contexts==null)
        {
            contexts=new ContextHandlerCollection();
            server.setHandler(contexts);
        }
        contexts.setHandlers(handlers);
        server.start();
    }

    public void destroy() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public Server getServer() throws Exception {
        if (server == null) {
            server = createServer();
        }
        return server;
    }

    /**
     * Sets the server instance to use
     */
    public void setServer(Server server) {
        this.server = server;
    }

    public Connector[] getConnectors() {
        return connectors;
    }

    /**
     * Sets the connectors used to listen for requests
     */
    public void setConnectors(Connector[] connectors) {
        this.connectors = connectors;
    }

    public Handler[] getHandlers() {
        return handlers;
    }

    /**
     * Sets the handlers of content such as web application contexts
     */
    public void setHandlers(Handler[] handlers) {
        this.handlers = handlers;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected Server createServer() throws Exception {
        Server server = new Server();
        return server;
    }

}
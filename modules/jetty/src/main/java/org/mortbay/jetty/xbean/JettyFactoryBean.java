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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
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

    private static final Log log = LogFactory.getLog(JettyFactoryBean.class);

    private int port = 8080;
    private String webAppDirectory;
    private String webAppContext = "/";

    private Server server;

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
        if (port >= 0) {
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(port);
            connector.setServer(server);
            server.setConnectors(new Connector[] { connector });
        }

        String description = "";
        if (webAppDirectory != null) {
            WebAppContext context = new WebAppContext();
            context.setResourceBase(webAppDirectory);
            context.setContextPath(webAppContext);
            context.setServer(server);
            server.setHandlers(new Handler[] { context });
            description = " using webAppDirectory: " + webAppDirectory;
        }

        log.info("Starting Jetty Web Server at: http://localhost:" + port + "/" + description);
        server.start();
    }

    public void destroy() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public int getPort() {
        return port;
    }

    /**
     * Sets the port that the web server will listen on
     */
    public void setPort(int port) {
        this.port = port;
    }

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

    public String getWebAppContext() {
        return webAppContext;
    }

    /**
     * Sets the web application context. Defaults to /
     */
    public void setWebAppContext(String webAppContext) {
        this.webAppContext = webAppContext;
    }

    public String getWebAppDirectory() {
        return webAppDirectory;
    }

    /**
     * Sets the web application directory
     */
    public void setWebAppDirectory(String webAppDirectory) {
        this.webAppDirectory = webAppDirectory;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected Server createServer() throws Exception {
        Server server = new Server();
        return server;
    }

}
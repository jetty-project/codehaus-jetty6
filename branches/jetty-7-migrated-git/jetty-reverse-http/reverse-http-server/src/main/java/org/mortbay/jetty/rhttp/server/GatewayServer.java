/*
 * Copyright 2009-2009 Webtide LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mortbay.jetty.rhttp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;
import org.mortbay.jetty.rhttp.client.RHTTPResponse;

/**
 * <p>The gateway server is a server component that acts as intermediary between
 * <em>external clients</em> which perform requests for resources, and the
 * <em>resource providers</em>.</p>
 * <p>The particularity of the gateway server is that the resource providers
 * connect to the gateway using a comet protocol. <br />
 * The comet procotol functionality is implemented by a gateway client. <br />
 * This is quite different from a normal proxy server where it is the proxy that
 * connects to the resource providers.</p>
 * <p>Schematically, this is how the gateway server works:</p>
 * <pre>
 * External Client       Gateway Server         Gateway Client         Resource Provider
 *                              |                      |
 *                              | &lt;-- comet req. 1 --- |
 *        | --- ext. req. 1 --&gt; |                      |
 *        |                     | --- comet res. 1 --&gt; |
 *        |                     | &lt;-- comet req. 2 --- |
 *        |                                            | --- ext. req. 1 --&gt; |
 *                                                                           |
 *        |                                            | &lt;-- ext. res. 1 --- |
 *        |                     | &lt;-- ext.  res. 1 --- |
 *        | &lt;-- ext. res. 1 --- |
 *
 *        | --- ext. req. 2 --&gt; |
 *        |                     | --- comet res. 2 --&gt; |
 *        .                     .                      .
 * </pre>
 * <p>The gateway server is made of two servlets:
 * <ul>
 * <li>the external servlet, that handles external requests</li>
 * <li>the gateway servlet, that handles the communication with the gateway client</li>
 * </ul>
 * </p>
 * <p>External requests are suspended using Jetty continuations until a response for
 * that request arrives from the resource provider, or a
 * {@link #getExternalTimeout() configurable timeout} expires. <br />
 * Comet requests made by the gateway client also expires after a (different)
 * {@link #getGatewayTimeout() configurable timeout}.</p>
 * <p>External requests are packed into {@link RHTTPRequest} objects, converted into an
 * opaque byte array and sent as the body of the comet reponse to the gateway
 * {@link RHTTPClient}.</p>
 * <p>The gateway client uses a notification mechanism to alert listeners interested
 * in external requests that have been forwarded through the gateway. It is up to the
 * listeners to connect to the resource provider however they like.</p>
 * <p>When the gateway client receives a response from the resource provider, it packs
 * the response into a {@link RHTTPResponse} object, converts it into an opaque byte array
 * and sends it as the body of a normal HTTP request to the gateway server.</p>
 * <p>It is possible to connect more than one gateway client to a gateway server; each
 * gateway client is identified by a unique <em>targetId</em>. <br />
 * External requests must specify a targetId that allows the gateway server to forward
 * the requests to the specific gateway client; how the targetId is retrieved from an
 * external request is handled by {@link TargetIdRetriever} implementations.</p>
 *
 * @version $Revision$ $Date$
 */
public class GatewayServer
{
    private final Logger logger = Log.getLogger(getClass().toString());
    private final List<Connector> connectors = new ArrayList<Connector>();
    private volatile Server server;
    private volatile String contextPath = "";
    private volatile String externalServletPath = "/gw";
    private volatile String gatewayServletPath = "/__rhttp";
    private volatile String resourcesPath = System.getProperty("java.io.tmpdir");
    private volatile String resourcesServletPath = "/__r";
    private volatile long gatewayTimeout = 20000;
    private volatile long externalTimeout = 60000;
    private volatile long clientTimeout = 15000;
    private volatile TargetIdRetriever targetIdRetriever = new StandardTargetIdRetriever();
    private volatile Gateway gateway;

    /**
     * Adds a Jetty Connector to this gateway.
     * @param connector the Jetty Connector to add
     */
    public void addConnector(Connector connector)
    {
        connectors.add(connector);
    }

    /**
     * @return the context path at which this gateway server is made available.
     * Default value is the empty string.
     * @see #setContextPath(String)
     */
    public String getContextPath()
    {
        return contextPath;
    }

    /**
     * @param contextPath the context path at which this gateway server is made available.
     * @see #getContextPath()
     */
    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }

    /**
     * @return the servlet path at which the external servlet handles requests.
     * Default value is the empty string.
     * @see #setExternalServletPath(String)
     */
    public String getExternalServletPath()
    {
        return externalServletPath;
    }

    /**
     * @param externalServletPath the servlet path at which the external servlet handles requests
     * @see #getExternalServletPath()
     */
    public void setExternalServletPath(String externalServletPath)
    {
        this.externalServletPath = externalServletPath;
    }

    /**
     * @return the servlet path at which the gateway servlet handles requests.
     * Default value is the string "__rhttp".
     * @see #setGatewayServletPath(String)
     */
    public String getGatewayServletPath()
    {
        return gatewayServletPath;
    }

    /**
     * @param gatewayServletPath the servlet path at which the gateway servlet handles requests.
     * @see #getGatewayServletPath()
     */
    public void setGatewayServletPath(String gatewayServletPath)
    {
        this.gatewayServletPath = gatewayServletPath;
    }

    /**
     * @return the absolute file path from which resources are served
     * Default value is the system's temporary directory
     * @see #setResourcesPath(String)
     */
    public String getResourcesPath()
    {
        return resourcesPath;
    }

    /**
     * @param resourcesPath the absolute file path from which resources are served
     * @see #getResourcesPath()
     */
    public void setResourcesPath(String resourcesPath)
    {
        this.resourcesPath = resourcesPath;
    }

    /**
     * @return the servlet path at which the resources servlet handles requests
     * Default value is the string "__resources".
     * @see #setResourcesServletPath(String)
     */
    public String getResourcesServletPath()
    {
        return resourcesServletPath;
    }

    /**
     * @param resourcesServletPath the servlet path at which the resources servlet handles requests
     * @see #getResourcesServletPath()
     */
    public void setResourcesServletPath(String resourcesServletPath)
    {
        this.resourcesServletPath = resourcesServletPath;
    }

    /**
     * @return the time, in milliseconds, after which a comet request from the gateway client expires.
     * Default value is 30 seconds.
     * @see #setGatewayTimeout(long)
     */
    public long getGatewayTimeout()
    {
        return gatewayTimeout;
    }

    /**
     * @param gatewayTimeout the time, in milliseconds, after which a comet request from the gateway client expires.
     * @see #getGatewayTimeout()
     */
    public void setGatewayTimeout(long gatewayTimeout)
    {
        this.gatewayTimeout = gatewayTimeout;
    }

    /**
     * @return the time, in milliseconds, after which an external request expires.
     * Default value is 1 minute.
     * @see #setExternalTimeout(long)
     */
    public long getExternalTimeout()
    {
        return externalTimeout;
    }

    /**
     * @param externalTimeout the time, in milliseconds, after which an external request expires.
     * @see #getExternalTimeout()
     */
    public void setExternalTimeout(long externalTimeout)
    {
        this.externalTimeout = externalTimeout;
    }

    /**
     * @return the time, in ms, after which a client that does not connect is expired.
     * Default value is 15 seconds
     * @see #setClientTimeout(long)
     */
    public long getClientTimeout()
    {
        return clientTimeout;
    }

    /**
     * @param clientTimeout the time, in ms, after which a client that does not connect is expired.
     */
    public void setClientTimeout(long clientTimeout)
    {
        this.clientTimeout = clientTimeout;
    }

    /**
     * @return the targetId retriever that extracts the targetId from an external request.
     * @see #setTargetIdRetriever(TargetIdRetriever)
     */
    public TargetIdRetriever getTargetIdRetriever()
    {
        return targetIdRetriever;
    }

    /**
     * @param targetIdRetriever the targetId retriever that extracts the targetId from an external request.
     * @see #getTargetIdRetriever()
     */
    public void setTargetIdRetriever(TargetIdRetriever targetIdRetriever)
    {
        this.targetIdRetriever = targetIdRetriever;
    }

    /**
     * @return the gateway object associated with this gateway server.
     */
    protected Gateway getGateway()
    {
        return gateway;
    }

    /**
     * Starts this gateway server, configuring the gateway object, configuring the external servlet and the
     * gateway servlet and starting the embedded Jetty server.
     * @throws Exception if the start fails
     */
    public void start() throws Exception
    {
        server = new Server();
        for (Connector connector : connectors)
            server.addConnector(connector);

        HandlerCollection handlers = new HandlerCollection();
        server.setHandler(handlers);

        ServletContextHandler servlets = new ServletContextHandler(handlers, contextPath, ServletContextHandler.SESSIONS);
        // Temporary workaround until we use a Jetty version that ships the fix for
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=293557
        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("jad", "text/vnd.sun.j2me.app-descriptor");
        servlets.setMimeTypes(mimeTypes);

        // Setup the gateway
        this.gateway = createGateway();

        // Setup external servlet
        ExternalServlet externalServlet = new ExternalServlet(gateway, targetIdRetriever);
        ServletHolder externalServletHolder = new ServletHolder(externalServlet);
        servlets.addServlet(externalServletHolder, externalServletPath + "/*");
        logger.debug("External servlet mapped to {}/*", externalServletPath);

        // Setup gateway servlet
        GatewayServlet gatewayServlet = new GatewayServlet(gateway, clientTimeout);
        ServletHolder gatewayServletHolder = new ServletHolder(gatewayServlet);
        servlets.addServlet(gatewayServletHolder, gatewayServletPath + "/*");
        logger.debug("Gateway servlet mapped to {}/*", gatewayServletPath);

        // Setup resources servlet
        DefaultServlet resourcesServlet = new DefaultServlet();
        ServletHolder resourcesServletHolder = new ServletHolder(resourcesServlet);
        resourcesServletHolder.setInitParameter("dirAllowed", "true");
        resourcesServletHolder.setInitParameter("resourceBase", resourcesPath);
        servlets.addServlet(resourcesServletHolder, resourcesServletPath + "/*");
        logger.debug("Resources servlet mapped to {}/*", resourcesServletPath);
        setupResourcesDirectory(resourcesServletPath);

        server.start();
        logger.info("{} started", getClass().getSimpleName());
    }

    /**
     * Creates and configures a {@link Gateway} object.
     * @return the newly created and configured Gateway object.
     */
    protected Gateway createGateway()
    {
        StandardGateway gateway = new StandardGateway();
        gateway.setGatewayTimeout(getGatewayTimeout());
        gateway.setExternalTimeout(getExternalTimeout());
        return gateway;
    }

    private void setupResourcesDirectory(String path)
    {
        File resourcesFile = new File(resourcesPath, path);
        if (resourcesFile.exists())
        {
            if (!resourcesFile.isDirectory())
                logger.info("Resources path " + resourcesFile.getAbsolutePath() + ", not a directory");
            else if (!resourcesFile.canRead())
                logger.info("Resources path " + resourcesFile.getAbsolutePath() + ", no permission to read");
        }
        else
        {
            boolean created = resourcesFile.mkdirs();
            if (!created)
                logger.info("Resources path " + resourcesFile.getAbsolutePath() + ", could not create it");
            else
                logger.info("Resources path " + resourcesFile.getAbsolutePath() + ", created successfully");
        }
    }

    /**
     * Stops this gateway server
     * @throws Exception if the stop fails
     */
    public void stop() throws Exception
    {
        server.stop();
        server.join();
        logger.info("{} stopped", getClass().getSimpleName());
    }
}

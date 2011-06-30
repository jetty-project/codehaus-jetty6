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

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.JettyClient;
import org.mortbay.jetty.rhttp.client.RHTTPListener;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;
import org.mortbay.jetty.rhttp.client.RHTTPResponse;


/**
 * @version $Revision$ $Date$
 */
public class GatewayEchoServer
{
    private volatile GatewayServer server;
    private volatile Address address;
    private volatile String uri;
    private volatile HttpClient httpClient;
    private volatile RHTTPClient client;

    public void start() throws Exception
    {
        server = new GatewayServer();
        Connector connector = new SelectChannelConnector();
        server.addConnector(connector);
        server.setTargetIdRetriever(new EchoTargetIdRetriever());
        server.start();
        address = new Address("localhost", connector.getLocalPort());
        uri = server.getContextPath() + server.getExternalServletPath();

        httpClient = new HttpClient();
        httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        httpClient.start();

        client = new JettyClient(httpClient, new Address("localhost", connector.getLocalPort()), server.getContextPath() + server.getGatewayServletPath(), "echo");
        client.addListener(new EchoListener(client));
        client.connect();
    }

    public void stop() throws Exception
    {
        client.disconnect();
        httpClient.stop();
        server.stop();
    }

    public Address getAddress()
    {
        return address;
    }

    public String getURI()
    {
        return uri;
    }

    public static class EchoTargetIdRetriever implements TargetIdRetriever
    {
        public String retrieveTargetId(HttpServletRequest httpRequest)
        {
            return "echo";
        }
    }

    private static class EchoListener implements RHTTPListener
    {
        private final RHTTPClient client;

        public EchoListener(RHTTPClient client)
        {
            this.client = client;
        }

        public void onRequest(RHTTPRequest request) throws Exception
        {
            RHTTPResponse response = new RHTTPResponse(request.getId(), 200, "OK", new HashMap<String, String>(), request.getBody());
            client.deliver(response);
        }
    }
}

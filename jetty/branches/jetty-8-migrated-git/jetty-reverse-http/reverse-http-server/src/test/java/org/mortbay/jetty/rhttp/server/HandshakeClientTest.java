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

import junit.framework.TestCase;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.JettyClient;


/**
 * @version $Revision$ $Date$
 */
public class HandshakeClientTest extends TestCase
{
    public void testConnectReturnsImmediately() throws Exception
    {
        GatewayServer server = new GatewayServer();
        SelectChannelConnector connector = new SelectChannelConnector();
        server.addConnector(connector);
        server.setGatewayTimeout(5000L);
        server.start();
        try
        {
            HttpClient httpClient = new HttpClient();
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            httpClient.start();
            try
            {
                RHTTPClient client = new JettyClient(httpClient, new Address("localhost", connector.getLocalPort()), server.getContextPath() + server.getGatewayServletPath(), "test1");
                long start = System.currentTimeMillis();
                client.connect();
                try
                {
                    long end = System.currentTimeMillis();
                    assertTrue(end - start < server.getGatewayTimeout() / 2);
                }
                finally
                {
                    client.disconnect();
                }
            }
            finally
            {
                httpClient.stop();
            }
        }
        finally
        {
            server.stop();
        }
    }
}

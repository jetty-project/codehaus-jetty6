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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.ClientListener;
import org.mortbay.jetty.rhttp.client.JettyClient;


/**
 * @version $Revision$ $Date$
 */
public class ClientTimeoutTest extends TestCase
{
    public void testClientTimeout() throws Exception
    {
        GatewayServer server = new GatewayServer();
        Connector connector = new SelectChannelConnector();
        server.addConnector(connector);
        final long clientTimeout = 2000L;
        server.setClientTimeout(clientTimeout);
        final long gatewayTimeout = 4000L;
        server.setGatewayTimeout(gatewayTimeout);
        server.start();
        try
        {
            Address address = new Address("localhost", connector.getLocalPort());

            HttpClient httpClient = new HttpClient();
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            httpClient.start();
            try
            {
                String targetId = "1";
                final RHTTPClient client = new JettyClient(httpClient, address, server.getContextPath() + server.getGatewayServletPath(), targetId)
                {
                    private final AtomicInteger connects = new AtomicInteger();

                    @Override
                    protected void asyncConnect()
                    {
                        if (connects.incrementAndGet() == 2)
                        {
                            try
                            {
                                // Wait here instead of connecting, so that the client expires on the server
                                Thread.sleep(clientTimeout * 2);
                            }
                            catch (InterruptedException x)
                            {
                                throw new RuntimeException(x);
                            }
                        }
                        super.asyncConnect();
                    }
                };

                final CountDownLatch connectLatch = new CountDownLatch(1);
                client.addClientListener(new ClientListener.Adapter()
                {
                    @Override
                    public void connectRequired()
                    {
                        connectLatch.countDown();
                    }
                });
                client.connect();
                try
                {
                    assertTrue(connectLatch.await(gatewayTimeout + clientTimeout * 3, TimeUnit.MILLISECONDS));
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

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

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.io.ByteArrayBuffer;
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
public class GatewayTimeoutTest extends TestCase
{
    /**
     * Tests a forwarded request that lasts longer than the gateway timeout.
     * The gateway client will perform 2 long polls before the forwarded request's response is returned.
     *
     * @throws Exception in case of test exceptions
     */
    public void testGatewayTimeout() throws Exception
    {
        GatewayServer server = new GatewayServer();
        Connector connector = new SelectChannelConnector();
        server.addConnector(connector);
        final long gatewayTimeout = 5000L;
        server.setGatewayTimeout(gatewayTimeout);
        final long externalTimeout = gatewayTimeout * 2;
        server.setExternalTimeout(externalTimeout);
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
                final RHTTPClient client = new JettyClient(httpClient, address, server.getContextPath() + server.getGatewayServletPath(), targetId);
                final AtomicReference<Exception> exceptionRef = new AtomicReference<Exception>();
                client.addListener(new RHTTPListener()
                {
                    public void onRequest(RHTTPRequest request)
                    {
                        try
                        {
                            // Wait until gateway timeout expires
                            Thread.sleep(gatewayTimeout + 1000L);
                            RHTTPResponse response = new RHTTPResponse(request.getId(), 200, "OK", new HashMap<String, String>(), request.getBody());
                            client.deliver(response);
                        }
                        catch (Exception x)
                        {
                            exceptionRef.set(x);
                        }
                    }
                });
                client.connect();
                try
                {
                    // Make a request to the gateway and check response
                    ContentExchange exchange = new ContentExchange(true);
                    exchange.setMethod(HttpMethods.POST);
                    exchange.setAddress(address);
                    exchange.setURI(server.getContextPath() + server.getExternalServletPath() + "/" + URLEncoder.encode(targetId, "UTF-8"));
                    String requestContent = "body";
                    exchange.setRequestContent(new ByteArrayBuffer(requestContent.getBytes("UTF-8")));
                    httpClient.send(exchange);

                    int status = exchange.waitForDone();
                    assertEquals(HttpExchange.STATUS_COMPLETED, status);
                    assertEquals(HttpServletResponse.SC_OK, exchange.getResponseStatus());
                    assertNull(exceptionRef.get());
                    String responseContent = exchange.getResponseContent();
                    assertEquals(responseContent, requestContent);
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

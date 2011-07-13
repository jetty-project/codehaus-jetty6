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

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.io.ByteArrayBuffer;

/**
 * @version $Revision$ $Date$
 */
public class GatewayEchoTest extends TestCase
{
    /**
     * Tests that the basic functionality of the gateway works,
     * by issuing a request and by replying with the same body.
     *
     * @throws Exception in case of test exceptions
     */
    public void testEcho() throws Exception
    {
        GatewayEchoServer server = new GatewayEchoServer();
        server.start();
        try
        {
            HttpClient httpClient = new HttpClient();
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            httpClient.start();
            try
            {
                // Make a request to the gateway and check response
                ContentExchange exchange = new ContentExchange(true);
                exchange.setMethod(HttpMethods.POST);
                exchange.setAddress(server.getAddress());
                exchange.setURI(server.getURI() + "/");
                String requestBody = "body";
                exchange.setRequestContent(new ByteArrayBuffer(requestBody.getBytes("UTF-8")));
                httpClient.send(exchange);
                int status = exchange.waitForDone();
                assertEquals(HttpExchange.STATUS_COMPLETED, status);
                assertEquals(HttpServletResponse.SC_OK, exchange.getResponseStatus());
                String responseContent = exchange.getResponseContent();
                assertEquals(responseContent, requestBody);
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

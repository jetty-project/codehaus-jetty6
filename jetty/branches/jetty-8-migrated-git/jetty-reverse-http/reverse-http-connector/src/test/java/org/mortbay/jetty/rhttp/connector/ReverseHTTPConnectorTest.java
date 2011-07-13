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

package org.mortbay.jetty.rhttp.connector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.ClientListener;
import org.mortbay.jetty.rhttp.client.RHTTPListener;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;
import org.mortbay.jetty.rhttp.client.RHTTPResponse;

/**
 * @version $Revision$ $Date$
 */
public class ReverseHTTPConnectorTest extends TestCase
{
    public void testGatewayConnectorWithoutRequestBody() throws Exception
    {
        testGatewayConnector(false);
    }

    public void testGatewayConnectorWithRequestBody() throws Exception
    {
        testGatewayConnector(true);
    }

    private void testGatewayConnector(boolean withRequestBody) throws Exception
    {
        Server server = new Server();
        final CountDownLatch handlerLatch = new CountDownLatch(1);
        CountDownLatch clientLatch = new CountDownLatch(1);
        AtomicReference<RHTTPResponse> responseRef = new AtomicReference<RHTTPResponse>();
        ReverseHTTPConnector connector = new ReverseHTTPConnector(new TestClient(clientLatch, responseRef));
        server.addConnector(connector);
        final String method = "POST";
        final String uri = "/test";
        final byte[] requestBody = withRequestBody ? "REQUEST-BODY".getBytes("UTF-8") : new byte[0];
        final int statusCode = HttpServletResponse.SC_CREATED;
        final String headerName = "foo";
        final String headerValue = "bar";
        final byte[] responseBody = "RESPONSE-BODY".getBytes("UTF-8");
        server.setHandler(new AbstractHandler()
        {
            public void handle(String pathInfo, org.eclipse.jetty.server.Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException
            {
                assertEquals(method, httpRequest.getMethod());
                assertEquals(uri, httpRequest.getRequestURI());
                assertEquals(headerValue, httpRequest.getHeader(headerName));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream input = httpRequest.getInputStream();
                int read;
                while ((read = input.read()) >= 0)
                    baos.write(read);
                baos.close();
                assertTrue(Arrays.equals(requestBody, baos.toByteArray()));

                httpResponse.setStatus(statusCode);
                httpResponse.setHeader(headerName, headerValue);
                OutputStream output = httpResponse.getOutputStream();
                output.write(responseBody);
                output.flush();
                request.setHandled(true);
                handlerLatch.countDown();
            }
        });
        server.start();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Host", "localhost");
        headers.put(headerName, headerValue);
        headers.put("Content-Length", String.valueOf(requestBody.length));
        RHTTPRequest request = new RHTTPRequest(1, method, uri, headers, requestBody);
        request = RHTTPRequest.fromRequestBytes(request.getId(), request.getRequestBytes());
        connector.onRequest(request);

        assertTrue(handlerLatch.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(clientLatch.await(1000, TimeUnit.MILLISECONDS));
        RHTTPResponse response = responseRef.get();
        assertEquals(request.getId(), response.getId());
        assertEquals(statusCode, response.getStatusCode());
        assertEquals(headerValue, response.getHeaders().get(headerName));
        assertTrue(Arrays.equals(response.getBody(), responseBody));
    }

    private class TestClient implements RHTTPClient
    {
        private final CountDownLatch latch;
        private final AtomicReference<RHTTPResponse> responseRef;

        private TestClient(CountDownLatch latch, AtomicReference<RHTTPResponse> response)
        {
            this.latch = latch;
            this.responseRef = response;
        }

        public String getTargetId()
        {
            return null;
        }

        public void connect() throws IOException
        {
        }

        public void disconnect() throws IOException
        {
        }

        public void deliver(RHTTPResponse response) throws IOException
        {
            responseRef.set(response);
            latch.countDown();
        }

        public void addListener(RHTTPListener listener)
        {
        }

        public void removeListener(RHTTPListener listener)
        {
        }

        public void addClientListener(ClientListener listener)
        {
        }

        public void removeClientListener(ClientListener listener)
        {
        }

        public String getHost()
        {
            return null;
        }

        public int getPort()
        {
            return 0;
        }

        public String getGatewayURI()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPath()
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
}

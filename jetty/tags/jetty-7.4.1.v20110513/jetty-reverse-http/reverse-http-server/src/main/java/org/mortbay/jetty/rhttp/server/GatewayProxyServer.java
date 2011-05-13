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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.JettyClient;
import org.mortbay.jetty.rhttp.client.RHTTPListener;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;
import org.mortbay.jetty.rhttp.client.RHTTPResponse;

/**
 * <p>This class combines a gateway server and a gateway client to obtain the functionality of a simple proxy server.</p>
 * <p>This gateway proxy server starts on port 8080 and can be set as http proxy in browsers such as Firefox, and used
 * to browse the internet.</p>
 * <p>Its functionality is limited (for example, it only supports http, and not https).</p>
 * @version $Revision$ $Date$
 */
public class GatewayProxyServer
{
    private static final Logger logger = Log.getLogger(GatewayProxyServer.class.toString());

    public static void main(String[] args) throws Exception
    {
        GatewayServer server = new GatewayServer();

        Connector plainConnector = new SelectChannelConnector();
        plainConnector.setPort(8080);
        server.addConnector(plainConnector);

        server.setExternalTimeout(180000);
        server.setGatewayTimeout(20000);
        server.setTargetIdRetriever(new ProxyTargetIdRetriever());
        server.start();

        HttpClient httpClient = new HttpClient();
        httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        httpClient.start();

        RHTTPClient client = new JettyClient(httpClient, new Address("localhost", plainConnector.getPort()), server.getContextPath() + server.getGatewayServletPath(), "proxy");
        client.addListener(new ProxyListener(httpClient, client));
        client.connect();

        Runtime.getRuntime().addShutdownHook(new Shutdown(server, httpClient, client));
        logger.info("{} started", GatewayProxyServer.class.getSimpleName());
    }

    private static class Shutdown extends Thread
    {
        private final GatewayServer server;
        private final HttpClient httpClient;
        private final RHTTPClient client;

        public Shutdown(GatewayServer server, HttpClient httpClient, RHTTPClient client)
        {
            this.server = server;
            this.httpClient = httpClient;
            this.client = client;
        }

        @Override
        public void run()
        {
            try
            {
                client.disconnect();
                httpClient.stop();
                server.stop();
                logger.info("{} stopped", GatewayProxyServer.class.getSimpleName());
            }
            catch (Exception x)
            {
                logger.debug("Exception while stopping " + GatewayProxyServer.class.getSimpleName(), x);
            }
        }
    }

    private static class ProxyListener implements RHTTPListener
    {
        private final HttpClient httpClient;
        private final RHTTPClient client;

        private ProxyListener(HttpClient httpClient, RHTTPClient client)
        {
            this.httpClient = httpClient;
            this.client = client;
        }

        public void onRequest(RHTTPRequest request) throws Exception
        {
            ProxyExchange exchange = new ProxyExchange();
            Address address = Address.from(request.getHeaders().get("Host"));
            if (address.getPort() == 0) address = new Address(address.getHost(), 80);
            exchange.setAddress(address);
            exchange.setMethod(request.getMethod());
            exchange.setURI(request.getURI());
            for (Map.Entry<String, String> header : request.getHeaders().entrySet())
                exchange.setRequestHeader(header.getKey(), header.getValue());
            exchange.setRequestContent(new ByteArrayBuffer(request.getBody()));
            int status = syncSend(exchange);
            if (status == HttpExchange.STATUS_COMPLETED)
            {
                int statusCode = exchange.getResponseStatus();
                String statusMessage = exchange.getResponseMessage();
                Map<String, String> responseHeaders = exchange.getResponseHeaders();
                byte[] responseBody = exchange.getResponseBody();
                RHTTPResponse response = new RHTTPResponse(request.getId(), statusCode, statusMessage, responseHeaders, responseBody);
                client.deliver(response);
            }
            else
            {
                int statusCode = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                String statusMessage = "Gateway error";
                HashMap<String, String> responseHeaders = new HashMap<String, String>();
                responseHeaders.put("Connection", "close");
                byte[] responseBody = new byte[0];
                RHTTPResponse response = new RHTTPResponse(request.getId(), statusCode, statusMessage, responseHeaders, responseBody);
                client.deliver(response);
            }
        }

        private int syncSend(ProxyExchange exchange) throws Exception
        {
            long start = System.nanoTime();
            httpClient.send(exchange);
            int status = exchange.waitForDone();
            long end = System.nanoTime();
            long millis = TimeUnit.NANOSECONDS.toMillis(end - start);
            long micros = TimeUnit.NANOSECONDS.toMicros(end - start - TimeUnit.MILLISECONDS.toNanos(millis));
            logger.debug("Proxied request took {}.{} ms", millis, micros);
            return status;
        }
    }

    private static class ProxyExchange extends ContentExchange
    {
        private String responseMessage;
        private Map<String, String> responseHeaders = new HashMap<String, String>();
        private ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        private ProxyExchange()
        {
            super(true);
        }

        public String getResponseMessage()
        {
            return responseMessage;
        }

        public Map<String, String> getResponseHeaders()
        {
            return responseHeaders;
        }

        public byte[] getResponseBody()
        {
            return responseBody.toByteArray();
        }

        @Override
        protected void onResponseStatus(Buffer version, int code, Buffer message) throws IOException
        {
            super.onResponseStatus(version, code, message);
            this.responseMessage = message.toString("UTF-8");
        }

        @Override
        protected void onResponseHeader(Buffer nameBuffer, Buffer valueBuffer) throws IOException
        {
            super.onResponseHeader(nameBuffer, valueBuffer);
            String name = nameBuffer.toString("UTF-8");
            String value = valueBuffer.toString("UTF-8");
            // Skip chunked header, since we read the whole body and will not re-chunk it
            if (!name.equalsIgnoreCase("Transfer-Encoding") || !value.equalsIgnoreCase("chunked"))
                responseHeaders.put(name, value);
        }

        @Override
        protected void onResponseContent(Buffer buffer) throws IOException
        {
            responseBody.write(buffer.asArray());
            super.onResponseContent(buffer);
        }
    }

    public static class ProxyTargetIdRetriever implements TargetIdRetriever
    {
        public String retrieveTargetId(HttpServletRequest httpRequest)
        {
            return "proxy";
        }
    }
}

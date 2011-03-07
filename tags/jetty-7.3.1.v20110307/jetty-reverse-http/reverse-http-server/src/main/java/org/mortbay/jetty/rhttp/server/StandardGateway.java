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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;

/**
 * Default implementation of {@link Gateway}.
 *
 * @version $Revision$ $Date$
 */
public class StandardGateway implements Gateway
{
    private final Logger logger = Log.getLogger(getClass().toString());
    private final ConcurrentMap<String, ClientDelegate> clients = new ConcurrentHashMap<String, ClientDelegate>();
    private final ConcurrentMap<Integer, ExternalRequest> requests = new ConcurrentHashMap<Integer, ExternalRequest>();
    private final AtomicInteger requestIds = new AtomicInteger();
    private volatile long gatewayTimeout;
    private volatile long externalTimeout;

    public long getGatewayTimeout()
    {
        return gatewayTimeout;
    }

    public void setGatewayTimeout(long timeout)
    {
        this.gatewayTimeout = timeout;
    }

    public long getExternalTimeout()
    {
        return externalTimeout;
    }

    public void setExternalTimeout(long externalTimeout)
    {
        this.externalTimeout = externalTimeout;
    }

    public ClientDelegate getClientDelegate(String targetId)
    {
        return clients.get(targetId);
    }

    public ClientDelegate newClientDelegate(String targetId)
    {
        StandardClientDelegate client = new StandardClientDelegate(targetId);
        client.setTimeout(getGatewayTimeout());
        return client;
    }

    public ClientDelegate addClientDelegate(String targetId, ClientDelegate client)
    {
        return clients.putIfAbsent(targetId, client);
    }

    public ClientDelegate removeClientDelegate(String targetId)
    {
        return clients.remove(targetId);
    }

    public ExternalRequest newExternalRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        int requestId = requestIds.incrementAndGet();
        RHTTPRequest request = convertHttpRequest(requestId, httpRequest);
        StandardExternalRequest gatewayRequest = new StandardExternalRequest(request, httpRequest, httpResponse, this);
        gatewayRequest.setTimeout(getExternalTimeout());
        return gatewayRequest;
    }

    protected RHTTPRequest convertHttpRequest(int requestId, HttpServletRequest httpRequest) throws IOException
    {
        Map<String, String> headers = new HashMap<String, String>();
        for (Enumeration headerNames = httpRequest.getHeaderNames(); headerNames.hasMoreElements();)
        {
            String name = (String)headerNames.nextElement();
            // TODO: improve by supporting getHeaders(name)
            String value = httpRequest.getHeader(name);
            headers.put(name, value);
        }

        byte[] body = Utils.read(httpRequest.getInputStream());
        return new RHTTPRequest(requestId, httpRequest.getMethod(), httpRequest.getRequestURI(), headers, body);
    }

    public ExternalRequest addExternalRequest(int requestId, ExternalRequest externalRequest)
    {
        ExternalRequest existing = requests.putIfAbsent(requestId, externalRequest);
        if (existing == null)
            logger.debug("Added external request {}/{} - {}", new Object[]{requestId, requests.size(), externalRequest});
        return existing;
    }

    public ExternalRequest removeExternalRequest(int requestId)
    {
        ExternalRequest externalRequest = requests.remove(requestId);
        if (externalRequest != null)
            logger.debug("Removed external request {}/{} - {}", new Object[]{requestId, requests.size(), externalRequest});
        return externalRequest;
    }
}

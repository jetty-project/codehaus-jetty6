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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;
import org.mortbay.jetty.rhttp.client.RHTTPResponse;

/**
 * The servlet that handles the communication with the gateway client.
 * @version $Revision$ $Date$
 */
public class GatewayServlet extends HttpServlet
{
    private final Logger logger = Log.getLogger(getClass().toString());
    private final TargetIdRetriever targetIdRetriever = new StandardTargetIdRetriever();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentMap<String, Future<?>> expirations = new ConcurrentHashMap<String, Future<?>>();
    private final Gateway gateway;
    private final long clientTimeout;

    public GatewayServlet(Gateway gateway, long clientTimeout)
    {
        this.gateway = gateway;
        this.clientTimeout = clientTimeout;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String targetId = targetIdRetriever.retrieveTargetId(request);

        String uri = request.getRequestURI();
        String path = uri.substring(request.getServletPath().length());
        String[] segments = path.split("/");
        if (segments.length < 3)
            throw new ServletException("Invalid request to " + getClass().getSimpleName() + ": " + uri);

        String action = segments[2];
        if ("handshake".equals(action))
            serviceHandshake(targetId, request, response);
        else if ("connect".equals(action))
            serviceConnect(targetId, request, response);
        else if ("deliver".equals(action))
            serviceDeliver(targetId, request, response);
        else if ("disconnect".equals(action))
            serviceDisconnect(targetId, request, response);
        else
            throw new ServletException("Invalid request to " + getClass().getSimpleName() + ": " + uri);
    }

    private void serviceHandshake(String targetId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        ClientDelegate client = gateway.getClientDelegate(targetId);
        if (client != null)
            throw new IOException("Client with targetId " + targetId + " is already connected");

        client = gateway.newClientDelegate(targetId);
        ClientDelegate existing = gateway.addClientDelegate(targetId, client);
        if (existing != null)
            throw new IOException("Client with targetId " + targetId + " is already connected");

        flush(client, httpRequest, httpResponse);
    }

    private void flush(ClientDelegate client, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        List<RHTTPRequest> requests = client.process(httpRequest);
        if (requests != null)
        {
            // Schedule before sending the requests, to avoid that the remote client
            // reconnects before we have scheduled the expiration timeout.
            if (!client.isClosed())
                schedule(client);

            ServletOutputStream output = httpResponse.getOutputStream();
            for (RHTTPRequest request : requests)
                output.write(request.getFrameBytes());
            // I could count the framed bytes of all requests and set a Content-Length header,
            // but the implementation of ServletOutputStream takes care of everything:
            // if the request was HTTP/1.1, then flushing result in a chunked response, but the
            // client know how to handle it; if the request was HTTP/1.0, then no chunking.
            // To avoid chunking in HTTP/1.1 I must set the Content-Length header.
            output.flush();
            logger.debug("Delivered to device {} requests {} ", client.getTargetId(), requests);
        }
    }

    private void schedule(ClientDelegate client)
    {
        Future<?> task = scheduler.schedule(new ClientExpirationTask(client), clientTimeout, TimeUnit.MILLISECONDS);
        Future<?> existing = expirations.put(client.getTargetId(), task);
        assert existing == null;
    }

    private void unschedule(String targetId)
    {
        Future<?> task = expirations.remove(targetId);
        if (task != null)
            task.cancel(false);
    }

    private void serviceConnect(String targetId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        unschedule(targetId);

        ClientDelegate client = gateway.getClientDelegate(targetId);
        if (client == null)
        {
            // Expired client tries to connect without handshake
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        flush(client, httpRequest, httpResponse);

        if (client.isClosed())
            gateway.removeClientDelegate(targetId);
    }

    private void expireConnect(ClientDelegate client, long time)
    {
        String targetId = client.getTargetId();
        logger.info("Client with targetId {} missing, last seen {} ms ago, closing it", targetId, System.currentTimeMillis() - time);
        client.close();
        // If the client expired, means that it did not connect,
        // so there no request to resume, and we cleanup here
        // (while normally this cleanup is done in serviceConnect())
        unschedule(targetId);
        gateway.removeClientDelegate(targetId);
    }

    private void serviceDeliver(String targetId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException
    {
        if (gateway.getClientDelegate(targetId) == null)
        {
            // Expired client tries to deliver without handshake
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        byte[] body = Utils.read(httpRequest.getInputStream());

        RHTTPResponse response = RHTTPResponse.fromFrameBytes(body);

        ExternalRequest externalRequest = gateway.removeExternalRequest(response.getId());
        if (externalRequest != null)
        {
            externalRequest.respond(response);
            logger.debug("Deliver request from device {}, gateway request {}, response {}", new Object[] {targetId, externalRequest, response});
        }
        else
        {
            // We can arrive here for a race with the continuation expiration, which expired just before
            // the gateway client responded with a valid response; log this case ignore it.
            logger.debug("Deliver request from device {}, missing gateway request, response {}", targetId, response);
        }
    }

    private void serviceDisconnect(String targetId, HttpServletRequest request, HttpServletResponse response)
    {
        // Do not remove the ClientDelegate from the gateway here,
        // since closing the ClientDelegate will resume the connect request
        // and we remove the ClientDelegate from the gateway there
        ClientDelegate client = gateway.getClientDelegate(targetId);
        if (client != null)
            client.close();
    }

    private class ClientExpirationTask implements Runnable
    {
        private final long time = System.currentTimeMillis();
        private final ClientDelegate client;

        public ClientExpirationTask(ClientDelegate client)
        {
            this.client = client;
        }

        public void run()
        {
            expireConnect(client, time);
        }
    }
}

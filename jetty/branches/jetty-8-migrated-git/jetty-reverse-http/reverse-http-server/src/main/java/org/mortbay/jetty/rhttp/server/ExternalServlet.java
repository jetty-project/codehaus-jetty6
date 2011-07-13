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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;

/**
 * The servlet that handles external requests.
 *
 * @version $Revision$ $Date$
 */
public class ExternalServlet extends HttpServlet
{
    private final Logger logger = Log.getLogger(getClass().toString());
    private final Gateway gateway;
    private final TargetIdRetriever targetIdRetriever;

    public ExternalServlet(Gateway gateway, TargetIdRetriever targetIdRetriever)
    {
        this.gateway = gateway;
        this.targetIdRetriever = targetIdRetriever;
    }

    @Override
    protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException
    {
        logger.debug("External http request: {}", httpRequest.getRequestURL());

        String targetId = targetIdRetriever.retrieveTargetId(httpRequest);
        if (targetId == null)
            throw new ServletException("Invalid request to " + getClass().getSimpleName() + ": " + httpRequest.getRequestURI());

        ClientDelegate client = gateway.getClientDelegate(targetId);
        if (client == null) throw new ServletException("Client with targetId " + targetId + " is not connected");

        ExternalRequest externalRequest = gateway.newExternalRequest(httpRequest, httpResponse);
        RHTTPRequest request = externalRequest.getRequest();
        ExternalRequest existing = gateway.addExternalRequest(request.getId(), externalRequest);
        assert existing == null;
        logger.debug("External request {} for device {}", request, targetId);

        boolean delivered = client.enqueue(request);
        if (delivered)
        {
            externalRequest.suspend();
        }
        else
        {
            // TODO: improve this: we can temporarly queue this request elsewhere and wait for the client to reconnect ?
            throw new ServletException("Could not enqueue request to client with targetId " + targetId);
        }
    }
}

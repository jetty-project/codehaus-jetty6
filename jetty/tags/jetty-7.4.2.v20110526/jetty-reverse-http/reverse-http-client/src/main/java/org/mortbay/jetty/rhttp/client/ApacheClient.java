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

package org.mortbay.jetty.rhttp.client;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

/**
 * Implementation of {@link RHTTPClient} that uses Apache's HttpClient.
 *
 * @version $Revision$ $Date$
 */
public class ApacheClient extends AbstractClient
{
    private final HttpClient httpClient;
    private final String gatewayPath;

    public ApacheClient(HttpClient httpClient, String gatewayPath, String targetId)
    {
        super(targetId);
        this.httpClient = httpClient;
        this.gatewayPath = gatewayPath;
    }

    public String getHost()
    {
        return ((HttpHost)httpClient.getParams().getParameter("http.default-host")).getHostName();
    }

    public int getPort()
    {
        return ((HttpHost)httpClient.getParams().getParameter("http.default-host")).getPort();
    }
    
    public String getPath()
    {
        return gatewayPath;
    }

    protected void syncHandshake() throws IOException
    {
        HttpPost handshake = new HttpPost(gatewayPath + "/" + urlEncode(getTargetId()) + "/handshake");
        HttpResponse response = httpClient.execute(handshake);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (entity != null)
            entity.consumeContent();
        if (statusCode != HttpStatus.SC_OK)
            throw new IOException("Handshake failed");
        getLogger().debug("Client {} handshake returned from gateway", getTargetId(), null);
    }

    protected void asyncConnect()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    HttpPost connect = new HttpPost(gatewayPath + "/" + urlEncode(getTargetId()) + "/connect");
                    getLogger().debug("Client {} connect sent to gateway", getTargetId(), null);
                    HttpResponse response = httpClient.execute(connect);
                    int statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    byte[] responseContent = EntityUtils.toByteArray(entity);
                    if (statusCode == HttpStatus.SC_OK)
                        connectComplete(responseContent);
                    else if (statusCode == HttpStatus.SC_UNAUTHORIZED)
                        notifyConnectRequired();
                    else
                        notifyConnectException();
                }
                catch (NoHttpResponseException x)
                {
                    notifyConnectClosed();
                }
                catch (IOException x)
                {
                    getLogger().debug("", x);
                    notifyConnectException();
                }
            }
        }.start();
    }

    protected void syncDisconnect() throws IOException
    {
        HttpPost disconnect = new HttpPost(gatewayPath + "/" + urlEncode(getTargetId()) + "/disconnect");
        HttpResponse response = httpClient.execute(disconnect);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (entity != null)
            entity.consumeContent();
        if (statusCode != HttpStatus.SC_OK)
            throw new IOException("Disconnect failed");
        getLogger().debug("Client {} disconnect returned from gateway", getTargetId(), null);
    }

    protected void asyncDeliver(final RHTTPResponse response)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    HttpPost deliver = new HttpPost(gatewayPath + "/" + urlEncode(getTargetId()) + "/deliver");
                    deliver.setEntity(new ByteArrayEntity(response.getFrameBytes()));
                    getLogger().debug("Client {} deliver sent to gateway, response {}", getTargetId(), response);
                    HttpResponse httpResponse = httpClient.execute(deliver);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    HttpEntity entity = httpResponse.getEntity();
                    if (entity != null)
                        entity.consumeContent();
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED)
                        notifyConnectRequired();
                    else if (statusCode != HttpStatus.SC_OK)
                        notifyDeliverException(response);
                }
                catch (IOException x)
                {
                    getLogger().debug("", x);
                    notifyDeliverException(response);
                }
            }
        }.start();
    }
}

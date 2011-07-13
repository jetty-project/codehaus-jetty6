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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractClient extends AbstractLifeCycle implements RHTTPClient
{
    private final Logger logger = Log.getLogger("org.mortbay.jetty.rhttp.client");
    private final List<RHTTPListener> listeners = new CopyOnWriteArrayList<RHTTPListener>();
    private final List<ClientListener> clientListeners = new CopyOnWriteArrayList<ClientListener>();
    private final String targetId;
    private volatile Status status = Status.DISCONNECTED;

    public AbstractClient(String targetId)
    {
        this.targetId = targetId;
    }

    public String getGatewayURI()
    {
        return "http://"+getHost()+":"+getPort()+getPath();
    }

    public String getTargetId()
    {
        return targetId;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void addListener(RHTTPListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(RHTTPListener listener)
    {
        listeners.remove(listener);
    }

    public void addClientListener(ClientListener listener)
    {
        clientListeners.add(listener);
    }

    public void removeClientListener(ClientListener listener)
    {
        clientListeners.remove(listener);
    }

    protected void notifyRequests(List<RHTTPRequest> requests)
    {
        for (RHTTPRequest request : requests)
        {
            for (RHTTPListener listener : listeners)
            {
                try
                {
                    listener.onRequest(request);
                }
                catch (Throwable x)
                {
                    logger.warn("Listener " + listener + " threw", x);
                    try
                    {
                        deliver(newExceptionResponse(request.getId(), x));
                    }
                    catch (IOException xx)
                    {
                        logger.debug("Could not deliver exception response", xx);
                    }
                }
            }
        }
    }

    protected RHTTPResponse newExceptionResponse(int requestId, Throwable x)
    {
        try
        {
            int statusCode = 500;
            String statusMessage = "Internal Server Error";
            Map<String, String> headers = new HashMap<String, String>();
            byte[] body = x.toString().getBytes("UTF-8");
            return new RHTTPResponse(requestId, statusCode, statusMessage, headers, body);
        }
        catch (UnsupportedEncodingException xx)
        {
            throw new AssertionError(xx);
        }
    }

    protected void notifyConnectRequired()
    {
        for (ClientListener listener : clientListeners)
        {
            try
            {
                listener.connectRequired();
            }
            catch (Throwable x)
            {
                logger.warn("ClientListener " + listener + " threw", x);
            }
        }
    }

    protected void notifyConnectException()
    {
        for (ClientListener listener : clientListeners)
        {
            try
            {
                listener.connectException();
            }
            catch (Throwable x)
            {
                logger.warn("ClientListener " + listener + " threw", x);
            }
        }
    }

    protected void notifyConnectClosed()
    {
        for (ClientListener listener : clientListeners)
        {
            try
            {
                listener.connectClosed();
            }
            catch (Throwable xx)
            {
                logger.warn("ClientListener " + listener + " threw", xx);
            }
        }
    }

    protected void notifyDeliverException(RHTTPResponse response)
    {
        for (ClientListener listener : clientListeners)
        {
            try
            {
                listener.deliverException(response);
            }
            catch (Throwable x)
            {
                logger.warn("ClientListener " + listener + " threw", x);
            }
        }
    }

    protected String urlEncode(String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException x)
        {
            getLogger().debug("", x);
            return null;
        }
    }

    protected boolean isConnected()
    {
        return status == Status.CONNECTED;
    }

    protected boolean isDisconnecting()
    {
        return status == Status.DISCONNECTING;
    }

    protected boolean isDisconnected()
    {
        return status == Status.DISCONNECTED;
    }

    public void connect() throws IOException
    {
        if (isDisconnected())
            status = Status.CONNECTING;

        syncHandshake();
        this.status = Status.CONNECTED;

        asyncConnect();
    }

    public void disconnect() throws IOException
    {
        if (isConnected())
        {
            status = Status.DISCONNECTING;
            try
            {
                syncDisconnect();
            }
            finally
            {
                status = Status.DISCONNECTED;
            }
        }
    }

    public void deliver(RHTTPResponse response) throws IOException
    {
        asyncDeliver(response);
    }

    protected abstract void syncHandshake() throws IOException;

    protected abstract void asyncConnect();

    protected abstract void syncDisconnect() throws IOException;

    protected abstract void asyncDeliver(RHTTPResponse response);

    protected void connectComplete(byte[] responseContent) throws IOException
    {
        List<RHTTPRequest> requests = RHTTPRequest.fromFrameBytes(responseContent);
        getLogger().debug("Client {} connect returned from gateway, requests {}", getTargetId(), requests);

        // Requests are arrived, reconnect while we process them
        if (!isDisconnecting() && !isDisconnected())
            asyncConnect();

        notifyRequests(requests);
    }

    protected enum Status
    {
        CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
    }
}

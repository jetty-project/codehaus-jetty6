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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jetty.io.ByteArrayEndPoint;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.RHTTPListener;
import org.mortbay.jetty.rhttp.client.RHTTPRequest;
import org.mortbay.jetty.rhttp.client.RHTTPResponse;

/**
 * An implementation of a Jetty connector that uses a {@link RHTTPClient} connected
 * to a gateway server to receive requests, feed them to the Jetty server, and
 * forward responses from the Jetty server to the gateway server.
 *
 * @version $Revision$ $Date$
 */
public class ReverseHTTPConnector extends AbstractConnector implements RHTTPListener
{
    private final BlockingQueue<RHTTPRequest> requests = new LinkedBlockingQueue<RHTTPRequest>();
    private final RHTTPClient client;

    public ReverseHTTPConnector(RHTTPClient client)
    {
        this.client = client;
        super.setHost(client.getHost());
        super.setPort(client.getPort());
    }

    @Override
    public void setHost(String host)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPort(int port)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doStart() throws Exception
    {
        if (client instanceof LifeCycle)
            ((LifeCycle)client).start();
        super.doStart();
        client.connect();
    }

    @Override
    protected void doStop() throws Exception
    {
        client.disconnect();
        super.doStop();
        if (client instanceof LifeCycle)
            ((LifeCycle)client).stop();
    }

    public void open()
    {
        client.addListener(this);
    }

    public void close()
    {
        client.removeListener(this);
    }

    public int getLocalPort()
    {
        return -1;
    }

    public Object getConnection()
    {
        return this;
    }

    @Override
    protected void accept(int acceptorId) throws IOException, InterruptedException
    {
        RHTTPRequest request = requests.take();
        IncomingRequest incomingRequest = new IncomingRequest(request);
        getThreadPool().dispatch(incomingRequest);
    }

    @Override
    public void persist(EndPoint endpoint) throws IOException
    {
        // Signals that the connection should not be closed
        // Do nothing in this case, as we run from memory
    }

    public void onRequest(RHTTPRequest request) throws Exception
    {
        requests.add(request);
    }

    private class IncomingRequest implements Runnable
    {
        private final RHTTPRequest request;

        private IncomingRequest(RHTTPRequest request)
        {
            this.request = request;
        }

        public void run()
        {
            byte[] requestBytes = request.getRequestBytes();

            ByteArrayEndPoint endPoint = new ByteArrayEndPoint(requestBytes, 1024);
            endPoint.setGrowOutput(true);

            HttpConnection connection = new HttpConnection(ReverseHTTPConnector.this, endPoint, getServer());
            connectionOpened(connection);
            try
            {
                // Loop over the whole content, since handle() only
                // reads up to the connection buffer's capacities
                while (endPoint.getIn().length() > 0)
                    connection.handle();

                byte[] responseBytes = endPoint.getOut().asArray();
                RHTTPResponse response = RHTTPResponse.fromResponseBytes(request.getId(), responseBytes);
                client.deliver(response);
            }
            catch (Exception x)
            {
                Log.debug(x);
            }
            finally
            {
                connectionClosed(connection);
            }
        }
    }
}

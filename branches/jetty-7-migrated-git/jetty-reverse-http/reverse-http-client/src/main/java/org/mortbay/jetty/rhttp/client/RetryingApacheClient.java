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

import org.apache.http.client.HttpClient;

/**
 * @version $Revision$ $Date$
 */
public class RetryingApacheClient extends ApacheClient
{
    public RetryingApacheClient(HttpClient httpClient, String gatewayURI, String targetId)
    {
        super(httpClient, gatewayURI, targetId);
        addClientListener(new RetryClientListener());
    }

    @Override
    protected void syncHandshake() throws IOException
    {
        while (true)
        {
            try
            {
                super.syncHandshake();
                break;
            }
            catch (IOException x)
            {
                getLogger().debug("Handshake failed, backing off and retrying");
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException xx)
                {
                    throw (IOException)new IOException().initCause(xx);
                }
            }
        }
    }

    private class RetryClientListener implements ClientListener
    {
        public void connectRequired()
        {
            getLogger().debug("Connect requested by server");
            try
            {
                connect();
            }
            catch (IOException x)
            {
                // The connect() method is retried, so if it fails, it's a hard failure
                getLogger().debug("Connect failed after server required connect, giving up");
            }
        }

        public void connectClosed()
        {
            connectException();
        }

        public void connectException()
        {
            getLogger().debug("Connect failed, backing off and retrying");
            try
            {
                Thread.sleep(1000);
                asyncConnect();
            }
            catch (InterruptedException x)
            {
                // Ignore and stop retrying
                Thread.currentThread().interrupt();
            }
        }

        public void deliverException(RHTTPResponse response)
        {
            getLogger().debug("Deliver failed, backing off and retrying");
            try
            {
                Thread.sleep(1000);
                asyncDeliver(response);
            }
            catch (InterruptedException x)
            {
                // Ignore and stop retrying
                Thread.currentThread().interrupt();
            }
        }
    }
}

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

/**
 * A listener for network-related events happening on the gateway client.
 *
 * @version $Revision$ $Date$
 */
public interface ClientListener
{
    /**
     * Called when the client detects that the server requested a new connect.
     */
    public void connectRequired();

    /**
     * Called when the client detects that the connection has been closed by the server.
     */
    public void connectClosed();

    /**
     * Called when the client detects a generic exception while trying to connect to the server.
     */
    public void connectException();

    /**
     * Called when the client detects a generic exception while tryint to deliver to the server.
     * @param response the Response object that should have been sent to the server
     */
    public void deliverException(RHTTPResponse response);

    public static class Adapter implements ClientListener
    {
        public void connectRequired()
        {
        }

        public void connectClosed()
        {
        }

        public void connectException()
        {
        }

        public void deliverException(RHTTPResponse response)
        {
        }
    }
}

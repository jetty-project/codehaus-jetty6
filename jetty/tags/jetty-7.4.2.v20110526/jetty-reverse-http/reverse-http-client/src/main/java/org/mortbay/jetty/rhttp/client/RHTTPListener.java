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
 * <p>Implementations of this class listen for requests arriving from the gateway server
 * and notified by {@link RHTTPClient}.</p>
 *
 * @version $Revision$ $Date$
 */
public interface RHTTPListener
{
    /**
     * Callback method called by {@link RHTTPClient} to inform that the gateway server
     * sent a request to the gateway client.
     * @param request the request sent by the gateway server.
     * @throws Exception allowed to be thrown by implementations
     */
    public void onRequest(RHTTPRequest request) throws Exception;
}

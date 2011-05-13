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

import org.mortbay.jetty.rhttp.client.RHTTPRequest;
import org.mortbay.jetty.rhttp.client.RHTTPResponse;


/**
 * <p><tt>ExternalRequest</tt> represent an external request made to the gateway server.</p>
 * <p><tt>ExternalRequest</tt>s that arrive to the gateway server are suspended, waiting
 * for a response from the corresponding gateway client.</p>
 *
 * @version $Revision$ $Date$
 */
public interface ExternalRequest
{
    /**
     * <p>Suspends this <tt>ExternalRequest</tt> waiting for a response from the gateway client.</p>
     * @return true if the <tt>ExternalRequest</tt> has been suspended, false if the
     * <tt>ExternalRequest</tt> has already been responded.
     */
    public boolean suspend();

    /**
     * <p>Responds to the original external request with the response arrived from the gateway client.</p>
     * @param response the response arrived from the gateway client
     * @throws IOException if responding to the original external request fails
     */
    public void respond(RHTTPResponse response) throws IOException;

    /**
     * @return the request to be sent to the gateway client
     */
    public RHTTPRequest getRequest();
}

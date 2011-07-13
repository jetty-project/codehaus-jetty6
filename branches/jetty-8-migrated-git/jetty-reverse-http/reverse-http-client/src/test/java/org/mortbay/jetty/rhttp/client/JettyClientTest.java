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

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;


/**
 * @version $Revision$ $Date$
 */
public class JettyClientTest extends ClientTest
{
    private HttpClient httpClient;

    protected RHTTPClient createClient(int port, String targetId) throws Exception
    {
        ((StdErrLog)Log.getLog()).setSource(true);
        httpClient = new HttpClient();
        httpClient.start();
        return new JettyClient(httpClient, new Address("localhost", port), "", targetId);
    }

    protected void destroyClient(RHTTPClient client) throws Exception
    {
        httpClient.stop();
    }
}

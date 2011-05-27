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

import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;


/**
 * @version $Revision$ $Date$
 */
public class ApacheClientTest extends ClientTest
{
    private ClientConnectionManager connectionManager;

    protected RHTTPClient createClient(int port, String targetId) throws Exception
    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
        connectionManager = new ThreadSafeClientConnManager(new BasicHttpParams(), schemeRegistry);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter("http.default-host", new HttpHost("localhost", port));
        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);
        httpClient.setHttpRequestRetryHandler(new NoRetryHandler());
        return new ApacheClient(httpClient, "", targetId);
    }

    protected void destroyClient(RHTTPClient client) throws Exception
    {
        connectionManager.shutdown();
    }

    private class NoRetryHandler implements HttpRequestRetryHandler
    {
        public boolean retryRequest(IOException x, int failedAttempts, HttpContext httpContext)
        {
            return false;
        }
    }
}

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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class RequestTest extends TestCase
{
    public void testRequestConversions() throws Exception
    {
        int id = 1;
        String method = "GET";
        String uri = "/test";
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("X", "X");
        headers.put("Y", "Y");
        headers.put("Z", "Z");
        byte[] body = "BODY".getBytes("UTF-8");
        headers.put("Content-Length", String.valueOf(body.length));
        RHTTPRequest request1 = new RHTTPRequest(id, method, uri, headers, body);
        byte[] requestBytes1 = request1.getRequestBytes();
        RHTTPRequest request2 = RHTTPRequest.fromRequestBytes(id, requestBytes1);
        assertEquals(id, request2.getId());
        assertEquals(method, request2.getMethod());
        assertEquals(uri, request2.getURI());
        assertEquals(headers, request2.getHeaders());
        assertTrue(Arrays.equals(request2.getBody(), body));

        byte[] requestBytes2 = request2.getRequestBytes();
        assertTrue(Arrays.equals(requestBytes1, requestBytes2));
    }

    public void testFrameConversions() throws Exception
    {
        int id = 1;
        String method = "GET";
        String uri = "/test";
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("X", "X");
        headers.put("Y", "Y");
        headers.put("Z", "Z");
        byte[] body = "BODY".getBytes("UTF-8");
        headers.put("Content-Length", String.valueOf(body.length));
        RHTTPRequest request1 = new RHTTPRequest(id, method, uri, headers, body);
        byte[] frameBytes1 = request1.getFrameBytes();
        List<RHTTPRequest> requests = RHTTPRequest.fromFrameBytes(frameBytes1);
        assertNotNull(requests);
        assertEquals(1, requests.size());
        RHTTPRequest request2 = requests.get(0);
        assertEquals(id, request2.getId());
        assertEquals(method, request2.getMethod());
        assertEquals(uri, request2.getURI());
        assertEquals(headers, request2.getHeaders());
        assertTrue(Arrays.equals(request2.getBody(), body));

        byte[] frameBytes2 = request2.getFrameBytes();
        assertTrue(Arrays.equals(frameBytes1, frameBytes2));
    }
}

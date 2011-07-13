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
import java.util.Map;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class ResponseTest extends TestCase
{
    public void testResponseConversions() throws Exception
    {
        int id = 1;
        int statusCode = 200;
        String statusMessage = "OK";
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("X", "X");
        headers.put("Y", "Y");
        headers.put("Z", "Z");
        byte[] body = "BODY".getBytes("UTF-8");
        RHTTPResponse response1 = new RHTTPResponse(id, statusCode, statusMessage, headers, body);
        byte[] responseBytes1 = response1.getResponseBytes();
        RHTTPResponse response2 = RHTTPResponse.fromResponseBytes(id, responseBytes1);
        assertEquals(id, response2.getId());
        assertEquals(statusCode, response2.getStatusCode());
        assertEquals(statusMessage, response2.getStatusMessage());
        assertEquals(headers, response2.getHeaders());
        assertTrue(Arrays.equals(response2.getBody(), body));

        byte[] responseBytes2 = response2.getResponseBytes();
        assertTrue(Arrays.equals(responseBytes1, responseBytes2));
    }

    public void testFrameConversions() throws Exception
    {
        int id = 1;
        int statusCode = 200;
        String statusMessage = "OK";
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("X", "X");
        headers.put("Y", "Y");
        headers.put("Z", "Z");
        byte[] body = "BODY".getBytes("UTF-8");
        RHTTPResponse response1 = new RHTTPResponse(id, statusCode, statusMessage, headers, body);
        byte[] frameBytes1 = response1.getFrameBytes();
        RHTTPResponse response2 = RHTTPResponse.fromFrameBytes(frameBytes1);
        assertEquals(id, response2.getId());
        assertEquals(statusCode, response2.getStatusCode());
        assertEquals(response2.getStatusMessage(), statusMessage);
        assertEquals(headers, response2.getHeaders());
        assertTrue(Arrays.equals(response2.getBody(), body));

        byte[] frameBytes2 = response2.getFrameBytes();
        assertTrue(Arrays.equals(frameBytes1, frameBytes2));
    }
}

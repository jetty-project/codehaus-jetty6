// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.mortbay.jetty.http;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.mortbay.jetty.io.Buffer;

public class HttpStatusCodeTest extends TestCase
{
    public void testInvalidGetCode()
    {
        assertNull("Invalid code: 800", HttpStatusCode.getCode(800));
        assertNull("Invalid code: 190", HttpStatusCode.getCode(190));
    }

    public void testGetResponseLine() throws UnsupportedEncodingException
    {
        assertResponseLine("404 Not Found", 404);
        assertResponseLine("507 Insufficient Storage", 507);
    }

    public void testGetResponseLineDirect() throws UnsupportedEncodingException
    {
        assertResponseLine("404 Not Found", HttpStatusCode.NOT_FOUND);
        assertResponseLine("507 Insufficient Storage", HttpStatusCode.INSUFFICIENT_STORAGE);
    }

    private void assertResponseLine(String expected, HttpStatusCode code) throws UnsupportedEncodingException
    {
        String actual = new String(code.getResponseLine().array(), "UTF-8");
        assertEquals("Response Line for " + code, expected, actual);
    }

    private void assertResponseLine(String expected, int ordinal) throws UnsupportedEncodingException
    {
        Buffer buf = HttpStatusCode.getResponseLine(ordinal);
        assertNotNull("Response Line for code [" + ordinal + "] should not be null", buf);
        String actual = new String(buf.array(), "UTF-8");
        assertEquals("Response Line for code [" + ordinal + "]", expected, actual);
    }
}

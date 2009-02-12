//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.util;

import org.mortbay.jetty.util.StringUtil;
import org.mortbay.jetty.util.Utf8StringBuffer;

public class Utf8StringBufferTest extends junit.framework.TestCase
{

    public void testUtfStringBuffer()
        throws Exception
    {
        String source="abcd012345\n\r\u0000\u00a4\u10fb\ufffdjetty";
        byte[] bytes = source.getBytes(StringUtil.__UTF8);
        Utf8StringBuffer buffer = new Utf8StringBuffer();
        for (int i=0;i<bytes.length;i++)
            buffer.append(bytes[i]);
        assertEquals(source, buffer.toString());
        assertTrue(buffer.toString().endsWith("jetty")); 
    }

}

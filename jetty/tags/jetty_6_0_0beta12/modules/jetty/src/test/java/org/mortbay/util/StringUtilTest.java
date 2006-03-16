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

package org.mortbay.util;


import junit.framework.TestCase;

/**
 * @author gregw
 *
 */
public class StringUtilTest extends TestCase
{

    /**
     * Constructor for StringUtilTest.
     * @param arg0
     */
    public StringUtilTest(String arg0)
    {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testAsciiToLowerCase()
    {
        String lc="·bc def 1Û3";
        assertEquals(StringUtil.asciiToLowerCase("·Bc DeF 1Û3"), lc);
        assertTrue(StringUtil.asciiToLowerCase(lc)==lc);
    }

    public void testStartsWithIgnoreCase()
    {
        
        assertTrue(StringUtil.startsWithIgnoreCase("·b·defg", "·b·"));
        assertTrue(StringUtil.startsWithIgnoreCase("·bcdefg", "·bc"));
        assertTrue(StringUtil.startsWithIgnoreCase("·bcdefg", "·Bc"));
        assertTrue(StringUtil.startsWithIgnoreCase("·Bcdefg", "·bc"));
        assertTrue(StringUtil.startsWithIgnoreCase("·Bcdefg", "·Bc"));
        assertTrue(StringUtil.startsWithIgnoreCase("·bcdefg", ""));
        assertTrue(StringUtil.startsWithIgnoreCase("·bcdefg", null));
        assertTrue(StringUtil.startsWithIgnoreCase("·bcdefg", "·bcdefg"));

        assertFalse(StringUtil.startsWithIgnoreCase(null, "xyz")); 
        assertFalse(StringUtil.startsWithIgnoreCase("·bcdefg", "xyz"));
        assertFalse(StringUtil.startsWithIgnoreCase("·", "xyz")); 
    }

    public void testEndsWithIgnoreCase()
    {
        assertTrue(StringUtil.endsWithIgnoreCase("·bcd·f·", "·f·"));
        assertTrue(StringUtil.endsWithIgnoreCase("·bcdefg", "efg"));
        assertTrue(StringUtil.endsWithIgnoreCase("·bcdefg", "eFg"));
        assertTrue(StringUtil.endsWithIgnoreCase("·bcdeFg", "efg"));
        assertTrue(StringUtil.endsWithIgnoreCase("·bcdeFg", "eFg"));
        assertTrue(StringUtil.endsWithIgnoreCase("·bcdefg", ""));
        assertTrue(StringUtil.endsWithIgnoreCase("·bcdefg", null));
        assertTrue(StringUtil.endsWithIgnoreCase("·bcdefg", "·bcdefg"));

        assertFalse(StringUtil.endsWithIgnoreCase(null, "xyz")); 
        assertFalse(StringUtil.endsWithIgnoreCase("·bcdefg", "xyz"));
        assertFalse(StringUtil.endsWithIgnoreCase("·", "xyz"));  
    }

    public void testIndexFrom()
    {
        assertEquals(StringUtil.indexFrom("·bcd", "xyz"),-1);
        assertEquals(StringUtil.indexFrom("·bcd", "·bcz"),0);
        assertEquals(StringUtil.indexFrom("·bcd", "bcz"),1);
        assertEquals(StringUtil.indexFrom("·bcd", "dxy"),3);
    }

    public void testReplace()
    {
        String s="·bc ·bc ·bc";
        assertEquals(StringUtil.replace(s, "·bc", "xyz"),"xyz xyz xyz");
        assertTrue(StringUtil.replace(s,"xyz","pqy")==s);
        
        s=" ·bc ";
        assertEquals(StringUtil.replace(s, "·bc", "xyz")," xyz ");
        
    }

    public void testUnquote()
    {
        String uq =" not quoted ";
        assertTrue(StringUtil.unquote(uq)==uq);
        assertEquals(StringUtil.unquote("' quoted string '")," quoted string ");
        assertEquals(StringUtil.unquote("\" quoted string \"")," quoted string ");
        assertEquals(StringUtil.unquote("' quoted\"string '")," quoted\"string ");
        assertEquals(StringUtil.unquote("\" quoted'string \"")," quoted'string ");
    }


    public void testNonNull()
    {
        String nn="";
        assertTrue(nn==StringUtil.nonNull(nn));
        assertEquals("",StringUtil.nonNull(null));
    }

    /*
     * Test for boolean equals(String, char[], int, int)
     */
    public void testEqualsStringcharArrayintint()
    {
        assertTrue(StringUtil.equals("·bc", new char[] {'x','·','b','c','z'},1,3));
        assertFalse(StringUtil.equals("axc", new char[] {'x','a','b','c','z'},1,3));
    }

    public void testAppend()
    {
        StringBuffer buf = new StringBuffer();
        buf.append('a');
        StringUtil.append(buf, "abc", 1, 1);
        StringUtil.append(buf, (byte)12, 16);
        StringUtil.append(buf, (byte)16, 16);
        StringUtil.append(buf, (byte)-1, 16);
        StringUtil.append(buf, (byte)-16, 16);
        assertEquals("ab0c10fff0",buf.toString());
        
    }
}

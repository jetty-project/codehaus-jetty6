package org.mortbay.cometd;

import junit.framework.TestCase;

public class ChannelPatternTest extends TestCase
{

    public void testChannelPattern()
    {
        ChannelPattern pattern;
        
        pattern=new ChannelPattern("/test/abc");
        assertTrue(pattern.matches("/test/abc"));
        assertFalse(pattern.matches("/test/abc/more"));
        assertFalse(pattern.matches("/test/ab"));
        assertFalse(pattern.matches("/abc"));
        assertFalse(pattern.matches(""));
        
        pattern=new ChannelPattern("/test/*");
        assertTrue(pattern.matches("/test/a"));
        assertTrue(pattern.matches("/test/abc"));
        assertFalse(pattern.matches("/test/abc/foo"));
        assertFalse(pattern.matches("/tost/abc"));
        assertFalse(pattern.matches("/test"));
        
        pattern=new ChannelPattern("/test/a*");
        assertTrue(pattern.matches("/test/ac"));
        assertTrue(pattern.matches("/test/abc"));
        assertTrue(pattern.matches("/test/abbbbc"));
        assertFalse(pattern.matches("/test/a/c"));
        assertFalse(pattern.matches("/test/abc/def"));
        assertFalse(pattern.matches("/tost/abc"));
        assertFalse(pattern.matches("/test"));
        
        pattern=new ChannelPattern("/test/a*c");
        assertTrue(pattern.matches("/test/ac"));
        assertTrue(pattern.matches("/test/abc"));
        assertTrue(pattern.matches("/test/abbbbc"));
        assertFalse(pattern.matches("/test/a/c"));
        assertFalse(pattern.matches("/test/abc/def"));
        assertFalse(pattern.matches("/tost/abc"));
        assertFalse(pattern.matches("/test"));
        
        pattern=new ChannelPattern("/test/*/foo");
        assertTrue(pattern.matches("/test/abc/foo"));
        assertFalse(pattern.matches("/test/foo"));
        assertFalse(pattern.matches("/test/abc/def/foo"));
        
        pattern=new ChannelPattern("/test/**/foo");
        assertTrue(pattern.matches("/test/foo"));
        assertTrue(pattern.matches("/test/abc/foo"));
        assertTrue(pattern.matches("/test/abc/def/foo"));
        
        pattern=new ChannelPattern("/test/a**c/foo");
        assertTrue(pattern.matches("/test/ac/foo"));
        assertTrue(pattern.matches("/test/abc/foo"));
        assertTrue(pattern.matches("/test/ab/bc/foo"));
        
       
        pattern=new ChannelPattern("/abc,/test/*,/other/**/foo");
        assertTrue(pattern.matches("/abc"));
        assertTrue(pattern.matches("/test/xxx"));
        assertTrue(pattern.matches("/other/xxx/foo"));
        assertFalse(pattern.matches("/abcd"));
        assertFalse(pattern.matches("/test"));
        assertFalse(pattern.matches("/other/abc"));
        
        
        
    }
}

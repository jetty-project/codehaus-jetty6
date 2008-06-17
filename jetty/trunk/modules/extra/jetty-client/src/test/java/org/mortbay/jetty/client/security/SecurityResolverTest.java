package org.mortbay.jetty.client.security;

import java.util.Map;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;

public class SecurityResolverTest extends TestCase
{
    public void testNothing()
    {
        
    }
    /* TODO

    public void testCredentialParsing() throws Exception
    {
        SecurityListener resolver = new SecurityListener();
        Buffer value = new ByteArrayBuffer("basic a=b".getBytes());
        
        assertEquals( "basic", resolver.scrapeAuthenticationType( value.toString() ) );
        assertEquals( 1, resolver.scrapeAuthenticationDetails( value.toString() ).size() );

        value = new ByteArrayBuffer("digest a=boo, c=\"doo\" , egg=foo".getBytes());
        
        assertEquals( "digest", resolver.scrapeAuthenticationType( value.toString() ) );
        Map<String,String> testMap = resolver.scrapeAuthenticationDetails( value.toString() );
        assertEquals( 3, testMap.size() );
        assertEquals( "boo", testMap.get("a") );
        assertEquals( "doo", testMap.get("c") );
        assertEquals( "foo", testMap.get("egg") );
    }
    
    */
}

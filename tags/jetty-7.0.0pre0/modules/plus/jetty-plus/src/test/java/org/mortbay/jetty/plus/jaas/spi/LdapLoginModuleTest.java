// ========================================================================
// Copyright 2007 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.jaas.spi;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.apacheds.ApacheDs;
import org.mortbay.jetty.security.Credential;
import org.apache.directory.shared.ldap.util.Base64;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.HashMap;
import java.util.Map;

public class LdapLoginModuleTest extends PlexusTestCase
{

    private ApacheDs apacheDs;

    private String suffix;


    protected void setUp()
        throws Exception
    {
        super.setUp();

        apacheDs = (ApacheDs)lookup(ApacheDs.ROLE, "test" );

        suffix = apacheDs.addSimplePartition( "test", new String[]{"jetty", "mortbay", "org"} ).getSuffix();

        System.out.println( "DN Suffix: " + suffix );

         apacheDs.startServer();

        makeUsers();

    }



    protected void tearDown() throws Exception {

        InitialDirContext context = apacheDs.getAdminContext();

        context.unbind( createDn( "jesse" ) );

        apacheDs.stopServer();

        super.tearDown();
    }


    public void testBindingAuth() throws Exception
    {
        LdapLoginModule lm = new LdapLoginModule();

        Map options = new HashMap();
        options.put( "hostname", "localhost" );
        options.put( "port", "10390" );
        options.put( "contextFactory", "com.sun.jndi.ldap.LdapCtxFactory" );
        options.put( "bindDn", "uid=admin,ou=system" );
        options.put( "bindPassword", "secret" );
        options.put( "userBaseDn", "dc=jetty,dc=mortbay,dc=org" );
        options.put( "forceBindingLogin", "true" );


        lm.initialize( null, null, null, options );

        assertTrue( lm.bindingLogin( "jesse", "foo" ) );
    }
    /*
    public void testCredentialAuth() throws Exception
    {
        LdapLoginModule lm = new LdapLoginModule();

        Map options = new HashMap();
        options.put( "hostname", "localhost" );
        options.put( "port", "10390" );
        options.put( "contextFactory", "com.sun.jndi.ldap.LdapCtxFactory" );
        options.put( "bindDn", "uid=admin,ou=system" );
        options.put( "bindPassword", "secret" );
        options.put( "userBaseDn", "dc=jetty,dc=mortbay,dc=org" );
        options.put( "forceBindingLogin", "false" );


        lm.initialize( null, null, null, options );

        UserInfo info = lm.getUserInfo( "jesse" );

        assertTrue( lm.credentialLogin( info, "foo" ) );
    }
    */
    private void makeUsers() throws Exception
    {
        InitialDirContext context = apacheDs.getAdminContext();

        String cn = "jesse";
        bindUserObject( context, cn, createDn( cn ) );
        assertExist( context, createDn( cn ), "cn", cn );

    }

    private void bindUserObject(DirContext context, String cn, String dn)
            throws Exception
    {
        Attributes attributes = new BasicAttributes(true);
        BasicAttribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("top");
        objectClass.add("inetOrgPerson");
        objectClass.add("person");
        objectClass.add("organizationalperson");
        attributes.put(objectClass);
        attributes.put("cn", cn);
        attributes.put("sn", "foo");
        attributes.put("mail", "foo");
        //System.out.println("setting password to : " + LdapLoginModule.convertCredentialJettyToLdap( Credential.MD5.digest( "foo" ) ));
        String pwd = Credential.MD5.digest( "foo" );
        pwd = pwd.substring("MD5:".length(), pwd.length() );
        //System.out.println(Credential.MD5.digest( "foo" ));
        //System.out.println(pwd);
        //System.out.println(Base64.encode( pwd.getBytes("ISO-8859-1") ));
        //System.out.println(Base64.encode( pwd.getBytes("UTF-8") ));
        attributes.put("userPassword", "{MD5}" + doStuff(pwd) );
        //attributes.put( "userPassword", "foo");
        attributes.put("givenName", "foo");
        context.createSubcontext( dn, attributes );
    }


    private String doStuff( String hpwd )
    {
        String HEX_VAL = "0123456789abcdef";

        byte[] bpwd = new byte[hpwd.length()>>1];

        byte b = 0;
        boolean high = true;
        int pos = 0;

        for ( char c:hpwd.toCharArray() )
        {
            if ( high )
            {
                high = false;
                b = (byte)HEX_VAL.indexOf( c );
            }
            else
            {
                high = true;
                b <<= 4;
                b += HEX_VAL.indexOf( c );
                bpwd[pos++] = b;
            }
        }

        String b64 = new String( Base64.encode( bpwd ) );
        return b64;
    }

    private String createDn( String cn )
    {
        return "cn=" + cn + "," + suffix;
    }

    private void assertExist( DirContext context, String dn, String attribute, String value ) throws NamingException
    {
        SearchControls ctls = new SearchControls();

        ctls.setDerefLinkFlag( true );
        ctls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
        ctls.setReturningAttributes( new String[] { "*" } );

        BasicAttributes matchingAttributes = new BasicAttributes();
        matchingAttributes.put( attribute, value );
        BasicAttribute objectClass = new BasicAttribute("objectClass");
        objectClass.add("inetOrgPerson");
        matchingAttributes.put(objectClass);

        NamingEnumeration<SearchResult> results = context.search( suffix, matchingAttributes );
        // NamingEnumeration<SearchResult> results = context.search( suffix, "(" + attribute + "=" + value + ")", ctls
        // );

        assertTrue( results.hasMoreElements() );
        SearchResult result = results.nextElement();
        Attributes attrs = result.getAttributes();
        Attribute testAttr = attrs.get( attribute );
        assertEquals( value, testAttr.get() );

    }
   
}

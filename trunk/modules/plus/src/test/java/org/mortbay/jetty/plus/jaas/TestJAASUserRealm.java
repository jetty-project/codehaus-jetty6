// ========================================================================
// $Id$
// Copyright 2003-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.jaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.mortbay.jetty.Request;


/* ---------------------------------------------------- */
/** TestJAASUserRealm
 * <p> Test JAAS in Jetty - relies on the JDBCUserRealm.
 *
 * <p><h4>Notes</h4>
 * <p>
 *
 * <p><h4>Usage</h4>
 * <pre>
 */
/*
 * </pre>
 *
 * @see
 * @version 1.0 Mon Apr 28 2003
 * @author Jan Bartel (janb)
 */
public class TestJAASUserRealm extends TestCase

{
   
    
    public TestJAASUserRealm(String name)
    {
        super (name);
    }

    public static Test suite()
    {
        return new TestSuite(TestJAASUserRealm.class);
    }


    public void setUp ()
	throws Exception
    {
        
    }

    public void testIt ()
        throws Exception
    {

       //set up the properties 
        File propsFile = File.createTempFile("props", null);
        Properties props = new Properties ();
        props.put("user", "user,user,pleb");
        props.store(new FileOutputStream(propsFile), "");
        
        //set up config
        File configFile = File.createTempFile ("loginConf", null);
        PrintWriter writer = new PrintWriter(new FileWriter(configFile));
        writer.println ("props {");
        writer.println ("org.mortbay.jetty.plus.jaas.spi.PropertyFileLoginModule required");     
        writer.println ("debug=\"true\"");
        writer.println ("file=\""+propsFile.getCanonicalPath() +"\";");
        writer.println ("};");
        writer.flush();
        writer.close();
        
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        String s = "";
        for (s = reader.readLine(); (s != null); s = reader.readLine())
        {
            System.out.println (s);
        }
        
        
        //create a login module config file
        System.setProperty ("java.security.auth.login.config", configFile.toURL().toExternalForm());

        //create a JAASUserRealm
        JAASUserRealm realm = new JAASUserRealm ("props");

        realm.setLoginModuleName ("props");

        JAASUserPrincipal userPrincipal = (JAASUserPrincipal)realm.authenticate ("user", "wrong",(Request)null);
        assertNull (userPrincipal);
        
        userPrincipal = (JAASUserPrincipal)realm.authenticate ("user", "user", (Request)null);

        assertNotNull (userPrincipal);
        assertTrue (userPrincipal.getName().equals("user"));

        assertTrue (userPrincipal.isUserInRole("pleb"));
        assertTrue (userPrincipal.isUserInRole("user"));
        assertTrue (!userPrincipal.isUserInRole("other"));       
        

        realm.disassociate (userPrincipal);
        
    }

    public void tearDown ()
        throws Exception
    {
       
    }
    
    
}

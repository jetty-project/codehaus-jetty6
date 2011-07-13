// ========================================================================
// Copyright (c) 2006-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses.
// ========================================================================

package org.mortbay.jetty.spring;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.xml.XmlConfiguration;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpringXmlConfigurationTest
{
    protected String _configure="org/mortbay/jetty/spring/configure.xml";
    
    @Test
    public void testPassedObject() throws Exception
    {
        TestConfiguration.VALUE=77;
        
        URL url = SpringXmlConfigurationTest.class.getClassLoader().getResource(_configure);
        XmlConfiguration configuration = new XmlConfiguration(url);
        
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("test", "xxx");
        
        TestConfiguration nested = new TestConfiguration();
        nested.setTestString0("nested");
        configuration.getIdMap().put("nested",nested);

        TestConfiguration tc = new TestConfiguration();
        tc.setTestString0("preconfig");
        tc.setTestInt0(42);
        configuration.getProperties().putAll(properties);
     
        tc=(TestConfiguration)configuration.configure(tc);

        assertEquals("preconfig",tc.getTestString0());
        assertEquals(42,tc.getTestInt0());
        assertEquals("SetValue",tc.getTestString1());
        assertEquals(1,tc.getTestInt1());
        
        assertEquals("nested",tc.getNested().getTestString0());
        assertEquals("nested",tc.getNested().getTestString1());
        assertEquals("default",tc.getNested().getNested().getTestString0());
        assertEquals("deep",tc.getNested().getNested().getTestString1());
        
        assertEquals("deep",((TestConfiguration)configuration.getIdMap().get("nestedDeep")).getTestString1());
        assertEquals(2,((TestConfiguration)configuration.getIdMap().get("nestedDeep")).getTestInt2());
    
        assertEquals("xxx",tc.getTestString2());
    }
    
    @Test
    public void testNewObject() throws Exception
    {
        TestConfiguration.VALUE=71;
        
        URL url = SpringXmlConfigurationTest.class.getClassLoader().getResource(_configure);
        XmlConfiguration configuration = new XmlConfiguration(url);
        
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("test", "xxx");
        
        TestConfiguration nested = new TestConfiguration();
        nested.setTestString0("nested");
        configuration.getIdMap().put("nested",nested);

        configuration.getProperties().putAll(properties);
        TestConfiguration tc = (TestConfiguration)configuration.configure();

        assertEquals("default",tc.getTestString0());
        assertEquals(-1,tc.getTestInt0());
        assertEquals("SetValue",tc.getTestString1());
        assertEquals(1,tc.getTestInt1());

        assertEquals("nested",tc.getNested().getTestString0());
        assertEquals("nested",tc.getNested().getTestString1());
        assertEquals("default",tc.getNested().getNested().getTestString0());
        assertEquals("deep",tc.getNested().getNested().getTestString1());
        
        assertEquals("deep",((TestConfiguration)configuration.getIdMap().get("nestedDeep")).getTestString1());
        assertEquals(2,((TestConfiguration)configuration.getIdMap().get("nestedDeep")).getTestInt2());
   
        assertEquals("xxx",tc.getTestString2());
    }

    @Test
    public void testJettyXml() throws Exception
    {
        URL url = SpringXmlConfigurationTest.class.getClassLoader().getResource("org/mortbay/jetty/spring/jetty.xml");
        XmlConfiguration configuration = new XmlConfiguration(url);
        
        Server server = (Server)configuration.configure();
        
        server.dumpStdErr();
        
    }
    
    @Test
    public void XmlConfigurationMain() throws Exception
    {
        XmlConfiguration.main(new String[]{"src/test/resources/org/mortbay/jetty/spring/jetty.xml"});
        
    }
}

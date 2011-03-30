package org.mortbay.jetty.tests.policy.integration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.jetty.toolchain.jmx.JmxServiceConnection;
import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Jetty with one webapp.
 */
public class DeployMgrJmxTest
{
    private static JettyProcess jetty;

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyProcess(DeployMgrJmxTest.class);

        jetty.delete("contexts/javadoc.xml");

        jetty.overlayConfig("deploymgr");
        
        jetty.start();
    }

    @AfterClass
    public static void shutdownJetty() throws Exception
    {
        if (jetty != null)
        {
            jetty.stop();
        }
    }

    @Test
    public void testWebAppStop() throws Exception
    {   	
        SimpleRequest request = new SimpleRequest(jetty.getBaseUri());
        String result = request.getString("/test/d.txt");
        assertTrue("Application didn't respond",result.length() > 0);
        
        JmxServiceConnection jmxConnection = new JmxServiceConnection(jetty.getJmxUrl());
        jmxConnection.connect();
        
        MBeanServerConnection mbsConnection = jmxConnection.getConnection();
        ObjectName dmObjName = new ObjectName("org.eclipse.jetty.deploy:type=deploymentmanager,id=0");
        ArrayList<String> apps = (ArrayList<String>)mbsConnection.getAttribute(dmObjName, "apps");
        
        String[] params = new String[] {apps.get(0), "deployed"};
        String[] signature = new String[] {"java.lang.String", "java.lang.String"};
        mbsConnection.invoke(dmObjName, "requestAppGoal", params, signature);

        try
        {
        	result = request.getString("/test/d.txt");
        }
        catch (IOException ex)
        {
        	assertTrue(ex.getMessage().contains("404"));
        }
    }
}

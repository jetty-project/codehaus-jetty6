package org.mortbay.jetty.test.validation;

import java.net.UnknownHostException;

import org.eclipse.jetty.toolchain.test.JettyDistro;
import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FrameworkTest
{
    public static JettyDistro jetty;

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyDistro(FrameworkTest.class);

        // Eliminate Distribution Test & Javadoc Webapps
        jetty.delete("webapps/test.war");
        jetty.delete("contexts/test.d");
        jetty.delete("contexts/javadoc.xml");
        jetty.delete("contexts/test.xml");
        jetty.delete("resources/log4j.properties");
        
        // Overlay config
        jetty.overlayConfig("basic");

        // Copy test war
        jetty.copyTestWar("test-war-remote-assert.war");
        
        jetty.setDebug(true);

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
    public void testExecuteSimpleSuite() throws Exception
    {
        SimpleRequest request = new SimpleRequest(jetty.getBaseUri());

        String result = request.getString("/remote-asserts/tests/");
        System.out.println("Result:\n" + result);
    }
}

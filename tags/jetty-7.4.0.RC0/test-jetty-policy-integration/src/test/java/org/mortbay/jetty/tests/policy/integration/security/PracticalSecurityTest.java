package org.mortbay.jetty.tests.policy.integration.security;

import static org.hamcrest.Matchers.*;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.tests.policy.integration.JettyProcess;

/**
 * Test Jetty with 2 webapps, and no jetty-policy or java security in place.
 */
public class PracticalSecurityTest
{
    private static JettyProcess jetty;
    private static final String MODE = "PRACTICAL";

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyProcess(PracticalSecurityTest.class);

        jetty.copyTestWar("test-war-java_util_logging.war");
        jetty.copyTestWar("test-war-policy.war");

        jetty.delete("webapps/test.war");
        jetty.delete("contexts/test.d");
        jetty.delete("contexts/javadoc.xml");
        jetty.delete("contexts/test.xml");

        jetty.delete("lib/servlet-api-2.5.jar");
        // Use AspectJ + Security enhanced servlet-api jar
        jetty.copyLib("jetty-aspect-servlet-api-2.5.jar","lib-secure/servlet-api-2.5.jar");
        //jetty.copyLib("aspectjrt.jar","lib-secure/aspectjrt.jar");
        jetty.copyLib("aspectjweaver.jar","aspectjweaver.jar");


        jetty.overlayConfig("practical_security");

        jetty.setDebug(true);
        jetty.setJVMArgs(new String[] {"-javaagent:aspectjweaver.jar" });//, "-Daj.weaving.verbose=true", "-Dorg.aspectj.weaver.showWeaveInfo=true"});    

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
    public void testFilesystem() throws Exception
    {        
        execSecurityTest(MODE,"testFilesystemAccess");
    }

    @Test
    public void testJettyLog() throws Exception
    {
        execSecurityTest(MODE, "testJettyLogAccess");
    }

    @Test
    public void testWebappSystemExit() throws Exception
    {
        execSecurityTest(MODE, "testSystemExit");
    }
    
    @Test
    @Ignore ("broken")
    public void testServletContext() throws Exception
    {
        execSecurityTest(MODE, "testFooWebappContext");
    }
    
    @Test
    @Ignore ("broken")
    public void testServletContextAttributes() throws Exception
    {
        execSecurityTest(MODE, "testServletContextAttributes");
    }

    @Test
    @Ignore ("broken")
    public void testRequestDispatcher() throws Exception
    {
        execSecurityTest(MODE, "testFooWebappRequestDispatcher");
    }

    @Test
    @Ignore ("while testing layout")
    public void testLib() throws Exception
    {
        execSecurityTest(MODE, "testLib");
    }

    @Test
    public void testSystemProperty() throws Exception
    {
        execSecurityTest(MODE, "testSystemPropertyAccess");
    }
    
    private void execSecurityTest(String mode, String testName) throws Exception
    {
        SimpleRequest request = new SimpleRequest(jetty.getBaseUri());
        String path = String.format("/test-war-policy/security/%s/%s",mode,testName);
        String response = request.getString(path);
        JSONArray array = new JSONArray(response);

        StringBuilder failures = new StringBuilder();
        int failureCount = 0;

        String id, expected, actual, message;
        int len = array.length();
        for (int i = 0; i < len; i++)
        {
            JSONObject result = array.getJSONObject(i);
            id = result.getString("id");
            if (result.getBoolean("success") == false)
            {
                failureCount++;
                expected = result.optString("expected");
                actual = result.optString("actual");
                message = result.optString("message");
                JSONObject cause = result.optJSONObject("cause");

                failures.append("\n ").append(id);

                if (expected != null)
                    failures.append("\n    expected: ").append(expected);
                if (actual != null)
                    failures.append("\n      actual: ").append(actual);
                if (message != null)
                    failures.append("\n     message: ").append(message);
                if (cause != null && cause.length() > 0)
                {
                    failures.append("\n       cause: ").append(cause.getString("class"));
                    failures.append("\n").append(cause.getString("stacktrace"));
                }
            }
        }

        Assert.assertEquals("Expecting 0 failures:" + failures.toString(),0,failureCount);
    }
}

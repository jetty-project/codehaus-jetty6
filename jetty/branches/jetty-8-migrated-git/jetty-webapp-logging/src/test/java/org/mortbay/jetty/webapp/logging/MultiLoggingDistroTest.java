package org.mortbay.jetty.webapp.logging;

import org.eclipse.jetty.toolchain.test.JettyDistro;
import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiLoggingDistroTest
{
    private static JettyDistro jetty;
    private static final String[] CONTEXTS =
    { "test-war-commons_logging_1.0.3", "test-war-commons_logging_1.1", "test-war-java_util_logging", "test-war-log4j_1.2.15", "test-war-log4j_1.1.3",
            "test-war-slf4j_1.2", "test-war-slf4j_1.5.6", "test-war-slf4j_1.6.1" };

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyDistro(MultiLoggingDistroTest.class);

        // Eliminate Distribution Test & Javadoc Webapps
        jetty.delete("webapps/test.war");
        jetty.delete("contexts/test.d");
        jetty.delete("contexts/javadoc.xml");
        jetty.delete("contexts/test.xml");
        jetty.delete("resources/log4j.properties");

        // Copy Test Wars & Configs
        for (String context : CONTEXTS)
        {
            jetty.copyTestWar(context + ".war");
            jetty.unpackConfig(context + "-config.jar");
        }

        // Copy Slf4j Libs
        jetty.copyLib("slf4j-api.jar","lib/logging/slf4j-api.jar");
        jetty.copyLib("jcl-over-slf4j.jar","lib/logging/jcl-over-slf4j.jar");
        jetty.copyLib("jul-to-slf4j.jar","lib/logging/jul-to-slf4j.jar");
        jetty.copyLib("log4j-over-slf4j.jar","lib/logging/log4j-over-slf4j.jar");

        // Copy Logback Libs
        jetty.copyLib("logback-core.jar","lib/logging/logback-core.jar");
        jetty.copyLib("logback-classic.jar","lib/logging/logback-classic.jar");
        jetty.copyLib("jetty-logback.jar","lib/logging/jetty-logback.jar");

        // Unpack Logback Config
        jetty.unpackConfig("jetty-logback-config.jar");

        // Copy Project Config
        jetty.copyProjectMainConfig();
        jetty.createProjectLib("jetty-webapp-logging.jar");

        // Overlay Manual Config
        jetty.overlayConfig("multi");

        jetty.setDebug(false);

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
    public void testLogging() throws Exception
    {
        SimpleRequest request = new SimpleRequest(jetty.getBaseUri());
        for (String context : CONTEXTS)
        {
            request.getString("/" + context + "/logging");
        }

        LogAssert.assertContainsEntries(jetty, "logs/jetty-central.log", "expected-multi.txt");
    }
}

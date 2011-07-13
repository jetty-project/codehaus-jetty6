package org.mortbay.jetty.webapp.logging;

import org.eclipse.jetty.toolchain.test.JettyDistro;
import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RollingFileLogDistroTest
{
    private static JettyDistro jetty;
    private static final String[] CONTEXTS =
    { "test-war-commons_logging_1.1", "test-war-java_util_logging", "test-war-log4j_1.2.15", "test-war-slf4j_1.5.6" };

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyDistro(RollingFileLogDistroTest.class);

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
        jetty.overlayConfig("rolling");

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
        
        LogAssert.assertNoLogsRegex(jetty, "logs/jetty-roll-20[0-9][0-9]-[01][0-9]-[0-3][0-9]_[012][0-9]-[0-5][0-9].log");

        long now = System.currentTimeMillis();
        long duration = (long)(1000 * 60 * (1.5)); // 1.5 minutes
        long end = now + duration;
        long left;
        while (System.currentTimeMillis() < end)
        {
            left = (end - System.currentTimeMillis());
            System.out.printf("%,d milliseconds left%n",left);
            for (String context : CONTEXTS)
            {
                request.getString("/" + context + "/logging");
            }
            Thread.sleep(Math.min(20000,left)); // every 20s.
        }

        LogAssert.assertLogExistsRegex(jetty, "logs/jetty-roll-20[0-9][0-9]-[01][0-9]-[0-3][0-9]_[012][0-9]-[0-5][0-9].log");
    }
}

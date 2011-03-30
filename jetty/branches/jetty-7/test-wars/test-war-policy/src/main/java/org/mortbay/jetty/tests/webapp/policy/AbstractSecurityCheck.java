package org.mortbay.jetty.tests.webapp.policy;

public abstract class AbstractSecurityCheck
{
    public String getJettyHome()
    {
        String jettyHome = System.getProperty("jetty.home");
        if (jettyHome == null)
        {
            return System.getProperty("user.dir");
        }
        return jettyHome;
    }
}

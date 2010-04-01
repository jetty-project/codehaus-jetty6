package org.mortbay.jetty.spring;


/* ------------------------------------------------------------ */
/**
 * Convenience class for jetty with Spring.
 * This class provides a setBeans method as an alternate
 * access to the {@link #addBean(Object)} API.
 */
public class Server extends org.eclipse.jetty.server.Server
{
    public void setBeans(Object[] beans)
    {
        for (Object o:beans)
            addBean(o);
    }
}

package org.mortbay.jetty.spring;

import java.util.Collection;


/* ------------------------------------------------------------ */
/**
 * Convenience class for jetty with Spring.
 * This class provides a setBeans method as an alternate
 * access to the {@link #addBean(Object)} API.
 */
public class Server extends org.eclipse.jetty.server.Server
{
    public void setBeans(Collection<Object> beans)
    {
        for (Object o:beans)
            addBean(o);
    }
    
}

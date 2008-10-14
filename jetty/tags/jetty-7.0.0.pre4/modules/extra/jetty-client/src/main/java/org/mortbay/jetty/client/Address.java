package org.mortbay.jetty.client;

import java.net.InetSocketAddress;

/**
 * @version $Revision$ $Date$
 */
public class Address
{
    private final String host;
    private final int port;

    public Address(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Address that = (Address)obj;
        if (port != that.port) return false;
        if (!host.equals(that.host)) return false;
        return true;
    }

    public int hashCode()
    {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public InetSocketAddress toSocketAddress()
    {
        return new InetSocketAddress(getHost(), getPort());
    }
}

package org.mortbay.jetty.tests.webapp.policy;

public enum SecurityCheckMode
{
    NO_SECURITY, PRACTICAL, PARANOID;

    public static SecurityCheckMode getMode(String securityMode)
    {
        if (securityMode == null)
        {
            return null;
        }

        for (SecurityCheckMode mode : values())
        {
            if (securityMode.equalsIgnoreCase(mode.name()))
            {
                return mode;
            }
        }
        return null;
    }
}

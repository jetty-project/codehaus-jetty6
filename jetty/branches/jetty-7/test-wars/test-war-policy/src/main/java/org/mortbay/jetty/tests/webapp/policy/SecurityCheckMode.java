package org.mortbay.jetty.tests.webapp.policy;

public enum SecurityCheckMode
{
    /** 
     * Expectation that there no java security or <code>jetty-policy</code> present in jetty 
     */
    NO_SECURITY,

    /**
     * Expectation that java security and <code>jetty-policy</code> is enabled, but specific useful grants have been
     * given, simulating a practical configuration of <code>jetty-policy</code> and the various java security
     * <code>*.policy</code> files
     */
    PRACTICAL,

    /** 
     * Expectation that java security and <code>jetty-policy</code> is enabled, but no grants have been given 
     */
    PARANOID;

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

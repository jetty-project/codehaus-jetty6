package org.mortbay.jetty.tests.webapp.policy;

public enum SecurityCheckMode
{
    /**
     * Expectation that there no java security or <code>jetty-policy</code> present in jetty.
     * <p>
     * This should accurately represent a basic, unadorned, jetty distribution with no java security in place.
     */
    NO_SECURITY,

    /**
     * Expectation that java security and <code>jetty-policy</code> is enabled, and specific useful grants have been
     * given via the <code>*.policy</code> files.
     * <p>
     * This should accurately represent a practical configuration of <code>jetty-policy</code> and the various java
     * security <code>*.policy</code> files
     */
    PRACTICAL,

    /**
     * Expectation that java security and <code>jetty-policy</code> is enabled, but no grants have been given.
     * <p>
     * This mode represents a jetty installation which has java security enabled, but no <code>*.policy</code> grants
     * have been provided. This mode could represent either a mis-configured jetty-policy, or an intentionally and
     * severly locked down jetty installation.
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

package org.mortbay.jetty.tests.webapp.policy;

import java.util.Comparator;

public class SecurityCheckModeNameSorter implements Comparator<SecurityCheckMode>
{
    public int compare(SecurityCheckMode o1, SecurityCheckMode o2)
    {
        return o1.name().compareTo(o2.name());
    }
}

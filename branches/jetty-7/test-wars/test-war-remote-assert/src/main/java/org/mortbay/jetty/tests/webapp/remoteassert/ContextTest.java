package org.mortbay.jetty.tests.webapp.remoteassert;

import org.junit.Rule;
import org.junit.Test;
import org.mortbay.jetty.test.validation.junit.ServletRequestContextRule;

public class ContextTest
{
    @Rule
    public ServletRequestContextRule context = new ServletRequestContextRule();
    
    @Test
    public void testContextPath() {
        
    }
}

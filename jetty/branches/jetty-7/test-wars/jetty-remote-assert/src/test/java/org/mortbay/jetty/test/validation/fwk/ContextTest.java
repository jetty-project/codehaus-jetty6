package org.mortbay.jetty.test.validation.fwk;

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

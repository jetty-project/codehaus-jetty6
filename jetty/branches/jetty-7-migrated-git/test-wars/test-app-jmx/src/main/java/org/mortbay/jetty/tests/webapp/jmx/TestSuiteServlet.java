package org.mortbay.jetty.tests.webapp.jmx;

import org.mortbay.jetty.test.validation.RemoteAssertServlet;
import org.mortbay.jetty.tests.webapp.jmx.tests.JmxAttributeTest;

public class TestSuiteServlet extends RemoteAssertServlet
{

    private static final long serialVersionUID = 0L;

    public TestSuiteServlet()
    {
        addTestClass(JmxAttributeTest.class);
    }   
    
}

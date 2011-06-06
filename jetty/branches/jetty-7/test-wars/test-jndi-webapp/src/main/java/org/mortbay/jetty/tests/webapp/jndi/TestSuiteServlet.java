package org.mortbay.jetty.tests.webapp.jndi;

import org.mortbay.jetty.test.remote.RemoteTestSuiteServlet;
import org.mortbay.jetty.tests.webapp.jndi.tests.JndiTest;

public class TestSuiteServlet extends RemoteTestSuiteServlet
{

    /**
     * 
     */
    private static final long serialVersionUID = 4925094851959842028L;

    /**
     * 
     */
    public TestSuiteServlet()
    {
        addTestClass(JndiTest.class);
    } 
    
}

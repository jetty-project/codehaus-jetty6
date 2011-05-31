package org.mortbay.jetty.tests.webapp.download;

import javax.servlet.ServletException;

import org.mortbay.jetty.test.validation.RemoteAssertServlet;
import org.mortbay.jetty.tests.webapp.download.tests.DownloadTest;

public class TestSuiteServlet extends RemoteAssertServlet
{

    /**
     * 
     */
    private static final long serialVersionUID = -2123042227141302602L;

    public TestSuiteServlet()
    {
        addTestClass(DownloadTest.class);
    }   
    
}

package org.mortbay.jetty.tests.webapp.download;

import javax.servlet.ServletException;

import org.mortbay.jetty.test.validation.RemoteAssertServlet;

public class ValidationServlet extends RemoteAssertServlet
{

    /**
     * 
     */
    private static final long serialVersionUID = -2123042227141302602L;

    public ValidationServlet()
    {
        addTestClass(DownloadTest.class);
    }

    

    
    
}

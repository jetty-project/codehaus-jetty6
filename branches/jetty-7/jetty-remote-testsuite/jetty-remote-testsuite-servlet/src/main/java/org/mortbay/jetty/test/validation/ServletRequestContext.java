package org.mortbay.jetty.test.validation;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletRequestContext
{
    /** The servlet for the test run */
    private HttpServlet servlet;
    /** The incoming request object for the test run */
    private HttpServletRequest request;
    /** The outgoing response object for the test run */
    private HttpServletResponse response;

    public ServletRequestContext(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
    {
        super();
        this.servlet = servlet;
        this.request = request;
        this.response = response;
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public HttpServletResponse getResponse()
    {
        return response;
    }

    public HttpServlet getServlet()
    {
        return servlet;
    }
}

package org.mortbay.jetty.test.validation.junit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Provides some basics about the servlet + request + response context for executing the test
 */
public class ServletRequestContextRule implements MethodRule
{
    /** The servlet for the test run */
    private HttpServlet servlet;
    /** The incoming request object for the test run */
    private HttpServletRequest request;
    /** The outgoing response object for the test run */
    private HttpServletResponse response;

    public ServletRequestContextRule()
    {
        // anonymous creation (to be populated later)
    }

    public Statement apply(final Statement statement, FrameworkMethod method, Object target)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                statement.evaluate();
            }
        };
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

    public void setRequest(HttpServletRequest request)
    {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response)
    {
        this.response = response;
    }

    public void setServlet(HttpServlet servlet)
    {
        this.servlet = servlet;
    }
}

package org.mortbay.jetty.test.validation.junit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mortbay.jetty.test.validation.ServletRequestContext;
import org.mortbay.jetty.test.validation.ThreadLocalServletRequestContext;

/**
 * Provides some basics about the servlet + request + response context for executing the test
 */
public class ServletRequestContextRule implements MethodRule
{
    private ServletRequestContext context;

    public Statement apply(final Statement statement, FrameworkMethod method, Object target)
    {
        this.context = ThreadLocalServletRequestContext.get();
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
        return context.getRequest();
    }

    public HttpServletResponse getResponse()
    {
        return context.getResponse();
    }

    public HttpServlet getServlet()
    {
        return context.getServlet();
    }
}

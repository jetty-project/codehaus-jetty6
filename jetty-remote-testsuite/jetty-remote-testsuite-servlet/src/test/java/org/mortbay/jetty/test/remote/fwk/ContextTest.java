package org.mortbay.jetty.test.remote.fwk;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mortbay.jetty.test.remote.junit.ServletRequestContextRule;

public class ContextTest
{
    @Rule
    public ServletRequestContextRule context = new ServletRequestContextRule();

    @Test
    public void testHasRequest()
    {
        Assert.assertThat("Context.request",context.getRequest(),not(nullValue()));
    }

    @Test
    public void testHasResponse()
    {
        Assert.assertThat("Context.response",context.getResponse(),not(nullValue()));
    }

    @Test
    public void testHasServlet()
    {
        Assert.assertThat("Context.servlet",context.getServlet(),not(nullValue()));
    }
}

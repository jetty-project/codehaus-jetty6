package org.mortbay.jetty.test.validation;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Assert;
import org.junit.Test;

public class TestScopeTest
{
    private void assertTestScope(String pathinfo, String expectedClassName, String expectedMethodName)
    {
        TestScope scope = new TestScope(pathinfo);

        Assert.assertThat("Expected class name",scope.getClassName(),is(expectedClassName));
        Assert.assertThat("Expected method name",scope.getMethodName(),is(expectedMethodName));
    }

    @Test
    public void testEmptyPathInfo()
    {
        assertTestScope("", null, null);
    }

    @Test
    public void testNullPathInfo()
    {
        assertTestScope(null,null,null);
    }

    @Test
    public void testSimplePathInfo()
    {
        assertTestScope("/simple","simple",null);
    }

    @Test
    public void testSimpleSlashPathInfo()
    {
        assertTestScope("/simple/","simple",null);
    }

    @Test
    public void testSimpleTestMePathInfo()
    {
        assertTestScope("/simple/testMe","simple","testMe");
    }

    @Test
    public void testSimpleTestMeSlashPathInfo()
    {
        assertTestScope("/simple/testMe/","simple","testMe");
    }

    @Test
    public void testSlashPathInfo()
    {
        assertTestScope("/",null,null);
    }
}

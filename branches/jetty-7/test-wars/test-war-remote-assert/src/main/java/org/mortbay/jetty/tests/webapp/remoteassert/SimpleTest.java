package org.mortbay.jetty.tests.webapp.remoteassert;

import org.junit.Assert;
import org.junit.Test;

public class SimpleTest
{
    @Test
    public void testQuoteEqualsSuccess()
    {
        Assert.assertEquals("Quote","Woah","Woah");
    }

    @Test
    public void testQuoteEqualsFailure()
    {
        Assert.assertEquals("Quote","Sweet","Dude");
    }

    @Test
    public void testNumberEqualsSuccess()
    {
        Assert.assertEquals("Year",2011,2011);
    }

    @Test
    public void testNumberEqualsFailure()
    {
        Assert.assertEquals("Year",2011,1995);
    }
}

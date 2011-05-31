package org.mortbay.jetty.test.validation.fwk;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class SimpleTest
{
    @Test
    public void testNumberEqualsFailure()
    {
        Assert.assertEquals("Year",2011,1995);
    }

    @Test
    public void testNumberEqualsSuccess()
    {
        Assert.assertEquals("Year",2011,2011);
    }

    @Test
    @Ignore("Skipping this test intentionally")
    public void testNumgerIgnored()
    {
        Assert.assertEquals("Year",2011,1995);
    }

    @Test
    public void testQuoteEqualsFailure()
    {
        Assert.assertEquals("Quote","Sweet","Dude");
    }

    @Test
    public void testQuoteEqualsSuccess()
    {
        Assert.assertEquals("Quote","Woah","Woah");
    }
}

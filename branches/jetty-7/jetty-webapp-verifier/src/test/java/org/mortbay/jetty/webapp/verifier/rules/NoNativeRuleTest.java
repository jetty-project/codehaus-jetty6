package org.mortbay.jetty.webapp.verifier.rules;

import junit.framework.TestCase;

import org.junit.Test;
import org.mortbay.jetty.webapp.verifier.RuleAssert;

public class NoNativeRuleTest extends TestCase
{
	@Test
    public void testNoNative() throws Exception
    {
        RuleAssert.assertIntegration("no_native");
    }
}

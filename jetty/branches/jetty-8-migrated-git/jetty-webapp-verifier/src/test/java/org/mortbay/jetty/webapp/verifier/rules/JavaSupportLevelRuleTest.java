package org.mortbay.jetty.webapp.verifier.rules;

import junit.framework.TestCase;

import org.junit.Test;
import org.mortbay.jetty.webapp.verifier.RuleAssert;

public class JavaSupportLevelRuleTest extends TestCase
{
	@Test
    public void testJava15() throws Exception
    {
        RuleAssert.assertIntegration("java_level_1.5");
    }

	@Test
    public void testJava14() throws Exception
    {
        RuleAssert.assertIntegration("java_level_1.4");
    }
}

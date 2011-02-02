// ========================================================================
// Copyright (c) Webtide LLC
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// The Apache License v2.0 is available at
// http://www.apache.org/licenses/LICENSE-2.0.txt
//
// You may elect to redistribute this code under either of these licenses.
// ========================================================================
package org.mortbay.jetty.webapp.verifier;

import org.junit.Assert;
import org.junit.Test;
import org.mortbay.jetty.webapp.verifier.Rule;
import org.mortbay.jetty.webapp.verifier.RuleSet;
import org.mortbay.jetty.webapp.verifier.rules.ForbiddenContentsRule;
import org.mortbay.jetty.webapp.verifier.rules.RequiredContentsRule;

public class RuleSetTest extends AbstractTestWebappVerifier
{
	@Test
    public void testLoad() throws Exception
    {
        RuleSet suite = loadRuleSet("basic-ruleset.xml");
        Assert.assertNotNull("Should have a valid RuleSet.",suite);

        Assert.assertNotNull("verifier list should not be null",suite.getRules());
        Assert.assertEquals("Should have 2 verifier",2,suite.getRules().size());

        Rule verifier = suite.getRules().get(0);
        Assert.assertEquals("Verifier[0]",ForbiddenContentsRule.class.getName(),verifier.getClass().getName());
        verifier = suite.getRules().get(1);
        Assert.assertEquals("Verifier[1]",RequiredContentsRule.class.getName(),verifier.getClass().getName());
    }
}

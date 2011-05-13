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
package org.mortbay.jetty.webapp.verifier.rules;

import org.junit.Test;
import org.mortbay.jetty.webapp.verifier.AbstractTestWebappVerifier;
import org.mortbay.jetty.webapp.verifier.RuleAssert;

public class NoScriptingRuleTest extends AbstractTestWebappVerifier
{
	@Test
    public void testJRubyConfiguration() throws Exception
    {
        RuleAssert.assertIntegration("no_scripting_jruby");
    }

	@Test
    public void testJythonConfiguration() throws Exception
    {
        RuleAssert.assertIntegration("no_scripting_jython");
    }

	@Test
    public void testGroovyConfiguration() throws Exception
    {
        RuleAssert.assertIntegration("no_scripting_groovy");
    }

	@Test
    public void testShellConfiguration() throws Exception
    {
        RuleAssert.assertIntegration("no_scripting_shell");
    }
}

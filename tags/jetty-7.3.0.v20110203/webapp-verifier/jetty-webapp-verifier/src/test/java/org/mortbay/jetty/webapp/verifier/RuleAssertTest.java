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

public class RuleAssertTest
{
	@Test
    public void testParseLine()
    {
        Violation v;

        v = RuleAssert.parseViolation("ERROR|/WEB-INF|Missing web.xml");

        Assert.assertNotNull("Verifier should not be null",v);
        Assert.assertEquals("Verifier.severity",Severity.ERROR,v.getSeverity());
        Assert.assertEquals("Verifier.path","/WEB-INF",v.getPath());
        Assert.assertEquals("Verifier.detail","Missing web.xml",v.getDetail());
    }
}

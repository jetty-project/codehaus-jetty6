// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================
package org.mortbay.jetty.webapp.verifier.rules;

import java.io.File;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.TestingDir;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.webapp.verifier.AbstractTestWebappVerifier;
import org.mortbay.jetty.webapp.verifier.Violation;
import org.mortbay.jetty.webapp.verifier.WebappVerifier;

/**
 * Tests against {@link JarSignatureRule}
 */
public class JarSignatureRuleTest extends AbstractTestWebappVerifier
{
    @org.junit.Rule
    public TestingDir testingdir = new TestingDir();

    @Test
    @Ignore("Certificate Issue")
    public void testSimpleVerify() throws Exception
    {
        testingdir.ensureEmpty();
        JarSignatureRule signed = new JarSignatureRule();

        // Create Webapp Specific Verifier from Verifier Suite
        File testwar = MavenTestingUtils.getTargetFile("test-classes/webapps/signed-jar-test-webapp.war");
        WebappVerifier verifier = new WebappVerifier(testwar.toURI());
        verifier.addRule(signed);
        verifier.setWorkDir(testingdir.getDir());

        // Run the verification.
        verifier.visitAll();

        for (Violation violation : verifier.getViolations())
        {
            System.out.println(violation);
            if (violation.getThrowable() != null)
            {
                violation.getThrowable().printStackTrace(System.out);
            }
        }

        Assert.assertNotNull("Violations should not be null",verifier.getViolations());
        Assert.assertEquals("Should have no violations",0,verifier.getViolations().size());
    }
}

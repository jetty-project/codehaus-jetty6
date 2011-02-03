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

import java.util.Collection;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.TestingDir;
import org.junit.Assert;
import org.junit.Test;

public class WebappVerifierTest extends AbstractTestWebappVerifier
{
	@org.junit.Rule
	public TestingDir testingdir = new TestingDir();

	@Test
    public void testVerifierVisitation() throws Exception
    {
        CountingRule counts = new CountingRule();

        // Create Webapp Specific Verifier from Verifier Suite
        WebappVerifier verifier = new WebappVerifier(MavenTestingUtils.getTargetFile("test-classes/webapps/simple-webapp.war").toURI());
        verifier.addRule(counts);
        verifier.setWorkDir(testingdir.getEmptyDir());

        // Run the verification.
        verifier.visitAll();

        // Collect the violations
        Collection<Violation> violations = verifier.getViolations();
        Assert.assertNotNull("Should never have a null set of Violations",violations);
        for (Violation v : violations)
        {
            System.out.println(v);
        }
        Assert.assertEquals("No violations caused",0,violations.size());

        // Ensure each visitor was visited according to real contents of WAR
        Assert.assertEquals("Counts.webappStart",1,counts.countWebappStart);
        Assert.assertEquals("Counts.countWebappEnd",1,counts.countWebappEnd);

        // Visits in Directory
        Assert.assertEquals("Counts.countDirStart",12,counts.countDirStart);
        Assert.assertEquals("Counts.countFile",6,counts.countFile);
        Assert.assertEquals("Counts.countDirEnd",12,counts.countDirEnd);
        Assert.assertEquals("Counts.countDir (Start == End)",counts.countDirStart,counts.countDirEnd);

        // Visits in WEB-INF/classes
        Assert.assertEquals("Counts.countWebInfClassesStart",1,counts.countWebInfClassesStart);
        Assert.assertEquals("Counts.countWebInfClass",1,counts.countWebInfClass);
        Assert.assertEquals("Counts.countWebInfClassResource",1,counts.countWebInfClassResource);
        Assert.assertEquals("Counts.countWebInfClassesEnd",1,counts.countWebInfClassesEnd);
        Assert.assertEquals("Counts.countWebInfClasses (Start == End)",counts.countWebInfClassesStart,counts.countWebInfClassesEnd);

        // Visits in WEB-INF/lib
        Assert.assertEquals("Counts.countWebInfLibStart",0,counts.countWebInfLibStart);
        Assert.assertEquals("Counts.countWebInfLibJar",0,counts.countWebInfLibJar);
        Assert.assertEquals("Counts.countWebInfLibZip",0,counts.countWebInfLibZip);
        Assert.assertEquals("Counts.countWebInfLibEnd",0,counts.countWebInfLibEnd);
        Assert.assertEquals("Counts.countWebInfLib (Start == End)",counts.countWebInfLibStart,counts.countWebInfLibEnd);
    }
}

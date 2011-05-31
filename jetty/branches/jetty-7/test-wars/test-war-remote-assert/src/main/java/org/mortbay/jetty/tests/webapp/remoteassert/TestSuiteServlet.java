//========================================================================
//Copyright (c) Webtide LLC
//------------------------------------------------------------------------
//All rights reserved. This program and the accompanying materials
//are made available under the terms of the Eclipse Public License v1.0
//and Apache License v2.0 which accompanies this distribution.
//The Eclipse Public License is available at 
//http://www.eclipse.org/legal/epl-v10.html
//The Apache License v2.0 is available at
//http://www.opensource.org/licenses/apache2.0.php
//You may elect to redistribute this code under either of these licenses. 
//========================================================================
package org.mortbay.jetty.tests.webapp.remoteassert;

import org.mortbay.jetty.test.validation.RemoteAssertServlet;

public class TestSuiteServlet extends RemoteAssertServlet
{
    private static final long serialVersionUID = -6644178549595265171L;

    public TestSuiteServlet()
    {
        addTestClass(SimpleTest.class);
        addTestClass(ContextTest.class);
    }
}

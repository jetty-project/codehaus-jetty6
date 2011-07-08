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
package org.mortbay.jetty.test.remote;

import org.mortbay.jetty.test.remote.RemoteTestSuiteServlet;
import org.mortbay.jetty.test.remote.fwk.ContextTest;
import org.mortbay.jetty.test.remote.fwk.SimpleTest;

public class BasicTestSuiteServlet extends RemoteTestSuiteServlet
{
    private static final long serialVersionUID = -6644178549595265171L;

    public BasicTestSuiteServlet()
    {
        addTestClass(SimpleTest.class);
        addTestClass(ContextTest.class);
    }
}

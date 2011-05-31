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
package org.mortbay.jetty.test.validation;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.test.validation.fwk.ContextTest;
import org.mortbay.jetty.test.validation.fwk.SimpleTest;

public class BasicTestSuiteServlet extends RemoteAssertServlet
{
    private static final long serialVersionUID = -6644178549595265171L;

    public BasicTestSuiteServlet()
    {
        System.out.println("Created " + this.getClass().getName());
        addTestClass(SimpleTest.class);
        addTestClass(ContextTest.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        System.out.println("SERVICE " + this.getClass().getName());
        super.service(req,resp);
    }
}

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
package org.mortbay.jetty.webapp.logging;

import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * WebAppContext {@link Configuration} for Centralized Logging.
 */
public class CentralizedWebAppLoggingConfiguration extends AbstractConfiguration implements Configuration
{
    public void configure(WebAppContext context) throws Exception
    {
        context.addSystemClass("org.apache.log4j.");
        context.addSystemClass("org.slf4j.");
        context.addSystemClass("org.apache.commons.logging.");
    }
}

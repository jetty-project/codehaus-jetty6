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

package org.mortbay.jetty.tests.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Servlet that tweaks the existing configuration.
 */
public class LoggingConfigServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String LOGID = LoggingConstants.LOGID;
    private Logger log = Logger.getLogger(LoggingConfigServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Attempt to reconfigure the level of the root logger
        Logger root = Logger.getRootLogger();
        root.setLevel(Level.WARN);
        log.info(LOGID + " Set level to WARN");
        log.warn(LOGID + " Set level to WARN");
        
        // Attempt to add a new console appender
        Layout layout = new PatternLayout("#CONFIGURED# %r [%t] %p %c %x - %m%n");
        ConsoleAppender appender = new ConsoleAppender(layout);
        log.addAppender(appender);

        log.info(LOGID + " Added ConsoleAppender");
        log.warn(LOGID + " Added ConsoleAppender");
    }
}

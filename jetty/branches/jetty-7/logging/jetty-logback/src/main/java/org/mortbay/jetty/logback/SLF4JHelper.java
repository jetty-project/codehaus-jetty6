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
package org.mortbay.jetty.logback;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Helper functions for working with Slf4j logging.
 */
public class SLF4JHelper
{
    /**
     * Establish the SLF4JBridgeHandler as the only handler for java.util.logging
     */
    public static void establishJavaUtilLoggingBridge()
    {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();

        // Remove all previous handlers
        for (int i = 0; i < handlers.length; i++)
        {
            rootLogger.removeHandler(handlers[i]);
        }

        // Install jul to slf4j bridge
        SLF4JBridgeHandler.install();
    }
}

/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2006 Mort Bay Consulting Pty. Ltd.
 * ------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package org.mortbay.jesper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version $Revision$
 */
public class JesperLog
{
    private static final Logger LOG = Logger.getLogger("jesper");

    public static void info(String message)
    {
        LOG.log(Level.INFO, message);
    }

    public static void error(String message)
    {
        LOG.log(Level.SEVERE, message);
    }
    
    public static void error(String message, Throwable cause)
    {
        LOG.log(Level.SEVERE, message, cause);
    }

    public static void warn(String message)
    {
        LOG.log(Level.WARNING, message);
    }
}

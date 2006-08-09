// ========================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package com.sun.org.apache.commons.logging;

import java.net.URLClassLoader;


/**
 * LogFactory
 *
 * Bridges com.sun.org.apache.commons.logging.LogFactory to
 * Jetty's log.
 *
 */
public class LogFactory
{
    private static final Log log = new Log();
    
    public static Log getLog (Class c)
    {
        return log;
    }
    
    public static void release (URLClassLoader cl)
    {
        //noop;
    }
}

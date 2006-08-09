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

import java.util.logging.Level;



/**
 * Log
 * 
 * Bridges the com.sun.org.apache.commons.logging.Log to Jetty's log.
 *
 **/
public class Log 
{
    public static void fatal (Object message)
    {
        org.mortbay.log.Log.warn(message.toString());
    }
    
    public static void fatal (Object message, Throwable t)
    {
        org.mortbay.log.Log.warn(message.toString(), t);
    }
    
    public static void debug(Object message)
    {
        org.mortbay.log.Log.debug(message.toString());
    }
    
    public static void debug (Object message, Throwable t)
    {
        org.mortbay.log.Log.debug(message.toString(), t);
    }
    
    public static void trace (Object message)
    {
        org.mortbay.log.Log.debug(message.toString());
    }
    
  
    public static void info(Object message)
    {
       org.mortbay.log.Log.info(message.toString());
    }

    public static void error(Object message)
    {
       org.mortbay.log.Log.warn(message.toString());
    }
    
    public static void error(Object message, Throwable cause)
    {
        org.mortbay.log.Log.warn(message.toString(), cause);
    }

    public static void warn(Object message)
    {
        org.mortbay.log.Log.warn(message.toString());
    }
    
    public static boolean isDebugEnabled ()
    {
        return org.mortbay.log.Log.isDebugEnabled();
    }
    
    public static boolean isWarnEnabled ()
    {
        return org.mortbay.log.Log.isDebugEnabled();
    }
    
    public static boolean isInfoEnabled ()
    {
        return true;
    }
    
    
    public static boolean isErrorEnabled ()
    {
        return true;
    }
    
  
    public static boolean isTraceEnabled ()
    {
        return org.mortbay.log.Log.isDebugEnabled();
    }
    
}

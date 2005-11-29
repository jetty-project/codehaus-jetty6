// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
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

package org.mortbay.log;

/*-----------------------------------------------------------------------*/
/** Logging.
 * This class provides a static logging interface.  If an instance of the 
 * org.slf4j.Logger class is found on the classpath, the static log methods
 * are directed to a slf4j logger for "org.mortbay.log".   Otherwise the logs
 * are directed to stderr.
 * 
 * If the system property VERBOSE is set, then ignored exceptions are logged in detail.
 * If the system property DEBUG is set, then debug logs are printed if stderr is being used.
 * 
 */
public class StdErrLog implements Logger
{    
    private static boolean debug = System.getProperty("DEBUG",null)!=null;
    private String name;
    
    StdErrLog()
    {
        this(null);
    }
    
    StdErrLog(String name)
    {    
        this.name=name==null?"":name;
    }
    
    public boolean isDebugEnabled()
    {
        return debug;
    }
    
    public void info(String msg,Object arg0, Object arg1)
    {
        System.err.println(name+":INFO:  "+format(msg,arg0,arg1));
    }
    
    public void debug(String msg,Throwable th)
    {
        if (debug)
        {
            System.err.println(name+":DEBUG: "+msg);
            th.printStackTrace();
        }
    }
    
    public void debug(String msg,Object arg0, Object arg1)
    {
        if (debug)
        {
            System.err.println(name+":DEBUG: "+format(msg,arg0,arg1));
        }
    }
    
    public void warn(String msg,Object arg0, Object arg1)
    {
        System.err.println(name+":WARN:  "+format(msg,arg0,arg1));
    }
    
    public void warn(String msg, Throwable th)
    {
        System.err.println(name+":WARN:  "+msg);
        th.printStackTrace();
    }

    private String format(String msg, Object arg0, Object arg1)
    {
        int i0=msg.indexOf("{}");
        int i1=i0<0?-1:msg.indexOf("{}",i0+2);
        
        if (arg1!=null && i1>=0)
            msg=msg.substring(0,i1)+arg1+msg.substring(i1+2);
        if (arg0!=null && i0>=0)
            msg=msg.substring(0,i0)+arg0+msg.substring(i0+2);
        return msg;
    }
    
    public Logger getLogger(String name)
    {
        if ((name==null && this.name==null) ||
            (name!=null && name.equals(this.name)))
            return this;
        return new StdErrLog(name);
    }
    
    public String toString()
    {
        return "STDERR"+name;
    }

}


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

package org.mortbay.jetty.util.log;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.mortbay.jetty.util.Loader;



/*-----------------------------------------------------------------------*/
/** Logging.
 * This class provides a static logging interface.  If an instance of the 
 * org.slf4j.Logger class is found on the classpath, the static log methods
 * are directed to a slf4j logger for "org.mortbay.log".   Otherwise the logs
 * are directed to stderr.
 * 
 * If the system property VERBOSE is set, then ignored exceptions are logged in detail.
 * 
 */
public class Log 
{    
    private static final String[] __nestedEx =
        {"getTargetException","getTargetError","getException","getRootCause"};
    /*-------------------------------------------------------------------*/
    private static final Class[] __noArgs=new Class[0];
    public final static String EXCEPTION= "EXCEPTION ";
    public final static String IGNORED= "IGNORED";
    public final static String IGNORED_FMT= "IGNORED: {}";
    public final static String NOT_IMPLEMENTED= "NOT IMPLEMENTED ";
    
    public static String __logClass;
    public static boolean __verbose;
    public static boolean __ignored;
    
    static
    {
        AccessController.doPrivileged(new PrivilegedAction<Boolean>() 
            {
                public Boolean run() 
                { 
                    __logClass = System.getProperty("org.mortbay.log.class","org.mortbay.log.Slf4jLog"); 
                    __verbose = System.getProperty("VERBOSE",null)!=null; 
                    __ignored = System.getProperty("IGNORED",null)!=null; return true; 
                }
            });
    }
       
    private static Logger __log;
    private static boolean _initialized;
    
    public static boolean initialized()
    {
        if (__log!=null)
            return true;
        
        synchronized (Log.class)
        {
            if (_initialized)
                return __log!=null;
            _initialized=true;
        }
        
        Class log_class=null;
        try
        {
            log_class=Loader.loadClass(Log.class, __logClass);
            if (__log==null || !__log.getClass().equals(log_class))
            {
                __log=(Logger) log_class.newInstance();
                __log.info("Logging to {} via {}",__log,log_class.getName());
            }
        }
        catch(NoClassDefFoundError e)
        {
            initStandardLogging(e);
        }
        catch(Exception e)
        {
            initStandardLogging(e);
        }

        return __log!=null;
    }

    private static void initStandardLogging(Throwable e) {
        Class log_class;
        if (__log==null)
        {
            log_class= StdErrLog.class;
            __log=new StdErrLog();
            __log.info("Logging to {} via {}",__log,log_class.getName());
            if(e != null && __verbose)
                e.printStackTrace();
        }
    }

    public static void setLog(Logger log)
    {
        Log.__log=log;
    }
    
    public static Logger getLog()
    {
        initialized();
        return __log;
    }

    
    /**
     * Set Log to parent Logger.
     * <p>
     * If there is a different Log class available from a parent classloader,
     * call {@link #getLogger(String)} on it and construct a {@link LoggerLog} instance
     * as this Log's Logger, so that logging is delegated to the parent Log.
     * <p>
     * This should be used if a webapp is using Log, but wishes the logging to be 
     * directed to the containers log.
     * <p>
     * If there is not parent Log, then this call is equivalent to<pre>
     *   Log.setLog(Log.getLogger(name));
     * </pre> 
     * @param name Logger name
     */
    public static void setLogToParent(String name)
    {
        ClassLoader loader = Log.class.getClassLoader();
        if (loader.getParent()!=null)
        {
            try
            {
                Class<?> uberlog = loader.getParent().loadClass("org.mortbay.log.Log");
                Method getLogger=uberlog.getMethod("getLogger",new Class[]{String.class});
                Object logger = getLogger.invoke(null,name);
                setLog(new LoggerLog(logger));
                return;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }     
        }
            
        setLog(getLogger(name));
    }
    
    public static void debug(Throwable th)
    {        
        if (!isDebugEnabled())
            return;
        __log.debug(EXCEPTION,th);
        unwind(th);
    }

    public static void debug(String msg)
    {
        if (!initialized())
            return;
        
        __log.debug(msg,null,null);
    }
    
    public static void debug(String msg,Object arg)
    {
        if (!initialized())
            return;
        __log.debug(msg,arg,null);
    }
    
    public static void debug(String msg,Object arg0, Object arg1)
    {
        if (!initialized())
            return;
        __log.debug(msg,arg0,arg1);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Ignore an exception unless trace is enabled.
     * This works around the problem that log4j does not support the trace level.
     */
    public static void ignore(Throwable th)
    {
        if (!initialized())
            return;
        if (__ignored)
        {
            __log.warn(IGNORED,th);
            unwind(th);
        }
        else if (__verbose)
        {
            __log.warn(IGNORED,th);
            unwind(th);
        }
    }
    
    public static void info(String msg)
    {
        if (!initialized())
            return;
        __log.info(msg,null,null);
    }
    
    public static void info(String msg,Object arg)
    {
        if (!initialized())
            return;
        __log.info(msg,arg,null);
    }
    
    public static void info(String msg,Object arg0, Object arg1)
    {
        if (!initialized())
            return;
        __log.info(msg,arg0,arg1);
    }
    
    public static boolean isDebugEnabled()
    {
        if (!initialized())
            return false;
        return __log.isDebugEnabled();
    }
    
    public static void warn(String msg)
    {
        if (!initialized())
            return;
        __log.warn(msg,null,null);
    }
    
    public static void warn(String msg,Object arg)
    {
        if (!initialized())
            return;
        __log.warn(msg,arg,null);        
    }
    
    public static void warn(String msg,Object arg0, Object arg1)
    {
        if (!initialized())
            return;
        __log.warn(msg,arg0,arg1);        
    }
    
    public static void warn(String msg, Throwable th)
    {
        if (!initialized())
            return;
        __log.warn(msg,th);
        unwind(th);
    }

    public static void warn(Throwable th)
    {
        if (!initialized())
            return;
        __log.warn(EXCEPTION,th);
        unwind(th);
    }

    /** Obtain a named Logger.
     * Obtain a named Logger or the default Logger if null is passed.
     */
    public static Logger getLogger(String name)
    {
        if (!initialized())
            return null;
        
        if (name==null)
          return __log;
        return __log.getLogger(name);
    }

    private static void unwind(Throwable th)
    {
        if (th==null)
            return;
        for (int i=0;i<__nestedEx.length;i++)
        {
            try
            {
                Method get_target = th.getClass().getMethod(__nestedEx[i],__noArgs);
                Throwable th2=(Throwable)get_target.invoke(th,(Object[])null);
                if (th2!=null && th2!=th)
                    warn("Nested in "+th+":",th2);
            }
            catch(Exception ignore){}
        }
    }
    

}


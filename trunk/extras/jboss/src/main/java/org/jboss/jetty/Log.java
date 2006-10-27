//========================================================================
//$Id: Log.java,v 1.1 2004/10/07 22:51:27 janb Exp $
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.jboss.jetty;

import org.apache.log4j.Level;
import org.jboss.logging.Log4jLoggerPlugin;
import org.jboss.logging.Logger;
import org.jboss.logging.XLevel;


/**
 * 
 * Log
 * Implementation of the commons-logging Log interface that delegates
 * to the JBoss logger, which itself delegates to log4j. 
 * 
 * This is necessary because the standard commons-logging log4j wrappers
 * report TRUE for isTraceEnabled() if DEBUG level is enabled, which means
 * that all of Jetty's TRACE level messages will appear in the log. JBoss's
 * log4J wrappers do, however support a proper TRACE level, so this
 * wrapper gives access to them. 
 *
 * @author janb
 * @version $Revision: 1.1 $ $Date: 2004/10/07 22:51:27 $
 *
 */
public class Log implements org.apache.commons.logging.Log
{
    private Logger delegate = null;
    
    
    
    
    public Log (String name)
    {
        delegate = Logger.getLogger(name);
    }
    
    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled ()
    {
        return delegate.isDebugEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled ()
    {
        org.apache.log4j.Logger log4JLogger = ((org.apache.log4j.Logger)((Log4jLoggerPlugin)delegate.getLoggerPlugin()).getLogger());
        boolean enabled = log4JLogger.isEnabledFor(Level.ERROR);
        if (!enabled)
            return enabled;
        return Level.ERROR.isGreaterOrEqual(log4JLogger.getEffectiveLevel());
    }
    
    /**
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled ()
    {
        org.apache.log4j.Logger log4JLogger = ((org.apache.log4j.Logger)((Log4jLoggerPlugin)delegate.getLoggerPlugin()).getLogger());
        boolean enabled = log4JLogger.isEnabledFor(Level.FATAL);
        if (!enabled)
            return enabled;
        return Level.FATAL.isGreaterOrEqual(log4JLogger.getEffectiveLevel());
    }
    
    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled ()
    {
        return delegate.isInfoEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled ()
    {
        return delegate.isTraceEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled ()
    {
        org.apache.log4j.Logger log4JLogger = ((org.apache.log4j.Logger)((Log4jLoggerPlugin)delegate.getLoggerPlugin()).getLogger());
        boolean enabled = log4JLogger.isEnabledFor(Level.WARN);
        if (!enabled)
            return enabled;
        return Level.WARN.isGreaterOrEqual(log4JLogger.getEffectiveLevel());

    }
    
    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace (Object arg0)
    {
        delegate.trace(arg0);
    }
    
    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace (Object arg0, Throwable arg1)
    {
        delegate.trace(arg0,arg1);   
    }
    
    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug (Object arg0)
    {
        delegate.debug(arg0);
        
    }
    
    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug (Object arg0, Throwable arg1)
    {
        delegate.debug(arg0, arg1);
        
    }
    
    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info (Object arg0)
    {
        delegate.info(arg0);
        
    }
    
    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public void info (Object arg0, Throwable arg1)
    {
        delegate.info(arg0, arg1);
        
    }
    
    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn (Object arg0)
    {
        delegate.warn(arg0);
        
    }
    
    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn (Object arg0, Throwable arg1)
    {
        delegate.warn(arg0, arg1);      
    }
    
    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error (Object arg0)
    {
        delegate.error(arg0);
        
    }
    
    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public void error (Object arg0, Throwable arg1)
    {
        delegate.error(arg0, arg1);
        
    }
    
    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal (Object arg0)
    {
        delegate.fatal (arg0);
    }
    
    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal (Object arg0, Throwable arg1)
    {
        delegate.fatal(arg0, arg1);
    }
    
}

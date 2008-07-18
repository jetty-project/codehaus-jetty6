package org.mortbay.log;

import java.lang.reflect.Method;

public class LoggerLog implements Logger
{
    boolean _debug;
    Object _logger;
    Method _debugMT;
    Method _debugMAA;
    Method _infoMAA;
    Method _warnMT;
    Method _warnMAA;
    Method _isDebugEnabled;
    Method _setDebugEnabledE;
    Method _getLoggerN;
    
    public LoggerLog(Object logger)
    {
        try
        {
            _logger=logger;
            Class<?> lc=logger.getClass();
            _debugMT=lc.getMethod("debug",new Class[]{String.class,Throwable.class});
            _debugMAA=lc.getMethod("debug",new Class[]{String.class,Object.class,Object.class});
            _infoMAA=lc.getMethod("info",new Class[]{String.class,Object.class,Object.class});
            _warnMT=lc.getMethod("warn",new Class[]{String.class,Throwable.class});
            _warnMAA=lc.getMethod("warn",new Class[]{String.class,Object.class,Object.class});
            _isDebugEnabled=lc.getMethod("isDebugEnabled",new Class[]{});
            _setDebugEnabledE=lc.getMethod("setDebugEnabled",new Class[]{Boolean.TYPE});
            _getLoggerN=lc.getMethod("getLogger",new Class[]{String.class});
            
            _debug=((Boolean)_isDebugEnabled.invoke(_logger,(Object[])null)).booleanValue();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public void debug(String msg, Throwable th)
    {
        if (_debug)
        {
            try
            {
                _debugMT.invoke(_logger,msg,th);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void debug(String msg, Object arg0, Object arg1)
    {
        if (_debug)
        {
            try
            {
                _debugMAA.invoke(_logger,msg,arg0,arg1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public Logger getLogger(String name)
    {
        try
        {
            Object logger=_getLoggerN.invoke(_logger,name);
            return new LoggerLog(logger);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return this;
    }

    public void info(String msg, Object arg0, Object arg1)
    {
        try
        {
            _infoMAA.invoke(_logger,msg,arg0,arg1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isDebugEnabled()
    {
        return _debug;
    }

    public void setDebugEnabled(boolean enabled)
    {
        try
        {
            _setDebugEnabledE.invoke(_logger,enabled);
            _debug=enabled;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }

    public void warn(String msg, Object arg0, Object arg1)
    {
        try
        {
            _warnMAA.invoke(_logger,msg,arg0,arg1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void warn(String msg, Throwable th)
    {
        try
        {
            _warnMT.invoke(_logger,msg,th);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}

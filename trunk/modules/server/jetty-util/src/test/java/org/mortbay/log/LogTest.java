package org.mortbay.log;

import junit.framework.TestCase;

public class LogTest extends TestCase
{
    public void testLoggerLog()
    {
        Logger log=new LoggerLog(Log.getLogger("test"));
        log.setDebugEnabled(true);
        log.debug("testing {} {}","LoggerLog","debug");
        log.info("testing {} {}","LoggerLog","info");
        log.warn("testing {} {}","LoggerLog","warn");
        log.setDebugEnabled(false);
        log.debug("YOU SHOULD NOT SEE THIS!",null,null);
        
        log=log.getLogger("next");
        log.info("testing {} {}","LoggerLog","info");
    }
}

//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.util.log;

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

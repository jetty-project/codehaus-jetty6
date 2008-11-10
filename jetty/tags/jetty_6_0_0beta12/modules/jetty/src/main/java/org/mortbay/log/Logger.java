//========================================================================
//$Id: Logger.java,v 1.1 2005/11/14 16:55:09 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.log;

public interface Logger
{
    public boolean isDebugEnabled();
    public void info(String msg,Object arg0, Object arg1);
    public void debug(String msg,Throwable th);
    public void debug(String msg,Object arg0, Object arg1);
    public void warn(String msg,Object arg0, Object arg1);
    public void warn(String msg, Throwable th);
    public Logger getLogger(String name);
}
//========================================================================
//$Id: PojoContextListenerTest.java 3363 2008-07-22 13:40:59Z janb $
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

package com.acme;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.annotation.ServletContextListener;

@ServletContextListener()
public class PojoContextListenerTest
{
    
    public void contextInitialized (ServletContextEvent e)
    {
        e.getServletContext().setAttribute("contextInitialized", Boolean.TRUE);
    }
    
    public void contextDestroyed (ServletContextEvent e)
    {
        e.getServletContext().setAttribute("contextDestroyed", Boolean.TRUE);
    }
}

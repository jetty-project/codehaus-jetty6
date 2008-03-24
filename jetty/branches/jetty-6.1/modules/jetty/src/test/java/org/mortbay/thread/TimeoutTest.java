//========================================================================
//$Id: TimeoutTest.java,v 1.1 2005/10/05 14:09:42 janb Exp $
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

package org.mortbay.thread;

import junit.framework.TestCase;

public class TimeoutTest extends TestCase
{
    Timeout timeout = new Timeout();
    Timeout.Task[] tasks;

    /* ------------------------------------------------------------ */
    /* 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        timeout=new Timeout();
        timeout.setDuration(1000000);
        tasks= new Timeout.Task[10]; 
        
        for (int i=0;i<tasks.length;i++)
        {
            tasks[i]=new Timeout.Task();
            timeout.setNow(1000+i*100);
            timeout.schedule(tasks[i]);
        }
        timeout.setNow(100);
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    

    /* ------------------------------------------------------------ */
    public void testExpiry()
    {
        timeout.setDuration(200);
        timeout.setNow(1500);
        timeout.tick();
        
        for (int i=0;i<tasks.length;i++)
        {
            assertEquals("isExpired "+i,i<4, tasks[i].isExpired());
        }
    }

    /* ------------------------------------------------------------ */
    public void testCancel()
    {
        timeout.setDuration(200);
        timeout.setNow(1700);

        for (int i=0;i<tasks.length;i++)
            if (i%2==1)
                tasks[i].cancel();

        timeout.tick();
        
        for (int i=0;i<tasks.length;i++)
        {
            assertEquals("isExpired "+i,i%2==0 && i<6, tasks[i].isExpired());
        }
    }

    /* ------------------------------------------------------------ */
    public void testTouch()
    {
        timeout.setDuration(200);
        timeout.setNow(1350);
        tasks[2].reschedule();
        
        
        timeout.setNow(1500);
        timeout.tick();
        for (int i=0;i<tasks.length;i++)
        {
            assertEquals("isExpired "+i,i!=2 && i<4, tasks[i].isExpired());
        }
        
        timeout.setNow(1550);
        timeout.tick();
        for (int i=0;i<tasks.length;i++)
        {
            assertEquals("isExpired "+i, i<4, tasks[i].isExpired());
        }  
    }


    /* ------------------------------------------------------------ */
    public void testDelay()
    {
        Timeout.Task task = new Timeout.Task();
        timeout.cancelAll();
        timeout.setDuration(200);

        timeout.setNow(100);
        timeout.schedule(task);
        assertEquals("delay", false, task.isExpired());
        timeout.setNow(200);
        timeout.tick();
        assertEquals("delay", false, task.isExpired());
        timeout.setNow(400);
        timeout.tick();
        assertEquals("delay", true, task.isExpired());
        


        timeout.setNow(500);
        timeout.schedule(task, 100);
        
        timeout.setNow(550);
        timeout.tick();
        assertEquals("delay", false, task.isExpired());
        
        timeout.setNow(650);
        timeout.tick();
        assertEquals("delay", true, task.isExpired());
        
        

        timeout.setNow(1100);
        timeout.schedule(task, 300);
        
        timeout.setNow(1350);
        timeout.tick();
        assertEquals("delay", false, task.isExpired());
        
        timeout.setNow(1450);
        timeout.tick();
        assertEquals("delay", true, task.isExpired());
    }

}

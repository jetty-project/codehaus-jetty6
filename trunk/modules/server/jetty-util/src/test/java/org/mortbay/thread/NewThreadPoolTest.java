//========================================================================
//Copyright 2004-2009 Mort Bay Consulting Pty. Ltd.
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

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

public class NewThreadPoolTest extends TestCase
{
    final AtomicInteger _jobs=new AtomicInteger();
    
    class Job implements Runnable
    {
        public volatile boolean _running=true;
        public void run()
        {
            try 
            {
                while(_running)
                    Thread.sleep(100);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
      
            _jobs.incrementAndGet();
        }
    };    
    
    public void testThreadPool() throws Exception
    {        
        NewQueuedThreadPool tp= new NewQueuedThreadPool();
        tp.setMinThreads(5);
        tp.setMaxThreads(10);
        tp.setMaxIdleTimeMs(500);
        tp.setThreadsPriority(Thread.NORM_PRIORITY-1);

        tp.start();
        assertEquals(5,tp.getThreads());
        assertEquals(5,tp.getIdleThreads());
        Thread.sleep(1000);
        assertEquals(5,tp.getThreads());
        assertEquals(5,tp.getIdleThreads());
        
        Job job=new Job();
        tp.dispatch(job);
        Thread.sleep(200);
        assertEquals(5,tp.getThreads());
        assertEquals(4,tp.getIdleThreads());
        job._running=false;
        Thread.sleep(200);
        assertEquals(5,tp.getThreads());
        assertEquals(5,tp.getIdleThreads());

        Job[] jobs = new Job[5];
        for (int i=0;i<jobs.length;i++)
        {
            jobs[i]=new Job();
            tp.dispatch(jobs[i]);
        }
        Thread.sleep(200);
        assertEquals(5,tp.getThreads());
        Thread.sleep(1000);
        assertEquals(5,tp.getThreads());
        
        job=new Job();
        tp.dispatch(job);
        assertEquals(6,tp.getThreads());
        
        job._running=false;
        Thread.sleep(1000);
        assertEquals(5,tp.getThreads());
        
        jobs[0]._running=false;
        Thread.sleep(1000);

        assertEquals(5,tp.getThreads());
        assertEquals(1,tp.getIdleThreads());
        
        for (int i=1;i<jobs.length;i++)
            jobs[i]._running=false;

        Thread.sleep(1000);

        assertEquals(5,tp.getThreads());
        
        
        jobs = new Job[15];
        for (int i=0;i<jobs.length;i++)
        {
            jobs[i]=new Job();
            tp.dispatch(jobs[i]);
        }
        assertEquals(10,tp.getThreads());
        Thread.sleep(100);
        assertEquals(0,tp.getIdleThreads());

        for (int i=0;i<9;i++)
            jobs[i]._running=false;
        Thread.sleep(1000);

        assertTrue(tp.getThreads()<10);
        int threads=tp.getThreads();
        Thread.sleep(1000);
        assertTrue(tp.getThreads()<threads);
        threads=tp.getThreads();
        Thread.sleep(1000);
        assertTrue(tp.getThreads()<threads);
        
        for (int i=9;i<jobs.length;i++)
            jobs[i]._running=false;
        Thread.sleep(1000);

        tp.stop();
    }
    

    public void testMaxStopTime() throws Exception
    {
        NewQueuedThreadPool tp= new NewQueuedThreadPool();
        tp.setMaxStopTimeMs(500);
        tp.start();
        tp.dispatch(new Runnable(){
            public void run () {
                while (true) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {}
                }
            }
        });

        long beforeStop = System.currentTimeMillis();
        tp.stop();
        long afterStop = System.currentTimeMillis();
        assertTrue(tp.isStopped());
        assertTrue(afterStop - beforeStop < 1000);
    }


}

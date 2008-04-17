package org.mortbay.thread;

import junit.framework.TestCase;

public class ThreadPoolTest extends TestCase
{
    int _jobs;
    long _result;
    
    Runnable _job = new Runnable()
    {
        public void run()
        {
            long t = System.currentTimeMillis()%1000;
            long r=t;
            for (int i=0;i<t;i++)
                r+=i;
                
            synchronized(ThreadPoolTest.class)
            {
                _jobs++;
                _result+=r;
            }
        }
    };
    
    
    public void testQueuedThreadPool() throws Exception
    {
        QueuedThreadPool tp= new QueuedThreadPool();
        tp.setMinThreads(5);
        tp.setMaxThreads(10);
        tp.setMaxIdleTimeMs(1000);
        tp.setSpawnOrShrinkAt(2);
        tp.setThreadsPriority(Thread.NORM_PRIORITY-1);
        
        tp.start();
        
        assertEquals(5,tp.getThreads());

	/* TODO Find a none timer based way to test this.
	 * or at least less fragile timers
        Thread.sleep(200);
        assertEquals(5,tp.getIdleThreads());
        tp.dispatch(_job);
        Thread.sleep(200);
        tp.dispatch(_job);
        Thread.sleep(200);
        assertEquals(5,tp.getThreads());
        assertEquals(5,tp.getIdleThreads());
        
        for (int i=0;i<1000;i++)
            tp.dispatch(_job);
        Thread.sleep(200);
        assertEquals(0,tp.getQueueSize());
        assertTrue(tp.getIdleThreads()>5);
        int threads=tp.getThreads();
        assertTrue(threads>5);
        Thread.sleep(1100);
        assertTrue(tp.getThreads()<threads);
        assertTrue(tp.getThreads()>5);
	*/
    }
}

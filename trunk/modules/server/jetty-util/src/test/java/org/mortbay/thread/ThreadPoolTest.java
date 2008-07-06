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
            try 
            {
                Thread.sleep(100);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            
            long t = System.currentTimeMillis()%10000;
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
        assertEquals(5,tp.getIdleThreads());
        tp.dispatch(_job);
        tp.dispatch(_job);
        assertEquals(5,tp.getThreads());
        assertEquals(3,tp.getIdleThreads());
        Thread.sleep(500);
        assertEquals(5,tp.getThreads());
        assertEquals(5,tp.getIdleThreads());
        
        for (int i=0;i<100;i++)
            tp.dispatch(_job);

        
        assertTrue(tp.getQueueSize()>10);
        assertTrue(tp.getIdleThreads()<=1);

        Thread.sleep(2000);

        assertEquals(0,tp.getQueueSize());
        assertTrue(tp.getIdleThreads()>5);
        
        int threads=tp.getThreads();
        assertTrue(threads>5);
        Thread.sleep(1100);
        assertTrue(tp.getThreads()<threads);
    }
}

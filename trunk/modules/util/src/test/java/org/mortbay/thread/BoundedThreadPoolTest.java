package org.mortbay.thread;

import junit.framework.TestCase;

public class BoundedThreadPoolTest extends TestCase
{
    volatile long _50jobs;
    volatile long _number;
    
    public void testPool() throws Exception
    {
        BoundedThreadPool pool = new BoundedThreadPool();
        pool.setDaemon(false);
        pool.setMinThreads(10);
        pool.setMaxThreads(100);
        pool.setMaxIdleTimeMs(1000);
        pool.setLowThreads(80);
        
        pool.start();
        Thread.sleep(100);
        
        assertEquals(10,pool.getThreads());
        assertEquals(10,pool.getIdleThreads());

        for (int i=0;i<15;i++)
            pool.dispatch(new Job500());

        assertEquals(15,pool.getThreads());
        assertEquals(0,pool.getIdleThreads());
        
        Thread.sleep(700);

        assertEquals(14,pool.getThreads());
        assertEquals(14,pool.getIdleThreads());
        
        Thread.sleep(1300);

        assertEquals(13,pool.getThreads());
        assertEquals(13,pool.getIdleThreads());
        
        for (int i=0;i<25;i++)
            pool.dispatch(new ForeverJob());
        
        assertEquals(25,pool.getThreads());
        assertEquals(0,pool.getIdleThreads());

        Thread.sleep(1100);

        assertEquals(25,pool.getThreads());
        assertEquals(0,pool.getIdleThreads());

        long latency=0;
        for (int i=0;i<50;i++)
        {
            long start=System.currentTimeMillis();
            pool.dispatch(new TrivialJob());
            long duration = System.currentTimeMillis()-start;
            if (duration>latency)
                latency=duration;
        }
        
        for (int i=0;i<200;i++)
        {
            long start=System.currentTimeMillis();
            pool.dispatch(new Job500());
            long duration = System.currentTimeMillis()-start;
            if (duration>latency)
                latency=duration;
        }
        assertTrue(latency<50);
        assertTrue(pool.getQueueSize()>100);
        Thread.sleep(1000);
        assertTrue(pool.getQueueSize()<100);
        
        Thread.sleep(100);
        assertTrue(pool.getThreads()>=99);
        
 
        pool.stop();
        
        assertTrue(_number!=0);
    }


    public void testStress() throws Exception
    {
        BoundedThreadPool pool = new BoundedThreadPool();
        pool.setDaemon(false);
        pool.setMinThreads(10);
        pool.setMaxThreads(100);
        pool.setMaxIdleTimeMs(1000);
        pool.setLowThreads(80);
        
        pool.start();
        Thread.sleep(100);
        
        assertEquals(10,pool.getThreads());
        assertEquals(10,pool.getIdleThreads());

        _50jobs=0;
        for (int i=0;i<1000;i++)
            pool.dispatch(new BreederJob(pool));
        
        while(pool.getQueueSize()>0)
            Thread.sleep(100);
        
        while(pool.getIdleThreads()<pool.getThreads())
            Thread.sleep(100);
        
        assertEquals(10000,_50jobs);
        pool.stop();
        
    }
    

    public class TrivialJob implements Runnable
    {
        public void run()
        {
            for(int i=0;i<10;i++)
            {
                _number+=i;
            }
        }
    }
    
    
    public class BreederJob implements Runnable
    {
        ThreadPool _pool;
        BreederJob(ThreadPool pool)
        {
            _pool=pool;
        }
        
        public void run()
        {
            try
            {
                for(int i=0;i<10;i++)
                {
                    _number+=i;
                    Thread.sleep(i*10);
                    _pool.dispatch(new Job5());
                }
            }
            catch (InterruptedException e)
            {}
        }
    }
    
    
    public class Job500 implements Runnable
    {
        public void run()
        {
            try
            {
                for(int i=0;i<10;i++)
                {
                    Thread.sleep(50);
                    _number+=i;
                }
            }
            catch (InterruptedException e)
            {}
        }
    }
    
    public class Job5 implements Runnable
    {
        public void run()
        {
            try
            {
                Thread.sleep(5);
                _number++;
                synchronized(BoundedThreadPoolTest.class)
                {
                    _50jobs++;
                }
            }
            catch (InterruptedException e)
            {}
        }
    }
    
    public class ForeverJob implements Runnable
    {
        public void run()
        {
            try
            {
                while (true)
                {
                    Thread.sleep(250);

                    _number++;
                }
            }
            catch (InterruptedException e)
            {}
        }
    }
    
}

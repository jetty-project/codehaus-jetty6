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

package org.mortbay.util;

import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

public class BlockingArrayQueueTest extends TestCase
{
    
    public void testWrap() throws Exception
    {
        BlockingArrayQueue<String> queue = new BlockingArrayQueue<String>(3);
        
        assertEquals(0,queue.size());

        for (int i=0;i<3;i++)
        {
            queue.offer("one");
            assertEquals(1,queue.size());

            queue.offer("two");
            assertEquals(2,queue.size());

            queue.offer("three");
            assertEquals(3,queue.size());

            assertEquals("one",queue.get(0));
            assertEquals("two",queue.get(1));
            assertEquals("three",queue.get(2));

            assertEquals("[one, two, three]",queue.toString());

            assertEquals("one",queue.poll());
            assertEquals(2,queue.size());

            assertEquals("two",queue.poll());
            assertEquals(1,queue.size());

            assertEquals("three",queue.poll());
            assertEquals(0,queue.size());


            queue.offer("xxx");
            assertEquals(1,queue.size());
            assertEquals("xxx",queue.poll());
            assertEquals(0,queue.size());

        }

    }

    public void testRemove() throws Exception
    {
        BlockingArrayQueue<String> queue = new BlockingArrayQueue<String>(3,3);
       
        queue.add("0");
        queue.add("x");
        
        for (int i=1;i<100;i++)
        {
            queue.add(""+i);
            queue.add("x");
            queue.remove(queue.size()-3);
            queue.set(queue.size()-3,queue.get(queue.size()-3)+"!");
        }
        
        for (int i=0;i<99;i++)
            assertEquals(i+"!",queue.get(i));
    }

    public void testGrow() throws Exception
    {
        BlockingArrayQueue<String> queue = new BlockingArrayQueue<String>(3,5);
        assertEquals(3,queue.getCapacity());
        
        queue.add("a");
        queue.add("b");
        assertEquals(3,queue.getCapacity());
        queue.add("c");
        queue.add("d");
        assertEquals(8,queue.getCapacity());
        
        for (int i=0;i<4;i++)
            queue.add(""+('d'+i));
        assertEquals(8,queue.getCapacity());
        for (int i=0;i<4;i++)
            queue.poll();
        assertEquals(8,queue.getCapacity());
        for (int i=0;i<4;i++)
            queue.add(""+('d'+i));
        assertEquals(8,queue.getCapacity());
        for (int i=0;i<4;i++)
            queue.poll();
        assertEquals(8,queue.getCapacity());
        for (int i=0;i<4;i++)
            queue.add(""+('d'+i));
        assertEquals(8,queue.getCapacity());

        queue.add("z");
        assertEquals(13,queue.getCapacity());
        
        queue.clear();
        assertEquals(13,queue.getCapacity());
        for (int i=0;i<12;i++)
            queue.add(""+('a'+i));
        assertEquals(13,queue.getCapacity());
        queue.clear();
        assertEquals(13,queue.getCapacity());
        for (int i=0;i<12;i++)
            queue.add(""+('a'+i));
        assertEquals(13,queue.getCapacity());
        
        
    }
    
    public void testTake() throws Exception
    {
        final String[] data=new String[4];

        final BlockingArrayQueue<String> queue = new BlockingArrayQueue<String>();
        
        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    data[0]=queue.take();
                    data[1]=queue.take();
                    Thread.sleep(1000);
                    data[2]=queue.take();
                    data[3]=queue.poll(100,TimeUnit.MILLISECONDS);
                }
                catch(Exception e)
                {
                    assertTrue(false);
                    e.printStackTrace();
                }
            }
        };
        
        thread.start();
        
        Thread.sleep(1000);

        queue.offer("zero");
        queue.offer("one");
        queue.offer("two");
        thread.join();

        assertEquals("zero",data[0]);
        assertEquals("one",data[1]);
        assertEquals("two",data[2]);
        assertEquals(null,data[3]);
        
    }
    
    volatile boolean _running;
    
    public void testConcurrentAccess() throws Exception
    {
        final int THREADS=50;
        final int LOOPS=500;

        final BlockingArrayQueue<Integer> queue = new BlockingArrayQueue<Integer>(1+THREADS*LOOPS);
        final AtomicInteger[] count = new AtomicInteger[THREADS];
        for (int i=0;i<count.length;i++)
            count[i]=new AtomicInteger();

        _running=true;
        
        // start consumers
        final CyclicBarrier barrier0 = new CyclicBarrier(count.length+1);
        for (int i=0;i<count.length;i++)
        {
            final Integer id = new Integer(i);
            new Thread()
            {
                public void run()
                {
                    setPriority(getPriority()-1);
                    try
                    {
                        while(_running)
                        {
                            Integer i=queue.poll();
                            if (i==null)
                            {
                                Thread.sleep(10*THREADS);
                                continue;
                            }

                            count[i].incrementAndGet();
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            barrier0.await();
                        }
                        catch (Exception e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        // start producers
        final AtomicInteger offers = new AtomicInteger();
        final CyclicBarrier barrier1 = new CyclicBarrier(count.length+1);
        for (int i=0;i<count.length;i++)
        {
            final Integer id = new Integer(i);
            new Thread()
            {
                public void run()
                {
                    try
                    {
                        for (int j=0;j<LOOPS;j++)
                        {
                            if (!queue.offer(id))
                                throw new Exception(id+" FULL! "+queue.size()+" "+offers.get());
                            offers.incrementAndGet();
                            Thread.sleep(5);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            barrier1.await();
                        }
                        catch (Exception e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
        
        barrier1.await();
        int size=queue.size();
        int last=size-1;
        while (size>0 && size!=last)
        {
            last=size;
            Thread.sleep(500);
            size=queue.size();
        }   
        _running=false;
        barrier0.await();
        for (int i=0;i<count.length;i++)
            assertEquals("loop "+i,LOOPS,count[i].get());
        
        
        
    }
}

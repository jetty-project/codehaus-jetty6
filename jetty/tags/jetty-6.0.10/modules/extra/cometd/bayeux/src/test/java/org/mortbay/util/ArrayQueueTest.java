package org.mortbay.util;

import junit.framework.TestCase;

public class ArrayQueueTest extends TestCase
{
    
    public void testQueue() throws Exception
    {
        ArrayQueue<String> queue = new ArrayQueue<String>(3);
        
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

}

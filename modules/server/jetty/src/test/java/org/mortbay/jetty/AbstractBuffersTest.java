package org.mortbay.jetty;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;

public class AbstractBuffersTest
    extends TestCase
{
    private int _headerBufferSize = 6 * 1024;

    InnerAbstractBuffers buffers;

    List<Thread> threadList = new ArrayList<Thread>();

    int numThreads = 200;

    int runTestLength = 20000;

    int threadWaitTime = 5;

    boolean runTest = false;

    Double buffersRetrieved = new Double( 0 );

    private static int __LOCAL = 1;
    private static int __LIST = 2;
    private static int __QUEUE = 3;

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }
    
    public void testNothing() throws Exception
    {
        
    }
/*
    public void testAbstractQueueBuffers()
        throws Exception
    {
        threadList.clear();
        buffers = new InnerAbstractBuffers( __QUEUE );

        for ( int i = 0; i < numThreads; ++i )
        {
            threadList.add( new BufferPeeper( "QueuedBufferPeeper: " + i ) );
        }

        runTest = true;
        long currentTime = System.currentTimeMillis();
        long startGet = buffers.getBufferGetTime();
        Thread.sleep( runTestLength );
        runTest = false;

        long testTime = System.currentTimeMillis() - currentTime;
        long totalBufferGetTime = buffers.getBufferGetTime() - startGet;
        double totalBuffersRetrieved = buffersRetrieved.doubleValue();

        System.out.println( "Queue Buffer Time in getBuffer: " + totalBufferGetTime );

        System.out.println( "Buffers Retrieved: " + totalBuffersRetrieved );
        System.out.println( "Test Time: " + testTime );

        for ( Iterator<Thread> i = threadList.iterator(); i.hasNext(); )
        {
            Thread t = i.next();
            t.stop();
        }
    }

    public void testListAbstractBuffers()
        throws Exception
    {
        buffers = new InnerAbstractBuffers( __LIST );
        for ( int i = 0; i < numThreads; ++i )
        {
            threadList.add( new BufferPeeper( "BufferPeeper: " + i ) );
        }

        runTest = true;
        long currentTime = System.currentTimeMillis();
        long startGet = buffers.getBufferGetTime();
        Thread.sleep( runTestLength );
        runTest = false;

        long testTime = System.currentTimeMillis() - currentTime;
        long totalBufferGetTime = buffers.getBufferGetTime() - startGet;
        double totalBuffersRetrieved = buffersRetrieved.doubleValue();

        System.out.println( "List Buffer Time in getBuffer: " + totalBufferGetTime );

        System.out.println( "Buffers Retrieved: " + totalBuffersRetrieved );
        System.out.println( "Test Time: " + testTime );

        for ( Iterator<Thread> i = threadList.iterator(); i.hasNext(); )
        {
            Thread t = i.next();
            t.stop();
        }
    }

    public void testThreadLocalAbstractBuffers()
        throws Exception
    {
        buffers = new InnerAbstractBuffers( __LOCAL );
        for ( int i = 0; i < numThreads; ++i )
        {
            threadList.add( new BufferPeeper( "BufferPeeper: " + i ) );
        }

        runTest = true;
        long currentTime = System.currentTimeMillis();
        long startGet = buffers.getBufferGetTime();
        Thread.sleep( runTestLength );
        runTest = false;

        long testTime = System.currentTimeMillis() - currentTime;
        long totalBufferGetTime = buffers.getBufferGetTime() - startGet;
        double totalBuffersRetrieved = buffersRetrieved.doubleValue();

        System.out.println( "Thread Local Buffer Time in getBuffer: " + totalBufferGetTime );

        System.out.println( "Buffers Retrieved: " + totalBuffersRetrieved );
        System.out.println( "Test Time: " + testTime );

        for ( Iterator<Thread> i = threadList.iterator(); i.hasNext(); )
        {
            Thread t = i.next();
            t.stop();
        }
    }
*/
    /**
     * wrapper for testing different types of AbstractBuffers
     * 
     * @author jesse
     *
     */
    private class InnerAbstractBuffers
    {
        AbstractBuffers abuf;

        long bufferGetTime = 0;

        public InnerAbstractBuffers( int type )
        {
            if ( type == __LIST )
            {
                abuf = new InnerListAbstractBuffers();
            }
            else if ( type == __QUEUE )
            {
                abuf = new InnerQueueAbstractBuffers();
            }
            else if ( type == __LOCAL )
            {
                abuf = new InnerThreadLocalAbstractBuffers();
            }

            if ( abuf == null )
            {
                throw new IllegalArgumentException( "failed to init buffers" );
            }
        }

        public Buffer getBuffer( int size )
        {
            long time = System.currentTimeMillis();
            Buffer b = abuf.getBuffer( size );
            synchronized ( this )
            {
                bufferGetTime = bufferGetTime + System.currentTimeMillis() - time;
            }
            return b;
        }

        public void returnBuffer( Buffer buffer )
        {
            abuf.returnBuffer( buffer );
        }

        public void clearBufferGetTime()
        {
            synchronized ( this )
            {
                bufferGetTime = 0;
            }
        }

        public long getBufferGetTime()
        {
            return bufferGetTime;
        }

    }

    class InnerListAbstractBuffers
        extends ListAbstractBuffers
    {

        public Buffer newBuffer( int size )
        {
            return new ByteArrayBuffer( size );
        }

    }

    class InnerQueueAbstractBuffers
        extends QueueAbstractBuffers
    {

        public Buffer newBuffer( int size )
        {
            return new ByteArrayBuffer( size );
        }

    }

    class InnerThreadLocalAbstractBuffers
        extends ThreadLocalAbstractBuffers
    {

        public Buffer newBuffer( int size )
        {
            return new ByteArrayBuffer( size );
        }

    }

    /**
     * generic buffer peeper
     * 
     * @author jesse
     */
    class BufferPeeper
        extends Thread
    {
        private String _bufferName;

        public BufferPeeper( String bufferName )
        {
            _bufferName = bufferName;

            start();
        }

        public void run()
        {
            while ( true )
            {
                try
                {

                    if ( runTest )
                    {
                        Buffer buf = buffers.getBuffer( _headerBufferSize );

                        synchronized ( buffersRetrieved )
                        {
                            ++buffersRetrieved;
                        }

                        buf.put( new Byte( "2" ).byteValue() );

                        // sleep( threadWaitTime );

                        buffers.returnBuffer( buf );
                    }
                    else
                    {
                        sleep( 1 );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

}

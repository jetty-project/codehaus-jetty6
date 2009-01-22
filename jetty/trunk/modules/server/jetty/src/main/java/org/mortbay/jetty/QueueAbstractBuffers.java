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

package org.mortbay.jetty;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;

/* ------------------------------------------------------------ */
/**
 * Abstract Buffer pool. simple unbounded pool of buffers based on a queue for storing buffers to reduce contention when
 * the pool is getting hit hard
 * 
 * @author gregw
 */
public abstract class QueueAbstractBuffers
    extends AbstractBuffers
{
    class AbstractBuffersQueue extends ConcurrentLinkedQueue<Buffer> {}

    protected transient int _loss;

    private transient AbstractBuffersQueue _headerBuffers;
    private transient AbstractBuffersQueue _requestBuffers;
    private transient AbstractBuffersQueue _responseBuffers;

    public QueueAbstractBuffers()
    {
        super();
    }

    /* ------------------------------------------------------------ */
    public Buffer getBuffer( int size )
    {
        if ( size == _headerBufferSize )
        {
            Buffer ret = _headerBuffers.poll();
            return ret == null ? newBuffer( size ) : ret;
        }
        else if ( size == _responseBufferSize )
        {
            Buffer ret = _responseBuffers.poll();
            return ret == null ? newBuffer( size ) : ret;
        }
        else if ( size == _requestBufferSize )
        {
            Buffer ret = _requestBuffers.poll();
            return ret == null ? newBuffer( size ) : ret;
        }

        return newBuffer( size );
    }

    /* ------------------------------------------------------------ */
    public void returnBuffer( Buffer buffer )
    {
        buffer.clear();
        if ( !buffer.isVolatile() && !buffer.isImmutable() )
        {
            int c = buffer.capacity();
            if ( c == _headerBufferSize )
            {
                _headerBuffers.offer( buffer );
            }
            else if ( c == _responseBufferSize )
            {
                _responseBuffers.offer( buffer );

            }
            else if ( c == _requestBufferSize )
            {

                _requestBuffers.offer( buffer );

            }
        }
    }

    /* ------------------------------------------------------------ */
    protected void doStart()
        throws Exception
    {
        super.doStart();

        _headerBuffers = new AbstractBuffersQueue();
        _requestBuffers = new AbstractBuffersQueue();
        _responseBuffers = new AbstractBuffersQueue();
    }

}

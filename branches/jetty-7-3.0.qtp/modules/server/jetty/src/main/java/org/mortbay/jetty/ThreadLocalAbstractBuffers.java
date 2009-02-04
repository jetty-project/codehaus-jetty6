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

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

/**
 * buffering pool that makes use of ThreadLocal storage for keeping buffers
 * close at hand
 * 
 * @author jesse
 *
 */
public abstract class ThreadLocalAbstractBuffers
    extends AbstractBuffers
{
    final static private int __HEADER=0;
    final static private int __REQUEST=1;
    final static private int __RESPONSE=2;
    final static private int __OTHER=3;
    final private int[] _pool={2,1,1,2};

    private final ThreadLocal<ThreadBuffers> _buffers=new ThreadLocal<ThreadBuffers>()
    {
        protected ThreadBuffers initialValue()
        {
            return new ThreadBuffers(_pool[__HEADER],_pool[__REQUEST],_pool[__RESPONSE],_pool[__OTHER]);
        }
    };
    
    public ThreadLocalAbstractBuffers()
    {
        super();
    }



    public Buffer getBuffer(final int size )
    {
        final int set = (size==_headerBufferSize)?__HEADER
                :(size==_responseBufferSize)?__RESPONSE
                        :(size==_requestBufferSize)?__REQUEST:__OTHER;

        final ThreadBuffers thread_buffers = _buffers.get();
        
        final Buffer[] buffers=thread_buffers._buffers[set];
        for (int i=0;i<buffers.length;i++)
        {
            final Buffer b=buffers[i];
            if (b!=null && b.capacity()==size)
            {
                buffers[i]=null;
                return b;
            }
        }
        
        return newBuffer(size);
    }

    public void returnBuffer( Buffer buffer )
    {
        buffer.clear();
        if (buffer.isVolatile() || buffer.isImmutable())
            return;
        
        int size=buffer.capacity();
        final int set = (size==_headerBufferSize)?__HEADER
                :(size==_responseBufferSize)?__RESPONSE
                        :(size==_requestBufferSize)?__REQUEST:__OTHER;

        final ThreadBuffers thread_buffers = _buffers.get();
        final Buffer[] buffers=thread_buffers._buffers[set];
        for (int i=0;i<buffers.length;i++)
        {
            if (buffers[i]==null)
            {
                buffers[i]=buffer;
                return;
            }
        }
            
    }
    

    protected void doStart()
        throws Exception
    {
        super.doStart();
        if (_headerBufferSize==_requestBufferSize && _headerBufferSize==_responseBufferSize)
        {
            _pool[__HEADER]+=_pool[__REQUEST]+_pool[__RESPONSE];
            _pool[__REQUEST]=0;
            _pool[__RESPONSE]=0;
        }
        else if (_headerBufferSize==_requestBufferSize)
        {
            _pool[__HEADER]+=_pool[__REQUEST];
            _pool[__REQUEST]=0;
        }
        else if (_headerBufferSize==_responseBufferSize)
        {
            _pool[__HEADER]+=_pool[__RESPONSE];
            _pool[__RESPONSE]=0;
        }
        else if (_requestBufferSize==_responseBufferSize)
        {
            _pool[__RESPONSE]+=_pool[__REQUEST];
            _pool[__REQUEST]=0;
        }
        
    }

    public abstract Buffer newBuffer( int size );

    protected static class ThreadBuffers
    {
        final Buffer[][] _buffers;
        ThreadBuffers(int headers,int requests,int responses,int others)
        {
            _buffers = new Buffer[4][];
            _buffers[__HEADER]=new Buffer[headers];
            _buffers[__REQUEST]=new Buffer[requests];
            _buffers[__RESPONSE]=new Buffer[responses];
            _buffers[__OTHER]=new Buffer[others];
            
        }
    }
}

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

    public ThreadLocalAbstractBuffers()
    {
        super();
    }

    private ThreadLocal tlHeader = new ThreadLocal()
    {
        protected synchronized Object initialValue()
        {
            return newBuffer( _headerBufferSize );
        }
    };

    private ThreadLocal tlRequest;
    private ThreadLocal tlResponse;


    public Buffer getBuffer( int size )
    {

        if ( size == _headerBufferSize )
        {
           return (Buffer) tlHeader.get();
        }
        else if ( size == _responseBufferSize )
        {
            return (Buffer) tlResponse.get();
        }
        else if ( size == _requestBufferSize )
        {
            return (Buffer) tlRequest.get();
        }

        return newBuffer( size );
    }

    public void returnBuffer( Buffer buffer )
    {
        buffer.clear();
    }
    
    

    protected void doStart()
        throws Exception
    {
        tlRequest = new ThreadLocal()
        {
            protected synchronized Object initialValue()
            {
                return newBuffer( _requestBufferSize );
            }
        };
        
        tlResponse = new ThreadLocal()
        {
            protected synchronized Object initialValue()
            {
                return newBuffer( _responseBufferSize );
            }
        };
        
        super.doStart();
    }

    public abstract Buffer newBuffer( int size );

}

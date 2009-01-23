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

    private ThreadLocal tlHeader;
    private ThreadLocal tlRequest;
    private ThreadLocal tlResponse;


    public Buffer getBuffer( int size )
    {

        if ( size == _headerBufferSize )
        {
           Buffer b = (Buffer) tlHeader.get();
	   if (b!=null)
	   {
	       tlHeader.set(null);
	       return b;
	   }
        }
        else if ( size == _responseBufferSize )
        {
           Buffer b = (Buffer) tlResponse.get();
	   if (b!=null)
	   {
	       tlResponse.set(null);
	       return b;
	   }
        }
        else if ( size == _requestBufferSize )
        {
           Buffer b = (Buffer) tlRequest.get();
	   if (b!=null)
	   {
	       tlRequest.set(null);
	       return b;
	   }
        }

        return newBuffer( size );
    }

    public void returnBuffer( Buffer buffer )
    {
        buffer.clear();
        if (!buffer.isVolatile() && !buffer.isImmutable())
        {
            int size=buffer.capacity();
	    if ( size == _headerBufferSize )
	    {
	       tlHeader.set(buffer);
	    }
	    else if ( size == _responseBufferSize )
	    {
	       tlResponse.set(buffer);
	    }
	    else if ( size == _requestBufferSize )
	    {
	       tlRequest.set(buffer);
	    }
	}
    }
    

    protected void doStart()
        throws Exception
    {
        tlHeader = new ThreadLocal()
        {
            protected synchronized Object initialValue()
            {
                return newBuffer( _headerBufferSize );
            }
        };
        
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

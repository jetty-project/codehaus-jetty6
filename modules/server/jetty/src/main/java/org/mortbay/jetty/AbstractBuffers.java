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

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;

/**
 * new abstract super class for abstract buffers, enabling us to try out 
 * different buffer pool solutions
 * 
 * @author jesse
 *
 */
public abstract class AbstractBuffers
    extends AbstractLifeCycle
    implements Buffers
{

    protected static int _headerBufferSize = 6 * 1024;

    protected static int _requestBufferSize = 8 * 1024;

    protected static int _responseBufferSize = 32 * 1024;

    public AbstractBuffers()
    {
        super();
    }

    public abstract Buffer getBuffer( int size );

    public abstract void returnBuffer( Buffer buffer );
    
    public abstract Buffer newBuffer( int size );

    protected void doStart()
        throws Exception
    {
        super.doStart();
    }

    /**
     * @return Returns the headerBufferSize.
     */
    public int getHeaderBufferSize()
    {
        return _headerBufferSize;
    }

    /**
     * @param headerBufferSize The headerBufferSize to set.
     */
    public void setHeaderBufferSize( int headerBufferSize )
    {
        _headerBufferSize = headerBufferSize;
    }

    /**
     * @return Returns the requestBufferSize.
     */
    public int getRequestBufferSize()
    {
        return _requestBufferSize;
    }

    /**
     * @param requestBufferSize The requestBufferSize to set.
     */
    public void setRequestBufferSize( int requestBufferSize )
    {
        _requestBufferSize = requestBufferSize;
    }

    /**
     * @return Returns the responseBufferSize.
     */
    public int getResponseBufferSize()
    {
        return _responseBufferSize;
    }

    /**
     * @param responseBufferSize The responseBufferSize to set.
     */
    public void setResponseBufferSize( int responseBufferSize )
    {
        _responseBufferSize = responseBufferSize;
    }

}
//========================================================================
// Parts Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//========================================================================

package org.mortbay.jetty.grizzly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.mortbay.io.Buffer;

import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpParser;
import org.mortbay.log.Log;
import org.mortbay.util.ajax.Continuation;

public class GrizzlyEndPoint extends ChannelEndPoint
{
    protected HttpConnection _connection;
    
    private GrizzlySocketChannel _blockingChannel;
    
    
    public GrizzlyEndPoint(GrizzlyConnector connector,ByteChannel channel)
        throws IOException
    {
        // TODO: Needs an empty constructor?
        super(channel);
        
        //System.err.println("\nnew GrizzlyEndPoint channel="+channel);
        _connection = new HttpConnection(connector,this,connector.getServer());
        _blockingChannel = new GrizzlySocketChannel();
    }

    public void handle()
    {
        //System.err.println("GrizzlyEndPoint.handle "+this);
        
        try
        {
            //System.err.println("handle  "+this);
            _connection.handle();
        }
        catch (Throwable e)
        {
            Log.warn("handle failed", e);
            throw new RuntimeException(e);
        }
        finally
        {
            //System.err.println("handled "+this);
            Continuation continuation =  _connection.getRequest().getContinuation();
            if (continuation != null && continuation.isPending())
            {
                // We have a continuation
                // TODO something!
            }
            else
            {
                // something else... normally re-enable this connection is the selectset with the latest interested ops
            }
        }
    
    }

    
    /* (non-Javadoc)
     * @see org.mortbay.io.EndPoint#fill(org.mortbay.io.Buffer)
     */
    public int fill(Buffer buffer) throws IOException
    {
        return 0; // Always filled way before by Grizzly.
    }
    
    /* (non-Javadoc)
     * @see org.mortbay.io.EndPoint#flush(org.mortbay.io.Buffer)
     */
    public int flush(Buffer buffer) throws IOException
    {
        Buffer buf = buffer.buffer();
        int len=0;
        if (buf instanceof NIOBuffer)
        {
            NIOBuffer nbuf = (NIOBuffer)buf;
            ByteBuffer bbuf=nbuf.getByteBuffer();

            // TODO synchronize or duplicate?
            synchronized(nbuf)
            {
                try
                {
                    bbuf.position(buffer.getIndex());
                    bbuf.limit(buffer.putIndex());
                    len=_blockingChannel.write(bbuf);
                }
                finally
                {
                    if (len>0)
                        buffer.skip(len);
                    bbuf.position(0);
                    bbuf.limit(bbuf.capacity());
                }
            }
        }
        else if (buffer.array()!=null)
        {
            ByteBuffer b = ByteBuffer.wrap(buffer.array(), buffer.getIndex(), buffer.length());
            len=_blockingChannel.write(b);
            if (len>0)
                buffer.skip(len);
        }
        else
        {
            throw new IOException("Not Implemented");
        }
        return len;
    }
    
    
    /* (non-Javadoc)
     * @see org.mortbay.io.EndPoint#flush(org.mortbay.io.Buffer, org.mortbay.io.Buffer, org.mortbay.io.Buffer)
     */
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
    {
        int length=0;

        Buffer buf0 = header==null?null:header.buffer();
        Buffer buf1 = buffer==null?null:buffer.buffer();
        Buffer buf2 = trailer==null?null:trailer.buffer();
        if (_blockingChannel.getSocketChannel() instanceof GatheringByteChannel &&
            header!=null && header.length()!=0 && header instanceof NIOBuffer && 
            buffer!=null && buffer.length()!=0 && buffer instanceof NIOBuffer)
        {
            NIOBuffer nbuf0 = (NIOBuffer)buf0;
            NIOBuffer nbuf1 = (NIOBuffer)buf1;
            NIOBuffer nbuf2 = buf2==null?null:(NIOBuffer)buf2;
            
            // Get the underlying NIO buffers
            ByteBuffer bbuf0=nbuf0.getByteBuffer();
            ByteBuffer bbuf1=nbuf1.getByteBuffer();
            ByteBuffer bbuf2=nbuf2==null?null:nbuf2.getByteBuffer();
            
            
            // We must sync because buffers may be shared (eg nbuf1 is likely to be cached content).
            synchronized(nbuf0)
            {
                synchronized(nbuf1)
                {
                    try
                    {
                        // Adjust position indexs of buf0 and buf1
                        bbuf0.position(header.getIndex());
                        bbuf0.limit(header.putIndex());
                        bbuf1.position(buffer.getIndex());
                        bbuf1.limit(buffer.putIndex());
                        
                        // if we don't have a buf2
                        if (bbuf2==null)
                        {
                            synchronized(this)
                            {
                                // create a gether array for 2 buffers
                                if (_gather2==null)
                                    _gather2=new ByteBuffer[2];
                                _gather2[0]=bbuf0;
                                _gather2[1]=bbuf1;

                                // do the gathering write.
                                length = _blockingChannel.write(_gather2);
                            }
                        }
                        else
                        {
                            // we have a third buffer, so sync on it as well
                            synchronized(nbuf2)
                            {
                                try
                                {
                                    // Adjust position indexs of buf2
                                    bbuf2.position(trailer.getIndex());
                                    bbuf2.limit(trailer.putIndex());

                                    synchronized(this)
                                    {
                                        // create a gether array for 3 buffers
                                        if (_gather3==null)
                                            _gather3=new ByteBuffer[3];
                                        _gather3[0]=bbuf0;
                                        _gather3[1]=bbuf1;
                                        _gather3[2]=bbuf2;
                                        // do the gathering write.
                                        length = (int)_blockingChannel.write(_gather3);
                                    }
                                }
                                finally
                                {
                                    // adjust buffer 2.
                                    if (!trailer.isImmutable())
                                        trailer.setGetIndex(bbuf2.position());
                                    bbuf2.position(0);
                                    bbuf2.limit(bbuf2.capacity());
                                }
                            }
                        }
                    }
                    finally
                    {
                        // adjust buffer 0 and 1
                        if (!header.isImmutable())
                            header.setGetIndex(bbuf0.position());
                        if (!buffer.isImmutable())
                            buffer.setGetIndex(bbuf1.position());
                       
                        bbuf0.position(0);
                        bbuf1.position(0);
                        bbuf0.limit(bbuf0.capacity());
                        bbuf1.limit(bbuf1.capacity());
                    }
                }
            }
        }
        else
        {
            // TODO - consider copying buffers buffer and trailer into header if there is space!
            
            
            // flush header
            if (header!=null && header.length()>0)
                length=flush(header);
            
            // flush buffer
            if ((header==null || header.length()==0) &&
                            buffer!=null && buffer.length()>0)
                length+=flush(buffer);
            
            // flush trailer
            if ((header==null || header.length()==0) &&
                            (buffer==null || buffer.length()==0) &&
                            trailer!=null && trailer.length()>0)
                length+=flush(trailer);
        }
        
        return length;
    }
    
    
    public boolean blockReadable(long millisecs)
    {
        Buffer buffer=((HttpParser)_connection.getParser()).getHeaderBuffer();
        if (buffer instanceof NIOBuffer)
        {
            ByteBuffer byteBuffer=((NIOBuffer)buffer).getByteBuffer();
            _blockingChannel.setReadTimeout(millisecs);
            try
            {
                _blockingChannel.read(byteBuffer);
            }
            catch (IOException ex)
            {
                ; // TODO: Rethrow in case the client closed the connection.
                return false;
            }
        }
        else
        {
            ; // TODO: How to handle this case.
        }
        return true;
    }

    public boolean blockWritable(long millisecs)
    {
        Buffer buffer=((HttpParser)_connection.getParser()).getHeaderBuffer();
        if (buffer instanceof NIOBuffer)
        {
            ByteBuffer byteBuffer=((NIOBuffer)buffer).getByteBuffer();
            _blockingChannel.setWriteTimeout(millisecs);
            try
            {
                _blockingChannel.write(byteBuffer);
            }
            catch (IOException ex)
            {
                // TODO: Rethrow in case the client closed the connection.
                return false;
            }
        }
        else
        {
            ; //TODO: How to handle this case.
        }
        return true;
    }
    
    public boolean keepAlive()
    {
        return _connection.getGenerator().isPersistent();
    }
    
    public boolean isComplete()
    {
        return _connection.getGenerator().isComplete();       
    }

    public boolean isBlocking()
    {
        return false;
    }
    
    
    public void setSelectionKey(SelectionKey key)
    {
        _blockingChannel.setSelectionKey(key);
    }    
   
    
    public void setChannel(SocketChannel channel)
    {
        _channel = _blockingChannel;
        _blockingChannel.setSocketChannel(channel);
    }
    
    public void recycle()
    {
        _connection.destroy();
    }    
    
    public HttpConnection getHttpConnection(){
        return _connection;
    }


}
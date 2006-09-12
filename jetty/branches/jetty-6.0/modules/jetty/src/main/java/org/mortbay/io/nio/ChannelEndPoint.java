//========================================================================
//$Id: ChannelEndPoint.java,v 1.1 2005/10/05 14:09:38 janb Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.Portable;


/**
 * @author gregw
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChannelEndPoint implements EndPoint
{
    protected ByteChannel _channel;
    protected ByteBuffer[] _gather2;
    protected ByteBuffer[] _gather3;
    protected Socket _socket;
    protected InetSocketAddress _local;
    protected InetSocketAddress _remote;
    
    /**
     * 
     */
    public ChannelEndPoint(ByteChannel channel)
    {
        super();
        this._channel = channel;
        if (channel instanceof SocketChannel)
            _socket=((SocketChannel)channel).socket();
    }
    
    public boolean isBlocking()
    {
        if (_channel instanceof SelectableChannel)
            return ((SelectableChannel)_channel).isBlocking();
        return true;
    }
    
    public void blockReadable(long millisecs)
    {
    }
    
    public void blockWritable(long millisecs)
    {
    }

    /* 
     * @see org.mortbay.io.EndPoint#isOpen()
     */
    public boolean isOpen()
    {
        return _channel.isOpen();
    }

    /* (non-Javadoc)
     * @see org.mortbay.io.EndPoint#close()
     */
    public void close() throws IOException
    {
        if (_channel.isOpen())
        {
            try
            {
                if (_channel instanceof SocketChannel)
                {
                    // TODO - is this really required?
                    Socket socket= ((SocketChannel)_channel).socket();
                    try
                    {
                        socket.shutdownOutput();
                    }
                    finally
                    {
                        socket.close();
                    }
                }
            }
            finally
            {
                _channel.close();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.mortbay.io.EndPoint#fill(org.mortbay.io.Buffer)
     */
    public int fill(Buffer buffer) throws IOException
    {
        Buffer buf = buffer.buffer();
        int len=0;
        if (buf instanceof NIOBuffer)
        {
            NIOBuffer nbuf = (NIOBuffer)buf;
            ByteBuffer bbuf=nbuf.getByteBuffer();
            synchronized(nbuf)
            {
                try
                {
                    bbuf.position(buffer.putIndex());
                    len=_channel.read(bbuf);
                }
                finally
                {
                    buffer.setPutIndex(bbuf.position());
                    bbuf.position(0);
                }
            }
        }
        else
        {
            throw new IOException("Not Implemented");
        }
        
        return len;
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
                    len=_channel.write(bbuf);
                }
                finally
                {
                    if (!buffer.isImmutable())
                        buffer.setGetIndex(bbuf.position());
                    bbuf.position(0);
                    bbuf.limit(bbuf.capacity());
                }
            }
        }
        else if (buffer.array()!=null)
        {
            ByteBuffer b = ByteBuffer.wrap(buffer.array(), buffer.getIndex(), buffer.length());
            len=_channel.write(b);
            if (!buffer.isImmutable())
                buffer.setGetIndex(b.position());
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
        if (_channel instanceof GatheringByteChannel &&
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
                                length=(int)((GatheringByteChannel)_channel).write(_gather2);
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
                                        length=(int)((GatheringByteChannel)_channel).write(_gather3);
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

    /**
     * @return Returns the channel.
     */
    public ByteChannel getChannel()
    {
        return _channel;
    }


    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getLocalAddr()
     */
    public String getLocalAddr()
    {
        if (_socket==null)
            return null;
        
        if (_local==null)
            _local=(InetSocketAddress)_socket.getLocalSocketAddress();
        
       if (_local==null || _local.getAddress()==null || _local.getAddress().isAnyLocalAddress())
           return Portable.ALL_INTERFACES;
        
        return _local.getAddress().getHostAddress();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getLocalHost()
     */
    public String getLocalHost()
    {
        if (_socket==null)
            return null;
        
        if (_local==null)
            _local=(InetSocketAddress)_socket.getLocalSocketAddress();
        
       if (_local==null || _local.getAddress()==null || _local.getAddress().isAnyLocalAddress())
           return Portable.ALL_INTERFACES;
        
        return _local.getAddress().getCanonicalHostName();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getLocalPort()
     */
    public int getLocalPort()
    {
        if (_socket==null)
            return 0;
        
        if (_local==null)
            _local=(InetSocketAddress)_socket.getLocalSocketAddress();
        if (_local==null)
            return -1;
        return _local.getPort();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getRemoteAddr()
     */
    public String getRemoteAddr()
    {
        if (_socket==null)
            return null;
        
        if (_remote==null)
            _remote=(InetSocketAddress)_socket.getRemoteSocketAddress();
        
        if (_remote==null)
            return null;
        return _remote.getAddress().getHostAddress();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getRemoteHost()
     */
    public String getRemoteHost()
    {
        if (_socket==null)
            return null;
        
        if (_remote==null)
            _remote=(InetSocketAddress)_socket.getRemoteSocketAddress();
        
        return _remote.getAddress().getCanonicalHostName();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getRemotePort()
     */
    public int getRemotePort()
    {
        if (_socket==null)
            return 0;
        
        if (_remote==null)
            _remote=(InetSocketAddress)_socket.getRemoteSocketAddress();
        
        return _remote==null?-1:_remote.getPort();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getConnection()
     */
    public Object getConnection()
    {
        return _channel;
    }

}

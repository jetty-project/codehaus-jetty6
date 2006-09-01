package org.mortbay.jetty.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.mortbay.io.Buffer;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.nio.HttpChannelEndPoint;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.nio.SelectChannelConnector.SelectSet;
import org.mortbay.log.Log;

/* ------------------------------------------------------------ */
/**
 * SslHttpChannelEndPoint.
 * 
 * @author Nik Gonzalez <ngonzalez@exist.com>
 * @author Greg Wilkins <gregw@mortbay.com>
 */
public class SslHttpChannelEndPoint extends HttpChannelEndPoint implements Runnable
{
    private static ByteBuffer[] __NO_BUFFERS={};
    private static ByteBuffer __EMPTY=ByteBuffer.allocate(0);
    
    private final SSLEngine _engine;
    private final ByteBuffer _inBuffer;
    private final NIOBuffer _inNIOBuffer;
    private final ByteBuffer _outBuffer;
    private final NIOBuffer _outNIOBuffer;
    
    private final ByteBuffer[] _outBuffers=new ByteBuffer[3];

    // ssl
    private final SSLSession _session;

    /* ------------------------------------------------------------ */
    public SslHttpChannelEndPoint(SelectChannelConnector connector, SocketChannel channel, SelectSet selectSet, SelectionKey key, SSLEngine engine)
            throws SSLException, IOException
    {
        super(connector,channel,selectSet,key);

        
        // ssl
        _engine=engine;
        _engine.setUseClientMode(false);
        _session=engine.getSession();

        _outNIOBuffer=new NIOBuffer(_session.getPacketBufferSize(),true);
        _outBuffer=_outNIOBuffer.getByteBuffer();
        _inNIOBuffer=new NIOBuffer(_session.getPacketBufferSize(),true);
        _inBuffer=_inNIOBuffer.getByteBuffer();
        
        System.err.println("Engine.hsState="+_engine.getHandshakeStatus());

    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        _engine.closeOutbound();
        
        try
        {   
            loop: while (_inBuffer.remaining()>0)
            {
                System.err.println("close loop in Engine.hsState="+_engine.getHandshakeStatus());
                switch(_engine.getHandshakeStatus())
                {
                    case FINISHED:
                    case NOT_HANDSHAKING:
                        break loop;
                        
                    case NEED_UNWRAP:
                        if(!fill(__EMPTY))
                            break loop; // TODO may need to reschedule?
                        break;
                        
                    case NEED_TASK:
                    {
                        Runnable task;
                        while ((task=_engine.getDelegatedTask())!=null)
                        {
                            System.err.println("delegate task "+task);
                            task.run();
                        }
                        break;
                    }
                        
                    case NEED_WRAP:
                    {
                        System.err.println("needs wrapping");

                        flushOutBuffer();
                        
                        SSLEngineResult result=null;
                        try
                        {
                            _outBuffer.position(_outNIOBuffer.putIndex());
                            result=_engine.wrap(__NO_BUFFERS,_outBuffer);
                        }
                        finally
                        {
                            _outBuffer.position(0);
                            _outNIOBuffer.setGetIndex(0);
                            _outNIOBuffer.setPutIndex(result.bytesProduced());
                        }
                    
                        System.err.println("wrap result="+result);

                        flushOutBuffer();
                        
                        break;
                    }
                }
            }
            
        }
        finally
        {
            super.close();
        }
        
        
    }

    /* ------------------------------------------------------------ */
    /* 
     */
    public int fill(Buffer buffer) throws IOException
    {
        System.err.println("fill");
        
        ByteBuffer bbuf=extractInputBuffer(buffer);
        int size=buffer.length();
        
        try
        {
            bbuf.position(buffer.putIndex());
            
            fill(bbuf);
            
            loop: while (_inBuffer.remaining()>0)
            {
                System.err.println("loop in Engine.hsState="+_engine.getHandshakeStatus());
                switch(_engine.getHandshakeStatus())
                {
                    case FINISHED:
                    case NOT_HANDSHAKING:
                        break loop;
                        
                    case NEED_UNWRAP:
                        if(!fill(bbuf))
                            break loop;
                        break;
                        
                    case NEED_TASK:
                    {
                        Runnable task;
                        while ((task=_engine.getDelegatedTask())!=null)
                        {
                            System.err.println("delegate task "+task);
                            task.run();
                        }
                        break;
                    }
                        
                    case NEED_WRAP:
                    {
                        System.err.println("needs wrapping");

                        flushOutBuffer();
                        
                        SSLEngineResult result=null;
                        try
                        {
                            _outBuffer.position(_outNIOBuffer.putIndex());
                            result=_engine.wrap(__NO_BUFFERS,_outBuffer);
                        }
                        finally
                        {
                            _outBuffer.position(0);
                            _outNIOBuffer.setGetIndex(0);
                            _outNIOBuffer.setPutIndex(result.bytesProduced());
                        }
                    
                        System.err.println("wrap result="+result);

                        flushOutBuffer();
                        
                        break;
                    }
                }
            }
            
        }
        finally
        {
            buffer.setPutIndex(bbuf.position());
            bbuf.position(0);
        }
        
        System.err.println("filled:"+buffer);
        return buffer.length()-size;
    }

    /* ------------------------------------------------------------ */
    public int flush(Buffer buffer) throws IOException
    {
        return flush(buffer,null,null);
    }


    /* ------------------------------------------------------------ */
    /*     
     */
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
    {
        System.err.println("flush");
        
        _outBuffers[0]=extractOutputBuffer(header);
        _outBuffers[1]=extractOutputBuffer(buffer);
        _outBuffers[2]=extractOutputBuffer(trailer);
        
        SSLEngineResult result=null;
        try
        {
            _outBuffer.position(_outNIOBuffer.putIndex());
            _outBuffer.limit(_outBuffer.capacity());
            System.err.println("pos="+_outBuffer.position()+" limit="+_outBuffer.limit());
            result=_engine.wrap(_outBuffers,_outBuffer);
            System.err.println("wrap result="+result);
        }
        finally
        {
            _outBuffer.position(0);
            _outNIOBuffer.setGetIndex(0);
            _outNIOBuffer.setPutIndex(result.bytesProduced());
            
            int consumed=result.bytesConsumed();
            if (consumed>0 && header!=null)
            {
                int len=consumed<header.length()?consumed:header.length();
                header.skip(len);
                consumed-=len;
                _outBuffers[0].position(0);
                _outBuffers[0].limit(_outBuffers[0].capacity());
            }
            if (consumed>0 && buffer!=null)
            {
                int len=consumed<buffer.length()?consumed:buffer.length();
                buffer.skip(len);
                consumed-=len;
                _outBuffers[1].position(0);
                _outBuffers[1].limit(_outBuffers[1].capacity());
            }
            if (consumed>0 && trailer!=null)
            {
                int len=consumed<trailer.length()?consumed:trailer.length();
                trailer.skip(len);
                consumed-=len;
                _outBuffers[1].position(0);
                _outBuffers[1].limit(_outBuffers[1].capacity());
            }
            assert consumed==0;
        }
    
        flushOutBuffer();
        
        return result.bytesConsumed();
    }

    
    /* ------------------------------------------------------------ */
    private void flushOutBuffer() throws IOException
    {
        System.err.println("flushOutBuffer "+_outNIOBuffer.length());
        while (_outNIOBuffer.length()>0)
        {
            System.err.println("flushing "+_outNIOBuffer.length());
            int flushed=super.flush(_outNIOBuffer);
            if (flushed==0)
            {
                // TODO schedule WRITE callback!
                System.err.println("NOT IMPLEMENTED!");
                assert false;
            }
        }
        _outNIOBuffer.compact();
    }

    /* ------------------------------------------------------------ */
    private ByteBuffer extractInputBuffer(Buffer buffer)
    {
        assert buffer instanceof NIOBuffer;
        NIOBuffer nbuf=(NIOBuffer)buffer;
        ByteBuffer bbuf=nbuf.getByteBuffer();
        bbuf.position(buffer.putIndex());
        return bbuf;
    }
    
    /* ------------------------------------------------------------ */
    private ByteBuffer extractOutputBuffer(Buffer buffer)
    {
        if(buffer==null)
            return __EMPTY;
        
        ByteBuffer src=null;
        NIOBuffer nBuf=null;

        if (buffer.buffer() instanceof NIOBuffer)
        {
            nBuf=(NIOBuffer)buffer.buffer();
            src=nBuf.getByteBuffer();
        }
        else
        {
            // TODO - expensive to do this all the time - need to reuse this buffer!
            src=ByteBuffer.allocateDirect(buffer.length());
            for (int i=0; i<buffer.length(); i++)
            {
                // TODO ouch! a byte at a time?
                src.put(buffer.peek(i));
            }
        }

        if (src!=null)
        {
            src.position(buffer.getIndex());
            src.limit(buffer.putIndex());
        }

        return src;
    }

    /* ------------------------------------------------------------ */
    private boolean fill(ByteBuffer buffer) throws IOException
    {
        System.err.println("unwrapTo");
        int in_len=0;
        _inNIOBuffer.compact();
        while (_inNIOBuffer.space()>0)
        {
            int len=super.fill(_inNIOBuffer);
            if (len<=0)
                break;
            in_len+=len;
        }

        System.err.println("filled "+in_len+" total "+_inNIOBuffer.length());
        if (_inNIOBuffer.length()==0)
            return false;
        
        SSLEngineResult result=null;
        try
        {
            _inBuffer.position(_inNIOBuffer.getIndex());
            _inBuffer.limit(_inNIOBuffer.putIndex());
            result=_engine.unwrap(_inBuffer,buffer);
        }
        finally
        {
            _inBuffer.position(0);
            _inBuffer.limit(_inBuffer.capacity());
            _inNIOBuffer.skip(result.bytesConsumed());
        }
        
        System.err.println("unwrap result="+result);
        System.err.println("unwrap result.status="+result.getStatus());
        System.err.println("Engine.hsState="+_engine.getHandshakeStatus());

        switch(result.getStatus())
        {
            case OK:
            case CLOSED:
                break;
            default:
                throw new IOException(result.getStatus().toString());
        }
        
        return (result.bytesProduced()+result.bytesConsumed())>0;
    }
}

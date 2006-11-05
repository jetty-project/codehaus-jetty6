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
import org.mortbay.io.nio.SelectorManager;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.log.Log;

/* ------------------------------------------------------------ */
/**
 * SslHttpChannelEndPoint.
 * 
 * @author Nik Gonzalez <ngonzalez@exist.com>
 * @author Greg Wilkins <gregw@mortbay.com>
 */
public class SslHttpChannelEndPoint extends SelectChannelConnector.ConnectorEndPoint implements Runnable
{
    private static ByteBuffer[] __NO_BUFFERS={};
    private static ByteBuffer __EMPTY=ByteBuffer.allocate(0);
    private static SSLEngineResult _result;
    
    private final SSLEngine _engine;
    private final ByteBuffer _inBuffer;
    private final NIOBuffer _inNIOBuffer;
    private final ByteBuffer _outBuffer;
    private final NIOBuffer _outNIOBuffer;

    private ByteBuffer _reuseBuffer;    
    private final ByteBuffer[] _outBuffers=new ByteBuffer[3];

    // ssl
    private final SSLSession _session;

    /* ------------------------------------------------------------ */
    public SslHttpChannelEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key, SSLEngine engine)
            throws SSLException, IOException
    {
        super(channel,selectSet,key);

        
        // ssl
        _engine=engine;
        _engine.setUseClientMode(false);
        _session=engine.getSession();

        _outNIOBuffer=new NIOBuffer(_session.getPacketBufferSize(),true);
        _outBuffer=_outNIOBuffer.getByteBuffer();
        _inNIOBuffer=new NIOBuffer(_session.getPacketBufferSize(),true);
        _inBuffer=_inNIOBuffer.getByteBuffer();

    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        _engine.closeOutbound();
        
        try
        {   
            loop: while (_inBuffer.remaining()>0)
            {
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
                            task.run();
                        }
                        break;
                    }
                        
                    case NEED_WRAP:
                    {
                        flush();
                        
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
                        
                        flush();
                        
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
        ByteBuffer bbuf=extractInputBuffer(buffer);
        int size=buffer.length();
		 
        try
        {
            fill(bbuf);
            
            loop: while (_inBuffer.remaining()>0)
            {
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
                            task.run();
                        }
                        break;
                    }
                        
                    case NEED_WRAP:
                    {
                        flush();
                        
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
                    
                        flush();
                        
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
        if (_outNIOBuffer.length()>0)
        {
            flush();
            if (_outNIOBuffer.length()>0)
                return 0;
        }
        
        _outBuffers[0]=extractOutputBuffer(header);
        _outBuffers[1]=extractOutputBuffer(buffer);
        _outBuffers[2]=extractOutputBuffer(trailer);
        
        try
        {
            _outNIOBuffer.clear();
            _outBuffer.position(0);
            _outBuffer.limit(_outBuffer.capacity());
            _result=_engine.wrap(_outBuffers,_outBuffer);
        }
        finally
        {
            _outBuffer.position(0);
            _outNIOBuffer.setGetIndex(0);
            _outNIOBuffer.setPutIndex(_result.bytesProduced());
            
            int consumed=_result.bytesConsumed();
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
    
        flush();
        
        return _result.bytesConsumed();
    }

    
    /* ------------------------------------------------------------ */
    public void flush() throws IOException
    {
        while (_outNIOBuffer.length()>0)
        {
            int flushed=super.flush(_outNIOBuffer);
            if (flushed==0)
            {
                Thread.yield();
                flushed=super.flush(_outNIOBuffer);
                if (flushed==0)
                    return;
            }
        }
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
        	if (_reuseBuffer == null)
        	{
        		_reuseBuffer = ByteBuffer.allocateDirect(_session.getPacketBufferSize());
        	}
            _reuseBuffer.put(buffer.asArray());
            src = _reuseBuffer;
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
        int in_len=0;
        
        if (!_inNIOBuffer.hasContent() || (_result != null && _result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW))
        {
            _inNIOBuffer.clear();
            while (_inNIOBuffer.space()>0)
            {
                int len=super.fill(_inNIOBuffer);
                if (len<=0)
                    break;
                in_len+=len;
            }
        }

        if (_inNIOBuffer.length()==0)
            return false;
        
        try
        {
            _inBuffer.position(_inNIOBuffer.getIndex());
            _inBuffer.limit(_inNIOBuffer.putIndex());
            _result=_engine.unwrap(_inBuffer,buffer);
            if (_result != null)
            {
            	if (_result.getStatus() == SSLEngineResult.Status.OK)	
            		_inNIOBuffer.skip(_result.bytesConsumed());
            	else if (_result.getStatus() == SSLEngineResult.Status.CLOSED)
            		throw new IOException("sslEngine closed");
            }
            
        }
        finally
        {
            _inBuffer.position(0);
            _inBuffer.limit(_inBuffer.capacity());
        }
        
        switch(_result.getStatus())
        {
            case OK:
            case CLOSED:
                break;
            case BUFFER_UNDERFLOW:
            	break;
            default:
                Log.warn("unwrap "+_result);
                throw new IOException(_result.toString());
        }
        
        return (_result.bytesProduced()+_result.bytesConsumed())>0;
    }

    /* ------------------------------------------------------------ */
    /**
     * Updates selection key. Adds operations types to the selection key as needed. No operations
     * are removed as this is only done during dispatch. This method records the new key and
     * schedules a call to syncKey to do the keyChange
     */
    protected void updateKey()
    {
        synchronized (this)
        {
            int ops = _key == null ? 0 : _key.interestOps();
            _interestOps = ops | ((!_dispatched || _readBlocked) ? SelectionKey.OP_READ : 0) | (_writable && !_writeBlocked && !isBufferingOutput() ? 0 : SelectionKey.OP_WRITE);
            _writable = true; // Once writable is in ops, only removed with dispatch.

            if (_interestOps != ops)
            {
                _selectSet.addChange(this);
                _selectSet.wakeup();
            }
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isBufferingInput()
    {
        return _inNIOBuffer.hasContent();
    }

    /* ------------------------------------------------------------ */
    public boolean isBufferingOutput()
    {
        return _outNIOBuffer.hasContent();
    }

    /* ------------------------------------------------------------ */
    public boolean isBufferred()
    {
        return true;
    }

    public SSLEngine getSSLEngine()
    {
        return _engine;
    }
}

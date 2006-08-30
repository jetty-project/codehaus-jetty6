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
    private final SSLEngine _engine;
    private ByteBuffer _handshakeBuffer;
    private SSLEngineResult.HandshakeStatus _handshakeStatus;
    private boolean _initialHandshake = false;
    private final NIOBuffer _outNIOBuffer;
    private final ByteBuffer _outBuffer;
    private final ByteBuffer _inBuffer;
    private final NIOBuffer _inNIOBuffer;
    
    // ssl
    private final SSLSession _session;
    private SSLEngineResult.Status _status = null;

    /* ------------------------------------------------------------ */
    public SslHttpChannelEndPoint(SelectChannelConnector connector, SocketChannel channel, SelectSet selectSet, SelectionKey key, SSLEngine engine) throws SSLException, IOException
    {
        super(connector, channel, selectSet, key);

        // ssl
        _engine = engine;
        _engine.setUseClientMode(false);
        _session = engine.getSession();

        _outNIOBuffer = new NIOBuffer(_session.getPacketBufferSize(), true);
        _outBuffer = _outNIOBuffer.getByteBuffer();
        _outBuffer.limit(_outBuffer.capacity());
        _outBuffer.position(_outBuffer.capacity());

        _inNIOBuffer = new NIOBuffer(_session.getApplicationBufferSize(), true);
        _inBuffer = _inNIOBuffer.getByteBuffer();
        _inBuffer.position(_inBuffer.limit());
        
        // begin handshake
        _engine.beginHandshake();
        _handshakeStatus = _engine.getHandshakeStatus();
        _initialHandshake = true;
        _handshakeBuffer = ByteBuffer.allocateDirect(_session.getApplicationBufferSize());
        doHandshake();
    }

    /* ------------------------------------------------------------ */
        private void doHandshake() throws IOException
        {
            while (true)
            {
                Log.debug("handshake status--------->"+_handshakeStatus);
                Log.debug("    status--------------->"+_status);
                SSLEngineResult result;
                if (_handshakeStatus.equals(SSLEngineResult.HandshakeStatus.FINISHED))
                {
                    if (_initialHandshake)
                    {
                        _initialHandshake = false;
                    }
                    return;
                }
                else if (_handshakeStatus.equals(SSLEngineResult.HandshakeStatus.NEED_TASK))
                {
                    doTasks();
                }
                else if (_handshakeStatus.equals(SSLEngineResult.HandshakeStatus.NEED_UNWRAP))
                {
                    if (_channel.read(_handshakeBuffer) < 0)
                    {
                        _engine.closeInbound();
                    }
                    _inBuffer.clear();
                    _handshakeBuffer.flip();
                    do
                    {
                        result = _engine.unwrap(_handshakeBuffer, _inBuffer);
                    }
                    while (result.getStatus() == SSLEngineResult.Status.OK && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && result.bytesProduced() == 0);

                    if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED)
                    {
                        _initialHandshake = false;
                    }
    
                    // If no data was produced, and the status is still ok, try to read once more
                    if (_inBuffer.position() == 0 && result.getStatus() == SSLEngineResult.Status.OK && _handshakeBuffer.hasRemaining())
                    {
                        result = _engine.unwrap(_handshakeBuffer, _inBuffer);
                    }
    
                    _status = result.getStatus();
                    _handshakeStatus = result.getHandshakeStatus();
    
                    _handshakeBuffer.compact();
                    _inBuffer.flip();
                }
                else if (_handshakeStatus.equals(SSLEngineResult.HandshakeStatus.NEED_WRAP))
                {
                    if (!_outBuffer.hasRemaining())
                    {
                        // Prepare to write
                        _outBuffer.clear();
                        result = _engine.wrap(_handshakeBuffer, _outBuffer);
                        _handshakeStatus = result.getHandshakeStatus();
                        _outBuffer.flip();
                    }
                    _channel.write(_outBuffer);
                }
                else if (_handshakeStatus.equals(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)) { return; }
            }
    
        }

    /* ------------------------------------------------------------ */
    private void doTasks()
    {
        Runnable task;
        while ((task = _engine.getDelegatedTask()) != null)
        {
            task.run();
        }
        _handshakeStatus = _engine.getHandshakeStatus();
    }

    /* ------------------------------------------------------------ */
    private ByteBuffer extractByteBuffer(Buffer buffer)
    {
        ByteBuffer src = null;
        NIOBuffer nBuf = null;
        synchronized (buffer)
        {
            // TODO - expensive to do this all the time - need to reuse this buffer!
            if (buffer.buffer() instanceof NIOBuffer)
            {
                nBuf = (NIOBuffer) buffer.buffer();
                src = nBuf.getByteBuffer();
            }
            else
            {
                Log.debug("allocate another bytebuffer: " + buffer.getClass().getName());
                src = ByteBuffer.allocateDirect(buffer.length());
                for (int i = 0; i < buffer.length(); i++)
                {
                    src.put(buffer.peek(i));
                }
            }
        }

        if (src != null)
        {
            synchronized (buffer)
            {
                // TODO... do you need to reset this buffer afterwards? See ChannelEndPoint flush
                // I am pretty sure this would be why it is not working 100%
                src.position(buffer.getIndex());
                src.limit(buffer.putIndex());
            }
        }

        return src;
    }

    /* ------------------------------------------------------------ */
    /* 
     */
    public int fill(Buffer buffer) throws IOException
    {
        int l;
        if (_initialHandshake) { return 0; }

        if (_inBuffer.position() == 0)
        {
            _inNIOBuffer.setPutIndex(buffer.putIndex());
            _inNIOBuffer.setGetIndex(buffer.getIndex());
            super.fill(_inNIOBuffer);
        }
        else if (buffer instanceof NIOBuffer)
        {
            NIOBuffer nbuf = (NIOBuffer)buffer;
            ByteBuffer bbuf = nbuf.getByteBuffer();
            if (bbuf.remaining() < _inBuffer.remaining())
            {
                super.fill(_inNIOBuffer);                
            }
        }            

        l = unWrap((NIOBuffer)buffer);

        return l;
    }

    /* ------------------------------------------------------------ */
    public int flush(Buffer buffer) throws IOException
    {
        return flush(buffer, null, null);
    }

    /* ------------------------------------------------------------ */
    private void flushOutBuffer() throws IOException
    {
        try
        {
            _outNIOBuffer.setPutIndex(_outBuffer.limit());
            _outNIOBuffer.setGetIndex(_outBuffer.position());
            super.flush(_outNIOBuffer);
        }
        finally
        {
            _outBuffer.limit(_outNIOBuffer.putIndex());
            _outBuffer.position(_outNIOBuffer.getIndex());
        }
    }

    /* ------------------------------------------------------------ */
    /*     */
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
    {
        if (_initialHandshake) { return 0; }

        if (_outBuffer.hasRemaining())
        {
            flushOutBuffer();
            return 0;
        }

        int len = 0;
        _outBuffer.clear();

        try
        {
            synchronized (_outBuffer)
            {
                if (header != null && header.length() > 0)
                    len += wrap(header);
                
                if (_outBuffer.hasRemaining() && buffer != null && buffer.length() > 0)
                    len += wrap(buffer);
                
                if (_outBuffer.hasRemaining() && trailer != null && trailer.length() > 0)
                    len += wrap(trailer);
            }
        }
        finally
        {
            _outBuffer.flip();
            flushOutBuffer();
        }

        return len;
    }



    /* ------------------------------------------------------------ */
    private int unWrap(NIOBuffer buffer) throws IOException
    {
        ByteBuffer bBuf = buffer.getByteBuffer();

        _inBuffer.position(_inNIOBuffer.getIndex());
        _inBuffer.limit(_inNIOBuffer.putIndex());

        bBuf.clear();
        SSLEngineResult result;

        result = _engine.unwrap(_inBuffer, bBuf);

        _status = result.getStatus();
        _handshakeStatus = result.getHandshakeStatus();
        
        buffer.setGetIndex(0);
        buffer.setPutIndex(bBuf.position());
        bBuf.position(0);

        if (_handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK || _handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP || _handshakeStatus == SSLEngineResult.HandshakeStatus.FINISHED)
        {
            doHandshake();
        }

        int bytesConsumed = result.bytesConsumed();
 
        if (bytesConsumed == _inBuffer.limit())
        {
          _inBuffer.clear();
        }
        else
        {
            _inBuffer.position(bytesConsumed);
            _inBuffer.compact();
        }

        _inNIOBuffer.setGetIndex(_inBuffer.position());
        _inNIOBuffer.setPutIndex(_inBuffer.position());            
        
        return result.bytesProduced();
    }

    /* ------------------------------------------------------------ */
    private int wrap(Buffer buffer) throws SSLException, IOException
    {
        SSLEngineResult result = null;
        int total_written = 0;
        ByteBuffer src = extractByteBuffer(buffer);
        try
        {
            synchronized (_outBuffer)
            {
                result = _engine.wrap(src, _outBuffer);

                _status = result.getStatus();
                _handshakeStatus = result.getHandshakeStatus();
                if (_status == SSLEngineResult.Status.CLOSED)
                {
                    throw new IOException("SSLEngine closed");
                }
                else if (_status == SSLEngineResult.Status.OK)
                {
                    // TODO: check for rehandshake
                }

                total_written = result.bytesConsumed();
            }
        }
        finally
        {
            src.position(0);
            src.limit(src.capacity());

            // set buffer length to 0
            buffer.setGetIndex(buffer.getIndex() + total_written);
        }

        _status = result.getStatus();
        _handshakeStatus = result.getHandshakeStatus();
        
        return total_written;
    }

    public void close() throws IOException
    {
        _engine.closeOutbound();
        if (_outBuffer.hasRemaining())
        {
            flushOutBuffer();
            
            if(_outBuffer.hasRemaining())
            {   
                // TODO - what if all data is not flushed???
                throw new IllegalStateException("TODO???");
            }
        }
        
        /*
         * By RFC 2616, we can "fire and forget" our close_notify message, so that's what we'll do
         * here.
         */
        _outBuffer.clear();
        SSLEngineResult result = _engine.wrap(_handshakeBuffer, _outBuffer);
        if (result.getStatus() != SSLEngineResult.Status.CLOSED) { throw new SSLException("Improper closed state."); }
        _outBuffer.flip();
        flushOutBuffer();
        
        if(_outBuffer.hasRemaining())
        {   
            // TODO - what if all data is not flushed???
            throw new IllegalStateException("TODO???");
        }

        super.close();
    }
}

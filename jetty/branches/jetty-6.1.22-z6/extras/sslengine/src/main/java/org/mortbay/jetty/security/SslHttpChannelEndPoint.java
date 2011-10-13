// ========================================================================
// Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.nio.IndirectNIOBuffer;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.io.nio.SelectorManager;
import org.mortbay.jetty.EofException;
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
    private static final Buffer __EMPTY_BUFFER=new IndirectNIOBuffer(0);
    private static final ByteBuffer __ZERO_BUFFER=ByteBuffer.allocate(0);

    private final SSLEngine _engine;
    private final SSLSession _session;
    private final ByteBuffer _inBuffer;
    private final NIOBuffer _inNIOBuffer;
    private final ByteBuffer _outBuffer;
    private final NIOBuffer _outNIOBuffer;
    private boolean _closing=false;
    private SSLEngineResult _result;
    private boolean _handshook=false;
    private boolean _allowRenegotiate=true;

    /* ------------------------------------------------------------ */
    public SslHttpChannelEndPoint(Buffers buffers,SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key, SSLEngine engine)
            throws IOException
    {
        super(channel,selectSet,key);

        // ssl
        _engine=engine;
        _session=engine.getSession();

        // TODO pool buffers and use only when needed.
        _outNIOBuffer=(NIOBuffer)buffers.getBuffer(_session.getPacketBufferSize());
        _outBuffer=_outNIOBuffer.getByteBuffer();
        _inNIOBuffer=(NIOBuffer)buffers.getBuffer(_session.getPacketBufferSize());
        _inBuffer=_inNIOBuffer.getByteBuffer();
    }


    /* ------------------------------------------------------------ */
    /**
     * @return True if SSL re-negotiation is allowed (default false)
     */
    public boolean isAllowRenegotiate()
    {
        return _allowRenegotiate;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set if SSL re-negotiation is allowed. CVE-2009-3555 discovered
     * a vulnerability in SSL/TLS with re-negotiation.  If your JVM
     * does not have CVE-2009-3555 fixed, then re-negotiation should
     * not be allowed.
     * @param allowRenegotiate true if re-negotiation is allowed (default false)
     */
    public void setAllowRenegotiate(boolean allowRenegotiate)
    {
        _allowRenegotiate = allowRenegotiate;
    }

    /* ------------------------------------------------------------ */
    // TODO get rid of these dumps
    public void dump()
    {
        System.err.println(_result);
        // System.err.println(h.toString());
        // System.err.println("--");
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.io.nio.SelectChannelEndPoint#idleExpired()
     */
    protected void idleExpired()
    {
        try
        {
            _selectSet.getManager().dispatch(new Runnable()
            {
                public void run()
                {
                    doIdleExpired();
                }
            });
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
    }

    /* ------------------------------------------------------------ */
    protected void doIdleExpired()
    {
        super.idleExpired();
    }

    /* ------------------------------------------------------------ */
    public void shutdownOutput() throws IOException
    {
        close();
    }

    private int process(ByteBuffer inBBuf, Buffer outBuf) throws IOException
    {
        Log.debug(_session+" process closing="+_closing+" in={} out={}",inBBuf,outBuf);

        if (inBBuf==null)
            inBBuf=__ZERO_BUFFER;

        int received=0;
        int sent=0;

        HandshakeStatus initialStatus = _engine.getHandshakeStatus();
        boolean progress=true;

        while (progress)
        {
            progress=false;

            // flush output data
            int len=_outNIOBuffer.length();

            // we must flush it, as the other end might be
            // waiting for that outgoing data before sending
            // more incoming data
            flush();

            // If we have written some bytes, then progress has been made.
            progress|=_outNIOBuffer.length()<len;

            // handle the current hand share status
            Log.debug("status {} {}",_engine,_engine.getHandshakeStatus());
            switch(_engine.getHandshakeStatus())
            {
                case FINISHED:
                    throw new IllegalStateException();

                case NOT_HANDSHAKING:

                    // If closing, don't process application data
                    if (_closing)
                    {
                        if (outBuf!=null && outBuf.hasContent())
			{
			    Log.debug("write while closing");
			    outBuf.clear();
			}
                        break;
                    }

                    // Try wrapping some application data
                    if (outBuf!=null && outBuf.hasContent())
                    {
                        int c=wrap(outBuf);
                        progress=c>0||_result.bytesProduced()>0||_result.bytesConsumed()>0;

                        if (c>0)
                            sent+=c;
                        else if (c<0 && sent==0)
                            sent=-1;
                    }

                    // Try unwrapping some application data
                    if (inBBuf.remaining()>0 && _inNIOBuffer!=null && _inNIOBuffer.hasContent())
                    {
                        int space=inBBuf.remaining();
                        progress|=unwrap(inBBuf);
                        received+=space-inBBuf.remaining();
                    }
                    break;


                case NEED_TASK:
                {
                    // A task needs to be run, so run it!
                    Runnable task;
                    while ((task=_engine.getDelegatedTask())!=null)
                    {
                        progress=true;
                        task.run();
                    }

                    // Detect SUN JVM Bug!!!
                    if(initialStatus==HandshakeStatus.NOT_HANDSHAKING &&
                       _engine.getHandshakeStatus()==HandshakeStatus.NEED_UNWRAP && sent==0)
                    {
                        // This should be NEED_WRAP
                        // The fix simply detects the signature of the bug and then close the connection (fail-fast) so that ff3 will delegate to using SSL instead of TLS.
                        // This is a jvm bug on java1.6 where the SSLEngine expects more data from the initial handshake when the client(ff3-tls) already had given it.
                        // See http://jira.codehaus.org/browse/JETTY-567 for more details
                        Log.warn("{} JETTY-567",_session);
                        return -1;
                    }
                    break;
                }

                case NEED_WRAP:
                {
                    checkRenegotiate();

                    // The SSL needs to send some handshake data to the other side
                    int c=0;
                    if (outBuf!=null && outBuf.hasContent())
                        c=wrap(outBuf);
                    else
                        c=wrap(__EMPTY_BUFFER);

                    progress=_result.bytesProduced()>0||_result.bytesConsumed()>0;
                    if (c>0)
                        sent+=c;
                    else if (c<0 && sent==0)
                        sent=-1;
                    break;
                }

                case NEED_UNWRAP:
                {
                    checkRenegotiate();

                    // Need more data to be unwrapped so try another call to unwrap
                    progress|=unwrap(inBBuf);
                    if (_closing)
                        inBBuf.clear();
                    break;
                }
            }

            Log.debug("{} progress {}",_session,progress);
        }

        Log.debug(_session+" received {} sent {}",received,sent);

        return (received<0||sent<0)?-1:(received+sent);
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        Log.debug("{} close",_session);
        try
        {
            if (!_closing)
            {
                _closing=true;
                _engine.closeOutbound();
                process(null,null);
            }
        }
        catch (EofException e)
        {
            // We could not write the SSL close message because the
            // socket was already closed, nothing more we can do.
            Log.ignore(e);
        }
        finally
        {
            super.close();
        }
    }


    /* ------------------------------------------------------------ */
    /* override dispatch with a defence against dispatch of closed SSL connections */
    int _closedDispatches=0;
    @Override
    public boolean dispatch(boolean assumeShortDispatch) throws IOException
    {
        synchronized (this)
        {
            if (_closing || _engine.isInboundDone() || _engine.isOutboundDone())
            {
                if (_closedDispatches++==100)
                {
                    Log.warn("Too many closed dispatches "+this);
                    final ByteChannel channel = getChannel();
                    _selectSet.addChange(new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                channel.close();
                            }
                            catch(Exception e)
                            {
                                Log.warn(e);
                            }
                        }
                    });
                }
            }
        }

        return super.dispatch(assumeShortDispatch);
    }


    /* ------------------------------------------------------------ */
    /*
     */
    public int fill(Buffer buffer) throws IOException
    {
        Log.debug("{} fill",_session);
        ByteBuffer bbuf=extractInputBuffer(buffer);

        // remember the original size of the unencrypted buffer
        int size=buffer.length();

        synchronized (bbuf)
        {
            bbuf.position(buffer.putIndex());
            try
            {
                // Call the SSLEngine unwrap method to process data in
                // the inBuffer.  If there is no data in the inBuffer, then
                // super.fill is called to read encrypted bytes.
                unwrap(bbuf);
                process(bbuf,null);
            }
            finally
            {
                // reset the Buffers
                buffer.setPutIndex(bbuf.position());
                bbuf.position(0);
            }
        }
        // return the number of unencrypted bytes filled.
        int filled=buffer.length()-size;
        if (filled==0 && (!isOpen() || _engine.isInboundDone()))
            return -1;

        return filled;
    }

    /* ------------------------------------------------------------ */
    public int flush(Buffer buffer) throws IOException
    {
        Log.debug("{} flush1",_session);
        return process(null,buffer);
    }


    /* ------------------------------------------------------------ */
    /*
     */
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
    {
        Log.debug("{} flush3",_session);
        int len=0;
        int flushed=0;
        if (header!=null && header.hasContent())
        {
            len=header.length();
            flushed=flush(header);
        }
        if (flushed==len && buffer!=null && buffer.hasContent())
        {
            int f=flush(buffer);
            if (f>=0)
                flushed+=f;
            else if (flushed==0)
                flushed=-1;
        }

        return flushed;
    }

    /* ------------------------------------------------------------ */
    public void flush() throws IOException
    {
        Log.debug("{} flush",_session);
        if (!isOpen())
            throw new EofException();

        if (isBufferingOutput())
        {
            int flushed=super.flush(_outNIOBuffer);
            Log.debug(_session + " flushed={} left={}", flushed, _outNIOBuffer.length());
        }
        else if (_engine.isOutboundDone() && super.isOpen())
        {
            Log.debug("{} flush shutdownOutput",_session);
            try
            {
                super.shutdownOutput();
            }
            catch(IOException e)
            {
                Log.ignore(e);
            }
        }
    }

    /* ------------------------------------------------------------ */
    private void checkRenegotiate() throws IOException
    {
        if (_handshook && !_allowRenegotiate && _channel!=null && _channel.isOpen())
        {
            Log.warn("SSL renegotiate denied: {}",_channel);
            super.close();
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
    /**
     * @return true if progress is made
     */
    private boolean unwrap(ByteBuffer buffer) throws IOException
    {
        if (_inNIOBuffer.hasContent())
            _inNIOBuffer.compact();
        else
            _inNIOBuffer.clear();

        Log.debug(_session+" unwrap space={} open={}",_inNIOBuffer.space(),super.isOpen());

        int total_filled=0;
        boolean remoteClosed = false;

        while (_inNIOBuffer.space()>0 && super.isOpen())
        {
            int filled=super.fill(_inNIOBuffer);
            Log.debug("{} filled {}",_session,filled);
            if (filled<0)
                remoteClosed = true;
            if (filled<=0)
                break;
            total_filled+=filled;
        }

        if (total_filled==0 && _inNIOBuffer.length()==0)
        {
            // Do we need to close?
            if (isOpen() && remoteClosed)
            {
                try
                {
                    _engine.closeInbound();
                }
                catch (SSLException x)
                {
                    // It may happen, for example, in case of truncation
                    // attacks, we close so that we do not spin forever
                    super.close();
                }
            }

            if(!isOpen())
            {
                _outNIOBuffer.clear();
                throw new EofException();
            }

            return false;
        }

        try
        {
            _inBuffer.position(_inNIOBuffer.getIndex());
            _inBuffer.limit(_inNIOBuffer.putIndex());

            _result=_engine.unwrap(_inBuffer,buffer);
            if (!_handshook && _result.getHandshakeStatus()==SSLEngineResult.HandshakeStatus.FINISHED)
                _handshook=true;
            Log.debug("{} unwrap {}",_session,_result);

            _inNIOBuffer.skip(_result.bytesConsumed());
        }
        catch(SSLException e)
        {
            Log.warn(getRemoteAddr() + ":" + getRemotePort() + " ",e);
            super.close();
            throw e;
        }
        finally
        {
            _inBuffer.position(0);
            _inBuffer.limit(_inBuffer.capacity());
        }

        switch(_result.getStatus())
        {
            case BUFFER_OVERFLOW:
                Log.debug("{} unwrap overflow",_session);
                return false;

            case BUFFER_UNDERFLOW:
                Log.debug("{} unwrap {}",_session,_result);
                if(!isOpen())
                {
                    _inNIOBuffer.clear();
                    _outNIOBuffer.clear();
                    throw new EofException();
                }
                return (total_filled > 0);

            case CLOSED:
                _closing=true;
                return total_filled>0 ||_result.bytesConsumed()>0 || _result.bytesProduced()>0;

            case OK:
                return total_filled>0 ||_result.bytesConsumed()>0 || _result.bytesProduced()>0;

            default:
                Log.warn("{} unwrap default: {}",_session,_result);
                throw new IOException(_result.toString());
        }
    }

    /* ------------------------------------------------------------ */
    private ByteBuffer extractOutputBuffer(Buffer buffer)
    {
        if (buffer.buffer() instanceof NIOBuffer)
            return ((NIOBuffer)buffer.buffer()).getByteBuffer();

        return ByteBuffer.wrap(buffer.array());
    }

    /* ------------------------------------------------------------ */
    private int wrap(Buffer buffer) throws IOException
    {
        ByteBuffer bbuf=extractOutputBuffer(buffer);
        synchronized(bbuf)
        {
            int consumed=0;
            synchronized(_outBuffer)
            {
                try
                {
                    _outNIOBuffer.compact();
                    bbuf.position(buffer.getIndex());
                    bbuf.limit(buffer.putIndex());
                    _outBuffer.position(_outNIOBuffer.putIndex());
                    _outBuffer.limit(_outBuffer.capacity());
                    _result=_engine.wrap(bbuf,_outBuffer);
                    Log.debug("{} wrap {}",_session,_result);
                    if (!_handshook && _result.getHandshakeStatus()==SSLEngineResult.HandshakeStatus.FINISHED)
                        _handshook=true;
                    _outNIOBuffer.setPutIndex(_outBuffer.position());
                    consumed=_result.bytesConsumed();
                }
                catch(SSLException e)
                {
                    Log.debug(getRemoteAddr()+":"+getRemotePort()+" ",e);
                    if (getChannel().isOpen())
                        getChannel().close();
                    throw e;
                }
                finally
                {
                    _outBuffer.position(0);
                    bbuf.position(0);
                    bbuf.limit(bbuf.capacity());

                    if (consumed>0)
                    {
                        int len=consumed<buffer.length()?consumed:buffer.length();
                        buffer.skip(len);
                        consumed-=len;
                    }
                }
            }
        }
        switch(_result.getStatus())
        {
            case BUFFER_UNDERFLOW:
                throw new IllegalStateException();

            case BUFFER_OVERFLOW:
                Log.debug("{} wrap {}",_session,_result);
                flush();
                return 0;

            case OK:
                return _result.bytesConsumed();
            case CLOSED:
                _closing=true;
                return _result.bytesConsumed()>0?_result.bytesConsumed():-1;

            default:
                Log.warn("{} wrap default {}",_session,_result);
            throw new IOException(_result.toString());
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

    /* ------------------------------------------------------------ */
    public SSLEngine getSSLEngine()
    {
        return _engine;
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return super.toString()+","+_engine.getHandshakeStatus()+", in/out="+_inNIOBuffer.length()+"/"+_outNIOBuffer.length()+" "+_result;
    }
}

package org.mortbay.jetty.server;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletOutputStream;

import org.mortbay.jetty.http.AbstractGenerator;
import org.mortbay.jetty.http.Generator;
import org.mortbay.jetty.io.Buffer;
import org.mortbay.jetty.io.ByteArrayBuffer;
import org.mortbay.jetty.io.EofException;
import org.mortbay.jetty.util.ByteArrayOutputStream2;

/** Output.
 * 
 * <p>
 * Implements  {@link javax.servlet.ServletOutputStream} from the {@link javax.servlet} package.   
 * </p>
 * A {@link ServletOutputStream} implementation that writes content
 * to a {@link AbstractGenerator}.   The class is designed to be reused
 * and can be reopened after a close.
 */
public class HttpOutput extends ServletOutputStream 
{
    protected final AbstractGenerator _generator;
    protected final long _maxIdleTime;
    protected final ByteArrayBuffer _buf = new ByteArrayBuffer(AbstractGenerator.NO_BYTES);
    protected boolean _closed;
    
    // These are held here for reuse by Writer
    String _characterEncoding;
    Writer _converter;
    char[] _chars;
    ByteArrayOutputStream2 _bytes;
    

    /* ------------------------------------------------------------ */
    public HttpOutput(AbstractGenerator generator, long maxIdleTime)
    {
        _generator=generator;
        _maxIdleTime=maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /*
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException
    {
        _closed=true;
    }
    
    /* ------------------------------------------------------------ */
    public void reopen()
    {
        _closed=false;
    }
    
    /* ------------------------------------------------------------ */
    public void flush() throws IOException
    {
        _generator.flush(_maxIdleTime);
    }

    /* ------------------------------------------------------------ */
    public void write(byte[] b, int off, int len) throws IOException
    {
        _buf.wrap(b, off, len);
        write(_buf);
    }

    /* ------------------------------------------------------------ */
    /*
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException
    {
        _buf.wrap(b);
        write(_buf);
    }

    /* ------------------------------------------------------------ */
    /*
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException
    {
        if (_closed)
            throw new IOException("Closed");
        if (!_generator.isOpen())
            throw new EofException();
        
        // Block until we can add _content.
        while (_generator.isBufferFull())
        {
            _generator.blockForOutput(_maxIdleTime);
            if (_closed)
                throw new IOException("Closed");
            if (!_generator.isOpen())
                throw new EofException();
        }

        // Add the _content
        if (_generator.addContent((byte)b))
            // Buffers are full so flush.
            flush();
       
        if (_generator.isContentWritten())
        {
            flush();
            close();
        }
    }

    /* ------------------------------------------------------------ */
    private void write(Buffer buffer) throws IOException
    {
        if (_closed)
            throw new IOException("Closed");
        if (!_generator.isOpen())
            throw new EofException();
        
        // Block until we can add _content.
        while (_generator.isBufferFull())
        {
            _generator.blockForOutput(_maxIdleTime);
            if (_closed)
                throw new IOException("Closed");
            if (!_generator.isOpen())
                throw new EofException();
        }

        // Add the _content
        _generator.addContent(buffer, Generator.MORE);

        // Have to flush and complete headers?
        if (_generator.isBufferFull())
            flush();
        
        if (_generator.isContentWritten())
        {
            flush();
            close();
        }

        // Block until our buffer is free
        while (buffer.length() > 0 && _generator.isOpen())
            _generator.blockForOutput(_maxIdleTime);
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.ServletOutputStream#print(java.lang.String)
     */
    public void print(String s) throws IOException
    {
        write(s.getBytes());
    }
}
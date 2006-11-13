//========================================================================
//$Id: ByteArrayEndPoint.java,v 1.2 2005/11/05 08:32:41 gregwilkins Exp $
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

package org.mortbay.io;

import java.io.IOException;



/* ------------------------------------------------------------ */
/** ByteArrayEndPoint.
 * @author gregw
 *
 */
public class ByteArrayEndPoint implements EndPoint
{
    byte[] _inBytes;
    ByteArrayBuffer _in;
    ByteArrayBuffer _out;
    boolean _closed;

    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public ByteArrayEndPoint()
    {
    }
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public ByteArrayEndPoint(byte[] input, int outputSize)
    {
        _inBytes=input;
        _in=new ByteArrayBuffer(input);
        _out=new ByteArrayBuffer(outputSize);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the in.
     */
    public ByteArrayBuffer getIn()
    {
        return _in;
    }
    /* ------------------------------------------------------------ */
    /**
     * @param in The in to set.
     */
    public void setIn(ByteArrayBuffer in)
    {
        _in = in;
    }
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the out.
     */
    public ByteArrayBuffer getOut()
    {
        return _out;
    }
    /* ------------------------------------------------------------ */
    /**
     * @param out The out to set.
     */
    public void setOut(ByteArrayBuffer out)
    {
        _out = out;
    }
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#isOpen()
     */
    public boolean isOpen()
    {
        return !_closed;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#isBlocking()
     */
    public boolean isBlocking()
    {
        return false;
    }

    /* ------------------------------------------------------------ */
    public boolean blockReadable(long millisecs)
    {
        return true;
    }

    /* ------------------------------------------------------------ */
    public boolean blockWritable(long millisecs)
    {
        return true;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#close()
     */
    public void close() throws IOException
    {
        _closed=true;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#fill(org.mortbay.io.Buffer)
     */
    public int fill(Buffer buffer) throws IOException
    {
        if (_closed)
            throw new IOException("CLOSED");
        if (_in.length()<=0)
            return -1;
        int len = buffer.put(_in);
        _in.skip(len);
        return len;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#flush(org.mortbay.io.Buffer)
     */
    public int flush(Buffer buffer) throws IOException
    {
        if (_closed)
            throw new IOException("CLOSED");
        int len = _out.put(buffer);
        buffer.skip(len);
        return len;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#flush(org.mortbay.io.Buffer, org.mortbay.io.Buffer, org.mortbay.io.Buffer)
     */
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
    {
        if (_closed)
            throw new IOException("CLOSED");
        int flushed=0;
        if (header!=null && header.length()>0)
        {
            int len=_out.put(header);
            header.skip(len);
            flushed+=len;
        }
        
        if (header==null || header.length()==0)
        {
            if (buffer!=null && buffer.length()>0)
            {
                int len=_out.put(buffer);
                buffer.skip(len);
                flushed+=len;
            }
            
            if (buffer==null || buffer.length()==0)
            {
                if (trailer!=null && trailer.length()>0)
                {
                    int len=_out.put(trailer);
                    trailer.skip(len);
                    flushed+=len;
                }
            }
        }
        
        return flushed;
    }

    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public void reset()
    {
        _closed=false;
        _in.clear();
        _out.clear();
        if (_inBytes!=null)
            _in.setPutIndex(_inBytes.length);
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getLocalAddr()
     */
    public String getLocalAddr()
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getLocalHost()
     */
    public String getLocalHost()
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getLocalPort()
     */
    public int getLocalPort()
    {
        return 0;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getRemoteAddr()
     */
    public String getRemoteAddr()
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getRemoteHost()
     */
    public String getRemoteHost()
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getRemotePort()
     */
    public int getRemotePort()
    {
        return 0;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.io.EndPoint#getConnection()
     */
    public Object getTransport()
    {
        return _inBytes;
    }

    /* ------------------------------------------------------------ */
    public void flush() throws IOException
    {   
    }

    /* ------------------------------------------------------------ */
    public boolean isBufferingInput()
    {
        return false;
    }

    /* ------------------------------------------------------------ */
    public boolean isBufferingOutput()
    {
        return false;
    }

    /* ------------------------------------------------------------ */
    public boolean isBufferred()
    {
        return false;
    }


}

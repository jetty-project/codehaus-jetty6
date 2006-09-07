//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.ajp;

import java.io.IOException;
import java.util.Iterator;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Generator;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpFields.Field;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.StringUtil;

/**
 * @author  lagdeppa (at) exist.com
 * @author Greg Wilkins
 */
public class Ajp13Generator implements Generator
{
    private Buffers _buffers;
    private Buffer _buffer;
    private boolean _done=false;
    private EndPoint _endp;
    private int _status;

    private String _statusMsg;

    public Ajp13Generator(Buffers buffers, EndPoint io, int headerBufferSize, int contentBufferSize)
    {
        this._status=0;
        this._statusMsg=null;
        this._buffers=buffers;
        this._endp=io;

        System.out.println(">>>>>> AJP Generator is instantiated buffers ");
    }

    public void addContent(Buffer content, boolean last) throws IOException
    {
        // TODO Auto-generated method stub
        System.out.println("AJPGenerator: addContent(content, last);");
    }

    public boolean addContent(byte b) throws IOException
    {
        System.out.write(b);
        return true;
    }

    public void complete() throws IOException
    {
        _done=true;
        // TODO Auto-generated method stub
        // System.out.println("AJPGenerator: complete();");
        // super.complete();
    }

    public void completeHeader(HttpFields fields, boolean allContentAdded) throws IOException
    {
        // TODO Auto-generated method stub
        System.out.println("AJPGenerator: completeHeader(fields, allContentAdded);");

        System.out.println(">>>allContentAdded>>>"+allContentAdded);
        
        if (_buffer==null)
            _buffer=_buffers.getBuffer(Ajp13Packet.MAX_PACKET_SIZE);  // TODO This is a temp buffer!  but with NIO it may not all be flushed!
        
        
        _buffer.put((byte)0x4);
        addInt(this._status);
        addString(_statusMsg==null?"unknown":_statusMsg);

        int field_index=_buffer.putIndex();
        addInt(0); // temp value

        Iterator i=fields.getFields();
        int num_fields=0;

        while (i.hasNext())
        {
            num_fields++;
            Field f=(Field)i.next();

            if (f.getName().equalsIgnoreCase("Content-Type"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x01);
            }
            else if (f.getName().equalsIgnoreCase("Content-Language"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x02);
            }
            else if (f.getName().equalsIgnoreCase("Content-Length"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x03);
            }
            else if (f.getName().equalsIgnoreCase("Date"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x04);
            }
            else if (f.getName().equalsIgnoreCase("Last-Modified"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x05);
            }
            else if (f.getName().equalsIgnoreCase("Location"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x06);
            }
            else if (f.getName().equalsIgnoreCase("Set-Cookie"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x07);
            }
            else if (f.getName().equalsIgnoreCase("Set-Cookie2"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x08);
            }
            else if (f.getName().equalsIgnoreCase("Servlet-Engine"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x09);
            }
            else if (f.getName().equalsIgnoreCase("Status"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x0A);
            }
            else if (f.getName().equalsIgnoreCase("WWW-Authenticate"))
            {
                _buffer.put((byte)0xA0);
                _buffer.put((byte)0x0B);
            }
            else
            {
                addString(f.getName());
            }
            addString(f.getValue());

            System.out.println(">>>Field>>>"+f.getName());
            System.out.println(">>>Value>>>"+f.getValue());
            System.out.println(">>>Ordinal>>>"+f.getValueOrdinal());
        }
        int tmp = _buffer.putIndex();
        _buffer.setPutIndex(field_index);
        addInt(num_fields);
        _buffer.setPutIndex(tmp);
        
        _endp.flush(_buffer);
        _endp.close();

    }

    public long flush() throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getContentBufferSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getContentWritten()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void increaseContentBufferSize(int size)
    {
        // TODO Auto-generated method stub
        
    }

    public boolean isCommitted()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isComplete()
    {
        return _done;
    }

    public boolean isPersistent()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void reset(boolean returnBuffers)
    {
        // TODO Auto-generated method stub
        
        if(returnBuffers && _buffer!=null)
        {
            _buffers.returnBuffer(_buffer);
            _buffer=null;
        }
        
    }

    public void resetBuffer()
    {
        // TODO Auto-generated method stub
        
    }

    public void sendError(int code, String reason, String content, boolean close) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void setHead(boolean head)
    {
        // TODO Auto-generated method stub
        
    }

    public void setResponse(int status, String reason)
    {
        // TODO Implement the code here
        System.out.println("AJPGenerator: setResponse(status, reason)"+status+", "+reason);

        this._status=status;
        this._statusMsg=reason;
    }

    public void setSendServerVersion(boolean sendServerVersion)
    {
        // TODO Auto-generated method stub
        
    }

    public void setVersion(int version)
    {
        // TODO Auto-generated method stub
        
    }

    public void addInt(int i)
    {
        _buffer.put((byte)((i>>8)&0xFF));
        _buffer.put((byte)(i&0xFF));
    }

    public void addString(String str)
    {
        if (str==null)
        {
            addInt(0xFFFF);
            return;
        }

        // TODO - need to use a writer to convert, to avoid this hacky conversion and temp buffer
        byte[] b = str.getBytes();
        
        addInt(b.length);
        
        _buffer.put(b);
        _buffer.put((byte)0);
    }
}

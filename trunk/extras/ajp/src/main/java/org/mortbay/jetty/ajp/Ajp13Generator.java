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
import java.util.HashMap;
import java.util.Iterator;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Generator;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpStatus;
import org.mortbay.jetty.HttpFields.Field;
import org.mortbay.util.TypeUtil;

/**
 * @author lagdeppa (at) exist.com
 * @author Greg Wilkins
 */
public class Ajp13Generator implements Generator
{
    private static HashMap __headerHash=new HashMap();

    static
    {
        byte[] xA001=
        { (byte)0xA0, (byte)0x01 };
        byte[] xA002=
        { (byte)0xA0, (byte)0x02 };
        byte[] xA003=
        { (byte)0xA0, (byte)0x03 };
        byte[] xA004=
        { (byte)0xA0, (byte)0x04 };
        byte[] xA005=
        { (byte)0xA0, (byte)0x05 };
        byte[] xA006=
        { (byte)0xA0, (byte)0x06 };
        byte[] xA007=
        { (byte)0xA0, (byte)0x07 };
        byte[] xA008=
        { (byte)0xA0, (byte)0x08 };
        byte[] xA009=
        { (byte)0xA0, (byte)0x09 };
        byte[] xA00A=
        { (byte)0xA0, (byte)0x0A };
        byte[] xA00B=
        { (byte)0xA0, (byte)0x0B };
        __headerHash.put("Content-Type",xA001);
        __headerHash.put("Content-Language",xA002);
        __headerHash.put("Content-Length",xA003);
        __headerHash.put("Date",xA004);
        __headerHash.put("Last-Modified",xA005);
        __headerHash.put("Location",xA006);
        __headerHash.put("Set-Cookie",xA007);
        __headerHash.put("Set-Cookie2",xA008);
        __headerHash.put("Servlet-Engine",xA009);
        __headerHash.put("Status",xA00A);
        __headerHash.put("WWW-Authenticate",xA00B);

    }

    private boolean _allContentAdded=false;
    private Buffer _buffer;
    private Buffer _header;
    private Buffers _buffers;
    private boolean _hasContent=false;
    
    private EndPoint _endp;
    private boolean _headerDone=false;
    private int _status;
    private String _reason;

    public Ajp13Generator(Buffers buffers, EndPoint io, int headerBufferSize, int contentBufferSize)
    {
        _buffers=buffers;
        _endp=io;
    }

    public void addContent(Buffer content, boolean last) throws IOException
    {
        // TODO Auto-generated method stub
        System.out.println("AJPGenerator: addContent(content, last);");
    }

    public boolean addContent(byte b) throws IOException
    {
        initContent();

        if (_buffer.length()>=_buffer.capacity())
        {
            flush();
            initContent();
        }

        _buffer.put(b);
        return true;
    }

    public void addInt(int i)
    {
        _buffer.put((byte)((i>>8)&0xFF));
        _buffer.put((byte)(i&0xFF));
    }

    public void addInt(int startIndex, int i)
    {
        _buffer.poke(startIndex,(byte)((i>>8)&0xFF));
        _buffer.poke((startIndex+1),(byte)(i&0xFF));
    }

    public void addString(String str)
    {
        if (str==null)
        {
            addInt(0xFFFF);
            return;
        }

        // TODO - need to use a writer to convert, to avoid this hacky
        // conversion and temp buffer
        byte[] b=str.getBytes();

        addInt(b.length);

        _buffer.put(b);
        _buffer.put((byte)0);
    }

    public void complete() throws IOException
    {
        flush();

        // send closing packet if all contents are added
        if (_allContentAdded)
        {
            reset(true);
            if (_buffer==null)
                _buffer=_buffers.getBuffer(Ajp13Packet.MAX_PACKET_SIZE);

            addPacketHeader();

            // send closing packet
            byte[] endByte=
            { 5, 1 };
            _buffer.put(endByte);

            addPacketFooter();

            _endp.flush(_buffer);
            reset(true);

            _endp.close();
        }

    }

    public void completeHeader(HttpFields fields, boolean allContentAdded) throws IOException
    {
        

        // get a header buffer
        if (_header == null) 
            _header = _buffers.getBuffer(Ajp13Packet.MAX_PACKET_SIZE);

        flush();

        _allContentAdded=allContentAdded;
        reset(true);
        if (_buffer==null)
            _buffer=_buffers.getBuffer(Ajp13Packet.MAX_PACKET_SIZE);

        addPacketHeader();

        _buffer.put((byte)0x4);
        addInt(_status);

        if (_reason==null)
        {
            Buffer tmp=HttpStatus.CACHE.get(_status);
            _reason=tmp==null?TypeUtil.toString(_status):tmp.toString();
        }
        addString(_reason==null?"status is unknown or no message definetion given...":_reason);

        int field_index=_buffer.putIndex();
        // allocate 2 bytes for number of headers
        addInt(0);

        System.out.println("********* Start of Response Headers **********");
        Iterator i=fields.getFields();
        int num_fields=0;

        while (i.hasNext())
        {
            num_fields++;
            Field f=(Field)i.next();

            byte[] codes=(byte[])__headerHash.get(f.getName());
            if (codes!=null)
            {
                System.out.println("0x"+TypeUtil.toHexString(codes)+":"+f.getName()+": "+f.getValue());
                _buffer.put(codes);
            }
            else
            {
                System.out.println(f.getName()+": "+f.getValue());
                addString(f.getName());
            }
            addString(f.getValue());


        }

        System.out.println("********* END of Response Headers **********");

        // insert the number of headers
        int tmp=_buffer.putIndex();
        _buffer.setPutIndex(field_index);
        addInt(num_fields);
        _buffer.setPutIndex(tmp);

        addPacketFooter();
        _endp.flush(_buffer);
        reset(true);

    }

    public int getContentBufferSize()
    {
        System.out.println("Ajp13: getContentBufferSize()");
        return 0;
    }

    public long getContentWritten()
    {
        System.out.println("Ajp13: getContentWritten()");
        return 0;
    }

    public void increaseContentBufferSize(int size)
    {
        System.out.println("Ajp13: increaseContentBufferSize()");
        // TODO Auto-generated method stub

    }

    public boolean isCommitted()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isComplete()
    {
        return _headerDone;
    }

    public boolean isPersistent()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void reset(boolean returnBuffers)
    {

        if (returnBuffers&&_buffer!=null)
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
        System.out.println("AJPGenerator: setResponse(status, reason)"+status+", "+reason);

        this._status=status;
        this._reason=reason;
    }

    public void setSendServerVersion(boolean sendServerVersion)
    {
        // TODO Auto-generated method stub

    }

    public void setVersion(int version)
    {
        // TODO Auto-generated method stub

    }

    private void addPacketFooter()
    {
        // get the payload size ( - 4 bytes for the ajp header) excluding the
        // ajp header
        int payloadSize=_buffer.length()-4;
        // insert the total packet size on 2nd and 3rd byte that was previously
        // allocated
        addInt(2,payloadSize);

    }

    private void addPacketHeader()
    {
        // AJP Header first 2 Response Pockets must contain ascii value AB
        _buffer.put((byte)'A');
        _buffer.put((byte)'B');
        // allocate 2 bytes for AJP Packet size, this will be on byte numbers
        // 2,3
        addInt(0);

    }
    
    public long flush() throws IOException
    {
        if (_hasContent)
        {
            _hasContent=false;
            // get the content length
            // -8 bytes
            // 4 bytes for the ajp header
            // 1 byte for response type
            // 2 bytes for the response size
            // 1 byte because we count from zero??
            int len=_buffer.length()-8;
            
            // insert the content length in the 5th, and 6th byte
            addInt(5,len);

            addPacketFooter();
            return _endp.flush(_buffer);
        }
        return 0;
    }

    private void initContent() throws IOException
    {
        if (_buffer==null)
        {
            _buffer=_buffers.getBuffer(Ajp13Packet.MAX_PACKET_SIZE);
            _hasContent=true;
            addPacketHeader();
            _buffer.put((byte)3);
            addInt(0);

        }

    }

}

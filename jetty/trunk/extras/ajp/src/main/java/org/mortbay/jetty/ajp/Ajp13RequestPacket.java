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

import org.mortbay.io.Buffer;
import org.mortbay.io.View;

/**
 * @author Markus Kobler
 * @author Greg Wilkins
 * @author Jason Jenkins <jj@aol.net>
 */
public class Ajp13RequestPacket
{
    private Buffer _buffer;
    private View _tok0=new View();
   
    public void setBuffer(Buffer buffer)
    {
        _buffer=buffer;
        reset();
    }

    public boolean isEmpty()
    {
        return _buffer.length()==0;
    }
    
    public int getInt()
    {
        return ((_buffer.get()&0xFF)<<8)|(_buffer.get()&0xFF);
    }

    public Buffer getString()
    {
        int len= ((_buffer.peek()&0xFF)<<8)|(_buffer.peek(_buffer.getIndex()+1)&0xFF);
        if (len==0xffff)
        {
            _buffer.skip(2);
            return null;
        }
        int start =_buffer.getIndex();
        _tok0.update(start+2,start+len+2);
        _buffer.skip(len+3);
        return _tok0;
    }
    
    public byte getByte()
    {
        return _buffer.get();
    }

    public boolean getBool()
    {
        return _buffer.get()>0;
    }

    public Buffer getMethod()
    {
        return Ajp13PacketMethods.CACHE.get(_buffer.get());
    }

    public Buffer getHeaderName()
    {
        int len= ((_buffer.peek()&0xFF)<<8)|(_buffer.peek(_buffer.getIndex()+1)&0xFF);
        if ((0xFF00&len)==0xA000)
        {
            _buffer.skip(1);
            return Ajp13RequestHeaders.CACHE.get(_buffer.get());
        }
        int start =_buffer.getIndex();
        _tok0.update(start+2,start+len+2);
        _buffer.skip(len+3);
        return _tok0;
        
    }
    

    public Buffer get(int length)
    {
        return _buffer.get(length);
    }
    
    public void reset()
    {
        _tok0.update(_buffer);
        _tok0.update(0,0);
    }


 
}

//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.io;

import java.text.DateFormatSymbols;
import java.util.Locale;

import org.mortbay.jetty.util.DateCache;

public class BufferDateCache extends DateCache
{
    Buffer _buffer;
    String _last;
    
    public BufferDateCache()
    {
        super();
    }

    public BufferDateCache(String format, DateFormatSymbols s)
    {
        super(format,s);
    }

    public BufferDateCache(String format, Locale l)
    {
        super(format,l);
    }

    public BufferDateCache(String format)
    {
        super(format);
    }

    public synchronized Buffer formatBuffer(long date)
    {
        String d = super.format(date);
        if (d==_last)
            return _buffer;
        _last=d;
        _buffer=new ByteArrayBuffer(d);
        
        return _buffer;
    }
}

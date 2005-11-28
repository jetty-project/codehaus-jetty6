// ========================================================================
// $Id: BufferCache.java,v 1.1 2005/10/05 14:09:25 janb Exp $
// Copyright 2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.io;

import java.util.ArrayList;
import java.util.HashMap;

import org.mortbay.util.StringMap;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class BufferCache
{
    private HashMap _bufferMap=new HashMap();
    private StringMap _stringMap=new StringMap(StringMap.CASE_INSENSTIVE);
    private ArrayList _index= new ArrayList();

    /* ------------------------------------------------------------------------------- */
    /** add.
     * @param GET
     * @param GET_METHOD
     */
    public CachedBuffer add(String value, int ordinal)
    {
        CachedBuffer buffer= new CachedBuffer(value, ordinal);
        _bufferMap.put(buffer, buffer);
        _stringMap.put(value, buffer);
        while ((ordinal - _index.size()) > 0)
            _index.add(null);
        _index.add(ordinal, buffer);
        return buffer;
    }

    public CachedBuffer get(int ordinal)
    {
        if (ordinal < 0 || ordinal >= _index.size())
            return null;
        return (CachedBuffer)_index.get(ordinal);
    }

    public CachedBuffer get(Buffer buffer)
    {
        return (CachedBuffer)_bufferMap.get(buffer);
    }

    public CachedBuffer get(String value)
    {
        return (CachedBuffer)_stringMap.get(value);
    }

    public Buffer lookup(Buffer buffer)
    {
        Buffer b= get(buffer);
        if (b == null)
        {
            return buffer;
        }

        return b;
    }

    public Buffer lookup(String value)
    {
        Buffer b= get(value);
        if (b == null)
        {
            return new CachedBuffer(value,-1);
        }
        return b;
    }

    public String toString(Buffer buffer)
    {
        return lookup(buffer).toString();
    }

    public int getOrdinal(Buffer buffer)
    {
        if (buffer instanceof CachedBuffer)
            return ((CachedBuffer)buffer).getOrdinal();
        buffer=lookup(buffer);
        if (buffer!=null && buffer instanceof CachedBuffer)
            return ((CachedBuffer)buffer).getOrdinal();
        return -1;
    }
    
    public class CachedBuffer extends ByteArrayBuffer.CaseInsensitive
    {
        private int _ordinal;
        public CachedBuffer(String value, int ordinal)
        {
            super(value);
            _ordinal= ordinal;
        }

        public int getOrdinal()
        {
            return _ordinal;
        }
    }
    
    public String toString()
    {
        return "CACHE["+
        	"bufferMap="+_bufferMap+
        	",stringMap="+_stringMap+
        	",index="+_index+
        	"]";
    }
}

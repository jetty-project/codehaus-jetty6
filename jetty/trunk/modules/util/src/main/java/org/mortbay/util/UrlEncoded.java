// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;


/* ------------------------------------------------------------ */
/** Handles coding of MIME  "x-www-form-urlencoded".
 * This class handles the encoding and decoding for either
 * the query string of a URL or the _content of a POST HTTP request.
 *
 * <p><h4>Notes</h4>
 * The hashtable either contains String single values, vectors
 * of String or arrays of Strings.
 *
 * This class is only partially synchronised.  In particular, simple
 * get operations are not protected from concurrent updates.
 *
 * @see java.net.URLEncoder
 * @author Greg Wilkins (gregw)
 */
public class UrlEncoded extends MultiMap
{

    /* ----------------------------------------------------------------- */
    public UrlEncoded(UrlEncoded url)
    {
        super(url);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded()
    {
        super(6);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded(String s)
    {
        super(6);
        decode(s,StringUtil.__UTF8);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded(String s, String charset)
    {
        super(6);
        decode(s,charset);
    }
    
    /* ----------------------------------------------------------------- */
    public void decode(String query)
    {
        decodeTo(query,this,StringUtil.__UTF8);
    }
    
    /* ----------------------------------------------------------------- */
    public void decode(String query,String charset)
    {
        decodeTo(query,this,charset);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     */
    public String encode()
    {
        return encode(StringUtil.__UTF8,false);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     */
    public String encode(String charset)
    {
        return encode(charset,false);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     * @param equalsForNullValue if True, then an '=' is always used, even
     * for parameters without a value. e.g. "blah?a=&b=&c=".
     */
    public synchronized String encode(String charset, boolean equalsForNullValue)
    {
        return encode(this,charset,equalsForNullValue);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     * @param equalsForNullValue if True, then an '=' is always used, even
     * for parameters without a value. e.g. "blah?a=&b=&c=".
     */
    public static String encode(MultiMap map, String charset, boolean equalsForNullValue)
    {
        if (charset==null)
            charset=StringUtil.__UTF8;
        
        StringBuffer result = new StringBuffer(128);
        synchronized(result)
        {
            Iterator iter = map.entrySet().iterator();
            while(iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)iter.next();
                
                String key = entry.getKey().toString();
                Object list = entry.getValue();
                int s=LazyList.size(list);
                
                if (s==0)
                {
                    result.append(encodeString(key,charset));
                    if(equalsForNullValue)
                        result.append('=');
                }
                else
                {
                    for (int i=0;i<s;i++)
                    {
                        if (i>0)
                            result.append('&');
                        Object val=LazyList.get(list,i);
                        result.append(encodeString(key,charset));

                        if (val!=null)
                        {
                            String str=val.toString();
                            if (str.length()>0)
                            {
                                result.append('=');
                                result.append(encodeString(str,charset));
                            }
                            else if (equalsForNullValue)
                                result.append('=');
                        }
                        else if (equalsForNullValue)
                            result.append('=');
                    }
                }
                if (iter.hasNext())
                    result.append('&');
            }
            return result.toString();
        }
    }


    /* -------------------------------------------------------------- */
    /** Decoded parameters to Map.
     * @param content the string containing the encoded parameters
     */
    public static void decodeTo(String content, MultiMap map, String charset)
    {
        if (charset==null)
            charset=StringUtil.__UTF8;

        synchronized(map)
        {
            String key = null;
            String value = null;
            int mark=-1;
            boolean encoded=false;
            for (int i=0;i<content.length();i++)
            {
                char c = content.charAt(i);
                switch (c)
                {
                  case '&':
                      value = encoded
                          ?decodeString(content,mark+1,i-mark-1,charset)
                          :content.substring(mark+1,i);
                      
                      mark=i;
                      encoded=false;
                      if (key != null)
                      {
                          map.add(key,value);
                          key = null;
                          value=null;
                      }
                      else if (value!=null)
                      {
                          map.add(value,null);
                          value=null;
                      }
                      break;
                  case '=':
                      if (key!=null)
                          break;
                      key = encoded
                          ?decodeString(content,mark+1,i-mark-1,charset)
                          :content.substring(mark+1,i);
                      mark=i;
                      encoded=false;
                      break;
                  case '+':
                      encoded=true;
                      break;
                  case '%':
                      encoded=true;
                      break;
                }                
            }
            
            if (key != null)
            {
                value =  encoded
                    ?decodeString(content,mark+1,content.length()-mark-1,charset)
                    :content.substring(mark+1);
                map.add(key,value);
            }
            else if (mark<content.length())
            {
                key = encoded
                    ?decodeString(content,mark+1,content.length()-mark-1,charset)
                    :content.substring(mark+1);
                map.add(key,"");
            }
        }
    }

    /* -------------------------------------------------------------- */
    /** Decoded parameters to Map.
     * @param data the byte[] containing the encoded parameters
     */
    public static void decodeUtf8To(byte[] raw,int offset, int length, MultiMap map)
    {
        synchronized(map)
        {
            Utf8StringBuffer buffer = new Utf8StringBuffer();
            String key = null;
            String value = null;
            
            // TODO cache of parameter names ???
            int end=offset+length;
            for (int i=offset;i<end;i++)
            {
                byte b=raw[i];
                switch ((char)(0xff&b))
                {
                    case '&':
                        value = buffer.length()==0?null:buffer.toString();
                        buffer.reset();
                        if (key != null)
                        {
                            map.add(key,value);
                            key = null;
                            value=null;
                        }
                        else if (value!=null)
                        {
                            map.add(value,null);
                            value=null;
                        }
                        break;
                        
                    case '=':
                        if (key!=null)
                        {
                            buffer.append(b);
                            break;
                        }
                        key = buffer.toString();
                        buffer.reset();
                        break;
                        
                    case '+':
                        buffer.append((byte)' ');
                        break;
                        
                    case '%':
                        if (i+2<end)
                            buffer.append((byte)((TypeUtil.convertHexDigit(raw[++i])<<4) + TypeUtil.convertHexDigit(raw[++i])));
                        break;
                    default:
                        buffer.append(b);
                    break;
                }
            }
            
            if (key != null)
            {
                value = buffer.toString();
                buffer.reset();
                map.add(key,value);
            }
        }
    }

    /* -------------------------------------------------------------- */
    /** Decoded parameters to Map.
     * @param data the byte[] containing the encoded parameters
     */
    public static void decodeUtf8To(InputStream in, MultiMap map)
    throws IOException
    {
        synchronized(map)
        {
            Utf8StringBuffer buffer = new Utf8StringBuffer();
            String key = null;
            String value = null;
            
            int b;
            
            // TODO cache of parameter names ???
            
            while ((b=in.read())>=0)
            {
                switch ((char) b)
                {
                    case '&':
                        value = buffer.length()==0?null:buffer.toString();
                        buffer.reset();
                        if (key != null)
                        {
                            map.add(key,value);
                            key = null;
                            value=null;
                        }
                        else if (value!=null)
                        {
                            map.add(value,null);
                            value=null;
                        }
                        break;
                        
                    case '=':
                        if (key!=null)
                        {
                            buffer.append((byte)b);
                            break;
                        }
                        key = buffer.toString();
                        buffer.reset();
                        break;
                        
                    case '+':
                        buffer.append((byte)' ');
                        break;
                        
                    case '%':
                        int dh=in.read();
                        int dl=in.read();
                        if (dh<0||dl<0)
                            break;
                        buffer.append((byte)((TypeUtil.convertHexDigit((byte)dh)<<4) + TypeUtil.convertHexDigit((byte)dl)));
                        break;
                    default:
                        buffer.append((byte)b);
                    break;
                }
            }
            
            if (key != null)
            {
                value = buffer.toString();
                buffer.reset();
                map.add(key,value);
            }
        }
    }
    /* -------------------------------------------------------------- */
    /** Decoded parameters to Map.
     * @param in the stream containing the encoded parameters
     */
    public static void decodeTo(InputStream in, MultiMap map, String charset)
    throws IOException
    {
        if (charset==null || StringUtil.__UTF8.equalsIgnoreCase(charset))
        {
            decodeUtf8To(in,map);
            return;
        }
        
        synchronized(map)
        {
            ByteArrayOutputStream2 buf=new ByteArrayOutputStream2(256);
            String key = null;
            String value = null;
            
            int c;
            int digit=0;
            int digits=0;
            
            // TODO cache of parameter names ???
            // TODO - more efficient??? 
            byte[] bytes=new byte[256]; // TODO Configure ?? size??? tune??? // reuse???
            int l=-1;
            
            while ((l=in.read(bytes))>=0)
            {
                for (int i=0;i<l;i++)
                {
                    c=bytes[i];
                    switch ((char) c)
                    {
                        case '&':
                            value = buf.size()==0?null:new String(buf.getBuf(), 0, buf.size(), charset);
                            buf.reset();
                            if (key != null)
                            {
                                map.add(key,value);
                                key = null;
                                value=null;
                            }
                            else if (value!=null)
                            {
                                map.add(value,null);
                                value=null;
                            }
                            break;
                        case '=':
                            if (key!=null)
                            {
                                buf.write(c);
                                break;
                            }
                            key = new String(buf.getBuf(), 0, buf.size(), charset);
                            buf.reset();
                            break;
                        case '+':
                            buf.write(' ');
                            break;
                        case '%':
                            digits=2;
                            break;
                        default:
                            if (digits==2)
                            {
                                digit=TypeUtil.convertHexDigit((byte)c);
                                digits=1;
                            }
                            else if (digits==1)
                            {
                                buf.write((digit<<4) + TypeUtil.convertHexDigit((byte)c));
                                digits=0;
                            }
                            else
                                buf.write(c);
                        break;
                    }
                }
            }
            if (key != null)
            {
                value = new String(buf.getBuf(), 0, buf.size(), charset);
                buf.reset();
                map.add(key,value);
            }
            
        }
    }
    
    /* -------------------------------------------------------------- */
    /** Decode String with % encoding.
     * This method makes the assumption that the majority of calls
     * will need no decoding and uses the 8859 encoding.
     */
    public static String decodeString(String encoded)
    {
        return decodeString(encoded,0,encoded.length(),StringUtil.__UTF8);
    }
    
    /* -------------------------------------------------------------- */
    /** Decode String with % encoding.
     * This method makes the assumption that the majority of calls
     * will need no decoding.
     */
    public static String decodeString(String encoded,String charset)
    {
        return decodeString(encoded,0,encoded.length(),charset);
    }
    
            
    /* -------------------------------------------------------------- */
    /** Decode String with % encoding.
     * This method makes the assumption that the majority of calls
     * will need no decoding.
     */
    public static String decodeString(String encoded,int offset,int length,String charset)
    {
        if (charset==null)
            charset=StringUtil.__UTF8;
        byte[] bytes=null;
        int n=0;
        
        for (int i=0;i<length;i++)
        {
            char c = encoded.charAt(offset+i);
            if (c<0||c>0xff)
                throw new IllegalArgumentException("Not encoded");
            
            if (c=='+')
            {
                if (bytes==null)
                {
                    bytes=new byte[length*2];
                    encoded.getBytes(offset, offset+i, bytes, 0);
                    n=i;
                }
                bytes[n++] = (byte) ' ';
            }
            else if (c=='%' && (i+2)<length)
            {
                byte b;
                char cn = encoded.charAt(offset+i+1);
                if (cn>='a' && cn<='z')
                    b=(byte)(10+cn-'a');
                else if (cn>='A' && cn<='Z')
                    b=(byte)(10+cn-'A');
                else
                    b=(byte)(cn-'0');
                cn = encoded.charAt(offset+i+2);
                if (cn>='a' && cn<='z')
                    b=(byte)(b*16+10+cn-'a');
                else if (cn>='A' && cn<='Z')
                    b=(byte)(b*16+10+cn-'A');
                else
                    b=(byte)(b*16+cn-'0');

                if (bytes==null)
                {
                    bytes=new byte[length];
                    encoded.getBytes(offset, offset+i, bytes, 0);
                    n=i;
                }
                i+=2;
                bytes[n++]=b;
            }
            else if (n>0)
                bytes[n++] = (byte) c;
        }

        if (bytes==null)
        {
            if (offset==0 && encoded.length()==length)
                return encoded;
            return encoded.substring(offset,offset+length);
        }
        
        try
        {
            return new String(bytes,0,n,charset);
        }
        catch (UnsupportedEncodingException e)
        {
            return new String(bytes,0,n);
        }
        
    }
    
    /* ------------------------------------------------------------ */
    /** Perform URL encoding.
     * Assumes 8859 charset
     * @param string 
     * @return encoded string.
     */
    public static String encodeString(String string)
    {
        return encodeString(string,StringUtil.__UTF8);
    }
    
    /* ------------------------------------------------------------ */
    /** Perform URL encoding.
     * @param string 
     * @return encoded string.
     */
    public static String encodeString(String string,String charset)
    {
        if (charset==null)
            charset=StringUtil.__UTF8;
        byte[] bytes=null;
        try
        {
            bytes=string.getBytes(charset);
        }
        catch(UnsupportedEncodingException e)
        {
            // Log.warn(LogSupport.EXCEPTION,e);
            bytes=string.getBytes();
        }
        
        int len=bytes.length;
        byte[] encoded= new byte[bytes.length*3];
        int n=0;
        boolean noEncode=true;
        
        for (int i=0;i<len;i++)
        {
            byte b = bytes[i];
            
            if (b==' ')
            {
                noEncode=false;
                encoded[n++]=(byte)'+';
            }
            else if (b>='a' && b<='z' ||
                     b>='A' && b<='Z' ||
                     b>='0' && b<='9')
            {
                encoded[n++]=b;
            }
            else
            {
                noEncode=false;
                encoded[n++]=(byte)'%';
                byte nibble= (byte) ((b&0xf0)>>4);
                if (nibble>=10)
                    encoded[n++]=(byte)('A'+nibble-10);
                else
                    encoded[n++]=(byte)('0'+nibble);
                nibble= (byte) (b&0xf);
                if (nibble>=10)
                    encoded[n++]=(byte)('A'+nibble-10);
                else
                    encoded[n++]=(byte)('0'+nibble);
            }
        }

        if (noEncode)
            return string;
        
        try
        {    
            return new String(encoded,0,n,charset);
        }
        catch(UnsupportedEncodingException e)
        {
            // Log.warn(LogSupport.EXCEPTION,e);
            return new String(encoded,0,n);
        }
    }


    /* ------------------------------------------------------------ */
    /** 
     */
    public Object clone()
    {
        return new UrlEncoded(this);
    }
}

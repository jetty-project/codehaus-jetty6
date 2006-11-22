// ========================================================================
// Copyright 2000-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import org.mortbay.io.Buffer;
import org.mortbay.io.BufferCache;
import org.mortbay.io.BufferCache.CachedBuffer;
import org.mortbay.util.StringUtil;


/* ------------------------------------------------------------ */
/** 
 * @author Greg Wilkins
 */
public class MimeTypes
{
    public final static String
      FORM_ENCODED="application/x-www-form-urlencoded",
      MESSAGE_HTTP="message/http",
      MULTIPART_BYTERANGES="multipart/byteranges",
      TEXT_HTML="text/html",
      TEXT_PLAIN="text/plain",
      TEXT_XML="text/xml",
      TEXT_HTML_8859_1="text/html; charset=ISO-8859-1",
      TEXT_PLAIN_8859_1="text/plain; charset=ISO-8859-1",
      TEXT_XML_8859_1="text/xml; charset=ISO-8859-1",
      TEXT_HTML_UTF_8="text/html; charset=UTF-8",
      TEXT_PLAIN_UTF_8="text/plain; charset=UTF-8",
      TEXT_XML_UTF_8="text/xml; charset=UTF-8";
    

    private static int index=1;
    
    private final static int
	    FORM_ENCODED_ORDINAL=index++,
    	MESSAGE_HTTP_ORDINAL=index++,
    	MULTIPART_BYTERANGES_ORDINAL=index++,
    	TEXT_HTML_ORDINAL=index++,
	    TEXT_PLAIN_ORDINAL=index++,
	    TEXT_XML_ORDINAL=index++,
        TEXT_HTML_8859_1_ORDINAL=index++,
        TEXT_PLAIN_8859_1_ORDINAL=index++,
        TEXT_XML_8859_1_ORDINAL=index++,
        TEXT_HTML_UTF_8_ORDINAL=index++,
        TEXT_PLAIN_UTF_8_ORDINAL=index++,
        TEXT_XML_UTF_8_ORDINAL=index++;
    
    public final static BufferCache CACHE = new BufferCache(); 

    public final static CachedBuffer
    	FORM_ENCODED_BUFFER=CACHE.add(FORM_ENCODED,FORM_ENCODED_ORDINAL),
    	MESSAGE_HTTP_BUFFER=CACHE.add(MESSAGE_HTTP, MESSAGE_HTTP_ORDINAL),
    	MULTIPART_BYTERANGES_BUFFER=CACHE.add(MULTIPART_BYTERANGES,MULTIPART_BYTERANGES_ORDINAL),
        
        TEXT_HTML_BUFFER=CACHE.add(TEXT_HTML,TEXT_HTML_ORDINAL),
        TEXT_PLAIN_BUFFER=CACHE.add(TEXT_PLAIN,TEXT_PLAIN_ORDINAL),
        TEXT_XML_BUFFER=CACHE.add(TEXT_XML,TEXT_XML_ORDINAL),
        
    	TEXT_HTML_8859_1_BUFFER=new CachedBuffer(TEXT_HTML_8859_1,TEXT_HTML_8859_1_ORDINAL),
    	TEXT_PLAIN_8859_1_BUFFER=new CachedBuffer(TEXT_PLAIN_8859_1,TEXT_PLAIN_8859_1_ORDINAL),
    	TEXT_XML_8859_1_BUFFER=new CachedBuffer(TEXT_XML_8859_1,TEXT_XML_8859_1_ORDINAL),
        TEXT_HTML_UTF_8_BUFFER=new CachedBuffer(TEXT_HTML_UTF_8,TEXT_HTML_UTF_8_ORDINAL),
        TEXT_PLAIN_UTF_8_BUFFER=new CachedBuffer(TEXT_PLAIN_UTF_8,TEXT_PLAIN_UTF_8_ORDINAL),
        TEXT_XML_UTF_8_BUFFER=new CachedBuffer(TEXT_XML_UTF_8,TEXT_XML_UTF_8_ORDINAL);
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private final static Map __dftMimeMap = new HashMap();
    private final static Map __encodings = new HashMap();
    static
    {
        ResourceBundle mime = ResourceBundle.getBundle("org/mortbay/jetty/mime");
        Enumeration i = mime.getKeys();
        while(i.hasMoreElements())
        {
            String ext = (String)i.nextElement();
            String m = mime.getString(ext);
            __dftMimeMap.put(StringUtil.asciiToLowerCase(ext),normalizeMimeType(m));
        }
        
        ResourceBundle encoding = ResourceBundle.getBundle("org/mortbay/jetty/encoding");
        i = encoding.getKeys();
        while(i.hasMoreElements())
        {
            Buffer type = normalizeMimeType((String)i.nextElement());
            __encodings.put(type,encoding.getString(type.toString()));
        }
        
        TEXT_HTML_BUFFER.setAssociate("ISO-8859-1",TEXT_HTML_8859_1_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("ISO_8859_1",TEXT_HTML_8859_1_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("iso-8859-1",TEXT_HTML_8859_1_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("ISO-8859-1",TEXT_PLAIN_8859_1_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("ISO_8859_1",TEXT_PLAIN_8859_1_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("iso-8859-1",TEXT_PLAIN_8859_1_BUFFER);
        TEXT_XML_BUFFER.setAssociate("ISO-8859-1",TEXT_XML_8859_1_BUFFER);
        TEXT_XML_BUFFER.setAssociate("ISO_8859_1",TEXT_XML_8859_1_BUFFER);
        TEXT_XML_BUFFER.setAssociate("iso-8859-1",TEXT_XML_8859_1_BUFFER);

        TEXT_HTML_BUFFER.setAssociate("UTF-8",TEXT_HTML_UTF_8_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("UTF8",TEXT_HTML_UTF_8_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("utf8",TEXT_HTML_UTF_8_BUFFER);
        TEXT_HTML_BUFFER.setAssociate("utf-8",TEXT_HTML_UTF_8_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("UTF-8",TEXT_PLAIN_UTF_8_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("UTF8",TEXT_PLAIN_UTF_8_BUFFER);
        TEXT_PLAIN_BUFFER.setAssociate("utf-8",TEXT_PLAIN_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate("UTF-8",TEXT_XML_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate("utf8",TEXT_XML_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate("UTF8",TEXT_XML_UTF_8_BUFFER);
        TEXT_XML_BUFFER.setAssociate("utf-8",TEXT_XML_UTF_8_BUFFER);
    }


    /* ------------------------------------------------------------ */
    private Map _mimeMap;
    private Map _encodingMap;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     */
    public MimeTypes()
    {
    }

    /* ------------------------------------------------------------ */
    public synchronized Map getMimeMap()
    {
        return _mimeMap;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param mimeMap A Map of file extension to mime-type.
     */
    public void setMimeMap(Map mimeMap)
    {
        if (mimeMap==null)
        {
            _mimeMap=null;
            return;
        }
        
        Map m=new HashMap();
        Iterator i=mimeMap.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry)i.next();
            m.put(entry.getKey(),normalizeMimeType(entry.getValue().toString()));
        }
        _mimeMap=m;
    }

    /* ------------------------------------------------------------ */
    /** Get the MIME type by filename extension.
     * @param filename A file name
     * @return MIME type matching the longest dot extension of the
     * file name.
     */
    public Buffer getMimeByExtension(String filename)
    {
        Buffer type=null;

        if (filename!=null)
        {
            int i=-1;
            while(type==null)
            {
                i=filename.indexOf(".",i+1);

                if (i<0 || i>=filename.length())
                    break;

                String ext=StringUtil.asciiToLowerCase(filename.substring(i+1));
                if (_mimeMap!=null)
                    type = (Buffer)_mimeMap.get(ext);
                if (type==null)
                    type=(Buffer)__dftMimeMap.get(ext);
            }
        }

        if (type==null)
        {
            if (_mimeMap!=null)
                type=(Buffer)_mimeMap.get("*");
             if (type==null)
                 type=(Buffer)__dftMimeMap.get("*");
        }

        return type;
    }

    /* ------------------------------------------------------------ */
    /** Set a mime mapping
     * @param extension
     * @param type
     */
    public void addMimeMapping(String extension,String type)
    {
        if (_mimeMap==null)
            _mimeMap=new HashMap();
        
        _mimeMap.put(StringUtil.asciiToLowerCase(extension),normalizeMimeType(type));
    }


    /* ------------------------------------------------------------ */
    /** Get the map of mime type to char encoding.
     * @return Map of mime type to character encodings.
     */
    public synchronized Map getEncodingMap()
    {
        return _encodingMap;
    }

    /* ------------------------------------------------------------ */
    /** Set the map of mime type to char encoding.
     * Also sets the org.mortbay.http.encodingMap context attribute
     * @param encodingMap Map of mime type to character encodings.
     */
    public void setEncodingMap(Map encodingMap)
    {
        if (encodingMap==null)
        {
            _encodingMap=null;
            return;
        }
        
        Map e=new HashMap();
        Iterator i=encodingMap.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry)i.next();
            e.put(normalizeMimeType(entry.getKey().toString()),entry.getValue());
        }
        _encodingMap=e;
    }

    /* ------------------------------------------------------------ */
    /** Get char encoding by mime type.
     * @param type A mime type.
     * @return The prefered character encoding for that type if known.
     */
    public String getEncodingByMimeType(String type)
    {
        String encoding =null;

        if (type!=null)
            encoding=(String)_encodingMap.get(type);

        return encoding;
    }

    /* ------------------------------------------------------------ */
    /** Set the encoding that should be used for a mimeType.
     * @param mimeType
     * @param encoding
     */
    public void addTypeEncoding(String mimeType,String encoding)
    {
        getEncodingMap().put(mimeType,encoding);
    }


    /* ------------------------------------------------------------ */
    private static synchronized Buffer normalizeMimeType(String type)
    {
        Buffer b =CACHE.get(type);
        if (b==null)
            b=CACHE.add(type,index++);
        return b;
    }


}

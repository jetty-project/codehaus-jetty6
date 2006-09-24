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

import java.io.UnsupportedEncodingException;



/* ------------------------------------------------------------ */
/** URI Holder.
 * This class assists with the decoding and encoding or HTTP URI's.
 * It differs from the java.net.URL class as it does not provide
 * communications ability, but it does assist with query string
 * formatting.
 * <P>UTF-8 encoding is used by default for % encoded characters. This
 * may be overridden with the org.mortbay.util.URI.charset system property.
 * @see UrlEncoded
 * @author Greg Wilkins (gregw)
 */
public class URIUtil
    implements Cloneable
{
    public static final String HTTP="http";
    public static final String HTTP_COLON="http:";
    public static final String HTTPS="https";
    public static final String HTTPS_COLON="https:";

    public static final String __CHARSET=System.getProperty("org.mortbay.util.URI.charset",StringUtil.__UTF8);
    
    private URIUtil()
    {}
    
    /* ------------------------------------------------------------ */
    /** Encode a URI path.
     * This is the same encoding offered by URLEncoder, except that
     * the '/' character is not encoded.
     * @param path The path the encode
     * @return The encoded path
     */
    public static String encodePath(String path)
    {
        if (path==null || path.length()==0)
            return path;
        
        StringBuffer buf = encodePath(null,path);
        return buf==null?path:buf.toString();
    }
        
    /* ------------------------------------------------------------ */
    /** Encode a URI path.
     * @param path The path the encode
     * @param buf StringBuffer to encode path into (or null)
     * @return The StringBuffer or null if no substitutions required.
     */
    public static StringBuffer encodePath(StringBuffer buf, String path)
    {
        if (buf==null)
        {
        loop:
            for (int i=0;i<path.length();i++)
            {
                char c=path.charAt(i);
                switch(c)
                {
                  case '%':
                  case '?':
                  case ';':
                  case '#':
                  case ' ':
                      buf=new StringBuffer(path.length()<<1);
                      break loop;
                }
            }
            if (buf==null)
                return null;
        }
        
        synchronized(buf)
        {
            for (int i=0;i<path.length();i++)
            {
                char c=path.charAt(i);       
                switch(c)
                {
                  case '%':
                      buf.append("%25");
                      continue;
                  case '?':
                      buf.append("%3F");
                      continue;
                  case ';':
                      buf.append("%3B");
                      continue;
                  case '#':
                      buf.append("%23");
                      continue;
                  case ' ':
                      buf.append("%20");
                      continue;
                  default:
                      buf.append(c);
                      continue;
                }
            }
        }

        return buf;
    }
    
    /* ------------------------------------------------------------ */
    /** Encode a URI path.
     * @param path The path the encode
     * @param buf StringBuffer to encode path into (or null)
     * @param encode String of characters to encode. % is always encoded.
     * @return The StringBuffer or null if no substitutions required.
     */
    public static StringBuffer encodeString(StringBuffer buf,
                                            String path,
                                            String encode)
    {
        if (buf==null)
        {
        loop:
            for (int i=0;i<path.length();i++)
            {
                char c=path.charAt(i);
                if (c=='%' || encode.indexOf(c)>=0)
                {    
                    buf=new StringBuffer(path.length()<<1);
                    break loop;
                }
            }
            if (buf==null)
                return null;
        }
        
        synchronized(buf)
        {
            for (int i=0;i<path.length();i++)
            {
                char c=path.charAt(i);
                if (c=='%' || encode.indexOf(c)>=0)
                {
                    buf.append('%');
                    StringUtil.append(buf,(byte)(0xff&c),16);
                }
                else
                    buf.append(c);
            }
        }

        return buf;
    }
    
    /* ------------------------------------------------------------ */
    /* Decode a URI path.
     * @param path The path the encode
     * @param buf StringBuffer to encode path into
     */
    public static String decodePath(String path)
    {
        int len=path.length();
        byte[] bytes=null;
        int n=0;
        boolean noDecode=true;
        
        for (int i=0;i<len;i++)
        {
            char c = path.charAt(i);
            if (c<0||c>0xff)
                throw new IllegalArgumentException("Not decoded");
            
            byte b = (byte)(0xff & c);

            if (c=='%' && (i+2)<len)
            {
                noDecode=false;
                b=(byte)(0xff&TypeUtil.parseInt(path,i+1,2,16));
                i+=2;
            }
            else if (bytes==null)
            {
                n++;
                continue;
            }
            
            if (bytes==null)
            {
                noDecode=false;
                bytes=new byte[len];
                for (int j=0;j<n;j++)
                    bytes[j]=(byte)(0xff & path.charAt(j));                
            }
            
            bytes[n++]=b;
        }

        if (noDecode)
            return path;

        try
        {    
            return new String(bytes,0,n,__CHARSET);
        }
        catch(UnsupportedEncodingException e)
        {
            return new String(bytes,0,n);
        }
    }
    
    /* ------------------------------------------------------------ */
    /* Decode a URI path.
     * @param path The path the encode
     * @param buf StringBuffer to encode path into
     */
    public static String decodePath(byte[] buf, int offset, int length)
    {
        byte[] bytes=null;
        int n=0;
        
        for (int i=0;i<length;i++)
        {
            byte b = buf[i + offset];
            
            if (b=='%' && (i+2)<length)
            {
                b=(byte)(0xff&TypeUtil.parseInt(buf,i+offset+1,2,16));
                i+=2;
            }
            else if (bytes==null)
            {
                n++;
                continue;
            }
            
            if (bytes==null)
            {
                bytes=new byte[length];
                for (int j=0;j<n;j++)
                    bytes[j]=buf[j + offset];
            }
            
            bytes[n++]=b;
        }
        
        try
        {
            if (bytes==null)
                return new String(buf,offset,length,__CHARSET);
            return new String(bytes,0,n,__CHARSET);
        }
        catch(UnsupportedEncodingException e)
        {
            if (bytes==null)
                return new String(buf,offset,length);
            return new String(bytes,0,n);
        }
    }

    
    /* ------------------------------------------------------------ */
    /** Add two URI path segments.
     * Handles null and empty paths, path and query params (eg ?a=b or
     * ;JSESSIONID=xxx) and avoids duplicate '/'
     * @param p1 URI path segment 
     * @param p2 URI path segment
     * @return Legally combined path segments.
     */
    public static String addPaths(String p1, String p2)
    {
        if (p1==null || p1.length()==0)
        {
            if (p1!=null && p2==null)
                return p1;
            return p2;
        }
        if (p2==null || p2.length()==0)
            return p1;
        
        int split=p1.indexOf(';');
        if (split<0)
            split=p1.indexOf('?');
        if (split==0)
            return p2+p1;
        if (split<0)
            split=p1.length();

        StringBuffer buf = new StringBuffer(p1.length()+p2.length()+2);
        buf.append(p1);
        
        if (buf.charAt(split-1)=='/')
        {
            if (p2.startsWith("/"))
            {
                buf.deleteCharAt(split-1);
                buf.insert(split-1,p2);
            }
            else
                buf.insert(split,p2);
        }
        else
        {
            if (p2.startsWith("/"))
                buf.insert(split,p2);
            else
            {
                buf.insert(split,'/');
                buf.insert(split+1,p2);
            }
        }

        return buf.toString();
    }
    
    /* ------------------------------------------------------------ */
    /** Return the parent Path.
     * Treat a URI like a directory path and return the parent directory.
     */
    public static String parentPath(String p)
    {
        if (p==null || "/".equals(p))
            return null;
        int slash=p.lastIndexOf('/',p.length()-2);
        if (slash>=0)
            return p.substring(0,slash+1);
        return null;
    }
    
    /* ------------------------------------------------------------ */
    /** Strip parameters from a path.
     * Return path upto any semicolon parameters.
     */
    public static String stripPath(String path)
    {
        if (path==null)
            return null;
        int semi=path.indexOf(';');
        if (semi<0)
            return path;
        return path.substring(0,semi);
    }
    
    /* ------------------------------------------------------------ */
    /** Convert a path to a cananonical form.
     * All instances of "//", "." and ".." are factored out.  Null is returned
     * if the path tries to .. above its root.
     * @param path 
     * @return path or null.
     */
    public static String canonicalPath(String path)
    {
        if (path==null || path.length()==0)
            return path;

        int end=path.length();
        int queryIdx=path.indexOf('?');
        int start = path.lastIndexOf('/', (queryIdx > 0 ? queryIdx : end));

    search:
        while (end>=0)
        {
            switch(end-start)
            {
              case 1: // double slash
                  if (start<0 || end==path.length())
                      break;
                  break search;
              case 2: // possible single dot
                  if (path.charAt(start+1)!='.')
                      break;
                  break search;
              case 3: // possible double dot
                  if (path.charAt(start+1)!='.' || path.charAt(start+2)!='.')
                      break;
                  break search;
            }
            
            end=start;
            start=path.lastIndexOf('/',end-1);
        }

        // If we have checked the entire string
        if (start>=end)
            return path;
        
        StringBuffer buf = new StringBuffer(path);
        int delStart=-1;
        int delEnd=-1;
        int skip=0;
        
        while (end>=0)
        {
            switch(end-start)
            {
              case 1: // double slash
                  if (start<0 || end==path.length())
                      break;
                  delStart=start;
                  if (delEnd<0)
                      delEnd=end;
                  if(delStart==0 && delEnd==buf.length())
                  {
                      delStart++;
                      break;
                  }
                          
                  end=start--;
                  while (start>=0 && buf.charAt(start)!='/')
                      start--;
                  continue;
                  
              case 2: // possible single dot
                  if (buf.charAt(start+1)!='.')
                  {
                      if (skip>0 && --skip==0)
                          delStart=start>=0?start:0;
                      break;
                  }
                  
                  if(delEnd<0)
                      delEnd=end;
                  delStart=start;
                  if (delStart<0 || delStart==0&&buf.charAt(delStart)=='/')
                  {
                      delStart++;
                      if (delEnd<buf.length() && buf.charAt(delEnd)=='/')
                          delEnd++;
                      break;
                  }
                  end=start--;
                  while (start>=0 && buf.charAt(start)!='/')
                      start--;
                  continue;
                  
              case 3: // possible double dot
                  if (buf.charAt(start+1)!='.' || buf.charAt(start+2)!='.')
                  {
                      if (skip>0 && --skip==0)
                          delStart=start>=0?start:0;
                      break;
                  }
                  
                  delStart=start;
                  if (delEnd<0)
                      delEnd=end;

                  skip++;
                  
                  end=start--;
                  while (start>=0 && buf.charAt(start)!='/')
                      start--;
                  continue;

              default:
                  if (skip>0 && --skip==0)
                      delStart=start>=0?start:0;
            }            

            // Do the delete
            if (skip<=0 && delStart>=0 && delStart>=0)
            {
                buf.delete(delStart,delEnd);
                delStart=delEnd=-1;
                if (skip>0)
                    delEnd=end;
            }
            
            end=start--;
            while (start>=0 && buf.charAt(start)!='/')
                start--;
        }      

        // Too many ..
        if (skip>0)
            return null;
        
        // Do the delete
        if (delEnd>=0)
            buf.delete(delStart,delEnd);

        // If it was a dir, keep it a dir
        if (path.endsWith(".") && buf.length()>0 && buf.charAt(buf.length()-1)!='/')
            buf.append('/');

        return buf.toString();
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param uri URI
     * @return True if the uri has a scheme
     */
    public static boolean hasScheme(String uri)
    {
        for (int i=0;i<uri.length();i++)
        {
            char c=uri.charAt(i);
            if (c==':')
                return true;
            if (!(c>='a'&&c<='z' ||
                  c>='A'&&c<='Z' ||
                  (i>0 &&(c>='0'&&c<='9' ||
                          c=='.' ||
                          c=='+' ||
                          c=='-'))
                  ))
                break;
        }
        return false;
    }
    
}




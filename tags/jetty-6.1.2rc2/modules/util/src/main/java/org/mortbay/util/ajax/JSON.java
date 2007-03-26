// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.util.ajax;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortbay.util.IO;
import org.mortbay.util.LazyList;
import org.mortbay.util.QuotedStringTokenizer;
import org.mortbay.util.TypeUtil;


/** JSON Parser and Generator.
 * 
 * <p>This class provides some static methods to convert POJOs to and from JSON
 * notation.  The mapping from JSON to java is:<pre>
 *   object ==> Map
 *   array  ==> Object[]
 *   number ==> Double or Long
 *   string ==> String
 *   null   ==> null
 *   bool   ==> Boolean
 * </pre>
 * </p><p>
 * The java to JSON mapping is:<pre>
 *   String --> string
 *   Number --> number
 *   Map    --> object
 *   List   --> array
 *   Array  --> array
 *   null   --> null
 *   Boolean--> boolean
 *   Object --> string (dubious!)
 * </pre>
 * </p><p>
 * The interface {@link JSON.Generator} may be implemented by classes that know how to render themselves as JSON and
 * the {@link #toString(Object)} method will use {@link JSON.Generator#addJSON(StringBuffer)} to generate the JSON.
 * The class {@link JSON.Literal} may be used to hold pre-gnerated JSON object. 
 * </p>
 * @author gregw
 *
 */
public class JSON
{
    private JSON(){}
    
    public static String toString(Object object)
    {
        StringBuffer buffer = new StringBuffer();
        append(buffer,object);
        return buffer.toString();
    }
    
    public static String toString(Map object)
    {
        StringBuffer buffer = new StringBuffer();
        appendMap(buffer,object);
        return buffer.toString();
    }
    
    public static String toString(Object[] array)
    {
        StringBuffer buffer = new StringBuffer();
        appendArray(buffer,array);
        return buffer.toString();
    }

    /**
     * @param s String containing JSON object or array.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(String s)
    {
        return parse(new Source(s));
    }

    /**
     * @param s Stream containing JSON object or array.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(InputStream in) throws IOException
    {
        String s=IO.toString(in);
        return parse(new Source(s));
    }
    
    /**
     * Append object as JSON to string buffer.
     * @param buffer
     * @param object
     */
    public static void append(StringBuffer buffer, Object object)
    {
        if (object==null)
            buffer.append("null");
        else if (object instanceof Generator)
            appendJSON(buffer, (Generator)object);
        else if (object instanceof Map)
            appendMap(buffer, (Map)object);
        else if (object instanceof List)
            appendArray(buffer,LazyList.toArray(object,Object.class));
        else if (object.getClass().isArray())
            appendArray(buffer,object);
        else if (object instanceof Number)
            appendNumber(buffer,(Number)object);
        else if (object instanceof Boolean)
            appendBoolean(buffer,(Boolean)object);
        else if (object instanceof String)
            appendString(buffer,(String)object);
        else 
            // TODO - maybe some bean stuff?
            appendString(buffer,object.toString());
    }

    private static void appendNull(StringBuffer buffer)
    {
        buffer.append("null");
    }

    private static void appendJSON(StringBuffer buffer, Generator generator)
    {
        generator.addJSON(buffer);
    }
    
    private static void appendMap(StringBuffer buffer, Map object)
    {
        if (object==null)
        {
            appendNull(buffer);
            return;
        }
        
        buffer.append('{');
        Iterator iter = object.entrySet().iterator();
        while(iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();
            QuotedStringTokenizer.quote(buffer,entry.getKey().toString());
            buffer.append(':');
            append(buffer,entry.getValue());
            if (iter.hasNext())
                buffer.append(',');
        }

        buffer.append('}');
    }
    
    private static void appendArray(StringBuffer buffer, Object array)
    {
        if (array==null)
        {
            appendNull(buffer);
            return;
        }

        buffer.append('[');
        int length = Array.getLength(array);
        
        for (int i=0;i<length;i++)
        {
            if(i!=0)
                buffer.append(',');
            append(buffer,Array.get(array,i));
        }
        
        buffer.append(']');
    }

    private static void appendBoolean(StringBuffer buffer, Boolean b)
    {
        if (b==null)
        {
            appendNull(buffer);
            return;
        }
        buffer.append(b.booleanValue()?"true":"false");
    }
    
    private static void appendNumber(StringBuffer buffer, Number number)
    {
        if (number==null)
        {
            appendNull(buffer);
            return;
        }
        buffer.append(number);
    }
    
    private static void appendString(StringBuffer buffer, String string)
    {
        if (string==null)
        {
            appendNull(buffer);
            return;
        }

        QuotedStringTokenizer.quote(buffer,string);
    }
    
    private static Object parse(Source source)
    {
        int comment_state=0;
        
        while(source.hasNext())
        {
            char c=source.peek();
            
            // handle // or /* comment
            if(comment_state==1)
            {
                switch(c)
                {
                    case '/' : 
                            comment_state=-1;
                            break;
                    case '*' : 
                        comment_state=2;
                }
            }
            // handle /* */ comment
            else if (comment_state>1)
            {
                switch(c)
                {
                    case '*' : 
                        comment_state=3;
                        break;
                    case '/' : 
                        if (comment_state==3)
                            comment_state=0;
                        else
                            comment_state=2;
                        break;
                    default:
                        comment_state=2;
                }
            }
            // handle // comment
            else if (comment_state<0)
            {
                switch(c)
                {
                    case '\r' : 
                    case '\n' : 
                        comment_state=0;
                        break;
                    default:
                        break;
                }
            }
            // handle unknown
            else
            {
                switch(c)
                {
                    case '{' : 
                        return parseObject(source);
                    case '[' : 
                        return parseArray(source);
                    case '"' : 
                        return parseString(source);
                    case '-' : 
                        return parseNumber(source);
                        
                    case 'n' : 
                        complete("null",source);
                        return null;
                    case 't' : 
                        complete("true",source);
                        return Boolean.TRUE;
                    case 'f' : 
                        complete("false",source);
                        return Boolean.FALSE;
                        
                    case '/' :
                        comment_state=1;
                        break;

                    default : 
                        if (Character.isDigit(c))
                            return parseNumber(source);
                        else if (Character.isWhitespace(c))
                            break;

                        throw new IllegalStateException("unknown char "+c);
                }
            }
            source.next();
        }
        
        return null;
    }
    
    private static Map parseObject(Source source)
    {
        if (source.next()!='{')
            throw new IllegalStateException();
        Map map = new HashMap();

        char next = seekTo("\"}",source);
        
        while(source.hasNext())
        {   
            if (next=='}')
            {
                source.next();
                break;
            }
                
            String name=parseString(source);
            seekTo(':',source);
            source.next();
            
            Object value=parse(source);
            map.put(name,value);
            
            seekTo(",}",source);
            next=source.next();
            if (next=='}')
                break;
            else
                next = seekTo("\"}",source);
        }
     
        return map;
    }
    
    private static Object parseArray(Source source)
    {
        if (source.next()!='[')
            throw new IllegalStateException();

        ArrayList list=new ArrayList();
        boolean coma=true;
        
        while(source.hasNext())
        {
            char c=source.peek();
            switch(c)
            {
                case ']':
                    source.next();
                    return list.toArray(new Object[list.size()]);

                case ',':
                    if (coma)
                        throw new IllegalStateException();
                    coma=true;
                    source.next();

                default:
                    if (Character.isWhitespace(c))
                        source.next();
                    else
                    {
                        coma=false;
                        list.add(parse(source));
                    }
            }

        }

        throw new IllegalStateException("unexpected end of array");
    }
    
    private static String parseString(Source source)
    {
        if (source.next()!='"')
            throw new IllegalStateException();
        
        boolean escape=false;
        StringBuffer b = new StringBuffer();
        while(source.hasNext())
        {
            char c=source.next();

            if (escape)
            {
                escape=false;
                switch (c)
                {
                    case 'n':
                        b.append('\n');
                        break;
                    case 'r':
                        b.append('\r');
                        break;
                    case 't':
                        b.append('\t');
                        break;
                    case 'f':
                        b.append('\f');
                        break;
                    case 'b':
                        b.append('\b');
                        break;
                    case 'u':
                        b.append((char)(
                                (TypeUtil.convertHexDigit((byte)source.next())<<24)+
                                (TypeUtil.convertHexDigit((byte)source.next())<<16)+
                                (TypeUtil.convertHexDigit((byte)source.next())<<8)+
                                (TypeUtil.convertHexDigit((byte)source.next()))
                                ) 
                        );
                        break;
                    default:
                        b.append(c);
                }
            }
            else if (c=='\\')
            {
                escape=true;
                continue;
            }
            else if (c=='\"')
                break;
            else
                b.append(c);
        }
            
        return b.toString();
    }
    
    private static Number parseNumber(Source source)
    {
        int start=source.index();
        int end=-1;
        boolean is_double=false;
        while(source.hasNext()&&end<0)
        {
            char c=source.peek();
            switch(c)
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                    source.next();
                    break;
                    
                case '.':
                case 'e':
                case 'E':
                    is_double=true;
                    source.next();
                    break;
                    
                default:
                    end=source.index();
            }
        }
        String s = end>=0?source.from(start,end):source.from(start);
        if (is_double)
            return new Double(s);
        else
            return new Long(s);
    }
    
    private static void seekTo(char seek, Source source)
    {
        while(source.hasNext())
        {
            char c=source.peek();
            if (c==seek)
                return;
            
            if (!Character.isWhitespace(c))
                throw new IllegalStateException("Unexpected '"+c+" while seeking '"+seek+"'");
            source.next();
        }

        throw new IllegalStateException("Expected '"+seek+"'");
    }
    
    private static char seekTo(String seek, Source source)
    {
        while(source.hasNext())
        {
            char c=source.peek();
            if(seek.indexOf(c)>=0)
            {
                return c;
            }
            
            if (!Character.isWhitespace(c))
                throw new IllegalStateException("Unexpected '"+c+"' while seeking one of '"+seek+"'");
            source.next();
        }

        throw new IllegalStateException("Expected one of '"+seek+"'");
    }
    
    private static void complete(String seek, Source source)
    {
        int i=0;
        while(source.hasNext()&& i<seek.length())
        {
            char c=source.next();
            if(c!=seek.charAt(i++))
                throw new IllegalStateException("Unexpected '"+c+" while seeking  \""+seek+"\"");
        }

        if (i<seek.length())
            throw new IllegalStateException("Expected \""+seek+"\"");
    }
    
    
    private static class Source
    {
        private final String string;
        private int index;
        
        Source(String s)
        {
            string=s;
        }
        
        boolean hasNext()
        {
            return (index<string.length());
        }
        
        char next()
        {
            return string.charAt(index++);
        }
        
        char peek()
        {
            return string.charAt(index);
        }
        
        int index()
        {
            return index;
        }
        
        String from(int mark)
        {
            return string.substring(mark,index);
        }
        
        String from(int mark,int end)
        {
            return string.substring(mark,end);
        }
    }
    
    public interface Generator
    {
        public void addJSON(StringBuffer buffer);
    }
    
    /* ------------------------------------------------------------ */
    /** A Literal JSON generator
     * A utility instance of {@link JSON.Generator} that holds a pre-generated string on JSON text.
     */
    public static class Literal implements Generator
    {
        private String _json;
        /* ------------------------------------------------------------ */
        /** Construct a literal JSON instance for use by {@link JSON#toString(Object)}.
         * @param json A literal JSON string that will be parsed to check validity.
         */
        public Literal(String json)
        {
            parse(json);
            _json=json;
        }
        
        public String toString()
        {
            return _json;
        }
        
        public void addJSON(StringBuffer buffer)
        {
            buffer.append(_json);
        }
    }
}

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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortbay.log.Log;
import org.mortbay.util.IO;
import org.mortbay.util.LazyList;
import org.mortbay.util.Loader;
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
 * The interface {@link JSON.Convertible} may be implemented by classes that wish to externalize and 
 * initialize specific fields to and from JSON objects.  Only directed acyclic graphs of objects are supported.
 * </p>
 * <p>
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
     * @param stripOuterComment If true, an outer comment around the JSON is ignored.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(String s,boolean stripOuterComment)
    {
        return parse(new StringSource(s),stripOuterComment);
    }

    /**
     * @param s Stream containing JSON object or array.
     * @param stripOuterComment If true, an outer comment around the JSON is ignored.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(Reader in,boolean stripOuterComment) throws IOException
    {
        return parse(new ReaderSource(in),stripOuterComment);
    }
    
    /**
     * @param s String containing JSON object or array.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(String s)
    {
        return parse(new StringSource(s),false);
    }

    /**
     * @param in Reader containing JSON object or array.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(Reader in) throws IOException
    {
        return parse(new ReaderSource(in),false);
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
        else if (object instanceof Convertible)
            appendJSON(buffer, (Convertible)object);
        else if (object instanceof Generator)
            appendJSON(buffer, (Generator)object);
        else if (object instanceof Map)
            appendMap(buffer, (Map)object);
        else if (object instanceof List)
            appendArray(buffer,((List) object).toArray ());
        else if (object instanceof Collection)
            appendArray(buffer,((Collection)object).toArray());
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

    private static void appendJSON(final StringBuffer buffer, Convertible converter)
    {
        buffer.append('{');
        converter.toJSON(new Output(){
            char c=0;
            public void addClass(Class type)
            {
                if (c>0)
                    buffer.append(c);
                buffer.append("\"class\":");
                append(buffer,type.getName());
                c=',';
            }
            public void add(String name, Object value)
            {
                if (c>0)
                    buffer.append(c);
                QuotedStringTokenizer.quote(buffer,name);
                buffer.append(':');
                append(buffer,value);
                c=',';
            }

            public void add(String name, double value)
            {
                if (c>0)
                    buffer.append(c);
                QuotedStringTokenizer.quote(buffer,name);
                buffer.append(':');
                appendNumber(buffer,new Double(value));
                c=',';
            }

            public void add(String name, long value)
            {
                if (c>0)
                    buffer.append(c);
                QuotedStringTokenizer.quote(buffer,name);
                buffer.append(':');
                appendNumber(buffer,new Long(value));
                c=',';
            }

            public void add(String name, boolean value)
            {
                if (c>0)
                    buffer.append(c);
                QuotedStringTokenizer.quote(buffer,name);
                buffer.append(':');
                appendBoolean(buffer,value?Boolean.TRUE:Boolean.FALSE);
                c=',';
            }
        });
        buffer.append('}');
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
    
    private static Object parse(Source source,boolean stripOuterComment)
    {
        int comment_state=0;                   // 0=no comment, 1="/", 2="/*", 3="/* *" -1="//"
        int strip_state=stripOuterComment?1:0; // 0=no strip, 1=wait for /*, 2= wait for */
        
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
                        if (strip_state==1)
                        {
                            comment_state=0;
                            strip_state=2;
                        }
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
                    case 'u' : 
                        complete("undefined",source);
                        return null;
                        
                    case '/' :
                        comment_state=1;
                        break;

                    case '*' : 
                        if (strip_state==2)
                        {
                            complete("*/",source);
                            strip_state=0;
                        }
                        return null;
                        
                    default : 
                        if (Character.isDigit(c))
                            return parseNumber(source);
                        else if (Character.isWhitespace(c))
                            break;
                        throw new IllegalStateException("unknown char "+(int)c);
                }
            }
            source.next();
        }
        
        return null;
    }
    
    private static Object parseObject(Source source)
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
            
            Object value=parse(source,false);
            map.put(name,value);
            
            seekTo(",}",source);
            next=source.next();
            if (next=='}')
                break;
            else
                next = seekTo("\"}",source);
        }
     
        String classname = (String)map.get("class");
        if (classname!=null)
        {
            try
            {
                Class c = Loader.loadClass(JSON.class,classname);
                if (c!=null && Convertible.class.isAssignableFrom(c));
                {
                    try
                    {
                        Convertible conv = (Convertible)c.newInstance();
                        conv.fromJSON(map);
                        return conv; 
                    }
                    catch(Exception e)
                    {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
            catch(ClassNotFoundException e)
            {
                e.printStackTrace();
            }
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
                        list.add(parse(source,false));
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
        boolean minus=false;
        long number=0;
        StringBuilder buffer=null;

        
        longLoop:
        while(source.hasNext())
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
                    number=number*10+(c-'0');
                    source.next();
                    break;

                case '-':
                    if (number!=0)
                        throw new IllegalStateException("bad number");
                    minus=true;
                    source.next();
                    break;
                    
                case '.':
                case 'e':
                case 'E':
                    buffer=new StringBuilder(16);
                    buffer.append(minus?-1*number:number);
                    buffer.append(c);
                    source.next();
                    break longLoop;
                    
                default:
                    break longLoop;
            }
        }
        
        if (buffer==null)
            return new Long(number);
    
        doubleLoop:
        while(source.hasNext())
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
                case '.':
                case 'e':
                case 'E':
                    buffer.append(c);
                    source.next();
                    break;
                    
                default:
                    break doubleLoop;
            }
        }
        return new Double(buffer.toString());
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
    

    private interface Source
    {
        boolean hasNext();
        char next();
        char peek();
    }
    
    private static class StringSource implements Source
    {
        private final String string;
        private int index;
        
        StringSource(String s)
        {
            string=s;
        }
        
        public boolean hasNext()
        {
            return (index<string.length());
        }
        
        public char next()
        {
            return string.charAt(index++);
        }
        
        public char peek()
        {
            return string.charAt(index);
        }
    }
    
    private static class ReaderSource implements Source
    {
        private Reader _reader;
        private int _next=-1;
        
        ReaderSource(Reader r)
        {
            _reader=r;
        }
        
        public boolean hasNext()
        {
            getNext();
            return _next>=0;
        }
        
        public char next()
        {
            getNext();
            char c= (char)_next;
            _next=-1;
            return c;
        }
        
        public char peek()
        { 
            getNext();
            return (char)_next;
        }
        
        private void getNext()
        {
            if (_next<0)
            {
                try 
                {
                    _next=_reader.read();
                }
                catch(IOException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * JSON Output class for use by {@link Convertible}.
     */
    public interface Output
    {
        public void addClass(Class c);
        public void add(String name,Object value);
        public void add(String name,double value);
        public void add(String name,long value);
        public void add(String name,boolean value);
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** JSON Convertible object.
     * Object can implement this interface in a similar way to the 
     * {@link Externalizable} interface is used to allow classes to
     * provide their own serialization mechanism.
     * <p>
     * A JSON.Convertible object may be written to a JSONObject 
     * or initialized from a Map of field names to values.
     * <p>
     * If the JSON is to be convertible back to an Object, then
     * the method {@link Output#addClass(Class)} must be called from within toJSON()
     * @author gregw
     *
     */
    public interface Convertible
    {
        public void toJSON(Output out) ;
        public void fromJSON(Map object);
    }

    /* ------------------------------------------------------------ */
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
         * If {@link Log#isDebugEnabled()} is true, the JSON will be parsed to check validity
         * @param json A literal JSON string. 
         */
        public Literal(String json)
        {
            if (Log.isDebugEnabled())
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

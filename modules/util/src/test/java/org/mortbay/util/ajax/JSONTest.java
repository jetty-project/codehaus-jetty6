package org.mortbay.util.ajax;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.util.ajax.JSON;
import org.mortbay.util.ajax.JSON.Output;

import junit.framework.TestCase;

public class JSONTest extends TestCase
{

    public void testToString()
    {
        HashMap map = new HashMap();
        HashMap obj6 = new HashMap();
        HashMap obj7 = new HashMap();
        
        Woggle w0 = new Woggle();
        Woggle w1 = new Woggle();
        
        w0.name="woggle0";
        w0.nested=w1;
        w0.number=100;
        w1.name="woggle1";
        w1.nested=null;
        w1.number=101;
       
        
        map.put("n1",null);
        map.put("n2",new Integer(2));
        map.put("n3",new Double(-0.00000000003));
        map.put("n4","4\n\r\t\"4");
        map.put("n5",new Object[]{"a",new Character('b'),new Integer(3),new String[]{},null,Boolean.TRUE,Boolean.FALSE});
        map.put("n6",obj6);
        map.put("n7",obj7);
        map.put("n8",new int[]{1,2,3,4});
        map.put("n9",new JSON.Literal("[{},  [],  {}]"));
        map.put("w0",w0);
        
        obj7.put("x","value");
        
        String s = JSON.toString(map);
        System.err.println(s);
        assertTrue(s.indexOf("\"n1\":null")>=0);
        assertTrue(s.indexOf("\"n2\":2")>=0);
        assertTrue(s.indexOf("\"n3\":-3.0E-11")>=0);
        assertTrue(s.indexOf("\"n4\":\"4\\n")>=0);
        assertTrue(s.indexOf("\"n5\":[\"a\",\"b\",")>=0);
        assertTrue(s.indexOf("\"n6\":{}")>=0);
        assertTrue(s.indexOf("\"n7\":{\"x\":\"value\"}")>=0);
        assertTrue(s.indexOf("\"n8\":[1,2,3,4]")>=0);
        assertTrue(s.indexOf("\"n9\":[{},  [],  {}]")>=0);
        assertTrue(s.indexOf("\"w0\":{\"class\":\"org.mortbay.util.ajax.JSONTest$Woggle\",\"name\":\"woggle0\",\"nested\":{\"class\":\"org.mortbay.util.ajax.JSONTest$Woggle\",\"name\":\"woggle1\",\"nested\":null,\"number\":101},\"number\":100}")>=0);
    }
    
    public void testParse()
    {
        String test="\n\n\n\t\t    "+
        "// ignore this ,a [ \" \n"+
        "/* and this \n" +
        "/* and * // this \n" +
        "*/" +
        "{ "+
        "\"onehundred\" : 100  ,"+
        "\"name\" : \"fred\"  ," +
        "\"empty\" : {}  ," +
        "\"map\" : {\"a\":-1.0e2}  ," +
        "\"array\" : [\"a\",-1.0e2,[],null,true,false]  ," +
        "\"w0\":{\"class\":\"org.mortbay.util.ajax.JSONTest$Woggle\",\"name\":\"woggle0\",\"nested\":{\"class\":\"org.mortbay.util.ajax.JSONTest$Woggle\",\"name\":\"woggle1\",\"nested\":null,\"number\":101},\"number\":100}" +
        "}";
        
        Map map = (Map)JSON.parse(test);
        System.err.println(map);
        assertEquals(new Long(100),map.get("onehundred"));
        assertEquals("fred",map.get("name"));
        assertTrue(map.get("array").getClass().isArray());
        assertTrue(map.get("w0") instanceof Woggle);
        assertTrue(((Woggle)map.get("w0")).nested instanceof Woggle);
        
        test="{\"data\":{\"source\":\"15831407eqdaawf7\",\"widgetId\":\"Magnet_8\"},\"channel\":\"/magnets/moveStart\",\"connectionId\":null,\"clientId\":\"15831407eqdaawf7\"}";
        map = (Map)JSON.parse(test);
    }
    
    
    public void testStripComment()
    {
        String test="\n\n\n\t\t    "+
        "// ignore this ,a [ \" \n"+
        "/* "+
        "{ "+
        "\"onehundred\" : 100  ,"+
        "\"name\" : \"fred\"  ," +
        "\"empty\" : {}  ," +
        "\"map\" : {\"a\":-1.0e2}  ," +
        "\"array\" : [\"a\",-1.0e2,[],null,true,false]  ," +
        "} */";
        
        Object o = JSON.parse(test,false);
        assertTrue(o==null);
        o = JSON.parse(test,true);
        assertTrue(o instanceof Map);
        assertEquals("fred",((Map)o).get("name"));
        
    }
    
    
    public static class Woggle implements JSON.Convertible
    {
        String name;
        Woggle nested;
        int number;
        
        public Woggle()
        {
        }
        
        public void fromJSON(Map object)
        {
            name=(String)object.get("name");
            nested=(Woggle)object.get("nested");
            number=((Number)object.get("number")).intValue();
        }

        public void toJSON(Output out)
        {
            out.addClass(Woggle.class);
            out.add("name",name);
            out.add("nested",nested);
            out.add("number",number);
        }
        
        public String toString()
        {
            return name+"<<"+nested+">>"+number;
        }
        
    }
}

package org.mortbay.cometd;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class JSONTest extends TestCase
{

    public void testToString()
    {
        HashMap map = new HashMap();
        HashMap obj6 = new HashMap();
        HashMap obj7 = new HashMap();
        
        map.put("n1",null);
        map.put("n2",new Integer(2));
        map.put("n3",new Double(-0.00000000003));
        map.put("n4","4\n\r\t\"4");
        map.put("n5",new Object[]{"a",new Character('b'),new Integer(3),new String[]{},null,Boolean.TRUE,Boolean.FALSE});
        map.put("n6",obj6);
        map.put("n7",obj7);
        map.put("n8",new int[]{1,2,3,4});
        map.put("n9",new JSON.Literal("[{},  [],  {}]"));
        
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
        "}";
        
        Map map = (Map)JSON.parse(test);
        System.err.println(JSON.toString(map));
        assertEquals(new Long(100),map.get("onehundred"));
        assertEquals("fred",map.get("name"));
        assertTrue(map.get("array").getClass().isArray());
        
        test="{\"data\":{\"source\":\"15831407eqdaawf7\",\"widgetId\":\"Magnet_8\"},\"channel\":\"/magnets/moveStart\",\"connectionId\":null,\"clientId\":\"15831407eqdaawf7\"}";
        map = (Map)JSON.parse(test);
    }
}

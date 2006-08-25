package org.mortbay.cometd;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortbay.util.LazyList;

public class JSONDataFilter implements DataFilter
{
    public Object filter(Object data)
    {
        if (data==null)
            return null;
        
        if (data instanceof Map)
            return filterMap((Map)data);
        if (data instanceof List)
            return filterArray(LazyList.toArray(data,Object.class));
        if (data.getClass().isArray() )
            return filterArray(data);
        if (data instanceof Number)
            return filterNumber((Number)data);
        if (data instanceof Boolean)
            return filterBoolean((Boolean)data);
        if (data instanceof String)
            return filterString((String)data);
        return filterString(data.toString());
    }

    private Object filterString(String string)
    {
        return string;
    }

    private Object filterBoolean(Boolean bool)
    {
        return bool;
    }

    private Object filterNumber(Number number)
    {
        return number;
    }

    private Object filterArray(Object array)
    {
        if (array==null)
            return null;
        
        int length = Array.getLength(array);
        
        for (int i=0;i<length;i++)
            Array.set(array,i,filter(Array.get(array,i)));
        
        return array;
    }

    private Object filterMap(Map object)
    {
        if (object==null)
            return null;
        
        Iterator iter = object.entrySet().iterator();
        while(iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();
            entry.setValue(filter(entry.getValue()));
        }

        return object;
    }

}

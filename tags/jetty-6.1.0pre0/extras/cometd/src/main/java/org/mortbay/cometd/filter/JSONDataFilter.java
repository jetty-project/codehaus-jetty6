package org.mortbay.cometd.filter;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortbay.cometd.Client;
import org.mortbay.cometd.DataFilter;
import org.mortbay.cometd.JSON;
import org.mortbay.util.LazyList;

/** JSON DataFilter
 * This {@link DataFilter} walks an Object as if it was a call to {@link JSON#toString(Object)} and 
 * calls the protected methods 
 * {@link #filterString(String)},
 * {@link #filterNumber(Number)},
 * {@link #filterBoolean(Boolean)},
 * {@link #filterArray(Object, Client)} or
 * {@link #filterMap(Map, Client)} appropriate.
 * Derived filters may override one or more of these methods to provide filtering of specific types.
 * 
 * @author gregw
 *
 */
public class JSONDataFilter implements DataFilter
{
    public void init(Object init)
    {}
    
    public Object filter(Object data, Client from) throws IllegalStateException
    {
        if (data==null)
            return null;
        
        if (data instanceof Map)
            return filterMap((Map)data,from);
        if (data instanceof List)
            return filterArray(LazyList.toArray(data,Object.class),from);
        if (data.getClass().isArray() )
            return filterArray(data,from);
        if (data instanceof Number)
            return filterNumber((Number)data);
        if (data instanceof Boolean)
            return filterBoolean((Boolean)data);
        if (data instanceof String)
            return filterString((String)data);
        return filterString(data.toString());
    }

    protected Object filterString(String string)
    {
        return string;
    }

    protected Object filterBoolean(Boolean bool)
    {
        return bool;
    }

    protected Object filterNumber(Number number)
    {
        return number;
    }

    protected Object filterArray(Object array, Client from)
    {
        if (array==null)
            return null;
        
        int length = Array.getLength(array);
        
        for (int i=0;i<length;i++)
            Array.set(array,i,filter(Array.get(array,i), from));
        
        return array;
    }

    protected Object filterMap(Map object, Client from)
    {
        if (object==null)
            return null;
        
        Iterator iter = object.entrySet().iterator();
        while(iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();
            entry.setValue(filter(entry.getValue(), from));
        }

        return object;
    }

}

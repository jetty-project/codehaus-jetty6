package org.mortbay.util.ajax;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mortbay.util.ajax.JSON.Output;

/* ------------------------------------------------------------ */
/**
 * Convert an Object to JSON using reflection on getters methods.
 * 
 * @author gregw
 *
 */
public class JSONObjectConvertor implements JSON.Convertor
{
    private boolean _fromJSON;
    private Set _excluded=null;

    public JSONObjectConvertor()
    {
        _fromJSON=false;
    }
    
    public JSONObjectConvertor(boolean fromJSON)
    {
        _fromJSON=fromJSON;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param fromJSON
     * @param excluded An array of field names to exclude from the conversion
     */
    public JSONObjectConvertor(boolean fromJSON,String[] excluded)
    {
        _fromJSON=fromJSON;
        if (excluded!=null)
            _excluded=new HashSet(Arrays.asList(excluded));
    }

    public Object fromJSON(Map map)
    {
        if (_fromJSON)
            throw new UnsupportedOperationException();
        return map;
    }

    public void toJSON(Object obj, Output out)
    {
        try
        {
            Class c=obj.getClass();

            if (_fromJSON)
                out.addClass(obj.getClass());

            Method[] methods = obj.getClass().getMethods();

            for (int i=0;i<methods.length;i++)
            {
                Method m=methods[i];
                if (!Modifier.isStatic(m.getModifiers()) &&  
                        m.getParameterTypes().length==0 && 
                        m.getReturnType()!=null &&
                        m.getDeclaringClass()!=Object.class)
                {
                    String name=m.getName();
                    if (name.startsWith("is"))
                        name=name.substring(2,3).toLowerCase()+name.substring(3);
                    else if (name.startsWith("get"))
                        name=name.substring(3,4).toLowerCase()+name.substring(4);
                    else
                        continue;

                    if (includeField(name,obj,m))
                        out.add(name, m.invoke(obj,(Object[])null));
                }
            }
        } 
        catch (Throwable e)
        {
            // e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }
    
    protected boolean includeField(String name, Object o, Method m)
    {
        return _excluded==null || !_excluded.contains(name);
    }

}

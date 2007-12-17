package org.mortbay.util.ajax;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

import org.mortbay.log.Log;
import org.mortbay.util.Loader;
import org.mortbay.util.ajax.JSON.Output;

public class JSONObjectConvertor implements JSON.Convertor
{
    private boolean _fromJSON;
    Method _isEnum;
    {
        try
        {
            _isEnum = Class.class.getMethod("isEnum", (Class[])null);
        }
        catch (Exception e)
        {
            Log.ignore(e);
        }
    }

    public JSONObjectConvertor(boolean fromJSON)
    {
        _fromJSON=fromJSON;
    }

    public Object fromJSON(Map map)
    {
        throw new UnsupportedOperationException();
    }

    public void toJSON(Object obj, Output out)
    {
        try
        {
            Class c=obj.getClass();

            try
            {
                if (_isEnum!=null)
                {
                    Boolean en=(Boolean)_isEnum.invoke(c, (Object[])null);
                    if (en.booleanValue())
                    {
                        out.add(obj.toString());
                        return;
                    }
                }
            }
            catch(Exception e)
            {
                Log.warn(e);
            }

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

}

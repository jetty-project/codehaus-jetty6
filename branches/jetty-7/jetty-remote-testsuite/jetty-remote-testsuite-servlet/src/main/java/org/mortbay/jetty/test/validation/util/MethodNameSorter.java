package org.mortbay.jetty.test.validation.util;

import java.lang.reflect.Method;
import java.util.Comparator;

public class MethodNameSorter implements Comparator<Method>
{
    public int compare(Method o1, Method o2)
    {
        return o1.getName().compareTo(o2.getName());
    }
}

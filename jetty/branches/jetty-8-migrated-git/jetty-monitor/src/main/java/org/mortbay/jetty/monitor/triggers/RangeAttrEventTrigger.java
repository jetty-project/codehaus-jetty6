// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================


package org.mortbay.jetty.monitor.triggers;

import javax.management.MalformedObjectNameException;


/* ------------------------------------------------------------ */
/**
 * RangeAttrEventTrigger
 * 
 * Event trigger that polls a value of an MXBean attribute and
 * checks if it is in a range from specified min value to
 * specified max value. 
 */
public class RangeAttrEventTrigger<TYPE extends Comparable<TYPE>> extends AttrEventTrigger<TYPE>
{
    protected final TYPE _min;
    protected final TYPE _max;
    
    /* ------------------------------------------------------------ */
    /**
     * Construct event trigger and specify the MXBean attribute
     * that will be polled by this event trigger as well as min
     * and max value of the attribute.
     * 
     * @param objectName object name of an MBean to be polled
     * @param attributeName name of an MBean attribute to be polled
     * @param min minimum value of the attribute
     * @param max maximum value of the attribute
     * 
     * @throws MalformedObjectNameException
     * @throws IllegalArgumentException
     */
    public RangeAttrEventTrigger(String objectName, String attributeName,TYPE min, TYPE max)
        throws MalformedObjectNameException, IllegalArgumentException
    {
        super(objectName,attributeName);
        
        if (min == null)
            throw new IllegalArgumentException("Value cannot be null");
        if (max == null)
            throw new IllegalArgumentException("Value cannot be null");

        _min = min;
        _max = max;
    }

    /* ------------------------------------------------------------ */
    /**
     * Compare the value of the MXBean attribute being polling
     * to check if it is in a range from specified min value to
     * specified max value. 
     * 
     * @see org.mortbay.jetty.monitor.triggers.AttrEventTrigger#match(java.lang.Comparable)
     */
    @Override
    public boolean match(Comparable<TYPE> value)
    {
        return (value.compareTo(_min) > 0) &&(value.compareTo(_max) < 0);
    }

    /* ------------------------------------------------------------ */
    /**
     * Returns the string representation of this event trigger
     * in the format "min<name<max". 
     * 
     * @return string representation of the event trigger
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        
        result.append(_min);
        result.append("<");
        result.append(getNameString());
        result.append("<");
        result.append(_max);
        
        return result.toString();
    }
}

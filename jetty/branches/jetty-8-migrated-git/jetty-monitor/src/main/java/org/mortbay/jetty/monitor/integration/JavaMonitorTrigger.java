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

package org.mortbay.jetty.monitor.integration;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.mortbay.jetty.monitor.triggers.AttrEventTrigger;


/* ------------------------------------------------------------ */
/**
 */
public class JavaMonitorTrigger <TYPE extends Comparable<TYPE>>
    extends AttrEventTrigger<TYPE>
{
    private final String _id;
    private final String _name;
    private final boolean _dynamic;
    private int _count;
    
    /* ------------------------------------------------------------ */
    /**
     * @param nameObject
     * @param attributeName
     * @param id
     * @param dynamic
     * @throws MalformedObjectNameException
     * @throws IllegalArgumentException
     */
    public JavaMonitorTrigger(ObjectName nameObject, String attributeName, String id, String name, boolean dynamic)
        throws IllegalArgumentException
    {   
        super(nameObject, attributeName);
      
        _id = id;
        _name = name;
        _dynamic = dynamic;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @see org.mortbay.jetty.monitor.triggers.AttrEventTrigger#match(java.lang.Comparable)
     */
    @Override
    public boolean match(Comparable<TYPE> value)
    {
        return _dynamic ? true : (_count++ < 1);
    }
    
    protected boolean getSaveAll()
    {
        return false;
    }
    
    @Override
    public String getID()
    {
        return _id;
    }
    
    @Override
    public String getNameString()
    {
        return _name;
    }
}

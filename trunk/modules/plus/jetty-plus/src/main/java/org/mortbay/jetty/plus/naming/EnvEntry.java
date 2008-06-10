// ========================================================================
// $Id$
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

package org.mortbay.jetty.plus.naming;


import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.mortbay.log.Log;
import org.mortbay.naming.NamingUtil;


/**
 * EnvEntry
 *
 *
 */
public class EnvEntry extends NamingEntry
{
    private boolean overrideWebXml;
    
    
    /**
     * Bind a name and value to java:comp/env, taking into account
     * any overriding EnvEntrys in the environment, either local or global.
     * 
     * @param name the name from web.xml env-entry
     * @param value the value from web.xml env-entry
     * @throws NamingException
     */
    public static void bindToENC (String name, Object value)
    throws NamingException
    {       
        if (name==null||name.trim().equals(""))
            throw new NamingException("No name for EnvEntry");

        //Is there an EnvEntry with the same name? If so, check its overrideWebXml setting. If true,
        //it's value should be used instead of the one supplied in web.xml - as the name matches, then in
        //fact the value is already bound, so there's nothing to do . If false, use the value
        //from web.xml. In the case where the EnvEntry has been scoped only to the webapp (ie it was
        //in jetty-env.xml).
        EnvEntry envEntry = (EnvEntry)NamingEntryUtil.lookupNamingEntry(name);
        if (envEntry!=null && envEntry.isOverrideWebXml())
            envEntry.bindToENC(name);
        else
        {
            //No EnvEntry, or it wasnt set to override, so just bind the value from web.xml
            InitialContext ic = new InitialContext();
            Context envCtx = (Context)ic.lookup("java:comp/env");
            NamingUtil.bind(envCtx, name, value);
        }     
    }
    
    public static List lookupGlobalEnvEntries ()
    throws NamingException
    {
        ArrayList list = new ArrayList();
        lookupEnvEntries(list, new InitialContext());
        return list;
    }
    
    private static List lookupEnvEntries (List list, Context context)
    throws NamingException
    {
        try
        {
            NamingEnumeration nenum = context.listBindings("");
            while (nenum.hasMoreElements())
            {
                Binding binding = (Binding)nenum.next();
                if (binding.getObject() instanceof Context)
                    lookupEnvEntries (list, (Context)binding.getObject());
                else if (EnvEntry.class.isInstance(binding.getObject()))
                  list.add(binding.getObject());
            }
        }
        catch (NameNotFoundException e)
        {
            Log.debug("No EnvEntries in context="+context);
        }

        return list;
    }

    
    
    public EnvEntry (String jndiName, Object objToBind)
    throws NamingException
    {
        this(jndiName, objToBind, false);
    }
    
    public EnvEntry (String jndiName, Object objToBind, boolean overrideWebXml)
    throws NamingException
    {
        super(jndiName, objToBind);
        this.overrideWebXml = overrideWebXml;
    }
    
    
    public boolean isOverrideWebXml ()
    {
        return this.overrideWebXml;
    }
}

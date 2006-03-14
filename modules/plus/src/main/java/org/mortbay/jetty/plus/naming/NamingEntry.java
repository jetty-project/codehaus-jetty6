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
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.mortbay.log.Log;
import org.mortbay.naming.NamingUtil;



/**
 * NamingEntry
 *
 * Base class for all java:comp related entities. Instances of
 * subclasses of this class are declared in jetty.xml or in a 
 * webapp's WEB-INF/jetty-env.xml file.
 *
 * NOTE: that all NamingEntries will be bound in a single namespace.
 *  The "global" level is just in the top level context. The "local"
 *  level is a context named after a webapp.
 */
public abstract class NamingEntry
{
    private String jndiName;
    private Object objectToBind;
    private String nameInNamespace;
    private static ThreadLocal webAppCompContext = new ThreadLocal();
    
   
    /**
     * @param c A context specific to a webapp
     */
    public static void setThreadLocalContext (Context c)
    {
       webAppCompContext.set(c);
    }
    
    public static Context getThreadLocalContext ()
    {
        return (Context)webAppCompContext.get();
    }
    
    
    /**
     * Find a NamingEntry.
     * @param context the context to search
     * @param clazz the type of the entry (ie subclass of this class)
     * @param jndiName the name of the class instance
     * @return
     * @throws NamingException
     */
    public static Object lookupNamingEntry (Context context, Class clazz, String jndiName)
    throws NamingException
    {
        NameParser parser = context.getNameParser("");       
        Name name = parser.parse("");
        name.add(clazz.getName());
        name.addAll(parser.parse(jndiName));
        
        return context.lookup(name);
    }
    
    
    /** Get all NameEntries of a certain type in a context.
     * 
     * @param context the context to search
     * @param clazz the type of the entry
     * @return
     * @throws NamingException
     */
    public static List lookupNamingEntries (Context context, Class clazz)
    throws NamingException
    {
        ArrayList list = new ArrayList();
        try
        {
            NamingEnumeration nenum = context.listBindings(clazz.getName());
            while (nenum.hasMoreElements())
            {
                Binding binding = (Binding)nenum.next();
                list.add(binding.getObject());
            }
        }
        catch (NameNotFoundException e)
        {
            Log.info("No entries of type "+clazz.getName());
        }
        
        return list;
    }
    
    
    /** Constructor
     * @param jndiName the name of the object which will eventually be in java:comp/env
     * @param object the object to be bound
     * @throws NamingException
     */
    public NamingEntry (String jndiName, Object object)
    throws NamingException
    {
        this.jndiName = jndiName;
        this.objectToBind = object;
        InitialContext icontext = new InitialContext();
      
        Context context = getThreadLocalContext();
        if (context == null)
            context = icontext;
       
        NameParser parser = context.getNameParser("");
        Name namingEntryName = parser.parse("");
        namingEntryName.add(getClass().getName());
        namingEntryName.add(getJndiName());
        //bind this NameEntry so we can access it later
        NamingUtil.bind(context, namingEntryName.toString(), this);
        //bind the object itself so that we can link to it later
        NamingUtil.bind(context, getJndiName(), getObjectToBind());
       
        Name name = parser.parse(context.getNameInNamespace());
        name.addAll(parser.parse(getJndiName()));
        //remember the name where we bound it
        nameInNamespace = name.toString();
        
        Log.debug("Bound "+nameInNamespace+" using context "+context);
        Log.debug("Bound "+namingEntryName+" using context "+context);
    }

    
    
    public void bindToEnv ()
    throws NamingException
    {
        InitialContext ic = new InitialContext();
        Context env = (Context)ic.lookup("java:comp/env");
        Log.debug("Binding java:comp/env/"+getJndiName()+" to "+nameInNamespace);
        NamingUtil.bind(env, getJndiName(), new LinkRef(nameInNamespace));
    }
    
    
   
    
    public String getJndiName ()
    {
        return this.jndiName;
    }
    
    public Object getObjectToBind()
    {
        return this.objectToBind;
    }
    
    protected void bind (Context context)
    throws NamingException
    {
        NamingUtil.bind(context, getJndiName(), getObjectToBind());
    }
}

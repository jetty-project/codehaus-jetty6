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
    protected String jndiName;
     protected Object objectToBind;
     protected Name objectNameInNamespace;
     protected Name namingEntryName;
     protected Name objectName;
     protected Context context;
     protected static ThreadLocal webAppCompContext = new ThreadLocal();

    
   
    /**
     * Set the webapp specific context into which
     * NamingEntries should be bound.
     * 
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
     * Make a new context which is specific to a webapp.
     * Using to store NamingEntries from a jetty-env.xml
     * file.
     * @param webappName
     * @return
     * @throws NamingException
     */
    public static Context createContext (String webappName)
    throws NamingException
    {
        InitialContext icontext = new InitialContext();
        return icontext.createSubcontext(webappName);
    }
    
     
    /**
     * Destroy a subcontext.
     * Used to remove a subcontext representing a webapp-scoped
     * context.
     * @param webappName
     * @throws NamingException
     */
    public static void destroyContext (String webappName)
    throws NamingException
    {
        InitialContext icontext = new InitialContext();
        icontext.destroySubcontext(webappName);
    }
    
    /**
     * Try and find a particular NamingEntry, looking first in a webapp-scoped
     * namespace and then failing that look in the global namespace.
     * @param clazz the type of the NamingEntry to find
     * @param jndiName the name of the NamingEntryntry to find
     * @return
     * @throws NamingException
     */
    public static NamingEntry lookupNamingEntry (Class clazz, String jndiName)
    throws NamingException
    {
        try
        {
            //lookup a Transaction manager first in the webapp specific naming
            //context, but if one doesn't exist, then try the global
            Context context = getThreadLocalContext();
            Object o = null;
            if (context != null)
            {
                try
                {
                    o = lookupNamingEntry(context, clazz, jndiName);
                }
                catch (NameNotFoundException e)
                {
                    Log.ignore(e);
                    Log.debug("Didn't find Resource "+jndiName +" in thread local context "+context);
                }
            }
            if (o == null)
            {
                o = lookupNamingEntry(new InitialContext(), clazz, jndiName);
                Log.debug("Found Resource in global context for "+jndiName);
            }
            return (NamingEntry)o;
        }
        catch (NameNotFoundException e)
        {
            Log.debug("Returning NULL for "+jndiName);
            return null;
        }
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
        lookupNamingEntries (list, context, clazz);
        return list;
    }
    
    
    private static List lookupNamingEntries (List list, Context context, Class clazz)
    throws NamingException
    {
        try
        {
            String name = (clazz==null?"": clazz.getName());
            NamingEnumeration nenum = context.listBindings(name);
            while (nenum.hasMoreElements())
            {
                Binding binding = (Binding)nenum.next();
                if (binding.getObject() instanceof Context)
                {
                    lookupNamingEntries (list, (Context)binding.getObject(), null);
                } 
                else               
                  list.add(binding.getObject());
            }
        }
        catch (NameNotFoundException e)
        {
            Log.debug("No entries of type "+clazz.getName()+" in context="+context);
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
      
        context = getThreadLocalContext();
        if (context == null)
            context = icontext;
       
        NameParser parser = context.getNameParser("");
        namingEntryName = parser.parse("");
        namingEntryName.add(getClass().getName());
        namingEntryName.add(getJndiName());
        //bind this NameEntry in the given context so we can access it later
        NamingUtil.bind(context, namingEntryName.toString(), this);
        
        //bind the object itself in the given context so that we can link to it later
        objectName = parser.parse(getJndiName());
        NamingUtil.bind(context, objectName.toString(), getObjectToBind());
       
        //remember the full name of the bound object so we can use it in the link
        objectNameInNamespace = parser.parse(context.getNameInNamespace());
        objectNameInNamespace.addAll(objectName);
        
        
        Log.info("Bound "+objectNameInNamespace+" using context "+context);
        Log.info("Bound "+namingEntryName+" using context "+context);
    }

    
    
    /**
     * Add a java:comp/env binding for the object represented by
     * this NamingEntry
     * @throws NamingException
     */
    public void bindToEnv ()
    throws NamingException
    {
        InitialContext ic = new InitialContext();
        Context env = (Context)ic.lookup("java:comp/env");
        Log.info("Binding java:comp/env/"+getJndiName()+" to "+objectNameInNamespace);
        NamingUtil.bind(env, getJndiName(), new LinkRef(objectNameInNamespace.toString()));
    }
    
    
    /**
     * Unbind this NamingEntry from a java:comp/env
     */
    public void unbindEnv ()
    {
        try
        {
            InitialContext ic = new InitialContext();
            Context env = (Context)ic.lookup("java:comp/env");
            Log.info("Unbinding java:comp/env/"+getJndiName());
            env.unbind(getJndiName());
        }
        catch (NamingException e)
        {
            Log.warn(e);
        }
    }
    
    /**
     * Unbind this NamingEntry entirely
     */
    public void unbind ()
    {
        try
        {
            context.unbind(objectName);
            context.unbind(namingEntryName);
        }
        catch (NamingException e)
        {
            Log.warn(e);
        }
    }
    
    /**
     * Get the unique name of the object
     * @return
     */
    public String getJndiName ()
    {
        return this.jndiName;
    }
    
    /**
     * Get the object that is to be bound
     * @return
     */
    public Object getObjectToBind()
    {
        return this.objectToBind;
    }
    
}

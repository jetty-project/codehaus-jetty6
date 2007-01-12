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
import javax.naming.Reference;
import javax.naming.Referenceable;

import org.mortbay.log.Log;
import org.mortbay.naming.NamingUtil;



/**
 * NamingEntry
 *
 * Base class for all jndi related entities. Instances of
 * subclasses of this class are declared in jetty.xml or in a 
 * webapp's WEB-INF/jetty-env.xml file.
 *
 * NOTE: that all NamingEntries will be bound in a single namespace.
 *  The "global" level is just in the top level context. The "local"
 *  level is a context specific to a webapp.
 */
public abstract class NamingEntry
{
    public static final int SCOPE_GLOBAL = 0;
    public static final int SCOPE_LOCAL = 1;
    protected String jndiName;
    protected Object objectToBind;
    protected String absoluteObjectNameString;
    protected String namingEntryNameString;
    protected String objectNameString;
    protected Context context;
    protected boolean isGlobal;
    protected static ThreadLocal scope = new ThreadLocal();
    
    public static void setScope (int scopeType)
    {
        scope.set(new Integer(scopeType));
    }
    
    public static int getScope ()
    {
        Integer val = (Integer)scope.get();
        return (val == null?SCOPE_GLOBAL:val.intValue());
    }
    
    
    
    
  
    public static NamingEntry lookupNamingEntry (int scopeType, Class clazz, String jndiName)
    throws NamingException
    {
        NamingEntry namingEntry = null;
        
        switch (scopeType)
        {
            case SCOPE_GLOBAL: 
            {
                try
                {
                    namingEntry  = (NamingEntry)lookupNamingEntry (new InitialContext(), clazz, jndiName);
                }
                catch (NameNotFoundException e)
                {
                    namingEntry = null;
                }
                break;
            }
            case SCOPE_LOCAL:
            {
                if (getScope()==SCOPE_LOCAL)
                {
                    //NOTE: LOCAL scope will only work if you are actually in the webapp scope itself
                    try
                    {
                        InitialContext ic = new InitialContext();
                        namingEntry = (NamingEntry)lookupNamingEntry((Context)ic.lookup("java:comp/env"), clazz, jndiName);
                    }
                    catch (NameNotFoundException e)
                    {
                        namingEntry = null;
                    }
                }
                else
                {
                    Log.warn("Can't lookup locally scoped naming entries outside of scope");
                    throw new NamingException("Can't lookup locally scoped naming entries outside of scope");
                }
                break;
            }
            default:
            {
                Log.info("No scope to lookup name: "+jndiName);
            }
        }
        return namingEntry;
    }
    
    
   
    
 
    
    
    /** Get all NameEntries of a certain type in a context.
     * 
     * @param scopeType local or global
     * @param clazz the type of the entry
     * @return
     * @throws NamingException
     */
    public static List lookupNamingEntries (int scopeType, Class clazz)
    throws NamingException
    {
        ArrayList list = new ArrayList();
        switch (scopeType)
        {
            case SCOPE_GLOBAL:
            {
                lookupNamingEntries(list, new InitialContext(), clazz);
                break;
            }
            case SCOPE_LOCAL:
            {
                //WARNING: you can only look up local scope if you are indeed in the scope
                if (getScope()==SCOPE_LOCAL)
                {
                    InitialContext ic = new InitialContext();
                    
                    lookupNamingEntries(list, (Context)ic.lookup("java:comp/env"), clazz);
                }
                else
                {
                    Log.warn("Can't lookup local scope naming entries outside of local scope");
                    throw new NamingException("Can't lookup locally scoped naming entries outside of scope");
                }
                break;
            }
        }
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
  
    
    /**
     * Find a NamingEntry.
     * @param context the context to search
     * @param clazz the type of the entry (ie subclass of this class)
     * @param jndiName the name of the class instance
     * @return
     * @throws NamingException
     */
    private static Object lookupNamingEntry (Context context, Class clazz, String jndiName)
    throws NamingException
    {
        NameParser parser = context.getNameParser("");       
        Name name = parser.parse("");
        name.add(clazz.getName());
        name.addAll(parser.parse(jndiName));
        
        return context.lookup(name);
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
      
        //if a threadlocal is set indicating we are inside a
        //webapp, then bind naming entries to the webapp's
        //context instead of the global context
        switch (getScope())
        {
            case SCOPE_GLOBAL: 
            {
                context = icontext;
                isGlobal = true;
                break;
            }
            case SCOPE_LOCAL:
            {
                context = (Context)icontext.lookup("java:comp/env");
                isGlobal = false;
                break;
            }
        }
        
       
        NameParser parser = context.getNameParser("");
        Name contextName = parser.parse(context.getNameInNamespace());
        
        Name name = parser.parse("");
        name.add(getClass().getName());
        name.add(getJndiName());
        namingEntryNameString = name.toString();
        //bind this NameEntry in the given context so we can access it later
        NamingUtil.bind(context, namingEntryNameString, this);
        String absoluteNamingEntryNameString = (isGlobal()?"":"java:")+name.addAll(0,contextName).toString();
        
        //bind the object itself in the given context so that we can get it later
        Name objectName = parser.parse(getJndiName());
        objectNameString = objectName.toString();
        NamingUtil.bind(context, objectNameString, getObjectToBind());       
       
        //remember the full name of the bound object so that it can be used in
        //link references later
        Name fullName = objectName.addAll(0,contextName);
        absoluteObjectNameString = (isGlobal()?"":"java:")+fullName.toString();
        
        
        Log.debug("Bound "+absoluteObjectNameString);
        Log.debug("Bound "+absoluteNamingEntryNameString);
    }

    
    
    /**
     * Add a java:comp/env binding for the object represented by
     * this NamingEntry
     * @throws NamingException
     */
    public void bindToEnv ()
    throws NamingException
    {
        //don't bind local scope naming entries as they are already bound to java:comp/env
        if (isGlobal())
        {
            InitialContext ic = new InitialContext();
            Context env = (Context)ic.lookup("java:comp/env");
            Log.info("Binding java:comp/env/"+getJndiName()+" to "+absoluteObjectNameString);
            NamingUtil.bind(env, getJndiName(), new LinkRef(absoluteObjectNameString));
        }
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
            context.unbind(objectNameString);
            context.unbind(namingEntryNameString);
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
    throws NamingException
    {   
        return this.objectToBind;
    }
    
    /**
     * Check if this naming entry was global or locally scoped to a webapp
     * @return true if naming entry was bound at global scope, false otherwise
     */
    public boolean isGlobal ()
    {
        return this.isGlobal;
    }
    
}

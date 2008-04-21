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

public class NamingEntryUtil
{
 
    
    /**
     * Bind a NamingEntry into JNDI.
     * 
     * Locally scoped entries take precedence over globally scoped ones to
     * allow webapps to override.
     * 
     * @param name the name of the NamingEntry from the runtime environment
     * @param overrideName the name it should be bound as into java:comp/env
     * @param namingEntryType
     * @throws NamingException
     */
    public static void bindToENC (String name, String overrideName, Class namingEntryType)
    throws NamingException
    {  
        if (name==null||name.trim().equals(""))
            throw new NamingException ("No name for NamingEntry");
        if (overrideName==null||overrideName.trim().equals(""))
            overrideName=name;
        
        
        //locally scoped entries take precedence over globally scoped entries of the same name
        NamingEntry entry = lookupNamingEntry (NamingEntry.SCOPE_LOCAL, namingEntryType, name);
        if (entry!=null)
        {
            if (!overrideName.equals(name))
                entry.bindToENC(overrideName);
            else
                entry.bindToENC();
        }
        else
        {
            entry = lookupNamingEntry (NamingEntry.SCOPE_GLOBAL, namingEntryType, name);
            if (entry != null) 
            {
                if (!overrideName.equals(name))
                    entry.bindToENC(overrideName);
                else
                    entry.bindToENC();
            }
            else
            {
                //last ditch effort, check if something has been locally bound in java:comp/env
                try
                {
                    InitialContext ic = new InitialContext();
                    Context envContext = (Context)ic.lookup("java:comp/env");
                    envContext.lookup(name);
                    
                    if (!overrideName.equals(name))
                        NamingUtil.bind(envContext, overrideName, new LinkRef("."+name));
                        
                }
                catch (NamingException e)
                {
                    throw new NameNotFoundException("No resource to bind matching name="+name);
                }
            }   
        }
    }

    
    
    
    /**
     * Check to see if a NamingEntry exists in the given 
     * scope (local or global).
     * 
     * @param scopeType local or global
     * @param namingEntryType the type of the  NamingEntry
     * @param jndiName the name in jndi
     * @return
     */
    public static boolean exists (int scopeType, Class namingEntryType, String jndiName)
    {        
        switch (scopeType)
        {
            case NamingEntry.SCOPE_GLOBAL: 
            {
                try
                {
                    return ((NamingEntry)lookupNamingEntry (new InitialContext(), namingEntryType, jndiName) != null);
                }
                catch (NameNotFoundException e)
                {
                    return false;
                } 
                catch (NamingException e)
                {
                    Log.warn(e);
                    return false;
                }
            }
            case NamingEntry.SCOPE_LOCAL:
            {
                try
                {
                    InitialContext ic = new InitialContext();
                    return ((NamingEntry)lookupNamingEntry((Context)ic.lookup("java:comp/env"), namingEntryType, jndiName) != null);
                }
                catch (NameNotFoundException e)
                {
                    return false;
                }
                catch (NamingException e)
                {
                    Log.warn(e);
                    return false;
                }
            }
            default:
            {
               return false;
            }
        }
    }
  
    
    
    /**
     * Find a NamingEntry of the given scope.
     * 
     * @param scopeType local or global
     * @param clazz a NamingEntry subclass
     * @param jndiName the name in jndi
     * @return
     * @throws NamingException
     */
    public static NamingEntry lookupNamingEntry (int scopeType, Class clazz, String jndiName)
    throws NamingException
    {
        NamingEntry namingEntry = null;
        
        switch (scopeType)
        {
            case NamingEntry.SCOPE_GLOBAL: 
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
            case NamingEntry.SCOPE_LOCAL:
            {
                try
                {
                    InitialContext ic = new InitialContext();
                    namingEntry = (NamingEntry)lookupNamingEntry((Context)ic.lookup("java:comp/env"), clazz, jndiName);
                }
                catch (NameNotFoundException e)
                {
                    namingEntry = null;
                }
                break;
            }
            default:
            {
                Log.debug("No scope to lookup name: "+jndiName);
            }
        }
        return namingEntry;
    }
    
    
   
    
 
    
    
    /** 
     * Get all NameEntries of a certain type in either the local or global
     * namespace.
     * 
     * @param scopeType local or global
     * @param clazz a subclass of NamingEntry
     * @return
     * @throws NamingException
     */
    public static List lookupNamingEntries (int scopeType, Class clazz)
    throws NamingException
    {
        ArrayList list = new ArrayList();
        switch (scopeType)
        {
            case NamingEntry.SCOPE_GLOBAL:
            {
                NamingEntryUtil.lookupNamingEntries(list, new InitialContext(), clazz);
                break;
            }
            case NamingEntry.SCOPE_LOCAL:
            {
                //WARNING: you can only look up local scope if you are indeed in the scope
                InitialContext ic = new InitialContext();                   
                lookupNamingEntries(list, (Context)ic.lookup("java:comp/env"), clazz);

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
     * @param clazz the type of the entry (ie subclass of NamingEntry)
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
    

}

package org.mortbay.jetty.plus.naming;




import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.naming.Binding;
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
     * Check to see if there is a mapping for a local resource name from
     * web.xml. A mapping links up a name as referenced in web.xml
     * with a name in the environment, represented as an 
     * org.mortbay.jetty.plus.naming.Link object, stored in jndi.
     * 
     * @param localName
     * @return
     * @throws NamingException
     */
    public static String getMappedName (String localName)
    throws NamingException
    {
        if (localName==null||"".equals(localName))
            return null;
        
       NamingEntry ne = lookupNamingEntry(localName);
       if (ne==null)
           return null;
       
       if (ne instanceof Link)
           return (String)((Link)ne).getObjectToBind();
       
       return null;
    }
    
    /**
     * Link a name in a webapps java:/comp/evn namespace to a pre-existing
     * resource. The pre-existing resource can be either in the webapp's
     * namespace, or in the container's namespace. Webapp's namespace takes
     * precedence over the global namespace (to allow for overriding).
     * 
     * @param asName the name to bind as
     * @param mappedName the name from the environment to link to asName
     * @param namingEntryType
     * @throws NamingException
     */
    public static void bindToENC (String asName, String mappedName)
    throws NamingException
    {  
        if (asName==null||asName.trim().equals(""))
            throw new NamingException ("No name for NamingEntry");

        if (mappedName==null || "".equals(mappedName))
            mappedName=asName;
        
        //locally scoped entries take precedence over globally scoped entries of the same name
        NamingEntry entry = lookupNamingEntry (mappedName);
        
        if (entry!=null)
            entry.bindToENC(asName);
        else
        {
            //No NamingEntry configured in environment, perhaps there is just an Object bound into this webapp's java:comp/env we can
            //link to
            try
            {
                InitialContext ic = new InitialContext();
                Context envContext = (Context)ic.lookup("java:comp/env");
                envContext.lookup(mappedName);

                if (!mappedName.equals(asName))
                    NamingUtil.bind(envContext, asName, new LinkRef("."+mappedName));   
            }
            catch (NamingException e)
            {
                throw new NameNotFoundException("No resource to bind matching name="+mappedName);
            }
        }
    }

    
    
 
    
    /**
     * Find a NamingEntry instance. 
     * 
     * First the webapp's naming space is searched, and then
     * the container's.
     * 
     * @param jndiName name to lookup
     * @return
     * @throws NamingException
     */
    public static NamingEntry lookupNamingEntry (String jndiName)
    throws NamingException
    {
        //locally scoped entries take precedence over globally scoped entries of the same name
        NamingEntry entry = null;
        InitialContext ic = new InitialContext();
        try
        {
            entry = (NamingEntry)lookupNamingEntry ((Context)ic.lookup("java:comp/env"), jndiName);
        }
        catch (NameNotFoundException e)
        {
            try
            {
                entry = (NamingEntry)lookupNamingEntry (ic, jndiName);
            }
            catch (NameNotFoundException ee)
            {
            }
        }

        System.err.println("Result of looking up "+jndiName+" = "+entry);
        return entry;
    }

    

  
  
    
    /**
     * Find a NamingEntry.
     * 
     * @param context the context to search
     * @param clazz the type of the entry (ie subclass of NamingEntry)
     * @param jndiName the name of the class instance
     * @return
     * @throws NamingException
     */
    public static Object lookupNamingEntry (Context context,  String jndiName)
    throws NamingException
    {
        NameParser parser = context.getNameParser("");    
        Name namingEntryName = NamingEntry.makeNamingEntryName(parser, jndiName);
        
        System.err.println("Looking up name="+namingEntryName.toString());
        return context.lookup(namingEntryName.toString());
    }
    
    
    
    /** 
     * Get all NameEntries of a certain type in either the local or global
     * namespace.
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
            case NamingEntry.SCOPE_CONTAINER:
            {
                lookupNamingEntries(list, new InitialContext(), EnvEntry.class);
                break;
            }
            case NamingEntry.SCOPE_WEBAPP:
            {
                //WARNING: you can only look up local scope if you are indeed in the scope
                InitialContext ic = new InitialContext();
                lookupNamingEntries(list, (Context)ic.lookup("java:comp/env"), EnvEntry.class);
                break;
            }
        }
        return list;
    }
    
    
    
    /**
     * Build up a list of NamingEntry objects that are of a specific type.
     * 
     * @param list
     * @param context
     * @param clazz
     * @return
     * @throws NamingException
     */
    private static List lookupNamingEntries (List list, Context context, Class clazz)
    throws NamingException
    {
        try
        {
            NamingEnumeration nenum = context.listBindings("");
            while (nenum.hasMoreElements())
            {
                Binding binding = (Binding)nenum.next();
                if (binding.getObject() instanceof Context)
                    lookupNamingEntries (list, (Context)binding.getObject(), clazz);
                else if (clazz.isInstance(binding.getObject()))
                  list.add(binding.getObject());
            }
        }
        catch (NameNotFoundException e)
        {
            Log.debug("No entries of type "+clazz.getName()+" in context="+context);
        }

        return list;
    }

}

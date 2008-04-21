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
    protected String jndiName;  //the name representing the object associated with the NamingEntry
    protected Object objectToBind; //the object associated with the NamingEntry
    protected String absoluteObjectNameString; //the absolute name of the object
    protected String namingEntryNameString; //the name of the NamingEntry relative to the context it is stored in
    protected String objectNameString; //the name of the object relative to the context it is stored in
    protected Context context; //the context in which both Naming Entry and object are saved
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
    
   
    
    
    /** 
     * Create a NamingEntry. 
     * A NamingEntry is a name associated with a value which can later
     * be looked up in JNDI by a webapp.
     * 
     * We create the NamingEntry and put it into JNDI where it can
     * be linked to the webapp's env-entry, resource-ref etc entries.
     * 
     * @param jndiName the name of the object which will eventually be in java:comp/env
     * @param object the object to be bound
     * @throws NamingException
     */
    public NamingEntry (String jndiName, Object object)
    throws NamingException
    {
        this.jndiName = jndiName;
        this.objectToBind = object;
        
        //if a threadlocal is set indicating we are inside a
        //webapp, then save naming entries to the webapp's
        //local context instead of the global context
        switch (getScope())
        {
            case SCOPE_GLOBAL: 
            {          
                isGlobal = true;
                break;
            }
            case SCOPE_LOCAL:
            {               
                isGlobal = false;
                break;
            }
        }
        save(); 
    }

    
    
    /**
     * Add a java:comp/env binding for the object represented by
     * this NamingEntry
     * @throws NamingException
     */
    public void bindToENC ()
    throws NamingException
    {
        if (isLocal())
        {
            //don't bind local scope naming entries as they are already bound to java:comp/env
        }
        else if (isGlobal())
        {
            InitialContext ic = new InitialContext();
            Context env = (Context)ic.lookup("java:comp/env");
            Log.debug("Binding java:comp/env/"+getJndiName()+" to "+absoluteObjectNameString);
            NamingUtil.bind(env, getJndiName(), new LinkRef(absoluteObjectNameString));
        }
    }
    
    
    /**
     * Add a java:comp/env binding for the object represented by this NamingEntry,
     * but bind it as a different name to the one supplied
     * @throws NamingException
     */
    public void bindToENC(String overrideName)
    throws NamingException
    {
        InitialContext ic = new InitialContext();
        Context env = (Context)ic.lookup("java:comp/env");
        Log.debug("Binding java:comp/env/"+overrideName+" to "+absoluteObjectNameString);
        NamingUtil.bind(env, overrideName, new LinkRef(absoluteObjectNameString));
    }
    
    /**
     * Unbind this NamingEntry from a java:comp/env
     */
    public void unbindENC ()
    {
        try
        {
            InitialContext ic = new InitialContext();
            Context env = (Context)ic.lookup("java:comp/env");
            Log.debug("Unbinding java:comp/env/"+getJndiName());
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
    public void release ()
    {
        try
        {
            context.unbind(objectNameString);
            context.unbind(namingEntryNameString);
            this.absoluteObjectNameString=null;
            this.jndiName=null;
            this.namingEntryNameString=null;
            this.objectNameString=null;
            this.objectToBind=null;
            this.context=null;
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
    
    public boolean isLocal()
    {
        return !this.isGlobal;
    }
    
 
    
    
    /**
     * Save the NamingEntry for later use.
     * 
     * Saving is done by binding the NamingEntry
     * itself, and the value it represents into
     * JNDI. In this way, we can link to the
     * value it represents later, but also
     * still retrieve the NamingEntry itself too.
     * 
     * @throws NamingException
     */
    private void save ()
    throws NamingException
    {
        InitialContext icontext = new InitialContext();
        if (isGlobal())
            context = icontext;
        else
            context = (Context)icontext.lookup("java:comp/env");
        
        NameParser parser = context.getNameParser("");
        Name contextName = parser.parse(context.getNameInNamespace());
        
        //save the NamingEntry itself so it can be accessed later       
        Name name = parser.parse("");
        name.add(getClass().getName());
        name.add(getJndiName());
        namingEntryNameString = name.toString();
        NamingUtil.bind(context, namingEntryNameString, this);
        Log.debug("Bound "+(isGlobal()?"":"java:")+name.addAll(0,contextName));
        
        //put the Object into JNDI so it can be linked to later  
        Name objectName = parser.parse(getJndiName());
        objectNameString = objectName.toString();
        NamingUtil.bind(context, objectNameString, getObjectToBind());       
       
        //remember the full name of the bound object so that it can be used in
        //link references later
        Name fullName = objectName.addAll(0,contextName);
        absoluteObjectNameString = (isGlobal()?"":"java:")+fullName.toString();       
        Log.debug("Bound "+absoluteObjectNameString);
    }
}

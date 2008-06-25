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
    public static final int SCOPE_CONTAINER = 0;
    public static final int SCOPE_WEBAPP = 1;
    protected String jndiName;  //the name representing the object associated with the NamingEntry
    protected Object objectToBind; //the object associated with the NamingEntry
    protected String absoluteObjectNameString; //the absolute name of the object
    protected String namingEntryNameString; //the name of the NamingEntry relative to the context it is stored in
    protected String objectNameString; //the name of the object relative to the context it is stored in
    protected Context context; //the context in which both Naming Entry and object are saved
    protected boolean isContainerScope;
    protected static ThreadLocal scope = new ThreadLocal();
    
    public static void setScope (int scopeType)
    {
        scope.set(new Integer(scopeType));
    }
    
    public static int getScope ()
    {
        Integer val = (Integer)scope.get();
        return (val == null?SCOPE_CONTAINER:val.intValue());
    }
    
   
    public static Name makeNamingEntryName (NameParser parser, String jndiName)
    throws NamingException
    {
        if (jndiName==null || parser==null)
            return null;
        
        Name name = parser.parse("");
        name.add(jndiName);
        String lastAtom = (String)name.remove(name.size()-1);
        lastAtom="__"+lastAtom;
        name.add(lastAtom);
        return name;
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
        //local context instead of the container's global context
        isContainerScope=(getScope()==SCOPE_CONTAINER);
        InitialContext icontext = new InitialContext();
        if (isContainerScope)
            context = icontext;
        else
            context = (Context)icontext.lookup("java:comp/env");
        save(); 
    }

    
    
    /**
     * Add a java:comp/env binding for the object represented by
     * this NamingEntry
     * @throws NamingException
     */
    /*
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
    */
    
    /**
     * Add a java:comp/env binding for the object represented by this NamingEntry,
     * but bind it as the name supplied
     * @throws NamingException
     */
    public void bindToENC(String localName)
    throws NamingException
    {
        
        
        if (localName.equals(jndiName) && isLocal())
        {
            Log.warn("Already bound "+localName+" to java:comp/env with "+absoluteObjectNameString);
            return; //name already bound to local
        }
        
        InitialContext ic = new InitialContext();
        Context env = (Context)ic.lookup("java:comp/env");
        Log.debug("Binding java:comp/env/"+localName+" to "+absoluteObjectNameString);
        NamingUtil.bind(env, localName, new LinkRef(absoluteObjectNameString));
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
        return this.isContainerScope;
    }
    
    public boolean isLocal()
    {
        return !this.isContainerScope;
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
     * The object is bound at the jndiName passed in.
     * This NamingEntry is bound at __jndiName.
     * 
     * eg
     * 
     * /jdbc/foo   : DataSource
     * /jdbc/__foo : NamingEntry
     * 
     * @throws NamingException
     */
    protected void save ()
    throws NamingException
    {
        NameParser parser = context.getNameParser("");
        Name contextName = parser.parse(context.getNameInNamespace());
        
        //save the NamingEntry itself so it can be accessed later       
        Name namingEntryName = makeNamingEntryName(parser, jndiName);
        namingEntryNameString = namingEntryName.toString();
        NamingUtil.bind(context, namingEntryNameString, this);
        Log.debug("Bound "+(isGlobal()?"":"java:")+namingEntryName.addAll(0,contextName));
        
        //put the Object into JNDI so it can be linked to later  
        Name objectName = parser.parse(getJndiName());
        objectNameString = objectName.toString();
        NamingUtil.bind(context, objectNameString, getObjectToBind());       
        
        //remember the full name of the bound object so that it can be used in
        //link references later
        Name fullName = objectName.addAll(0,contextName);
        absoluteObjectNameString = (isContainerScope?"":"java:")+fullName.toString();       
        Log.debug("Bound "+absoluteObjectNameString);
    }
    
    
}

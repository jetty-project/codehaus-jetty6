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

package org.mortbay.jetty.plus.webapp;

import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.mortbay.jetty.plus.naming.EnvEntry;
import org.mortbay.jetty.plus.naming.NamingEntry;
import org.mortbay.jetty.plus.naming.NamingEntryUtil;
import org.mortbay.jetty.plus.naming.Transaction;
import org.mortbay.log.Log;


/**
 * Configuration
 *
 *
 */
public class Configuration extends AbstractConfiguration
{

    private Integer _key;
    
    
    public Configuration () throws ClassNotFoundException
    {
        super();
    }
    
    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindEnvEntry(java.lang.String, java.lang.String)
     * @param name
     * @param value
     * @throws Exception
     */
    public void bindEnvEntry(String name, Object value) throws Exception
    {    
        EnvEntry.bindToENC(name, value);
    }

    /** 
     * Bind a resource reference.
     * 
     * If a resource reference with the same name is in a jetty-env.xml
     * file, it will already have been bound.
     * 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindResourceRef(java.lang.String)
     * @param name
     * @throws Exception
     */
    public void bindResourceRef(String name, Class typeClass)
    throws Exception
    {
        try
        {
            String mappedName = NamingEntryUtil.getMappedName (name);
            NamingEntryUtil.bindToENC(name, mappedName);
        }
        catch (NameNotFoundException e)
        {
            //There is no matching resource bound into the container's environment, try a default name.
            //The default name syntax is: the [res-type]/default
            //eg       javax.sql.DataSource/default
            NamingEntry defaultNE = NamingEntryUtil.lookupNamingEntry(typeClass.getName()+"/default");
            if (defaultNE!=null)
                defaultNE.bindToENC(name);
        }
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindResourceEnvRef(java.lang.String)
     * @param name
     * @throws Exception
     */
    public void bindResourceEnvRef(String name, Class typeClass)
    throws Exception
    {
        try
        {
            String mappedName = NamingEntryUtil.getMappedName (name);
            NamingEntryUtil.bindToENC(name, mappedName);
        }
        catch (NameNotFoundException e)
        {
            //There is no matching resource bound into the container's environment, try a default            
            //The default name syntax is: the [res-type]/default
            //eg       javax.sql.DataSource/default
            NamingEntry defaultNE = NamingEntryUtil.lookupNamingEntry(typeClass.getName()+"/default");
            if (defaultNE!=null)
                defaultNE.bindToENC(name);
        }
    }
    
    
    public void bindMessageDestinationRef(String name, Class typeClass)
    throws Exception
    {
        try
        {            
            String mappedName = NamingEntryUtil.getMappedName (name);
            NamingEntryUtil.bindToENC(name, mappedName);
        }        
        catch (NameNotFoundException e)
        {
            //There is no matching resource bound into the container's environment, try a default            
            //The default name syntax is: the [res-type]/default
            //eg       javax.sql.DataSource/default
            NamingEntry defaultNE = NamingEntryUtil.lookupNamingEntry(typeClass.getName()+"/default");
            if (defaultNE!=null)
                defaultNE.bindToENC(name);
        }
    }
    
    public void bindUserTransaction ()
    throws Exception
    {
        try
        {
           Transaction.bindToENC();
        }
        catch (NameNotFoundException e)
        {
            Log.info("No Transaction manager found - if your webapp requires one, please configure one.");
        }
    }
    
    public void configureClassLoader ()
    throws Exception
    {      
        super.configureClassLoader();
    }

    
    public void configureDefaults ()
    throws Exception
    {
        super.configureDefaults();
    }


    public void configureWebApp ()
    throws Exception
    {
        super.configureWebApp();
        //lock this webapp's java:comp namespace as per J2EE spec
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getWebAppContext().getClassLoader());
        lockCompEnv();
        Thread.currentThread().setContextClassLoader(oldLoader);
    }
    
    public void deconfigureWebApp() throws Exception
    {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getWebAppContext().getClassLoader());
        unlockCompEnv();
        Thread.currentThread().setContextClassLoader(oldLoader);
        super.deconfigureWebApp();
    }
    
    protected void lockCompEnv ()
    throws Exception
    {
        Random random = new Random ();
        _key = new Integer(random.nextInt());
        Context context = new InitialContext();
        Context compCtx = (Context)context.lookup("java:comp");
        compCtx.addToEnvironment("org.mortbay.jndi.lock", _key);
    }
    
    protected void unlockCompEnv ()
    throws Exception
    {
        if (_key!=null)
        {
            Context context = new InitialContext();
            Context compCtx = (Context)context.lookup("java:comp");
            compCtx.addToEnvironment("org.mortbay.jndi.unlock", _key); 
        }
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#parseAnnotations()
     */
    public void parseAnnotations() throws Exception
    {
        //see org.mortbay.jetty.annotations.Configuration instead
    }
    
}

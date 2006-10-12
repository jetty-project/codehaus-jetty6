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

import org.mortbay.jetty.plus.naming.EnvEntry;
import org.mortbay.jetty.plus.naming.NamingEntry;
import org.mortbay.jetty.plus.naming.Resource;
import org.mortbay.jetty.plus.naming.Transaction;
import org.mortbay.log.Log;
import org.mortbay.naming.NamingUtil;

/**
 * Configuration
 *
 *
 */
public class Configuration extends AbstractConfiguration
{

    private Integer key;
    
    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindEnvEntry(java.lang.String, java.lang.String)
     * @param name
     * @param value
     * @throws Exception
     */
    public void bindEnvEntry(String name, Object value) throws Exception
    {    
        EnvEntry envEntry = EnvEntry.getEnvEntry(NamingEntry.SCOPE_LOCAL, name);
        if ((envEntry != null) && envEntry.isOverrideWebXml())
            return; //already bound a locally scoped value which should override web.xml
        
        envEntry = EnvEntry.getEnvEntry(NamingEntry.SCOPE_GLOBAL, name);
        if ((envEntry == null) || !envEntry.isOverrideWebXml())
        {
            
            //if either there isn't an env-entry in jetty-env.xml or a global one,
            //or there is one and it isn't set to override the web.xml one, then
            //bind the web.xml one
            InitialContext ic = new InitialContext();
            Context envCtx = (Context)ic.lookup("java:comp/env");
            NamingUtil.bind(envCtx, name, value);
            Log.info("Bound java:comp/env/"+name+"="+value);  
        }
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindResourceRef(java.lang.String)
     * @param name
     * @throws Exception
     */
    public void bindResourceRef(String name) throws Exception
    {
        
        Resource resource = Resource.getResource(NamingEntry.SCOPE_LOCAL, name);
        if (resource != null)
            return; //already bound the locally scoped resource of that name
        
        resource = Resource.getResource(NamingEntry.SCOPE_GLOBAL, name);
        if (resource != null)
        {
            //no locally scoped overrides, so bind the global one
            resource.bindToEnv();
            Log.info("Bound resourceref java:comp/env/"+name);
        }
        else
        {
            Log.warn("No resource to bind matching name="+name);
        }
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindResourceEnvRef(java.lang.String)
     * @param name
     * @throws Exception
     */
    public void bindResourceEnvRef(String name) throws Exception
    {
        
        Resource resource = Resource.getResource(NamingEntry.SCOPE_LOCAL, name);
        if (resource != null)
            return; //already bound
        resource = Resource.getResource(NamingEntry.SCOPE_GLOBAL, name);
        if (resource != null)
        {
            resource.bindToEnv();
            Log.info("Bound resource-env-ref java:comp/env/"+name);
        }
        else
            Log.warn("No resource to bind matching name="+name);
    }
    
    public void bindUserTransaction () throws Exception
    {
        Transaction transaction = Transaction.getTransaction (NamingEntry.SCOPE_LOCAL);
        if (transaction != null)
        {
            //special case, still need to bind it because it has to be bound to java:comp instead
            //of java:comp/env
            transaction.bindToEnv();
            return;
        }
        
        transaction = Transaction.getTransaction(NamingEntry.SCOPE_GLOBAL);
        if (transaction != null)
        {
            transaction.bindToEnv();
            Log.info("Bound UserTransaction to java:comp/"+transaction.getJndiName());
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
        key = new Integer(random.nextInt());
        Context context = new InitialContext();
        Context compCtx = (Context)context.lookup("java:comp");
        compCtx.addToEnvironment("org.mortbay.jndi.lock", key);
    }
    
    protected void unlockCompEnv ()
    throws Exception
    {
        Context context = new InitialContext();
        Context compCtx = (Context)context.lookup("java:comp");
        compCtx.addToEnvironment("org.mortbay.jndi.unlock", key);        
    }
}

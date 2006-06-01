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
        InitialContext ic = new InitialContext();
        Context envCtx = (Context)ic.lookup("java:comp/env");
        EnvEntry envEntry = EnvEntry.getEnvEntry(name);
        if ((envEntry == null) ||
            ((envEntry != null) && (!envEntry.isOverrideWebXml())))
         {
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
        Resource resource = Resource.getResource(name);
        if (resource != null)
        {
            resource.bindToEnv();
            Log.info("Bound resourceref java:comp/env/"+name);
        }
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindResourceEnvRef(java.lang.String)
     * @param name
     * @throws Exception
     */
    public void bindResourceEnvRef(String name) throws Exception
    {
        Resource resource = Resource.getResource(name);
        if (resource != null)
        {
            resource.bindToEnv();
            Log.info("Bound resource-env-ref java:comp/env/"+name);
        }
    }
    
    public void bindUserTransaction () throws Exception
    {
        Transaction transaction = Transaction.getTransaction ();
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

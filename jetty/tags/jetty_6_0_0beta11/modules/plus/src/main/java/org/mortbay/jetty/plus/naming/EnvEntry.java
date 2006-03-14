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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingException;

import org.mortbay.log.Log;


/**
 * EnvEntry
 *
 *
 */
public class EnvEntry extends NamingEntry
{
    private boolean overrideWebXml;
    
    public static EnvEntry getEnvEntry (String jndiName)
    throws NamingException
    {
        try
        {
            //lookup an EnvEntry first in the webapp specific naming
            //context, but if one doesn't exist, then try the global
            Context context = getThreadLocalContext();
            Object o = null;
            if (context != null)
            {
                try
                {
                    o = lookupNamingEntry(context, EnvEntry.class, jndiName);
                }
                catch (NameNotFoundException e)
                {
                    Log.ignore(e);
                    Log.debug("Didn't find "+jndiName+" in thread context context");
                }
            }
            if (o == null)
            {
                o = lookupNamingEntry(new InitialContext(), EnvEntry.class, jndiName);
                Log.debug("Found env entry "+jndiName+" in global context");
            }
            return (EnvEntry)o;
        }
        catch (NameNotFoundException e)
        {
           Log.debug("Didn't find "+jndiName+" anywhere");
            return null;
        }
    }
    
    

    
    
    public EnvEntry (String jndiName, Object objToBind)
    throws NamingException
    {
        this(jndiName, objToBind, false);
    }
    
    public EnvEntry (String jndiName, Object objToBind, boolean overrideWebXml)
    throws NamingException
    {
        super(jndiName, objToBind);
        this.overrideWebXml = overrideWebXml;
    }
    
    
    public boolean isOverrideWebXml ()
    {
        return this.overrideWebXml;
    }
    
    /** Bind the object wrapped in this EnvEntry into java:comp/env.
     * If, however, it is set to NOT override the web.xml entry,
     * then don't bind it. This method works in conjunction with
     * org.mortbay.jetty.plus.webapp.Configuration.bindEnvEntry().
     * TODO clean this up
     * @see org.mortbay.jetty.plus.naming.NamingEntry#bindToEnv()
     * @throws NamingException
     */
    public void bindToEnv ()
    throws NamingException
    {
        InitialContext iContext = new InitialContext();
        Context env = (Context)iContext.lookup("java:comp/env");
        
        boolean doBind = false;
        try
        {
            env.lookup(getJndiName());
            if (isOverrideWebXml())
                doBind = true;       
        }
        catch (NameNotFoundException e)
        {
            doBind = true;
        }
        
        if (doBind)
            super.bindToEnv();
    }
    
}

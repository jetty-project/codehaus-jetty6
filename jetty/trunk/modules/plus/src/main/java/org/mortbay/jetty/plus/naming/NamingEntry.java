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
import javax.naming.LinkRef;
import javax.naming.NamingException;

import org.mortbay.log.Log;
import org.mortbay.naming.NamingUtil;



/**
 * NamingEntry
 *
 *
 */
public abstract class NamingEntry
{
    private String jndiName;
    private Object objectToBind;
    
   
    
    
    public NamingEntry (String jndiName, Object object)
    throws NamingException
    {
        this.jndiName = jndiName;
        this.objectToBind = object;
        InitialContext context = new InitialContext();
        //bind the NamingEntry so we can reference it later
        NamingUtil.bind(context, getClass().getName()+"/"+getJndiName(), this);
        //bind the object itself so that we can link to it later
        NamingUtil.bind(context, getJndiName(), getObjectToBind());
        Log.info("Bound "+getJndiName() + " to global directory");
    }

    
    
    public void bindToEnv ()
    throws NamingException
    {
        InitialContext ic = new InitialContext();
        Context env = (Context)ic.lookup("java:comp/env");
        NamingUtil.bind(env, getJndiName(), new LinkRef(getJndiName()));
    }
    
    
    public String getJndiName ()
    {
        return this.jndiName;
    }
    
    public Object getObjectToBind()
    {
        return this.objectToBind;
    }
    
    protected void bind (Context context)
    throws NamingException
    {
        NamingUtil.bind(context, getJndiName(), getObjectToBind());
    }
}

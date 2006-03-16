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
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.mortbay.log.Log;

/**
 * Resource
 *
 *
 */
public class Resource extends NamingEntry
{
  
    public static Resource getResource (String jndiName)
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
                    o = lookupNamingEntry(context, Resource.class, jndiName);
                }
                catch (NameNotFoundException e)
                {
                    Log.ignore(e);
                    Log.debug("Didn't find Resource "+jndiName +" in thread local context "+context);
                }
            }
            if (o == null)
            {
                o = lookupNamingEntry(new InitialContext(), Resource.class, jndiName);
                Log.debug("Found Resource in global context for "+jndiName);
            }
            return (Resource)o;
        }
        catch (NameNotFoundException e)
        {
            Log.debug("Returning NULL as Resource not found for "+jndiName);
            return null;
        }
    }
    
    /**
     * @param jndiName
     * @param objToBind
     */
    public Resource (String jndiName, Object objToBind)
    throws NamingException
    {
        super(jndiName, objToBind);
    }

}

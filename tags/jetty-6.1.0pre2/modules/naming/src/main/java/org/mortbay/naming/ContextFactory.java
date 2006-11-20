// ========================================================================
// $Id$
// Copyright 1999-2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.naming;


import java.util.Hashtable;
import java.util.WeakHashMap;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.mortbay.log.Log;



/**
 * CompContextFactory.java
 *
 *
 * Created: Fri Jun 27 09:26:40 2003
 *
 * @author <a href="mailto:janb@mortbay.com">Jan Bartel</a>
 * @version 1.0
 */
public class ContextFactory implements ObjectFactory
{
    //map of classloaders to contexts
    private static WeakHashMap _contextMap;


    static
    {
        _contextMap = new WeakHashMap();
    }
    
  

    public Object getObjectInstance (Object obj,
                                     Name name,
                                     Context nameCtx,
                                     Hashtable env)
        throws Exception
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if(Log.isDebugEnabled()) Log.debug("looking for context for "+loader);
        Context ctx = (Context)_contextMap.get(loader);
        
        //the map does not contain an entry for this classloader
        if (ctx == null)
        {
            //check if a parent classloader has created the context
            ctx = getParentClassLoaderContext(loader);

            if (ctx == null)
            {
                Reference ref = (Reference)obj;
                StringRefAddr parserAddr = (StringRefAddr)ref.get("parser");
                String parserClassName = (parserAddr==null?null:(String)parserAddr.getContent());
                NameParser parser = (NameParser)(parserClassName==null?null:loader.loadClass(parserClassName).newInstance());
                
                ctx = new NamingContext (env,
                                         name.get(0),
                                         nameCtx,
                                         parser);
                if(Log.isDebugEnabled())Log.debug("No entry for classloader: "+loader);
                _contextMap.put (loader, ctx);
            }
        }

        return ctx;
    }

    public Context getParentClassLoaderContext (ClassLoader loader)
    {
        Context ctx = null;
        ClassLoader cl = loader;
        for (cl = cl.getParent(); (cl != null) && (ctx == null); cl = cl.getParent())
        {
            ctx = (Context)_contextMap.get(cl);
        }

        return ctx;
    }
} 

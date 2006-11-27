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

import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.log.Log;



/**
 * ContextFactory.java
 *
 * This is an object factory that produces a jndi naming
 * context based on a classloader. 
 * 
 *  It is used for the java:comp context.
 *  
 *  This object factory is bound at java:comp. When a
 *  lookup arrives for java:comp,  this object factory
 *  is invoked and will return a context specific to
 *  the caller's environment (so producing the java:comp/env
 *  specific to a webapp).
 *  
 *  The context selected is based on classloaders. First
 *  we try looking in at the classloader that is associated
 *  with the current webapp context (if there is one). If
 *  not, we use the thread context classloader.
 * 
 * Created: Fri Jun 27 09:26:40 2003
 *
 * @author <a href="mailto:janb@mortbay.com">Jan Bartel</a>
 * 
 */
public class ContextFactory implements ObjectFactory
{
    //map of classloaders to contexts
    private static WeakHashMap _contextMap;


    static
    {
        _contextMap = new WeakHashMap();
    }
    
  

    /** 
     * Find or create a context which pertains to a classloader.
     * 
     * We use either the classloader for the current ContextHandler if
     * we are handling a request, OR we use the thread context classloader
     * if we are not processing a request.
     * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
     */
    public Object getObjectInstance (Object obj,
                                     Name name,
                                     Context nameCtx,
                                     Hashtable env)
        throws Exception
    {
        
        // First, see if we are in a webapp context, if we are, use
        // the classloader of the webapp to find the right jndi comp context
        ClassLoader loader = null;
        if (ContextHandler.getCurrentContext() != null)
        {
            loader = ContextHandler.getCurrentContext().getContextHandler().getClassLoader();
        }
        
        
        if (loader != null)
        {
            if (Log.isDebugEnabled()) Log.debug("Using classloader of current org.mortbay.jetty.handler.ContextHandler");
        }
        else
        {
            //Not already in a webapp context, in that case, we must use the
            //curren't thread's classloader instead
            loader = Thread.currentThread().getContextClassLoader();
            if (Log.isDebugEnabled()) Log.debug("Using thread context classloader");
        }
        
        //Get the context matching the classloader
        Context ctx = (Context)_contextMap.get(loader);
        
        //The map does not contain an entry for this classloader
        if (ctx == null)
        {
            //Check if a parent classloader has created the context
            ctx = getParentClassLoaderContext(loader);

            //Didn't find a context to match any of the ancestors
            //of the classloader, so make a context
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

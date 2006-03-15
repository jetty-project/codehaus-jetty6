// ========================================================================
// $Id$
// Copyright 1999-2004 Mort Bay Consulting Pty. Ltd.
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

import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.mortbay.jetty.plus.naming.EnvEntry;
import org.mortbay.jetty.plus.naming.NamingEntry;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlConfiguration;

/**
 * EnvConfiguration
 *
 *
 */
public class EnvConfiguration implements Configuration
{
    private WebAppContext webAppContext;
    private Context compCtx;
    private Context envCtx;
    private Context localContext;

    protected void createEnvContext ()
    throws NamingException
    {
        Context context = new InitialContext();
        compCtx =  (Context)context.lookup ("java:comp");
        envCtx = compCtx.createSubcontext("env");
        if (Log.isDebugEnabled())
            Log.debug("Created java:comp/env for webapp "+getWebAppContext().getContextPath());
    }
    
    
    /** 
     * @see org.mortbay.jetty.webapp.Configuration#setWebAppContext(org.mortbay.jetty.webapp.WebAppContext)
     * @param context
     */
    public void setWebAppContext(WebAppContext context)
    {
        this.webAppContext = context;
    }

    /** 
     * @see org.mortbay.jetty.webapp.Configuration#getWebAppContext()
     * @return
     */
    public WebAppContext getWebAppContext()
    {
        return webAppContext;
    }

    /** 
     * @see org.mortbay.jetty.webapp.Configuration#configureClassLoader()
     * @throws Exception
     */
    public void configureClassLoader() throws Exception
    {
    }

    /** 
     * @see org.mortbay.jetty.webapp.Configuration#configureDefaults()
     * @throws Exception
     */
    public void configureDefaults() throws Exception
    {
    }

    /** 
     * @see org.mortbay.jetty.webapp.Configuration#configureWebApp()
     * @throws Exception
     */
    public void configureWebApp() throws Exception
    {
        //create a java:comp/env
        createEnvContext();
        
        //add java:comp/env entries for any globally defined EnvEntries
        addGlobalEnvEntries();
        
        //create a special context in the global namespace that is
        //a place to bind any webapp specific jndi entries so that
        //they can be found during the parsing of web.xml
        InitialContext icontext = new InitialContext();
        localContext = icontext.createSubcontext(Long.toString(getWebAppContext().hashCode(),36)+getWebAppContext().getContextPath().replace('/','_'));
        NamingEntry.setThreadLocalContext(localContext);
        
        
        //look for a file called WEB-INF/jetty-env.xml
        //and process it if it exists
        Resource webInf = getWebAppContext().getWebInf();
        if(webInf!=null && webInf.isDirectory())
        {
            Resource jettyEnv = webInf.addPath("jetty-env.xml");
            if(jettyEnv.exists())
            {
                XmlConfiguration configuration = new XmlConfiguration(jettyEnv.getURL());
                configuration.configure(getWebAppContext());
            }
        }
        
        //add java:comp/env entries for all webapp-specific EnvEntries
        addLocalEnvEntries();
    }

    /** 
     * @see org.mortbay.jetty.webapp.Configuration#deconfigureWebApp()
     * @throws Exception
     */
    public void deconfigureWebApp() throws Exception
    {
    }
    
    /**
     * Add java:comp/env entries for all globally defined EnvEntries
     */
    public void addGlobalEnvEntries ()
    throws NamingException
    {
        Log.debug("Finding global env entries");
        addEnvEntries(new InitialContext());
    }
    
    public void addLocalEnvEntries()
    throws NamingException
    {
        Log.debug("Finding webapp specific env entries");
        addEnvEntries(localContext);
    }
    
    
    public void addEnvEntries (Context context)
    throws NamingException
    {
        List  list = NamingEntry.lookupNamingEntries (context, EnvEntry.class);
        Iterator itor = list.iterator();
        
        Log.debug("Finding env entries: size="+list.size());
        while (itor.hasNext())
        {
            EnvEntry ee = (EnvEntry)itor.next();
            Log.debug("configuring env entry "+ee.getJndiName());
            ee.bindToEnv();
        }
    }

}

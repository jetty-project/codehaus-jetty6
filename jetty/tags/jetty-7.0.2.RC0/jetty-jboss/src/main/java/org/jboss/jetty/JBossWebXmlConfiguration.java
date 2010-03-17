//========================================================================
//$Id: JBossWebXmlConfiguration.java 2564 2008-04-04 07:28:30Z janb $
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.jboss.jetty;

import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.webapp.WebXmlProcessor;
import org.eclipse.jetty.xml.XmlParser;
import org.jboss.logging.Logger;
import org.jboss.metadata.WebMetaData;

/**
 * JBossWebXmlConfiguration
 *
 * Extends the jetty WebXmlConfiguration to provide jboss
 * handling of various elements in the web.xml
 */
public class JBossWebXmlConfiguration extends WebXmlConfiguration
{
    protected static Logger __log=Logger.getLogger(JBossWebAppContext.class); 

  
    public class JBossWebXmlProcessor extends WebXmlProcessor
    {

        public JBossWebXmlProcessor(WebAppContext context) throws ClassNotFoundException
        {
            super(context);
        }
        
        protected void initLoginConfig(XmlParser.Node node) throws Exception
        {
            super.initLoginConfig(node);
            
            //use a security domain name from jboss-web.xml
            if (null==_securityHandler.getRealmName())
            {
                WebMetaData metaData = ((JBossWebAppContext)_context)._webApp.getMetaData();
                String realmName = metaData.getSecurityDomain();
                if (null!=realmName)
                {
                    if (realmName.endsWith("/"))
                        realmName = realmName.substring (0, realmName.length());
                    int idx = realmName.lastIndexOf('/');
                    if (idx >= 0)
                        realmName = realmName.substring(idx+1);
                }
                _securityHandler.setRealmName(realmName);
            }
           
            if(__log.isDebugEnabled())
                __log.debug("Realm name is : "+_securityHandler.getRealmName());
        }
    }

    public void configure(WebAppContext context) throws Exception
    {
        super.configure(context);
    }


    public void deconfigure(WebAppContext context) throws Exception
    {
        super.deconfigure(context);
    }


    public void postConfigure(WebAppContext context) throws Exception
    {
        super.postConfigure(context);
    }


    public void preConfigure(WebAppContext context) throws Exception
    {
        super.preConfigure(context);
    }
}

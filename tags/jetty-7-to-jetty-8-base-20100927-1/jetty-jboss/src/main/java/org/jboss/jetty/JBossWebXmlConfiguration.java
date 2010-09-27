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

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.IterativeDescriptorProcessor;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlParser;
import org.jboss.logging.Logger;
import org.jboss.metadata.WebMetaData;

/**
 * JBossWebXmlConfiguration
 *
 * Extends the jetty WebXmlConfiguration to provide jboss
 * handling of various elements in the web.xml
 */
public class JBossWebXmlConfiguration implements Configuration
{
    protected static Logger __log=Logger.getLogger(JBossWebAppContext.class); 
    protected WebAppContext _context;
    protected MetaData _metaData;
    protected SecurityHandler _securityHandler;
  
    public class JBossWebXmlProcessor extends IterativeDescriptorProcessor
    {

        public JBossWebXmlProcessor() throws ClassNotFoundException
        {
            try
            {
                registerVisitor("login-config",getClass().getDeclaredMethod("visitLoginConfig", __signature));
            }
            catch (Exception e)   
            {
                throw new IllegalStateException(e);
            }

        }
        
        /**
         * @see org.eclipse.jetty.webapp.IterativeDescriptorProcessor#start(org.eclipse.jetty.webapp.Descriptor)
         */
        public void start(Descriptor descriptor)
        {
            _metaData = descriptor.getMetaData();
            _context = _metaData.getContext();
            _securityHandler = (SecurityHandler)_context.getSecurityHandler();
        }


        /**
         * @see org.eclipse.jetty.webapp.IterativeDescriptorProcessor#end(org.eclipse.jetty.webapp.Descriptor)
         */
        public void end(Descriptor descriptor)
        {
            _metaData = null;
            _context = null;
        }

        protected void visitLoginConfig(Descriptor descriptor, XmlParser.Node node) throws Exception
        {
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
    }


    public void deconfigure(WebAppContext context) throws Exception
    {
    }


    public void postConfigure(WebAppContext context) throws Exception
    {
    }


    public void preConfigure(WebAppContext context) throws Exception
    {
    }
}

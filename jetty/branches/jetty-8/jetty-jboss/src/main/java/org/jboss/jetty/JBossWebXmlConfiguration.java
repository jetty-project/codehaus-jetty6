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
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.IterativeDescriptorProcessor;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.StandardDescriptorProcessor;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
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

  
    public static class JBossWebXmlProcessor extends IterativeDescriptorProcessor
    {
    	protected MetaData _metaData;
    	protected WebAppContext _context;
    	protected SecurityHandler _securityHandler;
    	
        public JBossWebXmlProcessor() throws ClassNotFoundException
        {
        	   try
               {
                   registerVisitor("login-config", this.getClass().getDeclaredMethod("visitLoginConfig", __signature));
               }
        	   catch (Exception e)
        	   {
        		   throw new IllegalStateException(e);
        	   }
        }
        
        @Override
		public void start(Descriptor descriptor) 
		{
            _metaData = descriptor.getMetaData();
            _context = _metaData.getContext();
            _securityHandler = (SecurityHandler)_context.getSecurityHandler();
		}
		
		@Override
		public void end(Descriptor descriptor) 
		{
	        _metaData = null;
	        _context = null;
		}

		
        public void visitLoginConfig(Descriptor descriptor, XmlParser.Node node) throws Exception
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
        super.configure(context);
        MetaData metaData = (MetaData)context.getAttribute(MetaData.METADATA); 
        if (metaData == null)
           throw new IllegalStateException ("No metadata");
        
        metaData.addDescriptorProcessor(new JBossWebXmlProcessor());
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

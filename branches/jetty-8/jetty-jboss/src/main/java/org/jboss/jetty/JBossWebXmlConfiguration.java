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

  
<<<<<<< .working
<<<<<<< .working
    public static class JBossWebXmlProcessor extends IterativeDescriptorProcessor
=======
    
=======
    
>>>>>>> .merge-right.r6613
    public class JBossWebXmlProcessor extends IterativeDescriptorProcessor
>>>>>>> .merge-right.r6623
    {
<<<<<<< .working
<<<<<<< .working
    	protected MetaData _metaData;
    	protected WebAppContext _context;
    	protected SecurityHandler _securityHandler;
    	
=======
>>>>>>> .merge-right.r6623
=======
>>>>>>> .merge-right.r6613
        public JBossWebXmlProcessor() throws ClassNotFoundException
        {
<<<<<<< .working
        	   try
               {
                   registerVisitor("login-config", this.getClass().getDeclaredMethod("visitLoginConfig", __signature));
               }
        	   catch (Exception e)
        	   {
        		   throw new IllegalStateException(e);
        	   }
=======
            try
            {
                registerVisitor("login-config",getClass().getDeclaredMethod("visitLoginConfig", __signature));
            }
            catch (Exception e)   
            {
                throw new IllegalStateException(e);
            }
<<<<<<< .working
>>>>>>> .merge-right.r6623
=======
>>>>>>> .merge-right.r6613
        }
        
<<<<<<< .working
<<<<<<< .working
        @Override
		public void start(Descriptor descriptor) 
		{
=======
        @Override
        public void start(WebAppContext context, Descriptor descriptor)
=======
        @Override
        public void start(WebAppContext context, Descriptor descriptor)
>>>>>>> .merge-right.r6613
        {
<<<<<<< .working
>>>>>>> .merge-right.r6623
            _metaData=context.getMetaData();
            _context=context;
=======
            _metaData=context.getMetaData();
            _context=context;
>>>>>>> .merge-right.r6613
            _securityHandler = (SecurityHandler)_context.getSecurityHandler();
		}
		
		@Override
		public void end(Descriptor descriptor) 
		{
	        _metaData = null;
	        _context = null;
		}

<<<<<<< .working
<<<<<<< .working
		
        public void visitLoginConfig(Descriptor descriptor, XmlParser.Node node) throws Exception
        {    
=======
        @Override
        public void end(WebAppContext context, Descriptor descriptor)
=======
        @Override
        public void end(WebAppContext context, Descriptor descriptor)
>>>>>>> .merge-right.r6613
        {
            _metaData = null;
            _context = null;
        }

        public void visitLoginConfig(WebAppContext context, Descriptor descriptor, XmlParser.Node node) throws Exception
        {
>>>>>>> .merge-right.r6623
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
        context.getMetaData().addDescriptorProcessor(new JBossWebXmlProcessor());
        context.getMetaData().addDescriptorProcessor(new JBossWebXmlProcessor());
    }

    public void deconfigure(WebAppContext context) throws Exception
    {
        super.deconfigure(context);
    }

    public void destroy(WebAppContext context) throws Exception
    {
    }

    public void postConfigure(WebAppContext context) throws Exception
    {
        super.postConfigure(context);
    }

    public void preConfigure(WebAppContext context) throws Exception
    {
        super.preConfigure(context);
    }

    public void cloneConfigure(WebAppContext template, WebAppContext context) throws Exception
    {
    }
}

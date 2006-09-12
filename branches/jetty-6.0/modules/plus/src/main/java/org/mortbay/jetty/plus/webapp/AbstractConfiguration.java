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

package org.mortbay.jetty.plus.webapp;

import org.mortbay.jetty.webapp.WebXmlConfiguration;
import org.mortbay.util.TypeUtil;
import org.mortbay.xml.XmlParser;



/**
 * Configuration
 *
 *
 */
public abstract class AbstractConfiguration extends WebXmlConfiguration
{
  
    
    public abstract void bindEnvEntry (String name, Object value) throws Exception;
    
    public abstract void bindResourceRef (String name) throws Exception;
    
    public abstract void bindResourceEnvRef (String name) throws Exception;
    
    public abstract void bindUserTransaction () throws Exception;
    
    
    
    public void configureDefaults ()
    throws Exception
    {
        super.configureDefaults();
    }
   
    public void configureWebApp ()
    throws Exception
    {
        super.configureWebApp();
        bindUserTransaction();
    }
    
    protected void initWebXmlElement(String element,XmlParser.Node node) throws Exception
    {
        if ("env-entry".equals(element))
        {
            String name=node.getString("env-entry-name",false,true);
            Object value= TypeUtil.valueOf(node.getString("env-entry-type",false,true),
                                           node.getString("env-entry-value",false,true));
            bindEnvEntry(name, value);
           
        }
        else if ("resource-ref".equals(element))
        {
            //resource-ref entries are ONLY for connection factories
            //the resource-ref says how the app will reference the jndi lookup relative
            //to java:comp/env, but it is up to the deployer to map this reference to
            //a real resource in the environment. At the moment, we insist that the
            //jetty.xml file name of the resource has to be exactly the same as the
            //name in web.xml deployment descriptor, but it shouldn't have to be
            bindResourceRef(node.getString("res-ref-name",false,true));
            
        }
        else if ("resource-env-ref".equals(element))
        {
            //resource-env-ref elements are a non-connection factory type of resource
            //the app looks them up relative to java:comp/env
            //again, need a way for deployer to link up app naming to real naming.
            //Again, we insist now that the name of the resource in jetty.xml is
            //the same as web.xml
            bindResourceEnvRef(node.getString("resource-env-ref-name",false,true));
        }
        else
        {
            super.initWebXmlElement(element, node);
        }
 
    }
    
    
}
//========================================================================
//$Id$
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

package org.mortbay.jetty.plugin;

import java.io.File;
import java.util.List;

import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

/**
 * Jetty6PluginWebAppContext
 *
 *
 */
public class JettyWebAppContext extends WebAppContext
{
    private List classpathFiles;
    private File jettyEnvXmlFile;
    private File webXmlFile;
    private WebInfConfiguration webInfConfig = new WebInfConfiguration();
    private EnvConfiguration envConfig =  new EnvConfiguration();
    private JettyMavenConfiguration mvnConfig;
    private JettyWebXmlConfiguration jettyWebConfig;
    private TagLibConfiguration tagConfig;
    private Configuration[] configs;
    
    public JettyWebAppContext ()
    throws Exception
    {
        super();
       
        mvnConfig = new JettyMavenConfiguration();
        jettyWebConfig = new JettyWebXmlConfiguration();
        tagConfig =  new TagLibConfiguration();
        configs = new Configuration[]{webInfConfig,envConfig, mvnConfig, jettyWebConfig, tagConfig};
        setConfigurations(configs);
    }
    
    public void setClassPathFiles(List classpathFiles)
    {
        this.classpathFiles = classpathFiles;
    }

    public List getClassPathFiles()
    {
        return this.classpathFiles;
    }
    
    public void setWebXmlFile(File webXmlFile)
    {
        this.webXmlFile = webXmlFile;
    }
    
    public File getWebXmlFile()
    {
        return this.webXmlFile;
    }
    
    public void setJettyEnvXmlFile (File jettyEnvXmlFile)
    {
        this.jettyEnvXmlFile = jettyEnvXmlFile;
    }
    
    public File getJettyEnvXmlFile()
    {
        return this.jettyEnvXmlFile;
    }
    
    public void configure ()
    {        
        setConfigurations(configs);
        mvnConfig.setClassPathConfiguration (classpathFiles);
        mvnConfig.setWebXml (webXmlFile);  
        try
        {
            if (this.jettyEnvXmlFile != null)
                envConfig.setJettyEnvXml(this.jettyEnvXmlFile.toURL());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public void doStart () throws Exception
    {
        setShutdown(false);
        super.doStart();
    }
     
    public void doStop () throws Exception
    {
        setShutdown(true);
        //just wait a little while to ensure no requests are still being processed
        Thread.currentThread().sleep(500L);
        super.doStop();
    }
}

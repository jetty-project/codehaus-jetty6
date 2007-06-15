//========================================================================
//$Id$
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
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

import org.mortbay.jetty.plugin.util.JettyPluginWebApplication;
import org.mortbay.jetty.plus.webapp.EnvConfiguration;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.JettyWebXmlConfiguration;
import org.mortbay.jetty.webapp.TagLibConfiguration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.webapp.WebInfConfiguration;

/**
 * Jetty6PluginWebApplication
 * 
 * Wrapper for jetty6 WebAppContext
 *
 */

class Jetty6PluginWebApplication implements JettyPluginWebApplication
{
    private WebAppContext context;
    private File webAppDir;
    private File webXmlFile;
    private File overrideWebXmlFile;
    private File jettyEnvXmlFile;
    private List classpathFiles;
    private Configuration[] configurations = {new WebInfConfiguration(), new EnvConfiguration(), new Jetty6MavenConfiguration(), new JettyWebXmlConfiguration(), new TagLibConfiguration()};
    
    protected Jetty6PluginWebApplication()
    {
        context = new WebAppContext();
    }
    
    public void setContextPath(String path)
    {
       context.setContextPath(path);                
    }

    public String getContextPath()
    {
        return this.context.getContextPath();
    }
   
    public void setWebAppSrcDir(File webAppDir) throws Exception
    {
        this.webAppDir = webAppDir;
        context.setWar(webAppDir.getCanonicalPath());
    }

    
    public void setTempDirectory(File tmpDir)
    {
        context.setTempDirectory(tmpDir);
        tmpDir.deleteOnExit();
    }
    
    /**
     * Set the location of the defaults descriptor to use.
     * NOTE: if the user hasn't supplied one, then don't set
     * a null value as we want jetty to use its own default.
     * 
     * @see org.mortbay.jetty.plugin.util.JettyPluginWebApplication#setWebDefaultXmlFile(java.io.File)
     */
    public void setWebDefaultXmlFile(File webDefaultXml) 
    throws Exception
    {
        if (webDefaultXml != null)
            context.setDefaultsDescriptor(webDefaultXml.getCanonicalPath());
    }
    
    
    public void setOverrideWebXmlFile (File overrideWebXml)
    throws Exception
    {
        this.overrideWebXmlFile = overrideWebXml;
        if (overrideWebXml != null)
            context.setOverrideDescriptor(overrideWebXml.getCanonicalPath());
    }
    
    public void configure ()
    {        

        for (int i=0;i<configurations.length; i++)
        {
            if (configurations[i] instanceof Jetty6MavenConfiguration)
            {
                ((Jetty6MavenConfiguration)configurations[i]).setClassPathConfiguration (classpathFiles);
                ((Jetty6MavenConfiguration)configurations[i]).setWebXml (webXmlFile);              
            }
            else if (configurations[i] instanceof EnvConfiguration)
            {
                try
                {
                    if (this.jettyEnvXmlFile != null)
                        ((EnvConfiguration)configurations[i]).setJettyEnvXml(this.jettyEnvXmlFile.toURL());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
            
        this.context.setConfigurations(configurations);
    }

  
    public void setClassPathFiles(List classpathFiles)
    {
        this.classpathFiles = classpathFiles;
    }

    
    public void setWebXmlFile(File webXmlFile)
    {
        this.webXmlFile = webXmlFile;
    }
    
    public void setJettyEnvXmlFile (File jettyEnvXmlFile)
    {
        this.jettyEnvXmlFile = jettyEnvXmlFile;
    }
    
    public void start () throws Exception
    {
        this.context.setShutdown(false);
        this.context.start();
    }
     
    public void stop () throws Exception
    {
        this.context.setShutdown(true);
        //just wait a little while to ensure no requests are still being processed
        Thread.currentThread().sleep(500L);
        this.context.stop();
    }

    /**
     * @see org.mortbay.jetty.plugin.util.JettyPluginWebApplication#getProxiedObject()
     */
    public Object getProxiedObject()
    {
        return this.context;
    }

   
}

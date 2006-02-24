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
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.JettyWebXmlConfiguration;
import org.mortbay.jetty.webapp.TagLibConfiguration;
import org.mortbay.jetty.webapp.WebAppContext;

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
    private List classpathFiles;
    private Configuration[] configurations = {null, new JettyWebXmlConfiguration(), new TagLibConfiguration()};
    
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
    }
    
    
    public void configure ()
    {        
        Jetty6MavenConfiguration mavenConfig = new Jetty6MavenConfiguration();
        mavenConfig.setClassPathConfiguration (webAppDir, classpathFiles);
        mavenConfig.setWebXml (webXmlFile);
        configurations[0]=mavenConfig;
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
    
    public void start () throws Exception
    {
        this.context.start();
    }
     
    public void stop () throws Exception
    {
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
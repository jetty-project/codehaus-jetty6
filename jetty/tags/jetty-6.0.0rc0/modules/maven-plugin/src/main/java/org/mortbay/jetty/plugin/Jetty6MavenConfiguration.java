//========================================================================
//$Id$
//Copyright 2000-2005 Mort Bay Consulting Pty. Ltd.
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
import java.util.Iterator;
import java.util.List;

import org.mortbay.jetty.plugin.util.PluginLog;
import org.mortbay.jetty.plus.webapp.Configuration;
import org.mortbay.jetty.webapp.WebAppClassLoader;

public class Jetty6MavenConfiguration extends Configuration 
{
    private List classPathFiles;
    private File webXmlFile;
    private File webAppDir;
    
   
    public Jetty6MavenConfiguration()
    {
        super();
    }

    public void setClassPathConfiguration(File webAppDir, List classPathFiles)
    {
        this.webAppDir = webAppDir;
        this.classPathFiles = classPathFiles;
    }
    
    public void setWebXml (File webXmlFile)
    {
        this.webXmlFile = webXmlFile;
    }
    
    
    /** Set up the classloader for the webapp, using the various parts of the Maven project
     * @see org.mortbay.jetty.webapp.Configuration#configureClassLoader()
     */
    public void configureClassLoader() throws Exception 
    {
        PluginLog.getLog().debug("Setting up classpath ...");
      
        //put the classes dir and all dependencies into the classpath
        Iterator itor = classPathFiles.iterator();
        while (itor.hasNext())
            ((WebAppClassLoader)getWebAppContext().getClassLoader()).addClassPath(((File)itor.next()).getCanonicalPath());
        
        PluginLog.getLog().info("Classpath = "+((WebAppClassLoader)getWebAppContext().getClassLoader()).getUrlClassPath());
    }

    
    /** Parse the default webapp descriptor
     * @see org.mortbay.jetty.webapp.Configuration#configureDefaults()
     */
    public void configureDefaults() throws Exception 
    {
        super.configureDefaults();

    }

    /** Prepare webapp for starting by parsing web.xml
     * @see org.mortbay.jetty.webapp.Configuration#configureWebApp()
     */
    public void configureWebApp() throws Exception 
    {
        //cannot configure if the context is already started
        if (getWebAppContext().isStarted())
        {
            PluginLog.getLog().error("Cannot configure webapp after it is started");
            return;
        }
        
        PluginLog.getLog().debug("Started configuring web.xml, resource base="+webAppDir.getCanonicalPath());
        getWebAppContext().setResourceBase(webAppDir.getCanonicalPath());
        if (webXmlFile.exists())
            configure(webXmlFile.toURL().toString());
        PluginLog.getLog().debug("Finished configuring web.xml");
        
        bindUserTransaction();
        lockCompEnv();
    }

    
    
    /** Prepare webapp for stopping
     * @see org.mortbay.jetty.webapp.Configuration#deconfigureWebApp()
     */
    public void deconfigureWebApp() throws Exception 
    {
       super.deconfigureWebApp();
    }
    
}

//========================================================================
//$Id: JettyMavenConfiguration.java,v 1.7 2005/11/25 20:58:59 janb Exp $
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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebXmlConfiguration;
import org.mortbay.xml.XmlParser;

public class JettyMavenConfiguration extends WebXmlConfiguration 
{
    private Log log;
    private List classPathFiles;
    private File webXmlFile;
    private File webAppDir;
    
   
    public JettyMavenConfiguration()
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
    
    public void setLog (Log log)
    {
        this.log = log;       
    }
    
    public Log getLog ()
    {
        return this.log;
    }
    
    /** Set up the classloader for the webapp, using the various parts of the Maven project
     * @see org.mortbay.jetty.webapp.Configuration#configureClassLoader()
     */
    public void configureClassLoader() throws Exception 
    {
        getLog().info("Setting up classpath ...");
      
        //put the classes dir and all dependencies into the classpath
        Iterator itor = classPathFiles.iterator();
        while (itor.hasNext())
            ((WebAppClassLoader)getWebAppContext().getClassLoader()).addClassPath(((File)itor.next()).getCanonicalPath());
        
        getLog().info("Finished setting up classpath");
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
            getLog().info("Cannot configure webapp after it is started");
            return;
        }
        
        getLog().info("Started configuring web.xml, resource base="+webAppDir.getCanonicalPath());
        getWebAppContext().setResourceBase(webAppDir.getCanonicalPath());
        XmlParser.Node config = null;               
        config=_xmlParser.parse(webXmlFile.getCanonicalPath());          
        initialize(config);
        getLog().info("Finished configuring web.xml");
    }

    
    
    /** Prepare webapp for stopping
     * @see org.mortbay.jetty.webapp.Configuration#deconfigureWebApp()
     */
    public void deconfigureWebApp() throws Exception 
    {
       super.deconfigureWebApp();
    }
    
    
    private File findToolsJar()
    throws Exception
    {
    	 String javaHomeStr = System.getProperty("java.home");
         if ((javaHomeStr==null) || (javaHomeStr.equals("")))
         {
         		getLog().info("Environment variable JAVA_HOME not set, JSP compilation not available");
         		return null;
         }
         
    	getLog().info("java.home="+javaHomeStr);
    	File jdkHomeDir = new File (javaHomeStr);
    	File jdkLibDir = new File(jdkHomeDir, "lib");
    	File toolsJar = new File (jdkLibDir, "tools.jar");
    	
    	if (!toolsJar.exists())
    	{
    		jdkLibDir = new File (jdkHomeDir.getParentFile(), "lib");
    		toolsJar = new File(jdkLibDir, "tools.jar");
    		
    		if (!toolsJar.exists())
    		{
    			getLog().info("tools.jar does not exist, JSP compilation not available");
    			return null;
    		}
    	}
    	return toolsJar;
    }
   

}

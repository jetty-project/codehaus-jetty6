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

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

/**
 * JettyWebAppContext
 * 
 * Extends the WebAppContext to specialize for the maven environment.
 * We pass in the list of files that should form the classpath for
 * the webapp when executing in the plugin, and any jetty-env.xml file
 * that may have been configured.
 *
 */
public class JettyWebAppContext extends WebAppContext
{
    private List<File> classpathFiles;
    private String jettyEnvXml;
    private MavenWebInfConfiguration webInfConfig = new MavenWebInfConfiguration();
    private WebXmlConfiguration webXmlConfig = new WebXmlConfiguration();
    private MetaInfConfiguration metaInfConfig = new MetaInfConfiguration();
    private EnvConfiguration envConfig =  new EnvConfiguration();
    private AnnotationConfiguration annotationConfig = new MavenAnnotationConfiguration();
    private FragmentConfiguration fragConfig = new FragmentConfiguration();
    private org.eclipse.jetty.plus.webapp.Configuration plusConfig = new org.eclipse.jetty.plus.webapp.Configuration();
    private JettyWebXmlConfiguration jettyWebConfig =  new JettyWebXmlConfiguration();
    private TagLibConfiguration tagConfig = new TagLibConfiguration();
    private Configuration[] configs;
    
    public JettyWebAppContext ()
    throws Exception
    {
        super();     
        configs = new Configuration[]{webInfConfig, webXmlConfig,  metaInfConfig,  fragConfig, envConfig, annotationConfig, jettyWebConfig, tagConfig };
        setConfigurations(configs);
    }
    
    public void setClassPathFiles(List<File> classpathFiles)
    {
        this.classpathFiles = classpathFiles;
    }

    public List<File> getClassPathFiles()
    {
        return this.classpathFiles;
    }
    
    public void setJettyEnvXml (String jettyEnvXml)
    {
        this.jettyEnvXml = jettyEnvXml;
    }
    
    public String getJettyEnvXml()
    {
        return this.jettyEnvXml;
    }


    public void doStart () throws Exception
    {
        if (this.jettyEnvXml != null)
            envConfig.setJettyEnvXml(new File(this.jettyEnvXml).toURL());
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

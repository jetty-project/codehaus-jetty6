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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.mortbay.jetty.plugin.util.JettyPluginServer;
import org.mortbay.jetty.plugin.util.JettyPluginWebApplication;
import org.mortbay.jetty.plugin.util.PluginLog;
import org.mortbay.jetty.plugin.util.Scanner;
import org.mortbay.jetty.plugin.util.SystemProperty;
import org.mortbay.start.Version;



/**
 * AbstractJettyMojo
 *
 *
 */
public abstract class AbstractJettyMojo extends AbstractMojo
{
    
    public static String POM_VERSION;

    
    
    static
    {
        try
        {
            InputStream stream = AbstractJettyMojo.class.getResourceAsStream("/META-INF/maven/org.mortbay.jetty/maven-jetty6-plugin/pom.properties");
            if (stream != null)
            {
                Properties props = new Properties();
                props.load(stream);
                POM_VERSION = props.getProperty("version");
            }
        }
        catch (Exception e)
        {
            POM_VERSION = "6.0-SNAPSHOT";
        }
        
        
    }
    
    /**
     * The "virtual" webapp created by the plugin
     */
    private JettyPluginWebApplication webapp;
    
    

    /**
     * The proxy for the Server object
     */
    private JettyPluginServer server;
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    

    
    /**
     * The context path for the webapp. Defaults to the
     * name of the webapp's artifact.
     * 
     * @parameter expression="/${project.artifactId}"
     * @required
     */
    private String contextPath;
    
    
    /**
     * The temporary directory to use for the webapp.
     * Defaults to target/jetty-tmp
     * 
     * @parameter expression="${project.build.directory}/work"
     * @required
     */
    private File tmpDirectory;
    
    /**
     * The interval in seconds to scan the webapp for changes 
     * and restart the context if necessary. Disabled by default.
     * 
     * @parameter expression="0"
     * @required
     */
    private int scanIntervalSeconds;
    
    
    /**
     * System properties to set before execution. 
     * Note that these properties will NOT override System properties 
     * that have been set on the command line or by the JVM. Optional.
     * @parameter 
     */
    private SystemProperty[] systemProperties;
    
    
    
    /**
     * Location of a jetty xml configuration file whose contents 
     * will be applied before any plugin configuration. Optional.
     * @parameter
     */
    private String jettyConfig;
  
    
    /**
     * @component
     */
    private ArtifactResolver artifactResolver;
    
    /**
     * The component used for creating artifact instances.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    
    
    
    /**
    *
    *
    * @component
    */
    private ArtifactMetadataSource metadataSource;
    
    
    /**
     * The local repository.
     *
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * Remote repositories used for the project.
     *
     * @todo this is used for site descriptor resolution - it should relate to the actual project but for some reason they are not always filled in
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private List remoteRepositories;

    
    
    /**
    *
    *
    * @component 
    */
    private MavenProjectBuilder projectBuilder;
    
    /**
     *  List of files and directories to scan
     */
    private ArrayList scanList;
    
    /**
     * List of Listeners for the scanner
     */
    private ArrayList scannerListeners;
    
    
    private RuntimeDependencyResolver resolver;
    
    
    /**
     * @return Returns the realms configured in the pom
     */
    public abstract Object[] getConfiguredUserRealms();
    
    /**
     * @return Returns the connectors configured in the pom
     */
    public abstract Object[] getConfiguredConnectors();

    

    public abstract void checkPomConfiguration() throws MojoExecutionException;
    
    
    
    public abstract void configureScanner () throws MojoExecutionException;
    
    
    public abstract void applyJettyXml () throws Exception;
    
    
    /**
     * create a proxy that wraps a particular jetty version Server object
     * @return
     */
    public abstract JettyPluginServer createServer() throws Exception;
    
    
    public abstract void finishConfigurationBeforeStart() throws Exception;
    
    
    public MavenProject getProject()
    {
        return this.project;
    }
    
    public File getTmpDirectory()
    {
        return this.tmpDirectory;
    }

    /**
     * @return Returns the contextPath.
     */
    public String getContextPath()
    {
        return this.contextPath;
    }

    /**
     * @return Returns the scanIntervalSeconds.
     */
    public int getScanIntervalSeconds()
    {
        return this.scanIntervalSeconds;
    }
    

    public SystemProperty[] getSystemProperties()
    {
        return this.systemProperties;
    }

    public String getJettyXmlFileName ()
    {
        return this.jettyConfig;
    }

    public JettyPluginWebApplication getWebApplication()
    {
        return this.webapp;
    }
    
    public void setWebApplication (JettyPluginWebApplication webapp)
    {
        this.webapp = webapp;
    }

    public JettyPluginServer getServer ()
    {
        return this.server;
    }
    
    public void setServer (JettyPluginServer server)
    {
        this.server = server;
    }
    
    
    public void setScanList (ArrayList list)
    {
        this.scanList = new ArrayList(list);
    }
    
    public ArrayList getScanList ()
    {
        return this.scanList;
    }
    
    
    public void setScannerListeners (ArrayList listeners)
    {
        this.scannerListeners = new ArrayList(listeners);
    }
    
    public ArrayList getScannerListeners ()
    {
        return this.scannerListeners;
    }
    
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Configuring Jetty for project: " + getProject().getName());
        PluginLog.setLog(getLog());
        checkPomConfiguration();
        startJetty();
    }
    
    
    public void startJetty () throws MojoExecutionException
    {
        try 
        {
            getLog().debug("Starting Jetty Server ...");
            
            getLog().debug("Resolving runtime dependencies for jdk version: "+System.getProperty("java.version"));
            setupRuntimeClasspath();
            
            configureSystemProperties();
            setServer(createServer());
        
            //apply any config from a jetty.xml file first which is able to 
            //be overwritten by config in the pom.xml
            applyJettyXml ();
            
            // if the user hasn't configured their project's pom to use a
            // different set of connectors,
            // use the default
            Object[] configuredConnectors = getConfiguredConnectors();
            if (configuredConnectors == null|| configuredConnectors.length == 0)
            {
                configuredConnectors = new Object[] { server.createDefaultConnector() };
            }
            
            server.setConnectors(configuredConnectors);           
            setWebApplication(getServer().createWebApplication());  
            configureWebApplication();
            getServer().addWebApplication(getWebApplication());
            
            
            // set up security realms
            Object[] configuredRealms = getConfiguredUserRealms();
            for (int i = 0; (configuredRealms != null) && i < configuredRealms.length; i++)
                getLog().debug(configuredRealms[i].getClass().getName() + ": "+ configuredRealms[i].toString());
            
            server.setUserRealms(configuredRealms);
                        
            //do any other configuration required by the
            //particular Jetty version
            finishConfigurationBeforeStart();   
            
            // start Jetty
            server.start();

            getLog().info("Started Jetty Server");
            
            // start the scanner thread (if necessary) on the main webapp
            configureScanner ();            
            startScanner();
            
            // keep the thread going
            server.join();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Failure", e);
        }
        finally
        {
            getLog().info("Jetty server exiting.");
        }
        
    }
    
    

    /**
     * Subclasses should invoke this to setup basic info
     * on the webapp
     * 
     * @throws MojoExecutionException
     */
    public void configureWebApplication () throws Exception
    {
        if (getWebApplication() == null)
            return;
        
        JettyPluginWebApplication webapp = getWebApplication();
        webapp.setTempDirectory(getTmpDirectory());
        String contextPath = getContextPath();
        webapp.setContextPath((contextPath.startsWith("/") ? contextPath : "/"+ contextPath));
        getLog().info("Context path = " + webapp.getContextPath());
    }
    
    /**
     * Run a scanner thread on the given list of files and directories, calling
     * stop/start on the given list of LifeCycle objects if any of the watched
     * files change.
     * 
     */
    private void startScanner()
    {
        // check if scanning is enabled
        if (getScanIntervalSeconds() <= 0) return;

        Scanner scanner = new Scanner();
        scanner.setScanInterval(getScanIntervalSeconds());
        scanner.setRoots(getScanList());
        scanner.setListeners(getScannerListeners());
        getLog().info("Starting scanner at interval of " + getScanIntervalSeconds()+ " seconds.");
        scanner.start();
    }
    
    private void configureSystemProperties ()
    {
        // get the system properties set up
        for (int i = 0; (getSystemProperties() != null) && i < getSystemProperties().length; i++)
        {
            boolean result = getSystemProperties()[i].setIfNotSetAlready();
            getLog().info("Property " + getSystemProperties()[i].getName() + "="
                    + getSystemProperties()[i].getValue() + " was "
                    + (result ? "set" : "skipped"));
        }
    }
    
    
    /**
     * Insert another classloader into the maven classloader
     * hierarchy with all runtime-resolved jars on it.
     * 
     * @throws Exception
     */
    private void setupRuntimeClasspath () 
    throws Exception
    {
        resolver = new RuntimeDependencyResolver(artifactFactory, artifactResolver, 
                metadataSource, localRepository, remoteRepositories);
        
        
        Set runtimeArtifacts = resolveRuntimeJSP();
        
        
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("jetty.container", Thread.currentThread().getContextClassLoader());
        ClassRealm jspRealm = realm.createChildRealm("jsp");
        
        
        URL[] urls = new URL[runtimeArtifacts.size()];
        Iterator itor = runtimeArtifacts.iterator();
        int i = 0;
        while (itor.hasNext())
        {
            Artifact a = (Artifact)itor.next();
            urls[i] = a.getFile().toURL();
            getLog().debug("Adding to runtime classpath: "+a);
            jspRealm.addConstituent(urls[i]);
            i++; 
        }
        
       Thread.currentThread().setContextClassLoader(jspRealm.getClassLoader());
    }
    
    
    
    /** Sort out which version of JSP libs to use at runtime
     * based on the jvm version in use.
     * @return
     * @throws Exception
     */
    private Set resolveRuntimeJSP () 
    throws Exception
    {
        try
        {
            Version jdkVersion = new Version(System.getProperty("java.version"));
            Version jdk1_5Version = new Version("1.5");
            
            Set dependencies = Collections.EMPTY_SET;
            if (jdkVersion.compare(jdk1_5Version) < 0)
            {
                getLog().info("Using JSP2.0 for non-jdk1.5 runtime");
                
                //get the dependencies
                dependencies = resolver.transitivelyResolvePomDependencies(projectBuilder, "org.mortbay.jetty", "jsp-2.0", POM_VERSION, true);
                
                //check if there is already commons logging on the classpath, and if so, take out the jetty default slf4j commons
                //logging bridge, and the slf4j impl
                try
                {
                    Thread.currentThread().getContextClassLoader().loadClass("org.apache.commons.logging.Log");   
                    //there is already a CommonsLogging jar on the classpath, so we want to remove the default jetty one
                    resolver.removeDependency (dependencies, "org.slf4j", "jcl104-over-slf4j", null, "jar");
                    resolver.removeDependency (dependencies, "org.slf4j", "slf4j-simple", null, "jar");
                }
                catch (ClassNotFoundException e)
                {
                   getLog().debug("Using jetty default jcl104-over-slf4j bridge");
                }
                
                try
                {
                    Thread.currentThread().getContextClassLoader().loadClass("org.slf4j.Logger");
                    //already an slf4j log impl on the classpath, so take out the default jetty one
                    resolver.removeDependency (dependencies, "org.slf4j", "slf4j-simple", null, "jar");
                }
                catch (ClassNotFoundException e)
                {
                    getLog().debug("Using jetty default slf4j-simple log impl");
                }   
            }
            else
            {
                getLog().info("Using JSP2.1 for jdk1.5 runtime");
                dependencies = resolver.transitivelyResolvePomDependencies(projectBuilder, 
                                                                           "org.mortbay.jetty", "jsp-2.1", POM_VERSION, true);
            }
               
            //get rid of any copies of the servlet-api jar that might have been transitively introduced
            //by the jsp project
            resolver.removeDependency(dependencies, "org.mortbay.jetty", "servlet-api-2.5", null, "jar");
            
            return dependencies;
        }
        catch (Exception e)
        {
            getLog().debug(e);
            throw e;
        }

    }
}

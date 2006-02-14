//========================================================================
//$Id: JettyMojo.java,v 1.12 2005/11/25 20:58:59 janb Exp $
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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.NotFoundHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.JettyWebXmlConfiguration;
import org.mortbay.jetty.webapp.TagLibConfiguration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.management.MBeanContainer;

/**
 *  This plugin runs the <a href="http://jetty.mortbay.org/jetty6">Jetty6</a> web container in-situ on a Maven project without 
 *  first requiring that the project is assembled into a war or exploded web application, saving time during the development cycle.
 *  <p>
 *  The plugin forks a parallel lifecycle to ensure that the "compile" phase has been completed before invoking Jetty. This means
 *  that you do not need to explicity execute a "mvn compile" first. It also means that a "mvn clean jetty6:run" will ensure that
 *  a full fresh compile is done before invoking Jetty.
 *  </p>
 *  <p>
 *  Furthermore, once invoked, the plugin can be configured to run continuously, scanning for changes in the project and automatically performing a 
 *  hot redeploy when necessary. This allows the developer to concentrate on coding changes to the project using their IDE of choice and have those changes
 *  immediately and transparently reflected in the running web container, eliminating development time that is wasted on rebuilding, reassembling and redeploying.
 *  
 *  There is a <a href="run-mojo.html">reference guide</a> to the configuration parameters for this plugin, and more detailed information
 *  with examples in the <a href="howto.html">Configuration How-To</a>.
 *  </p>
 *  
 * @goal run
 * @requiresDependencyResolution runtime
 * @execute phase="compile"
 * @description Runs jetty6 directly from a maven project
 *
 */
public class JettyMojo extends AbstractMojo 
{
	
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * The plugin dependencies.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List pluginArtifacts;
    
    /**
     * The location of the web.xml file. If not
     * set then it is assumed it is in ${basedir}/src/main/webapp/WEB-INF
     * 
     * @parameter expression="${maven.war.webxml}"
     */
    private String webXml;
    
    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File classesDirectory;
    
    /**
     * Root directory for all html/jsp etc files
     *
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    private File webAppSourceDirectory;
    
    
    /**
     * List of connectors to use. If none are configured
     * then we use a single SelectChannelConnector at port 8080
     * 
     * @parameter 
     */
    private Connector[] connectors;
    
    
    /**
     * List of other contexts to set up. Optional.
     * @parameter
     */
    private ContextHandler[] contextHandlers;
    
    
    /**
     * List of security realms to set up. Optional.
     * @parameter
     */
    private UserRealm[] userRealms;
    
    
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
     * @parameter expression="${basedir}/target/jetty-tmp"
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
     * System properties to set before execution.Optional.
     * @parameter 
     */
    private SystemProperty[] systemProperties;
    
    

    /**
     * The webapp
     */
    private WebAppContext webAppHandler;
    
    
   
    public static SelectChannelConnector DEFAULT_CONNECTOR = new SelectChannelConnector();
    public static int DEFAULT_PORT = 8080;
    public static long DEFAULT_MAX_IDLE_TIME = 30000L;
    public Configuration[] configurations = {null, new JettyWebXmlConfiguration(), new TagLibConfiguration()};
   

    
    public MavenProject getProject()
    {
        return project;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }
    
    public String getWebXml ()
    {
        return this.webXml;
    }
    public void setWebXml (String webXml)
    {
        this.webXml = webXml;
    }
    
    public File getClassesDirectory ()
    {
        return this.classesDirectory;
    }
    
    public void setClassesDirectory (File classesDirectory)
    {
        this.classesDirectory = classesDirectory;
    }
    
    public File getWebAppSourceDirectory ()
    {
        return this.webAppSourceDirectory;
    }
    public void setWebAppSourceDirectory(File webAppSourceDir)
    {
        this.webAppSourceDirectory = webAppSourceDir;
    }

    public File getTmpDirectory ()
    {
    	return this.tmpDirectory;
    }
    
    public void setTmpDirectory (File tmpDir)
    {
    	this.tmpDirectory = tmpDir;
    }

    /**
	 * @return Returns the connectors.
	 */
	public Connector[] getConnectors()
	{
		return connectors;
	}

	/**
	 * @param connectors The connectors to set.
	 */
	public void setConnectors(Connector[] connectors)
	{
		this.connectors = connectors;
	}
    
    /**
	 * @return Returns the contextPath.
	 */
    public String getContextPath() 
    {
        return contextPath;
    }

    /**
     * @param contextPath The contextPath to set.
     */
    public void setContextPath(String contextPath) 
    {
        this.contextPath = contextPath;
    }

    
    /**
	 * @return Returns the contextHandlers.
	 */
	public ContextHandler[] getContextHandlers()
	{
		return this.contextHandlers;
	}

	/**
	 * @param contextHandlers The contextHandlers to set.
	 */
	public void setContextHandlers(ContextHandler[] contextHandlers)
	{
		this.contextHandlers = contextHandlers;
	}

	/**
	 * @return Returns the scanIntervalSeconds.
	 */
	public int getScanIntervalSeconds()
	{
		return this.scanIntervalSeconds;
	}

	/**
	 * @param scanIntervalSeconds The scanIntervalSeconds to set.
	 */
	public void setScanIntervalSeconds(int scanIntervalSeconds)
	{
		this.scanIntervalSeconds = scanIntervalSeconds;
	}

	/**
	 * @return Returns the realms.
	 */
	public UserRealm[] getUserRealms()
	{
		return this.userRealms;
	}

	/**
	 * @param realms The realms to set.
	 */
	
	public void setUserRealms(UserRealm[] realms)
	{
		this.userRealms = realms;
	}
	
    
	public void setSystemProperties(SystemProperty[] systemProperties)
	{
		this.systemProperties = systemProperties;
	}
    
	
	public SystemProperty[] getSystemProperties ()
	{
		return this.systemProperties;
	}
    

	
	public Handler getWebApplication ()
	{
		if (this.webAppHandler==null)
			this.webAppHandler = new WebAppContext();
		
		return this.webAppHandler;
	}

    /** 
     * Execute the Mojo
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Configuring Jetty for project: " + getProject().getName());
        
        //check the location of the static content/jsps etc
        try
        {
            if ((getWebAppSourceDirectory() == null) || !getWebAppSourceDirectory().exists())
                throw new MojoExecutionException ("Webapp source directory "
                                                 + (getWebAppSourceDirectory()==null?"null":getWebAppSourceDirectory().getCanonicalPath())
                                                 + " does not exist");
            else
                getLog().info("Webapp source directory is: "+getWebAppSourceDirectory().getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Webapp source directory does not exist", e);
        }
        
        File webXmlFile;        
        //get the web.xml file if one has been provided, otherwise assume it is 
        //in the webapp src directory
        if (getWebXml() == null || (getWebXml().trim().equals("")))
            webXmlFile = new File (new File(getWebAppSourceDirectory(), "WEB-INF"), "web.xml");
        else
            webXmlFile = new File(getWebXml());
        
        try
        {
            if (!webXmlFile.exists())
                throw new MojoExecutionException("web.xml does not exist at location "+webXmlFile.getCanonicalPath());
            else
                getLog().info("web.xml file located at: "+webXmlFile.getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException ("web.xml does not exist", e);
        }
        
        //check the classes to form a classpath with
        try
        {
            if (getClassesDirectory() == null)
                throw new MojoExecutionException ("Location of classesDirectory is not set");
            if (!getClassesDirectory().exists())
                getLog().info("Classes directory "+getClassesDirectory().getCanonicalPath()+" does not exist");
            else
            	getLog().info("Classes located at: "+getClassesDirectory().getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Location of classesDirectory does not exist");
        }
        
        //check the tmp directory   
        if (getTmpDirectory() != null) 
        {
            if (!getTmpDirectory().exists())
            {
                if (! getTmpDirectory().mkdirs())
                    throw new MojoExecutionException("Unable to create tmp directory at "+getTmpDirectory());
            }
            getLog().info("tmp dir for webapp will be "+getTmpDirectory().toString());
        }
        else
            getLog().info("tmp dir will be Jetty default");
       
        //get the system properties set up
        for (int i=0; (getSystemProperties() != null) && i < getSystemProperties().length;i++)
        {
        	boolean result = getSystemProperties()[i].setIfNotSetAlready();       	
        	getLog().info("Property "+getSystemProperties()[i].getName()
        			+"="+getSystemProperties()[i].getValue() +" was "+(result?"set":"skipped"));
        }
        
        startJetty (webXmlFile);
    }

    
    
    
    /** Configure and start a Jetty server.
     * 
     * @param webXmlFile location of web.xml file in the Maven project
     * @throws MojoExecutionException
     */
    public void startJetty (final File webXmlFile)
    throws MojoExecutionException
    {
        try
        {
            getLog().info("Starting Jetty Server ...");
 
            Server server = new Server();
            server.setStopAtShutdown(true);
            
            
            //if the user hasn't configured their project's pom to use a different set of connectors,
            //use the default
            if (getConnectors()==null || getConnectors().length==0)
            {                
            	getLog().info("No connectors configured, using defaults: "+DEFAULT_CONNECTOR.getClass().getName()+" listening on "+DEFAULT_PORT+" with maxIdleTime "+DEFAULT_MAX_IDLE_TIME);
                DEFAULT_CONNECTOR.setPort(DEFAULT_PORT);
                DEFAULT_CONNECTOR.setMaxIdleTime(DEFAULT_MAX_IDLE_TIME);
                setConnectors (new Connector[]{DEFAULT_CONNECTOR});
            }          
            server.setConnectors(getConnectors());
            

            List classPathFiles = setUpClassPath();          
            WebAppContext webapp = (WebAppContext)configureWebApplication(webXmlFile, classPathFiles);
            webapp.setServer(server);
            webapp.setTempDirectory(getTmpDirectory());
            
            //include any other ContextHandlers that the user has configured in their project's pom
            //TODO remove this as unnecessary?
            Handler[] handlers = new Handler[(getContextHandlers()!=null?getContextHandlers().length:0)+2];   
            handlers[0] = webapp;
            for (int i=0; (getContextHandlers()!=null && i < getContextHandlers().length); i++)
            	handlers[1+i] = getContextHandlers()[i];

            handlers[handlers.length-1]=new NotFoundHandler();   
            ((NotFoundHandler)handlers[handlers.length-1]).setServer(server);
            server.setHandlers(handlers);
            
            
            //set up security realms
            for (int i=0;(getUserRealms()!=null)&&i<getUserRealms().length;i++)
            getLog().debug(getUserRealms()[i].getClass().getName()+ ": "+getUserRealms()[i].toString());
            
            server.setUserRealms(getUserRealms());
            
            //start Jetty
            server.start();
            
            //start the scanner thread (if necessary) on the main webapp
            final ArrayList scanList = new ArrayList ();
        	scanList.add(webXmlFile);
        	scanList.add(getProject().getFile());
        	scanList.addAll(classPathFiles);
        	ArrayList listeners = new ArrayList();
        	listeners.add(new Scanner.Listener()
        	{
				public void changesDetected(Scanner scanner, List changes)
				{
        			try
        			{
        				getLog().info("Stopping webapp ...");
        				getWebApplication().stop();
        				getLog().info("Reconfiguring webapp ...");
        				      
        	            List classPathFiles = setUpClassPath();
        				configureWebApplication(webXmlFile, classPathFiles);
        				
        				//check if we need to reconfigure the scanner, 
        				//which is if the pom changes
        				if (changes.contains(getProject().getFile().getCanonicalPath()))
        				{
        					getLog().info("Reconfiguring scanner after change to pom.xml ...");
        					scanList.clear();
        					scanList.add(webXmlFile);
        					scanList.add(getProject().getFile());
        					scanList.addAll(classPathFiles);
        					scanner.setRoots(scanList);
        				}
        				
        				getLog().info("Restarting webapp ...");
        				getWebApplication().start();
        				getLog().info("Restart completed.");
        			}
        			catch (Exception e)
        			{
        				getLog().error("Error reconfiguring/restarting webapp after change in watched files", e);
        			}
				}
        	});
            startScanner(getScanIntervalSeconds(), scanList, listeners);
            
            //keep the thread going
            server.getThreadPool().join();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException ("Failure",e);
        }
        finally 
        {
            getLog().info("Jetty server exiting.");
        }
    }


    
    /**
     * Run a scanner thread on the given list of files and directories,
     * calling stop/start on the given list of LifeCycle objects if any
     * of the watched files change.
     * 
     * @param scanList the list of files and directories to watch
     * @param scanListeners list of listeners for the watched files
     */
    private void startScanner (int scanInterval, List scanList, List scanListeners)
    {
    	//check if scanning is enabled
    	if (scanInterval <= 0)
    		return;
    	
    	Scanner scanner = new Scanner ();
    	scanner.setLog(getLog());
    	scanner.setScanInterval(scanInterval);
    	scanner.setRoots(scanList); 	
    	scanner.setListeners(scanListeners); 	
    	getLog().info("Starting scanner at interval of "+scanInterval+" seconds.");
    	scanner.start();
    }
    
    
    
    private Handler configureWebApplication (File webXmlFile, List classPathFiles)
    throws Exception
    { 	  
        //make a webapp handler and set the context
        WebAppContext webapp = (WebAppContext)getWebApplication();
        
        String contextPath = getContextPath();
        if (!contextPath.startsWith("/"))
            contextPath = "/"+contextPath;
        getLog().info("Context path = "+contextPath);
        webapp.setContextPath(contextPath);
        getLog().info("Webapp directory = "+getWebAppSourceDirectory().getCanonicalPath());
        webapp.setWar(getWebAppSourceDirectory().getCanonicalPath());
        
        //do special configuration of classpaths and web.xml etc in Jetty startup
        JettyMavenConfiguration mavenConfig = new JettyMavenConfiguration();
        mavenConfig.setClassPathConfiguration (getWebAppSourceDirectory(), classPathFiles);
        mavenConfig.setWebXml (webXmlFile);
        mavenConfig.setLog (getLog());       
        configurations[0] = mavenConfig;       
        webapp.setConfigurations(configurations);
        return webapp;
    }
    
    
    private List getDependencyFiles()
    {
        List dependencyFiles = new ArrayList();
        for ( Iterator iter = project.getArtifacts().iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            
            // Include runtime and compile time libraries
            if (!Artifact.SCOPE_TEST.equals( artifact.getScope()) )
            {
                dependencyFiles.add(artifact.getFile());
                getLog().debug( "Adding artifact " + artifact.getFile().getName() + " for WEB-INF/lib " );   
            }
        }
        return dependencyFiles;  
    }
    
    
    private List setUpClassPath()
    {
        List classPathFiles = new ArrayList();
        classPathFiles.addAll(getDependencyFiles());
        classPathFiles.add(getClassesDirectory());
        for (int i=0; i< classPathFiles.size(); i++)
        {
            getLog().debug("classpath element: "+((File)classPathFiles.get(i)).getName());
        }
        return classPathFiles;
    }
    
    
}

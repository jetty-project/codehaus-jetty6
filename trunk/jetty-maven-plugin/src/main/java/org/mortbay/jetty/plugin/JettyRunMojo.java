//========================================================================
//$Id$
//Copyright 2000-2009 Mort Bay Consulting Pty. Ltd.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 *  <p>
 *  This goal is used in-situ on a Maven project without first requiring that the project 
 *  is assembled into a war, saving time during the development cycle.
 *  The plugin forks a parallel lifecycle to ensure that the "compile" phase has been completed before invoking Jetty. This means
 *  that you do not need to explicity execute a "mvn compile" first. It also means that a "mvn clean jetty:run" will ensure that
 *  a full fresh compile is done before invoking Jetty.
 *  </p>
 *  <p>
 *  Once invoked, the plugin can be configured to run continuously, scanning for changes in the project and automatically performing a 
 *  hot redeploy when necessary. This allows the developer to concentrate on coding changes to the project using their IDE of choice and have those changes
 *  immediately and transparently reflected in the running web container, eliminating development time that is wasted on rebuilding, reassembling and redeploying.
 *  </p>
 *  <p>
 *  You may also specify the location of a jetty.xml file whose contents will be applied before any plugin configuration.
 *  This can be used, for example, to deploy a static webapp that is not part of your maven build. 
 *  </p>
 *  <p>
 *  There is a <a href="run-mojo.html">reference guide</a> to the configuration parameters for this plugin, and more detailed information
 *  with examples in the <a href="http://docs.codehaus.org/display/JETTY/Maven+Jetty+Plugin">Configuration Guide</a>.
 *  </p>
 * 
 * 
 * @goal run
 * @requiresDependencyResolution runtime
 * @execute phase="test-compile"
 * @description Runs jetty6 directly from a maven project
 */
public class JettyRunMojo extends AbstractJettyMojo
{
    /**
     * If true, the &lt;testOutputDirectory&gt;
     * and the dependencies of &lt;scope&gt;test&lt;scope&gt;
     * will be put first on the runtime classpath.
     * @parameter default-value="false"
     */
    private boolean useTestClasspath;
    
    
    
    /**
     * The default location of the web.xml file. Will be used
     * if <webAppConfig><descriptor> is not set.
     * 
     * @parameter expression="${maven.war.webxml}"
     * @readonly
     */
    private String webXml;
    
    
    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * 
     */
    private File classesDirectory;
    
    
    
    /**
     * The directory containing generated test classes.
     * 
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    private File testClassesDirectory;
    
    /**
     * Root directory for all html/jsp etc files
     *
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    private File webAppSourceDirectory;
    
    /**
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     */
    private List pluginArtifacts;
    
    /**
     * List of files or directories to additionally periodically scan for changes. Optional.
     * @parameter
     */
    private File[] scanTargets;
    
    
    /**
     * List of directories with ant-style &lt;include&gt; and &lt;exclude&gt; patterns
     * for extra targets to periodically scan for changes. Can be used instead of,
     * or in conjunction with &lt;scanTargets&gt;.Optional.
     * @parameter
     */
    private ScanTargetPattern[] scanTargetPatterns;


    /**
     * List of files on the classpath for the webapp
     */
    private List<File> classPathFiles;
    
    
    /**
     * Extra scan targets as a list
     */
    private List<File> extraScanTargets;
    
    /**
     * overlays (resources)
     */
    private List _overlays;
    
   
    
    static boolean isEqual(List list1, List list2)
    {
        if(list2==null || list1.size()!=list2.size())
            return false;
        
        for(int i=0; i<list1.size(); i++)
        {
            if(!list1.get(i).equals(list2.get(i)))
                return false;
        }
        return true;
    }
    

    public String getWebXml()
    {
        return this.webXml;
    }
    
   

    public File getClassesDirectory()
    {
        return this.classesDirectory;
    }

    public File getWebAppSourceDirectory()
    {
        return this.webAppSourceDirectory;
    }

   
    
    public void setClassPathFiles (List<File> list)
    {
        this.classPathFiles = new ArrayList<File>(list);
    }

    public List<File> getClassPathFiles ()
    {
        return this.classPathFiles;
    }


    public List<File> getExtraScanTargets ()
    {
        return this.extraScanTargets;
    }
    
    public void setExtraScanTargets(List<File> list)
    {
        this.extraScanTargets = list;
    }
    
    
    
    /**
     * Verify the configuration given in the pom.
     * 
     * @see org.eclipse.jetty.server.plugin.AbstractJettyMojo#checkPomConfiguration()
     */
    public void checkPomConfiguration () throws MojoExecutionException
    {
        // check the location of the static content/jsps etc
        try
        {
            if ((getWebAppSourceDirectory() == null) || !getWebAppSourceDirectory().exists())
                throw new MojoExecutionException("Webapp source directory "
                        + (getWebAppSourceDirectory() == null ? "null" : getWebAppSourceDirectory().getCanonicalPath())
                        + " does not exist");
            else
                getLog().info( "Webapp source directory = "
                        + getWebAppSourceDirectory().getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Webapp source directory does not exist", e);
        }
        
        // check reload mechanic
        if ( !"automatic".equalsIgnoreCase( reload ) && !"manual".equalsIgnoreCase( reload ) )
        {
            throw new MojoExecutionException( "invalid reload mechanic specified, must be 'automatic' or 'manual'" );
        }
        else
        {
            getLog().info("Reload Mechanic: " + reload );
        }


        // check the classes to form a classpath with
        try
        {
            //allow a webapp with no classes in it (just jsps/html)
            if (getClassesDirectory() != null)
            {
                if (!getClassesDirectory().exists())
                    getLog().info( "Classes directory "+ getClassesDirectory().getCanonicalPath()+ " does not exist");
                else
                    getLog().info("Classes = " + getClassesDirectory().getCanonicalPath());
            }
            else
                getLog().info("Classes directory not set");         
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Location of classesDirectory does not exist");
        }
        
        
        setExtraScanTargets(new ArrayList<File>());
        if (scanTargets != null)
        {            
            for (int i=0; i< scanTargets.length; i++)
            {
                getLog().info("Added extra scan target:"+ scanTargets[i]);
                getExtraScanTargets().add(scanTargets[i]);
            }            
        }
        
        
        if (scanTargetPatterns!=null)
        {
            for (int i=0;i<scanTargetPatterns.length; i++)
            {
                Iterator itor = scanTargetPatterns[i].getIncludes().iterator();
                StringBuffer strbuff = new StringBuffer();
                while (itor.hasNext())
                {
                    strbuff.append((String)itor.next());
                    if (itor.hasNext())
                        strbuff.append(",");
                }
                String includes = strbuff.toString();
                
                itor = scanTargetPatterns[i].getExcludes().iterator();
                strbuff= new StringBuffer();
                while (itor.hasNext())
                {
                    strbuff.append((String)itor.next());
                    if (itor.hasNext())
                        strbuff.append(",");
                }
                String excludes = strbuff.toString();

                try
                {
                    List<File> files = FileUtils.getFiles(scanTargetPatterns[i].getDirectory(), includes, excludes);
                    itor = files.iterator();
                    while (itor.hasNext())
                        getLog().info("Adding extra scan target from pattern: "+itor.next());
                    List<File> currentTargets = getExtraScanTargets();
                    if(currentTargets!=null && !currentTargets.isEmpty())
                        currentTargets.addAll(files);
                    else
                        setExtraScanTargets(files);
                }
                catch (IOException e)
                {
                    throw new MojoExecutionException(e.getMessage());
                }
            }
        }
    }

   



    public void configureWebApplication() throws Exception
    {
       super.configureWebApplication();
        setClassPathFiles(setUpClassPath());
        
        //if we have not already set web.xml location, need to set one up
        if (webAppConfig.getDescriptor() == null)
        {
            //Check to see if there is a web.xml in the configured webAppSourceDirectory/WEB-INF, if so, use it
            File f = new File (new File (getWebAppSourceDirectory(), "WEB-INF"), "web.xml");
            if (!f.exists())
            {
                //Try the default web.xml location
                f = new File (webXml);
                if (!f.exists())
                    throw new MojoExecutionException("No web.xml file found");
            }
            webAppConfig.setDescriptor(f.getPath());
        }
        getLog().info( "web.xml file = "+webAppConfig.getDescriptor());

        if (webAppConfig.getClassPathFiles() == null)
            webAppConfig.setClassPathFiles(getClassPathFiles());
        
        if (webAppConfig.getWar() == null)
            webAppConfig.setWar(getWebAppSourceDirectory().getCanonicalPath());
        getLog().info("Webapp directory = " + getWebAppSourceDirectory().getCanonicalPath());
    }
    
    public void configureScanner ()
    {
        // start the scanner thread (if necessary) on the main webapp
        final ArrayList<File> scanList = new ArrayList<File>();
        scanList.add(new File(webAppConfig.getDescriptor()));
        if (webAppConfig.getJettyEnvXml() != null)
            scanList.add(new File (webAppConfig.getJettyEnvXml()));
        File jettyWebXmlFile = findJettyWebXmlFile(new File(getWebAppSourceDirectory(),"WEB-INF"));
        if (jettyWebXmlFile != null)
            scanList.add(jettyWebXmlFile);
        scanList.addAll(getExtraScanTargets());
        scanList.add(getProject().getFile());
        scanList.addAll(getClassPathFiles());
        setScanList(scanList);
        ArrayList<Scanner.BulkListener> listeners = new ArrayList<Scanner.BulkListener>();
        listeners.add(new Scanner.BulkListener()
        {
            public void filesChanged (List changes)
            {
                try
                {
                    boolean reconfigure = changes.contains(getProject().getFile().getCanonicalPath());
                    restartWebApp(reconfigure);
                }
                catch (Exception e)
                {
                    getLog().error("Error reconfiguring/restarting webapp after change in watched files",e);
                }
            }
        });
        setScannerListeners(listeners);
    }

    public void restartWebApp(boolean reconfigureScanner) throws Exception 
    {
        getLog().info("restarting "+webAppConfig);
        getLog().debug("Stopping webapp ...");
        webAppConfig.stop();
        getLog().debug("Reconfiguring webapp ...");

        checkPomConfiguration();
        configureWebApplication();

        // check if we need to reconfigure the scanner,
        // which is if the pom changes
        if (reconfigureScanner)
        {
            getLog().info("Reconfiguring scanner after change to pom.xml ...");
            scanList.clear();
            scanList.add(new File(webAppConfig.getDescriptor()));
            if (webAppConfig.getJettyEnvXml() != null)
                scanList.add(new File(webAppConfig.getJettyEnvXml()));
            scanList.addAll(getExtraScanTargets());
            scanList.add(getProject().getFile());
            scanList.addAll(getClassPathFiles());
            getScanner().setScanDirs(scanList);
        }

        getLog().debug("Restarting webapp ...");
        webAppConfig.start();
        getLog().info("Restart completed at "+new Date().toString());
    }
    
    private List<File> getDependencyFiles ()
    {
        List<File> dependencyFiles = new ArrayList<File>();
        List<Resource> overlays = new ArrayList<Resource>();
        for ( Iterator<Artifact> iter = getProject().getArtifacts().iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            // Include runtime and compile time libraries, and possibly test libs too
            if(artifact.getType().equals("war"))
            {
                try
                {
                    Resource r = Resource.newResource("jar:" + artifact.getFile().toURL().toString() + "!/");
                    overlays.add(r);
                    getExtraScanTargets().add(artifact.getFile());
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }
                continue;
            }
            if (((!Artifact.SCOPE_PROVIDED.equals(artifact.getScope())) && (!Artifact.SCOPE_TEST.equals( artifact.getScope()))) 
                    ||
                (useTestClasspath && Artifact.SCOPE_TEST.equals( artifact.getScope())))
            {
                dependencyFiles.add(artifact.getFile());
                getLog().debug( "Adding artifact " + artifact.getFile().getName() + " for WEB-INF/lib " );   
            }
        }
        if(!overlays.isEmpty() && !isEqual(overlays, _overlays))
        {
            try
            {
                Resource resource = _overlays==null ? webAppConfig.getBaseResource() : null;
                ResourceCollection rc = new ResourceCollection();
                if(resource==null)
                {
                    // nothing configured, so we automagically enable the overlays                    
                    int size = overlays.size()+1;
                    Resource[] resources = new Resource[size];
                    resources[0] = Resource.newResource(getWebAppSourceDirectory().toURL());
                    for(int i=1; i<size; i++)
                    {
                        resources[i] = overlays.get(i-1);
                        getLog().info("Adding overlay: " + resources[i]);
                    }
                    rc.setResources(resources);
                }                
                else
                {                    
                    if(resource instanceof ResourceCollection)
                    {
                        // there was a preconfigured ResourceCollection ... append the artifact wars
                        Resource[] old = ((ResourceCollection)resource).getResources();
                        int size = old.length + overlays.size();
                        Resource[] resources = new Resource[size];
                        System.arraycopy(old, 0, resources, 0, old.length);
                        for(int i=old.length,j=0; i<size; i++,j++)
                        {
                            resources[i] = overlays.get(j);
                            getLog().info("Adding overlay: " + resources[i]);
                        }
                        rc.setResources(resources);
                    }
                    else
                    {
                        // baseResource was already configured w/c could be src/main/webapp
                        if(!resource.isDirectory() && String.valueOf(resource.getFile()).endsWith(".war"))
                        {
                            // its a war                            
                            resource = Resource.newResource("jar:" + resource.getURL().toString() + "!/");
                        }
                        int size = overlays.size()+1;
                        Resource[] resources = new Resource[size];
                        resources[0] = resource;
                        for(int i=1; i<size; i++)
                        {
                            resources[i] = overlays.get(i-1);
                            getLog().info("Adding overlay: " + resources[i]);
                        }
                        rc.setResources(resources);
                    }
                }
                webAppConfig.setBaseResource(rc);
                _overlays = overlays;
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return dependencyFiles; 
    }
    
    
   

    private List<File> setUpClassPath()
    {
        List<File> classPathFiles = new ArrayList<File>();       
        
        //if using the test classes, make sure they are first
        //on the list
        if (useTestClasspath && (testClassesDirectory != null))
            classPathFiles.add(testClassesDirectory);
        
        if (getClassesDirectory() != null)
            classPathFiles.add(getClassesDirectory());
        
        //now add all of the dependencies
        classPathFiles.addAll(getDependencyFiles());
        
        if (getLog().isDebugEnabled())
        {
            for (int i = 0; i < classPathFiles.size(); i++)
            {
                getLog().debug("classpath element: "+ ((File) classPathFiles.get(i)).getName());
            }
        }
        return classPathFiles;
    }
  

    public void finishConfigurationBeforeStart() throws Exception
    {
        HandlerCollection contexts = (HandlerCollection)server.getChildHandlerByClass(ContextHandlerCollection.class);
        if (contexts==null)
            contexts = (HandlerCollection)server.getChildHandlerByClass(HandlerCollection.class);
        
        for (int i=0; (this.contextHandlers != null) && (i < this.contextHandlers.length); i++)
        {
            contexts.addHandler(this.contextHandlers[i]);
        }
    }

   
 
    
    public void applyJettyXml() throws Exception
    {
        if (getJettyXmlFile() == null)
            return;
        
        getLog().info( "Configuring Jetty from xml configuration file = " + getJettyXmlFile() );        
        XmlConfiguration xmlConfiguration = new XmlConfiguration(getJettyXmlFile().toURL());
        xmlConfiguration.configure(this.server);   
    }


    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        super.execute();
    }
}

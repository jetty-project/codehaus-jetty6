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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.mortbay.jetty.plugin.util.JettyPluginWebApplication;
import org.mortbay.jetty.plugin.util.Scanner;


/**
 * AbstractJettyRunMojo
 * 
 * 
 * Base class for all jetty versions for the "run" mojo.
 * 
 */
public abstract class AbstractJettyRunMojo extends AbstractJettyMojo
{

    
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
     * web.xml as a File
     */
    private File webXmlFile;

    /**
     * List of files on the classpath for the webapp
     */
    private List classPathFiles;

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

    public void setWebXmlFile (File f)
    {
        this.webXmlFile = f;
    }
    
    public File getWebXmlFile ()
    {
        return this.webXmlFile;
    }
    
    public void setClassPathFiles (List list)
    {
        this.classPathFiles = new ArrayList(list);
    }

    public List getClassPathFiles ()
    {
        return this.classPathFiles;
    }



    /**
     * Run the mojo
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
       super.execute();
    }
    
    
    /**
     * Verify the configuration given in the pom.
     * 
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#checkPomConfiguration()
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
                getLog().info( "Webapp source directory is: "
                        + getWebAppSourceDirectory().getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Webapp source directory does not exist", e);
        }
        
       
        // get the web.xml file if one has been provided, otherwise assume it is
        // in the webapp src directory
        if (getWebXml() == null || (getWebXml().trim().equals("")))
            setWebXmlFile(new File(new File(getWebAppSourceDirectory(),"WEB-INF"), "web.xml"));
        else
            setWebXmlFile(new File(getWebXml()));
        
        try
        {
            if (!getWebXmlFile().exists())
                throw new MojoExecutionException( "web.xml does not exist at location "
                        + webXmlFile.getCanonicalPath());
            else
                getLog().info( "web.xml file located at: "
                        + webXmlFile.getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("web.xml does not exist", e);
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
                    getLog().info("Classes located at: " + getClassesDirectory().getCanonicalPath());
            }
            else
                getLog().info("Classes directory not set");         
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Location of classesDirectory does not exist");
        }
        
        // check the tmp directory
        if (getTmpDirectory() != null)
        {
            if (!getTmpDirectory().exists())
            {
                if (!getTmpDirectory().mkdirs())
                    throw new MojoExecutionException("Unable to create tmp directory at " + getTmpDirectory());
            }
            getLog().info("tmp dir for webapp will be "+ getTmpDirectory().toString());
        }
        else
            getLog().info("tmp dir will be Jetty default");
        
      
    }

   



    public void configureWebApplication() throws Exception
    {
       super.configureWebApplication();
        
        JettyPluginWebApplication webapp = getWebApplication();
        setClassPathFiles(setUpClassPath());
        webapp.setWebXmlFile(getWebXmlFile());
        webapp.setClassPathFiles(getClassPathFiles());
        webapp.setWebAppSrcDir(getWebAppSourceDirectory());
        getLog().info("Webapp directory = " + getWebAppSourceDirectory().getCanonicalPath());

        webapp.configure();
    }
    
    public void configureScanner ()
    {
        // start the scanner thread (if necessary) on the main webapp
        final ArrayList scanList = new ArrayList();
        scanList.add(getWebXmlFile());
        scanList.add(getProject().getFile());
        scanList.addAll(getClassPathFiles());
        setScanList(scanList);
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

                    checkPomConfiguration();
                    configureWebApplication();

                    // check if we need to reconfigure the scanner,
                    // which is if the pom changes
                    if (changes.contains(getProject().getFile().getCanonicalPath()))
                    {
                        getLog().info("Reconfiguring scanner after change to pom.xml ...");
                        scanList.clear();
                        scanList.add(getWebXmlFile());
                        scanList.add(getProject().getFile());
                        scanList.addAll(getClassPathFiles());
                        scanner.setRoots(scanList);
                    }

                    getLog().info("Restarting webapp ...");
                    getWebApplication().start();
                    getLog().info("Restart completed at "+new Date().toString());
                }
                catch (Exception e)
                {
                    getLog().error("Error reconfiguring/restarting webapp after change in watched files",e);
                }
            }
        });
        setScannerListeners(listeners);
    }

    
    private List getDependencyFiles ()
    {
        List dependencyFiles = new ArrayList();
        for ( Iterator iter = getProject().getArtifacts().iterator(); iter.hasNext(); )
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
        if (getClassesDirectory() != null)
            classPathFiles.add(getClassesDirectory());
        for (int i = 0; i < classPathFiles.size(); i++)
        {
            getLog().debug("classpath element: "+ ((File) classPathFiles.get(i)).getName());
        }
        return classPathFiles;
    }

}

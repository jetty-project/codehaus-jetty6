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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.annotations.Configuration;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppClassLoader;

public class Jetty6MavenConfiguration extends Configuration 
{
    private List classPathFiles;
    private File webXmlFile;
   
    public Jetty6MavenConfiguration() throws ClassNotFoundException
    {
        super();
    }

    public void setClassPathConfiguration(List classPathFiles)
    {
        this.classPathFiles = classPathFiles;
    }
    
    public void setWebXml (File webXmlFile)
    {
        this.webXmlFile = webXmlFile;
    }
    
    
    /** Set up the classloader for the webapp, using the various parts of the Maven project
     * @see org.eclipse.jetty.webapp.Configuration#configureClassLoader()
     */
    public void configureClassLoader() throws Exception 
    {
        if (classPathFiles != null)
        {
            Log.debug("Setting up classpath ...");

            //put the classes dir and all dependencies into the classpath
            Iterator itor = classPathFiles.iterator();
            while (itor.hasNext())
                ((WebAppClassLoader)getWebAppContext().getClassLoader()).addClassPath(((File)itor.next()).getCanonicalPath());

            if (Log.isDebugEnabled())
                Log.debug("Classpath = "+LazyList.array2List(((URLClassLoader)getWebAppContext().getClassLoader()).getURLs()));
        }
        else
        {
            super.configureClassLoader();
        }

        // knock out environmental maven and plexus classes from webAppContext
        String[] existingServerClasses = getWebAppContext().getServerClasses();
        String[] newServerClasses = new String[2+(existingServerClasses==null?0:existingServerClasses.length)];
        newServerClasses[0] = "-org.apache.maven.";
        newServerClasses[1] = "-org.codehaus.plexus.";
        System.arraycopy( existingServerClasses, 0, newServerClasses, 2, existingServerClasses.length );
        
        getWebAppContext().setServerClasses( newServerClasses );
    }

    

    
    protected URL findWebXml () throws IOException, MalformedURLException
    {
        //if an explicit web.xml file has been set (eg for jetty:run) then use it
        if (webXmlFile != null && webXmlFile.exists())
            return webXmlFile.toURL();
        
        //if we haven't overridden location of web.xml file, use the
        //standard way of finding it
        Log.debug("Looking for web.xml file in WEB-INF");
        return super.findWebXml();
    }
    
    
    
    public void parseAnnotations()
    throws Exception
    {
        String v = System.getProperty("java.version");
        String[] version = v.split("\\.");
        if (version==null)
        {
            Log.info("Unable to determine jvm version, annotations will not be supported");
            return;
        }
        int  major = Integer.parseInt(version[0]);
        int minor = Integer.parseInt(version[1]);
        if ((major >= 1) && (minor >= 5))
        {     
            super.parseAnnotations();
        }
        else
            Log.info("Annotations are not supported on jvms prior to jdk1.5");
    }
}

//========================================================================
//$Id$
//Copyright 2009 Mort Bay Consulting Pty. Ltd.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

public class MavenWebInfConfiguration extends WebInfConfiguration
{
    public void configure(WebAppContext context) throws Exception
    {
        JettyWebAppContext jwac = (JettyWebAppContext)context;
        if (jwac.getClassPathFiles() != null)
        {
            Log.debug("Setting up classpath ...");

            //put the classes dir and all dependencies into the classpath
            Iterator itor = jwac.getClassPathFiles().iterator();
            while (itor.hasNext())
                ((WebAppClassLoader)context.getClassLoader()).addClassPath(((File)itor.next()).getCanonicalPath());

            if (Log.isDebugEnabled())
                Log.debug("Classpath = "+LazyList.array2List(((URLClassLoader)context.getClassLoader()).getURLs()));
        }
        super.configure(context);

        // knock out environmental maven and plexus classes from webAppContext
        String[] existingServerClasses = context.getServerClasses();
        String[] newServerClasses = new String[2+(existingServerClasses==null?0:existingServerClasses.length)];
        newServerClasses[0] = "-org.apache.maven.";
        newServerClasses[1] = "-org.codehaus.plexus.";
        System.arraycopy( existingServerClasses, 0, newServerClasses, 2, existingServerClasses.length );

        context.setServerClasses( newServerClasses );
    }



    public void deconfigure(WebAppContext context) throws Exception
    {
        super.deconfigure(context);
    }

    public void postConfigure(WebAppContext context) throws Exception
    {
        super.postConfigure(context);
    }

    /**
     * Get the jars to examine from the files from which we have
     * synthesized the classpath. Note that the classpath is not
     * set at this point, so we cannot get them from the classpath.
     * @param context
     * @return
     */
    protected List<Resource> findJars (WebAppContext context)
    throws Exception
    {
        List<Resource> list = new ArrayList<Resource>();
        JettyWebAppContext jwac = (JettyWebAppContext)context;
        if (jwac.getClassPathFiles() != null)
        {
            for (File f: jwac.getClassPathFiles())
            {
                if (f.getName().toLowerCase().endsWith(".jar"))
                {
                    try
                    {
                        list.add(Resource.newResource(f.toURI()));
                    }
                    catch (Exception e)
                    {
                        Log.warn("Bad url ", e);
                    }
                }
            }
        }
        
        List<Resource> superList = super.findJars(context);
          
        list.addAll(superList);
     
        return list;
    }

    /* ------------------------------------------------------------ */
    @Override
    public void unpack(WebAppContext context) throws IOException
    {
        // TODO remove this override after 7.0.0.RC2
        super.unpack(context);

        Resource web_app = context.getBaseResource();
               
        // Do we need to extract WEB-INF/lib?
        Resource web_inf= web_app.addPath("WEB-INF/");
        if (web_inf.exists() && web_inf.isDirectory() && (web_inf.getFile()==null || !web_app.getFile().isDirectory()))
        {
            File extractedWebInfDir= new File(context.getTempDirectory(), "webinf");
            if (extractedWebInfDir.exists())
                extractedWebInfDir.delete();
            extractedWebInfDir.mkdir();
            File webInfDir=new File(extractedWebInfDir,"WEB-INF");
            webInfDir.mkdir();
            Log.info("Extract " + web_inf + " to " + webInfDir);
            web_inf.copyTo(webInfDir);
            web_inf=Resource.newResource(extractedWebInfDir.toURL());
            
            ResourceCollection rc = new ResourceCollection(new Resource[]{web_inf,web_app});
            context.setBaseResource(rc);
        }    
    }
    
}

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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

public class MavenWebInfConfiguration extends WebInfConfiguration
{
    protected Resource _originalResourceBase;
    
    public void configure(WebAppContext context) throws Exception
    {
        JettyWebAppContext jwac = (JettyWebAppContext)context;
        if (jwac.getClassPathFiles() != null)
        {
            if (Log.isDebugEnabled()) Log.debug("Setting up classpath ...");

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


    public void preConfigure(WebAppContext context) throws Exception
    {
        _originalResourceBase = context.getBaseResource();
        JettyWebAppContext jwac = (JettyWebAppContext)context;

        //Add in any overlaid wars as base resources
        if (jwac.getOverlays() != null && !jwac.getOverlays().isEmpty())
        {
            ResourceCollection rc = new ResourceCollection();

            if(jwac.getBaseResource()==null)
            {
                // nothing configured, so we automagically enable the overlays                    
                int size = jwac.getOverlays().size()+1;
                Resource[] resources = new Resource[size];
                for(int i=0; i<size; i++)
                {
                    resources[i] = jwac.getOverlays().get(i);
                    Log.info("Adding overlay: " + resources[i]);
                }
                rc.setResources(resources);
            }                
            else
            {                    
                if(jwac.getBaseResource() instanceof ResourceCollection)
                {
                    // there was a preconfigured ResourceCollection ... append the artifact wars
                    Resource[] old = ((ResourceCollection)jwac.getBaseResource()).getResources();
                    int size = old.length + jwac.getOverlays().size();
                    Resource[] resources = new Resource[size];
                    System.arraycopy(old, 0, resources, 0, old.length);
                    for(int i=old.length,j=0; i<size; i++,j++)
                    {
                        resources[i] = jwac.getOverlays().get(j);
                        Log.info("Adding overlay: " + resources[i]);
                    }
                    rc.setResources(resources);
                }
                else
                {
                    int size = jwac.getOverlays().size()+1;
                    Resource[] resources = new Resource[size];
                    resources[0] = jwac.getBaseResource();
                    for(int i=1; i<size; i++)
                    {
                        resources[i] = jwac.getOverlays().get(i-1);
                        Log.info("Adding overlay: " + resources[i]);
                    }
                    rc.setResources(resources);
                }
            }

            jwac.setBaseResource(rc);
        }
        super.preConfigure(context);
    }
    
    public void postConfigure(WebAppContext context) throws Exception
    {
        super.postConfigure(context);
    }


    public void deconfigure(WebAppContext context) throws Exception
    {
        super.deconfigure(context);
        //restore whatever the base resource was before we might have included overlaid wars
        context.setBaseResource(_originalResourceBase);
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



}

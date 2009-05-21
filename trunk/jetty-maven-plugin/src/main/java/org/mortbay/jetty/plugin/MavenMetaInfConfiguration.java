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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

public class MavenMetaInfConfiguration extends MetaInfConfiguration
{
    /**
     * Get the jars to examine from the files from which we have
     * synthesized the classpath. Note that the classpath is not
     * set at this point, so we cannot get them from the classpath.
     * @param context
     * @return
     */
    protected List<URL> findJars (WebAppContext context)
    {
        JettyWebAppContext jwac = (JettyWebAppContext)context;
        List<URL> list = new ArrayList<URL>();
        
        for (File f: jwac.getClassPathFiles())
        {
            if (f.getName().toLowerCase().endsWith(".jar"))
            {
                try
                {
                    list.add(f.toURL());
                }
                catch (Exception e)
                {
                    Log.warn("Bad url ", e);
                }
            }
        }
     
        return list;
    }
}

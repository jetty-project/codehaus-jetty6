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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mortbay.jetty.webapp.TagLibConfiguration;
import org.mortbay.resource.Resource;
import org.mortbay.util.TypeUtil;

/**
 * @author janb
 *
 */
public class Jetty6MavenTagLibConfiguration extends TagLibConfiguration 
{
       
    private List  classPathFiles;
    
    public void setClassPathFiles (List classPathFiles)
    {
        this.classPathFiles = classPathFiles;
    }
    
    public List getClassPathFiles()
    {
        return this.classPathFiles;
    }
    
    protected List getJarResourceList() throws MalformedURLException, IOException
    {
        List list = new ArrayList();

        Iterator itor = (getClassPathFiles() == null?null:getClassPathFiles().iterator());
        while (itor.hasNext())
        {
            File f = (File)itor.next();
            if (f.getName().toLowerCase().endsWith(".jar"))
                list.add(Resource.newResource(f.toURL()));
        }
        list.addAll(getServerJarResourceList());

        return list;
    }
    

}

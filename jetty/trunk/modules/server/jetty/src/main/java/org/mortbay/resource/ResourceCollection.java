//========================================================================
//Copyright 2007 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * A collection of resources (dirs).
 * Allows webapps to have multiple (static) sources.
 * The first resource in the collection is the main resource.
 * If a resource is not found in the main resource, it looks it up in 
 * the order the resources were constructed.
 * 
 * @author dyu
 *
 */
public class ResourceCollection extends Resource
{
    
    private Resource[] _resources;
    
    public ResourceCollection(Resource[] resources)
    {
        if(resources.length==0)
            throw new IllegalArgumentException("arg *resources* must not be one or more resources.");
        _resources = resources;
    }
    
    public ResourceCollection(String[] resources)
    {
        if(resources.length==0)
            throw new IllegalArgumentException("arg *resources* must not be one or more resources.");
        _resources = new Resource[resources.length];
        try
        {
            for(int i=0; i<resources.length; i++)
                _resources[i] = Resource.newResource(resources[i]);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public ResourceCollection(String csvResources)
    {
        StringTokenizer tokenizer = new StringTokenizer(csvResources, ",;");
        int len = tokenizer.countTokens();
        if(len==0)
            throw new IllegalArgumentException("arg *resources* must not be one or more resources.");
        _resources = new Resource[len];
        try
        {            
            for(int i=0; tokenizer.hasMoreTokens(); i++)
                _resources[i] = Resource.newResource(tokenizer.nextToken().trim());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }        
    }
    
    public Resource[] getResources()
    {
        return _resources;
    }
    
    /**
     * @param path The path segment to add
     * @return The contained resource (found first) in the collection of resources
     */
    public Resource addPath(String path) throws IOException, MalformedURLException
    {        
        Resource mainLookup = _resources[0].addPath(path);
        if(mainLookup==null || !mainLookup.exists())
        {
            for(int i=1; i<_resources.length; i++)
            {
                Resource r = _resources[i].addPath(path);
                if(r!=null && r.exists())
                    return r;
            }
        }
        return mainLookup;
    }
    
    public boolean delete() throws SecurityException
    {        
        return _resources[0].delete();
    }
    
    public boolean exists()
    {        
        return _resources[0].exists();
    }
    
    public File getFile() throws IOException
    {        
        return  _resources[0].getFile();
    }
    
    public InputStream getInputStream() throws IOException
    {        
        return _resources[0].getInputStream();
    }
    
    public String getName()
    {        
        return _resources[0].getName();
    }
    
    public OutputStream getOutputStream() throws IOException, SecurityException
    {        
        return _resources[0].getOutputStream();
    }
    
    public URL getURL()
    {        
        return _resources[0].getURL();
    }
    
    public boolean isDirectory()
    {
        return _resources[0].isDirectory();
    }
    
    public long lastModified()
    {        
        return _resources[0].lastModified();
    }
    
    public long length()
    {        
        return _resources[0].length();
    }    
    
    /**
     * @return The list of resource names(merged) contained in the collection of resources.
     */    
    public String[] list()
    {
        HashSet<String> set = new HashSet<String>();
        for(Resource r : _resources)
        {
            for(String s : r.list())
                set.add(s);
        }
        return set.toArray(new String[set.size()]);
    }
    
    public void release()
    {        
        _resources[0].release();
    }
    
    public boolean renameTo(Resource dest) throws SecurityException
    {        
        return _resources[0].renameTo(dest);
    }

}

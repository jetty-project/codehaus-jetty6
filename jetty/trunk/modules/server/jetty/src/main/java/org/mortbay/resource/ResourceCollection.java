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
import java.util.ArrayList;
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
    
    public ResourceCollection()
    {
        
    }
    
    public ResourceCollection(Resource[] resources)
    {
        setResources(resources);
    }
    
    public ResourceCollection(String[] resources)
    {
        setResources(resources);
    }
    
    public ResourceCollection(String csvResources)
    {
        setResources(csvResources);
    }
    
    /**
     * 
     * @param resources Resource array
     */
    public void setResources(Resource[] resources)
    {
        if(_resources!=null)
            throw new IllegalStateException("*resources* already set.");
        
        if(resources==null)
            throw new IllegalArgumentException("*resources* must not be null.");
        
        if(resources.length==0)
            throw new IllegalArgumentException("arg *resources* must be one or more resources.");
        
        _resources = resources;
        for(Resource r : _resources)
        {
            if(!r.isDirectory())
                throw new IllegalArgumentException(r + " is not a directory");
        }
    }
    
    /**
     * 
     * @param resources String array
     */
    public void setResources(String[] resources)
    {
        if(_resources!=null)
            throw new IllegalStateException("*resources* already set.");
        
        if(resources==null)
            throw new IllegalArgumentException("*resources* must not be null.");
        
        if(resources.length==0)
            throw new IllegalArgumentException("arg *resources* must be one or more resources.");
        
        _resources = new Resource[resources.length];
        try
        {
            for(int i=0; i<resources.length; i++)
            {
                _resources[i] = Resource.newResource(resources[i]);
                if(!_resources[i].isDirectory())
                    throw new IllegalArgumentException(_resources[i] + " is not a directory");
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @param csvResources Comma separated values
     */
    public void setResources(String csvResources)
    {
        if(_resources!=null)
            throw new IllegalStateException("*resources* already set.");
        
        if(csvResources==null)
            throw new IllegalArgumentException("*csvResources* must not be null.");
        
        StringTokenizer tokenizer = new StringTokenizer(csvResources, ",;");
        int len = tokenizer.countTokens();
        if(len==0)
            throw new IllegalArgumentException("arg *resources* must be one or more resources.");
        
        _resources = new Resource[len];
        try
        {            
            for(int i=0; tokenizer.hasMoreTokens(); i++)
            {
                _resources[i] = Resource.newResource(tokenizer.nextToken().trim());
                if(!_resources[i].isDirectory())
                    throw new IllegalArgumentException(_resources[i] + " is not a directory");
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @return the resource array
     */
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
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        Object resource = findResource(path);        
        
        if(resource==null)
            return null;
        
        if(resource instanceof Resource)
            return (Resource)resource;
        
        ArrayList<Resource> resources = (ArrayList<Resource>)resource;
        return new ResourceCollection(resources.toArray(new Resource[resources.size()]));
    }
    
    /**
     * 
     * @param path
     * @return the resource(file) if found, returns a list of resource dirs if its a dir, else null.
     * @throws IOException
     * @throws MalformedURLException
     */
    protected Object findResource(String path) throws IOException, MalformedURLException
    {        
        ArrayList<Resource> resources = null;
        Resource mainLookup = _resources[0].addPath(path);
        if(mainLookup!=null && mainLookup.exists())
        {
            if(!mainLookup.isDirectory())
                return mainLookup;
            
            resources = new ArrayList<Resource>();
            resources.add(mainLookup);
        }        
         
        for(int i=1; i<_resources.length; i++)
        {
            Resource r = _resources[i].addPath(path);            
            if(r!=null && r.exists())
            {
                if(!r.isDirectory())
                    return r;
                
                if(resources==null)
                    resources = new ArrayList<Resource>();
                
                resources.add(r);           
            }            
        }
        return resources!=null ? resources : mainLookup;
    }
    
    public boolean delete() throws SecurityException
    {
        throw new UnsupportedOperationException();
    }
    
    public boolean exists()
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return _resources[0].exists();
    }
    
    public File getFile() throws IOException
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return  _resources[0].getFile();
    }
    
    public InputStream getInputStream() throws IOException
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return _resources[0].getInputStream();
    }
    
    public String getName()
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return _resources[0].getName();
    }
    
    public OutputStream getOutputStream() throws IOException, SecurityException
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return _resources[0].getOutputStream();
    }
    
    public URL getURL()
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return _resources[0].getURL();
    }
    
    public boolean isDirectory()
    {
        return true;
    }
    
    public long lastModified()
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return _resources[0].lastModified();
    }
    
    public long length()
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        return _resources[0].length();
    }    
    
    /**
     * @return The list of resource names(merged) contained in the collection of resources.
     */    
    public String[] list()
    {
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
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
        if(_resources==null)
            throw new IllegalStateException("*resources* not set.");
        
        for(Resource r : _resources)
            r.release();
    }
    
    public boolean renameTo(Resource dest) throws SecurityException
    {
        throw new UnsupportedOperationException();
    }

}

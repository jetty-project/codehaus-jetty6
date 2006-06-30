// ========================================================================
// Copyright 1999-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.StringTokenizer;

import org.mortbay.io.IO;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.util.LazyList;


/* ------------------------------------------------------------ */
/** ClassLoader for HttpContext.
 * Specializes ClassLoader to be knowledgeable about Web Context.
 *
 * This loader delegates to a loader created by a call to {@link #newInstance(URL[], ClassLoader)}
 * and potentially to the classloader returned by {@link java.lang.ClassLoader#getParent()} of that
 * loader.
 * 
 * Java2 compliant loading, where the parent loader
 * always has priority, can be selected with the 
 * {@link org.mortbay.jetty.webapp.WebAppContext#setParentLoaderPriority(boolean)} method.
 *
 * @author Greg Wilkins (gregw)
 */
public class WebAppClassLoader extends ClassLoader
{
    private WebAppContext _context;
    private ClassLoader _loader;
    private ClassLoader _parent;
    private String _urlClassPath;
    private URL[] _urls=new URL[0];
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     */
    public WebAppClassLoader(ClassLoader parent, WebAppContext context)
    {
        _parent=parent;
        _context=context;
        if (parent==null)
            throw new IllegalArgumentException("no parent classloader!");
    }

    /* ------------------------------------------------------------ */
    /** Create new instance of a classloader.
     * This method is called by {@link #loadClass(String, boolean)} and 
     * {@link #getResource(String)} the first time they are called in order to create
     * the classloader that this loader delegaes to.   This class should be specialized if
     * specialized class loading is required.  Note that the loader returned by
     * {@link java.lang.ClassLoader#getParent()} may also be directly accessed when loading 
     * classes.
     * @param urls Array of URLs to load from
     * @param parent The parent classloader
     * @return A new classloader.
     */
    ClassLoader newInstance(URL[] urls, ClassLoader parent)
    {
        return new URLClassLoader(urls,parent);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param classPath Comma or semicolon separated path of filenames or URLs
     * pointing to directories or jar files. Directories should end
     * with '/'.
     */
    public void addClassPath(String classPath)
    	throws IOException
    {
        if (_loader!=null)
            throw new IllegalStateException();
        
        if (classPath == null)
            return;
            
        StringTokenizer tokenizer= new StringTokenizer(classPath, ",;");
        while (tokenizer.hasMoreTokens())
        {
            Resource resource= Resource.newResource(tokenizer.nextToken());
            if (Log.isDebugEnabled())
                Log.debug("Path resource=" + resource);

            // Resolve file path if possible
            File file= resource.getFile();
            if (file != null)
            {
                URL url= resource.getURL();
                addURL(url);
                _urlClassPath= (_urlClassPath == null) ? url.toString() : (_urlClassPath + "," + url.toString());
            }
            else
            {
                // Add resource or expand jar/
                if (!resource.isDirectory() && file == null)
                {
                    InputStream in= resource.getInputStream();
                    File tmp_dir=_context.getTempDirectory();
                    if (tmp_dir==null)
                    {
                        tmp_dir = File.createTempFile("jetty.cl.lib",null);
                        tmp_dir.mkdir();
                        tmp_dir.deleteOnExit();
                    }
                    File lib= new File(tmp_dir, "lib");
                    if (!lib.exists())
                    {
                        lib.mkdir();
                        lib.deleteOnExit();
                    }
                    File jar= File.createTempFile("Jetty-", ".jar", lib);
                    
                    jar.deleteOnExit();
                    if (Log.isDebugEnabled())
                        Log.debug("Extract " + resource + " to " + jar);
                    FileOutputStream out = null;
                    try
                    {
                        out= new FileOutputStream(jar);
                        IO.copy(in, out);
                    }
                    finally
                    {
                        IO.close(out);
                    }
                    
                    URL url= jar.toURL();
                    addURL(url);
                    _urlClassPath=
                        (_urlClassPath == null) ? url.toString() : (_urlClassPath + "," + url.toString());
                }
                else
                {
                    URL url= resource.getURL();
                    addURL(url);
                    _urlClassPath=
                        (_urlClassPath == null) ? url.toString() : (_urlClassPath + "," + url.toString());
                }
            }
        }
    }

    private void addURL(URL url)
        throws IOException
    {
        _urls = (URL[])LazyList.addToArray(_urls, url, URL.class);
    }
    
    
    /* ------------------------------------------------------------ */
    /** Add elements to the class path for the context from the jar and zip files found
     *  in the specified resource.
     * @param lib the resource that contains the jar and/or zip files.
     * @param append true if the classpath entries are to be appended to any
     * existing classpath, or false if they replace the existing classpath.
     * @see #setClassPath(String)
     */
    public void addJars(Resource lib)
    {
        if (lib.exists() && lib.isDirectory())
        {
            String[] files=lib.list();
            for (int f=0;files!=null && f<files.length;f++)
            {
                try {
                    Resource fn=lib.addPath(files[f]);
                    String fnlc=fn.getName().toLowerCase();
                    if (fnlc.endsWith(".jar") || fnlc.endsWith(".zip"))
                    {
                        addClassPath(fn.toString());
                    }
                }
                catch (Exception ex)
                {
                    Log.warn(Log.EXCEPTION,ex);
                }
            }
        }
    }
    
    
    /* ------------------------------------------------------------ */
    public void destroy()
    {
        this._parent=null;
        this._urlClassPath=null;
    }
    

    /* ------------------------------------------------------------ */
    public PermissionCollection getPermissions(CodeSource cs)
    {
        // TODO check CodeSource
        PermissionCollection permissions=_context.getPermissions();
        return permissions;
    }

    /* ------------------------------------------------------------ */
    public synchronized URL getResource(String name)
    {
        if (_loader==null)
        {
            _loader=newInstance(_urls,_parent);
            _parent=_loader.getParent();
        }
        
        URL url= null;
        boolean tried_parent= false;
        if (_context.isParentLoaderPriority() || isSystemPath(name))
        {
            tried_parent= true;
            
            if (_parent!=null)
                url= _parent.getResource(name);
        }

        if (url == null)
        {
            url= _loader.getResource(name);

            if (url == null && name.startsWith("/"))
            {
                if (Log.isDebugEnabled())
                    Log.debug("HACK leading / off " + name);
                url= this.findResource(name.substring(1));
            }
        }

        if (url == null && !tried_parent)
        {
            if (_parent!=null)
                url= _parent.getResource(name);
        }

        if (url != null)
            if (Log.isDebugEnabled())
                Log.debug("getResource("+name+")=" + url);

        return url;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the urlClassPath.
     */
    public String getUrlClassPath()
    {
        return _urlClassPath;
    }

    /* ------------------------------------------------------------ */
    public boolean isServerPath(String name)
    {
        name=name.replace('/','.');
        while(name.startsWith("."))
            name=name.substring(1);

        String[] server_classes = _context.getServerClasses();
        if (server_classes!=null)
        {
            for (int i=0;i<server_classes.length;i++)
            {
                boolean result=true;
                String c=server_classes[i];
                if (c.startsWith("-"))
                {
                    c=c.substring(1); // TODO cache
                    result=false;
                }
                
                if (c.endsWith("."))
                {
                    if (name.startsWith(c))
                        return result;
                }
                else if (name.equals(c))
                    return result;
            }
        }
        return false;
    }

    /* ------------------------------------------------------------ */
    public boolean isSystemPath(String name)
    {
        name=name.replace('/','.');
        while(name.startsWith("."))
            name=name.substring(1);
        String[] system_classes = _context.getSystemClasses();
        if (system_classes!=null)
        {
            for (int i=0;i<system_classes.length;i++)
            {
                boolean result=true;
                String c=system_classes[i];
                
                if (c.startsWith("-"))
                {
                    c=c.substring(1); // TODO cache
                    result=false;
                }
                
                if (c.endsWith("."))
                {
                    if (name.startsWith(c))
                        return result;
                }
                else if (name.equals(c))
                    return result;
            }
        }
        
        return false;
        
    }

    /* ------------------------------------------------------------ */
    public synchronized Class loadClass(String name) throws ClassNotFoundException
    {
        return loadClass(name, false);
    }

    /* ------------------------------------------------------------ */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        if (_loader==null)
        {
            _loader=newInstance(_urls,_parent);
            _parent=_loader.getParent();
        }
        
        Class c= findLoadedClass(name);
        ClassNotFoundException ex= null;
        boolean tried_parent= false;
        
        if (c == null && _parent!=null && (_context.isParentLoaderPriority() || isSystemPath(name)) )
        {
            tried_parent= true;
            try
            {
                c= _parent.loadClass(name);
                if (Log.isDebugEnabled())
                    Log.debug("loaded " + c);
            }
            catch (ClassNotFoundException e)
            {
                ex= e;
            }
        }

        if (c == null)
        {
            try
            {
                c= _loader.loadClass(name);
            }
            catch (ClassNotFoundException e)
            {
                ex= e;
            }
        }

        if (c == null && _parent!=null && !tried_parent && !isServerPath(name) )
            c= _parent.loadClass(name);

        if (c == null)
            throw ex;

        if (resolve)
            resolveClass(c);

        if (Log.isDebugEnabled())
            Log.debug("loaded " + c+ " from "+c.getClassLoader());
        
        return c;
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        if (Log.isDebugEnabled())
            return "ContextLoader@" + hashCode() + "(" + _urlClassPath + ") / " + _parent;
        return "ContextLoader@" + hashCode();
    }
    
}

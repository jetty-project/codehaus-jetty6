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
import java.util.Enumeration;
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
public class DelegatingWebAppClassLoader extends WebAppClassLoader
{
    private URL[] _urls=new URL[0];
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     */
    public DelegatingWebAppClassLoader(ClassLoader parent, WebAppContext context)
    {
        super(parent,context);
        _loader=null; // wait until first use
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
    ClassLoader newDelegateInstance(URL[] urls, ClassLoader parent)
    {
        return new URLClassLoader(urls,parent);
    }
    
    /* ------------------------------------------------------------ */
    public void addURL(URL url)
    {
        _urls = (URL[])LazyList.addToArray(_urls, url, URL.class);
    }
    

    /* ------------------------------------------------------------ */
    public URL[] getURLs()
    {
        return _urls;
    }
    
    
    /* ------------------------------------------------------------ */
    public void destroy()
    {
        _urls=null;
        super.destroy();
    }

    /* ------------------------------------------------------------ */
    public synchronized URL getResource(String name)
    {
        if (_loader==null)
        {
            _loader=newDelegateInstance(_urls,_parent);
            _parent=_loader.getParent();
        }
        
        return super.getResource(name);
    }
     
    /* ------------------------------------------------------------ */
    public synchronized Class loadClass(String name) throws ClassNotFoundException
    {
        if (_loader==null)
        {
            _loader=newDelegateInstance(_urls,_parent);
            _parent=_loader.getParent();
        }
        return super.loadClass(name,false);
    }

    /* ------------------------------------------------------------ */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        if (_loader==null)
        {
            _loader=newDelegateInstance(_urls,_parent);
            _parent=_loader.getParent();
        }
        
        return super.loadClass(name,resolve);
    }
    
}

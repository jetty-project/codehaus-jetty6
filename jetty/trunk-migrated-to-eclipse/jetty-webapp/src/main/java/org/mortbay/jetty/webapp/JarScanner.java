//========================================================================
//$Id$
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
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


package org.mortbay.jetty.webapp;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.resource.Resource;

/**
 * JarScannerConfiguration
 *
 * Abstract base class for configurations that want to scan jars in
 * WEB-INF/lib and the classloader hierarchy.
 * 
 * Jar name matching based on regexp patterns is provided.
 * 
 * Subclasses should implement the processEntry(URL jarUrl, JarEntry entry)
 * method to handle entries in jar files whose names match the supplied 
 * pattern.
 */
public abstract class JarScanner
{

    public abstract void processEntry (URL jarUrl, JarEntry entry);
    
    
    /**
     * Find jar names from the classloader matching a pattern.
     * 
     * If the pattern is null and isNullInclusive is true, then
     * all jar names in the classloader will match.
     * 
     * A pattern is a set of acceptable jar names. Each acceptable
     * jar name is a regex. Each regex can be separated by either a
     * "," or a "|". If you use a "|" this or's together the jar
     * name patterns. This means that ordering of the matches is
     * unimportant to you. If instead, you want to match particular
     * jar names, and you want to match them in order, you should
     * separate the regexs with "," instead. 
     * 
     * Eg "aaa-.*\\.jar|bbb-.*\\.jar"
     * Will iterate over the jar names in the classloader and match
     * in any order.
     * 
     * Eg "aaa-*\\.jar,bbb-.*\\.jar"
     * Will iterate over the jar names in the classloader, matching
     * all those starting with "aaa-" first, then "bbb-".
     * 
     * If visitParent is true, then the pattern is applied to the
     * parent loader hierarchy. If false, it is only applied to the
     * classloader passed in.
     * 
     * @param pattern
     * @param loader
     * @param isNullInclusive
     * @param visitParent
     * @throws Exception
     */
    public void scan (Pattern pattern, ClassLoader loader, boolean isNullInclusive, boolean visitParent)
    throws Exception
    {
        String[] patterns = (pattern==null?null:pattern.pattern().split(","));

        List<Pattern> subPatterns = new ArrayList<Pattern>();
        for (int i=0; patterns!=null && i<patterns.length;i++)
            subPatterns.add(Pattern.compile(patterns[i]));
        if (subPatterns.isEmpty())
            subPatterns.add(pattern);
        
        
        while (loader!=null)
        {
            if (loader instanceof URLClassLoader)
            {
                URL[] urls = ((URLClassLoader)loader).getURLs();

                if (urls!=null)
                {
                    if (subPatterns.isEmpty())
                    {
                        processJars(null, urls, isNullInclusive);
                    }
                    else
                    {
                        //for each subpattern, iterate over all the urls, processing those that match
                        for (Pattern p : subPatterns)
                        {
                           processJars(p, urls, isNullInclusive);
                        }
                    }
                }
            }     
            if (visitParent)
                loader=loader.getParent();
            else
                loader = null;
        }  
    }
    
    
    
    public void processJars (Pattern pattern, URL[] urls, boolean isNullInclusive)
    throws Exception
    {
        for (int i=0; i<urls.length;i++)
        {
            if (urls[i].toString().toLowerCase().endsWith(".jar"))
            {
                String jar = urls[i].toString();
                int slash=jar.lastIndexOf('/');
                jar=jar.substring(slash+1);
                
                if ((pattern == null && isNullInclusive)
                    ||
                    (pattern!=null && pattern.matcher(jar).matches()))
                {
                    processJar(urls[i]);
                }
            }
        }
    }
    
    public void processJar (URL url)
    throws Exception
    {
        Log.debug("Search of {}",url);
        
        InputStream in = Resource.newResource(url).getInputStream();
        if (in==null)
            return;

        JarInputStream jar_in = new JarInputStream(in);
        try
        { 
            JarEntry entry = jar_in.getNextJarEntry();
            while (entry!=null)
            {
                processEntry(url, entry);
                entry = jar_in.getNextJarEntry();
            }
        }
        finally
        {
            jar_in.close();
        }   
    }
}

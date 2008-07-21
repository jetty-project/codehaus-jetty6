//========================================================================
//$Id$
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.annotations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

/**
 * Configuration
 *
 *
 */
public class Configuration extends org.mortbay.jetty.plus.webapp.Configuration
{
    public static final String __web_inf_pattern = "org.mortbay.jetty.webapp.WebInfIncludeAnnotationJarPattern";
    public static final String __container_pattern = "org.mortbay.jetty.webapp.ContainerIncludeAnnotationJarPattern";
                                                      
    
    
    public Configuration () throws ClassNotFoundException
    {
        super();
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#parseAnnotations()
     */
    public void parseAnnotations() throws Exception
    {
        /*
         * TODO Need to also take account of hidden classes on system classpath that should never
         * contribute annotations to a webapp (system and server classes):
         * 
         * --- when scanning system classpath:
         *   + system classes : should always be scanned (subject to pattern)
         *   + server classes : always ignored
         *   
         * --- when scanning webapp classpath:
         *   + system classes : always ignored
         *   + server classes : always scanned
         * 
         * 
         * If same class is found in both container and in context then need to use
         * webappcontext parentloaderpriority to work out which one contributes the
         * annotation.
         */
       
        //Scan classes from webapp specifically mentioned in web.xml
        List<String> classNames = new ArrayList<String>();
         
        //look thru _servlets      
        Iterator itor = LazyList.iterator(_servlets);
        while (itor.hasNext())
        {
            ServletHolder holder = (ServletHolder)itor.next();
            classNames.add(holder.getClassName());
        }
        
        //look thru _filters
        itor = LazyList.iterator(_filters);
        while (itor.hasNext())
        {
            FilterHolder holder = (FilterHolder)itor.next();
            classNames.add(holder.getClassName());
        }
        
        //look thru _listeners
        itor = LazyList.iterator(_listeners);
        while (itor.hasNext())
        {
            Object listener = itor.next();
            classNames.add(listener.getClass().getName());
        }
        
        AnnotationFinder finder = new AnnotationFinder();

        //if no pattern for the container path is defined, then by default scan NOTHING
        Log.debug("Scanning system jars");
        finder.find(getWebAppContext().getClassLoader().getParent(), true, getWebAppContext().getInitParameter(__container_pattern), false, 
                new ClassNameResolver ()
                {
                    public boolean isExcluded (String name)
                    {
                        if (getWebAppContext().isSystemClass(name)) return false;
                        if (getWebAppContext().isServerClass(name)) return true;
                        return false;
                    }

                    public boolean shouldOverride (String name)
                    { 
                        //looking at system classpath
                        if (getWebAppContext().isParentLoaderPriority())
                            return true;
                        return false;
                    }
                });

        Log.debug("Scanning WEB-INF/lib jars");
        //if no pattern for web-inf/lib is defined, then by default scan everything in it
        finder.find (getWebAppContext().getClassLoader(), false, getWebAppContext().getInitParameter(__web_inf_pattern), true,
                new ClassNameResolver()
                {
                    public boolean isExcluded (String name)
                    {    
                        if (getWebAppContext().isSystemClass(name)) return true;
                        if (getWebAppContext().isServerClass(name)) return false;
                        return false;
                    }

                    public boolean shouldOverride (String name)
                    {
                        //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                        if (getWebAppContext().isParentLoaderPriority())
                            return false;
                        return true;
                    }
                });

        Log.debug("Scanning classes from web.xml");
        finder.find(classNames, 
                new ClassNameResolver()
                {
                    public boolean isExcluded (String name)
                    {
                        if (getWebAppContext().isSystemClass(name)) return true;
                        if (getWebAppContext().isServerClass(name)) return false;
                        return false;
                    }

                    public boolean shouldOverride (String name)
                    {
                        //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                        if (getWebAppContext().isParentLoaderPriority())
                            return false;
                        return true;
                    }
                });
        
        AnnotationProcessor processor = new AnnotationProcessor(finder, _runAsCollection, _injections, _callbacks, 
                LazyList.getList(_servlets), LazyList.getList(_filters), LazyList.getList(_listeners), 
                LazyList.getList(_servletMappings), LazyList.getList(_filterMappings));
        processor.process();
        _servlets = processor.getServlets();
        _filters = processor.getFilters();
        _servletMappings = processor.getServletMappings();
        _filterMappings = processor.getFilterMappings();
    }
}

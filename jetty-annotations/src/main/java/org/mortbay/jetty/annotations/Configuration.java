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
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import org.mortbay.jetty.server.servlet.FilterHolder;
import org.mortbay.jetty.server.servlet.FilterMapping;
import org.mortbay.jetty.server.servlet.ServletHolder;
import org.mortbay.jetty.server.servlet.ServletMapping;
import org.mortbay.jetty.util.LazyList;
import org.mortbay.jetty.util.log.Log;

/**
 * Configuration
 *
 *
 */
public class Configuration extends org.mortbay.jetty.plus.webapp.Configuration
{
    public static final String __web_inf_pattern = "org.mortbay.jetty.server.webapp.WebInfIncludeAnnotationJarPattern";
    public static final String __container_pattern = "org.mortbay.jetty.server.webapp.ContainerIncludeAnnotationJarPattern";
                                                      
    
    
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
        
        Log.debug("Scanning classes in WEB-INF/classes");
        finder.find(_context.getWebInf().addPath("classes/"), 
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
        
        AnnotationProcessor processor = new AnnotationProcessor(getWebAppContext(), finder, _runAsCollection, _injections, _callbacks, 
                LazyList.getList(_servlets), LazyList.getList(_filters), LazyList.getList(_listeners), 
                LazyList.getList(_servletMappings), LazyList.getList(_filterMappings));
        processor.process();
        _servlets = processor.getServlets();
        _filters = processor.getFilters();
        _servletMappings = processor.getServletMappings();
        _filterMappings = processor.getFilterMappings();
        _listeners = processor.getListeners();
        _servletHandler.setFilters((FilterHolder[])LazyList.toArray(_filters,FilterHolder.class));
        _servletHandler.setFilterMappings((FilterMapping[])LazyList.toArray(_filterMappings,FilterMapping.class));
        _servletHandler.setServlets((ServletHolder[])LazyList.toArray(_servlets,ServletHolder.class));
        _servletHandler.setServletMappings((ServletMapping[])LazyList.toArray(_servletMappings,ServletMapping.class));
        getWebAppContext().setEventListeners((EventListener[])LazyList.toArray(_listeners,EventListener.class));
    }
}

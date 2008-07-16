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
import org.mortbay.util.LazyList;

/**
 * Configuration
 *
 *
 */
public class Configuration extends org.mortbay.jetty.plus.webapp.Configuration
{
    
    public Configuration () throws ClassNotFoundException
    {
        super();
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#parseAnnotations()
     */
    public void parseAnnotations() throws Exception
    {
        //TODO - future is to look thru all jars in the classloader context
        
        //Replicate servlet spec < 3.0 behaviour
        List<String> classNames = new ArrayList();
        
  
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
        
        AnnotationFinder finder = new AnnotationFinder (getWebAppContext().getClassLoader(), classNames);
        AnnotationProcessor processor = new AnnotationProcessor(finder, _runAsCollection, _injections, _callbacks);
        processor.process();
    }
}

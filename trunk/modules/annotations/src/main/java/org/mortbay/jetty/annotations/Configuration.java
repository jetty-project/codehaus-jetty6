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

import java.util.Iterator;


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


    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#parseAnnotations()
     */
    protected void parseAnnotations() throws Exception
    {
        AnnotationProcessor processor = new AnnotationProcessor();
        
        //look thru _servlets
        Iterator itor = LazyList.iterator(_servlets);
        while (itor.hasNext())
        {
            ServletHolder holder = (ServletHolder)itor.next();
            Class servlet = getWebAppContext().loadClass(holder.getClassName());
            processor.processClass(servlet);
            processor.processAnnotations(holder, servlet, _injections, _callbacks);
        }
        
        //look thru _filters
        itor = LazyList.iterator(_filters);
        while (itor.hasNext())
        {
            FilterHolder holder = (FilterHolder)itor.next();
            Class filter = getWebAppContext().loadClass(holder.getClassName());
            processor.processClass(filter);
            processor.processAnnotations(null, filter, _injections, _callbacks);
        }
        
        //look thru _listeners
        itor = LazyList.iterator(_listeners);
        while (itor.hasNext())
        {
            Class listener = (Class)itor.next();
            processor.processClass(listener);
            processor.processAnnotations(null, listener, _injections, _callbacks);
        }
    }
}

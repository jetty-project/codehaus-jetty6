//========================================================================
//$Id: TagLibConfiguration.java,v 1.4 2005/08/13 00:01:27 gregwilkins Exp $
//Copyright 2004 Mort Bay Consulting Pty. Ltd.
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
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

import javax.servlet.Servlet;

import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.util.Loader;
import org.mortbay.xml.XmlParser;

/* ------------------------------------------------------------ */
/** TagLibConfiguration.
 * 
 * The class searches for TLD descriptors found in web.xml, in WEB-INF/*.tld files of the web app
 * or *.tld files withing jars found in WEB-INF/lib of the webapp.   Any listeners defined in these
 * tld's are added to the context.
 * 
 * &lt;bile&gt;This is total rubbish special case for JSPs! If there was a general use-case for web app
 * frameworks to register listeners directly, then a generic mechanism could have been added to the servlet
 * spec.  Instead some special purpose JSP support is required that breaks all sorts of encapsualtion rules as
 * the servlet container must go searching for and then parsing the descriptors for one particular framework.
 * It only appears to be used by JSF, which is being developed by the same developer who implemented this
 * feature in the first place!
 * &lt;/bile&gt;
 * 
 * @author gregw
 *
 */
public class TagLibConfiguration implements Configuration
{
    WebAppContext _context;
    
    /* ------------------------------------------------------------ */
    public void setWebAppContext(WebAppContext context)
    {
        _context=context;
    }

    /* ------------------------------------------------------------ */
    public WebAppContext getWebAppContext()
    {
        return _context;
    }

    /* ------------------------------------------------------------ */
    public void configureClassLoader() throws Exception
    {
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.servlet.WebAppContext.Configuration#configureDefaults()
     */
    public void configureDefaults() throws Exception
    {
    }

    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.servlet.WebAppContext.Configuration#configureWebApp()
     */
    public void configureWebApp() throws Exception
    {   
        Set tlds = new HashSet();
        Set jars = new HashSet();
        
        // Find tld's from web.xml
        // When the XMLConfigurator (or other configurator) parsed the web.xml,
        // It should have created aliases for all TLDs.  So search resources aliases
        // for aliases ending in tld
        if (_context.getResourceAliases()!=null && 
            _context.getBaseResource()!=null && 
            _context.getBaseResource().exists())
        {
            Iterator iter=_context.getResourceAliases().values().iterator();
            while(iter.hasNext())
            {
                String location = (String)iter.next();
                if (location!=null && location.toLowerCase().endsWith(".tld"))
                {
                    if (!location.startsWith("/"))
                        location="/WEB-INF/"+location;
                    Resource l=_context.getBaseResource().addPath(location);
                    tlds.add(l);
                }
            }
        }
        
        // Look for any tlds in WEB-INF directly.
        Resource web_inf = _context.getWebInf();
        if (web_inf!=null)
        {
            String[] contents = web_inf.list();
            for (int i=0;contents!=null && i<contents.length;i++)
            {
                if (contents[i]!=null && contents[i].toLowerCase().endsWith(".tld"))
                {
                    Resource l=_context.getWebInf().addPath(contents[i]);
                    tlds.add(l);
                }
                
            }
        }
        
        // Look for tlds in any jars
 
        //Change to an opt-in style instead:
        //
        //org.mortbay.jetty.webapp.WebInfIncludeTLDJarPattern and
        //org.mortbay.jetty.webapp.ContainerIncludeTLDJarPattern
        //
        //When examining jars in WEB-INF/lib:
        //   if WebInfIncludeTLDJarPattern is null
        //       examine ALL for tlds
        //   else
        //       examine only files matching pattern
        //
        //When examining jars in parent loaders:
        //    If IncludeTLDJarPattern is null
        //       examine none
        //    else
        //       examine only files matching pattern
        //
        String tmp = _context.getInitParameter("org.mortbay.jetty.webapp.WebInfIncludeTLDJarPattern");
        Pattern webInfIncludePattern = (tmp==null?null:Pattern.compile(tmp));
        
        tmp = _context.getInitParameter("org.mortbay.jetty.webapp.ContainerIncludeTLDJarPattern");
        Pattern containerIncludePattern = (tmp==null?null:Pattern.compile(tmp));
        
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        boolean parent=false;
        
        while (loader!=null)
        {
            if (loader instanceof URLClassLoader)
            {
                URL[] urls = ((URLClassLoader)loader).getURLs();

                if (urls!=null)
                {
                    for (int i=0;i<urls.length;i++)
                    {   
                        if (urls[i].toString().toLowerCase().endsWith(".jar"))
                        {

                            String jar = urls[i].toString();
                            int slash=jar.lastIndexOf('/');
                            jar=jar.substring(slash+1);
                            
                            if ((!parent && ((webInfIncludePattern==null) || (webInfIncludePattern.matcher(jar).matches())))
                                    ||
                                (parent && (containerIncludePattern!=null && containerIncludePattern.matcher(jar).matches())))
                            {
                                jars.add(jar);

                                Log.debug("TLD search of {}",urls[i]);

                                InputStream in = _context.newResource(urls[i]).getInputStream();
                                if (in==null)
                                    continue;

                                JarInputStream jar_in = new JarInputStream(in);
                                try
                                { 
                                    JarEntry entry = jar_in.getNextJarEntry();
                                    while (entry!=null)
                                    {
                                        String name = entry.getName();
                                        if (name.startsWith("META-INF/") && name.toLowerCase().endsWith(".tld"))
                                        {
                                            Resource tld=_context.newResource("jar:"+urls[i]+"!/"+name);
                                            tlds.add(tld);
                                            Log.debug("TLD found {}",tld);
                                        }
                                        entry = jar_in.getNextJarEntry();
                                    }
                                }
                                finally
                                {
                                    jar_in.close();
                                }   
                            }
                        }
                    }
                }
            }
            loader=loader.getParent();
            parent=true; 
        }
        
        // Create a TLD parser
        XmlParser parser = new XmlParser(false);
        
        URL taglib11=null;
        URL taglib12=null;
        URL taglib20=null;
        URL taglib21=null;

        try
        {
            Class jsp_page = Loader.loadClass(WebXmlConfiguration.class,"javax.servlet.jsp.JspPage");
            taglib11=jsp_page.getResource("javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd");
            taglib12=jsp_page.getResource("javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd");
            taglib20=jsp_page.getResource("javax/servlet/jsp/resources/web-jsptaglibrary_2_0.xsd");
            taglib21=jsp_page.getResource("javax/servlet/jsp/resources/web-jsptaglibrary_2_1.xsd");
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
        finally
        {
            if(taglib11==null)
                taglib11=Loader.getResource(Servlet.class,"javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd",true);
            if(taglib12==null)
                taglib12=Loader.getResource(Servlet.class,"javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd",true);
            if(taglib20==null)
                taglib20=Loader.getResource(Servlet.class,"javax/servlet/jsp/resources/web-jsptaglibrary_2_0.xsd",true);
            if(taglib21==null)
                taglib21=Loader.getResource(Servlet.class,"javax/servlet/jsp/resources/web-jsptaglibrary_2_1.xsd",true);
        }
        

        if(taglib11!=null)
        {
            parser.redirectEntity("web-jsptaglib_1_1.dtd",taglib11);
            parser.redirectEntity("web-jsptaglibrary_1_1.dtd",taglib11);
        }
        if(taglib12!=null)
        {
            parser.redirectEntity("web-jsptaglib_1_2.dtd",taglib12);
            parser.redirectEntity("web-jsptaglibrary_1_2.dtd",taglib12);
        }
        if(taglib20!=null)
        {
            parser.redirectEntity("web-jsptaglib_2_0.xsd",taglib20);
            parser.redirectEntity("web-jsptaglibrary_2_0.xsd",taglib20);
        }
        if(taglib21!=null)
        {
            parser.redirectEntity("web-jsptaglib_2_1.xsd",taglib21);
            parser.redirectEntity("web-jsptaglibrary_2_1.xsd",taglib21);
        }
        
        parser.setXpath("/taglib/listener/listener-class");
        
        // Parse all the discovered TLDs
        Iterator iter = tlds.iterator();
        while (iter.hasNext())
        {
            try
            {
                Resource tld = (Resource)iter.next();
                if (Log.isDebugEnabled()) Log.debug("TLD="+tld);
                
                XmlParser.Node root;
                
                try
                {
                    //xerces on apple appears to sometimes close the zip file instead
                    //of the inputstream, so try opening the input stream, but if
                    //that doesn't work, fallback to opening a new url
                    root = parser.parse(tld.getInputStream());
                }
                catch (Exception e)
                {
                    root = parser.parse(tld.getURL().toString());
                }

		if (root==null)
		{
		    Log.warn("No TLD root in {}",tld);
		    continue;
		}
                
                for (int i=0;i<root.size();i++)
                {
                    Object o=root.get(i);
                    if (o instanceof XmlParser.Node)
                    {
                        XmlParser.Node node = (XmlParser.Node)o;
                        if ("listener".equals(node.getTag()))
                        {
                            String className=node.getString("listener-class",false,true);
                            if (Log.isDebugEnabled()) Log.debug("listener="+className);
                            
                            try
                            {
                                Class listenerClass=getWebAppContext().loadClass(className);
                                EventListener l=(EventListener)listenerClass.newInstance();
                                _context.addEventListener(l);
                            }
                            catch(Exception e)
                            {
                                Log.warn("Could not instantiate listener "+className+": "+e);
                                Log.debug(e);
                            }
                            catch(Error e)
                            {
                                Log.warn("Could not instantiate listener "+className+": "+e);
                                Log.debug(e);
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                Log.warn(e);
            }
        }
    }


    public void deconfigureWebApp() throws Exception
    {
    }
    

}

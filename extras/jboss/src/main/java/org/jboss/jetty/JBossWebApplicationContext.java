/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
// $Id: JBossWebApplicationContext.java,v 1.10 2004/10/03 01:35:42 gregwilkins Exp $
// A Jetty HttpServer with the interface expected by JBoss'
// J2EEDeployer...
//------------------------------------------------------------------------------
package org.jboss.jetty;

//------------------------------------------------------------------------------
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.jetty.jmx.JBossWebApplicationContextMBean;
import org.jboss.jetty.security.JBossUserRealm;
import org.jboss.logging.Logger;
import org.jboss.metadata.WebMetaData;
import org.jboss.web.WebApplication;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
//import org.mortbay.j2ee.J2EEWebApplicationContext;
//import org.mortbay.j2ee.session.AbstractReplicatedStore;
//import org.mortbay.j2ee.session.Manager;
//import org.mortbay.j2ee.session.Store;
import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.webapp.WebXmlConfiguration;
import org.mortbay.util.MultiException;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlParser;
//------------------------------------------------------------------------------
public class JBossWebApplicationContext extends WebAppContext
{
    protected static Logger __log=Logger.getLogger(JBossWebApplicationContext.class);
    protected Jetty _jetty;
    protected WebDescriptorParser _descriptorParser;
    protected WebApplication _webApp;
    protected String _subjAttrName="j_subject";
    protected JBossUserRealm _realm=null;
    

    public JBossWebApplicationContext(Jetty jetty,WebDescriptorParser descriptorParser,WebApplication webApp,
            String warUrl) throws IOException
    {
        super(warUrl, "");
        _jetty=jetty;
        _descriptorParser=descriptorParser;
        _webApp=webApp;
        _subjAttrName=jetty.getSubjectAttributeName();
        // other stuff
        // we'll add this when we figure out where to get the TransactionManager...
        //      addHandler(new JBossWebApplicationHandler());

    }

    /* ------------------------------------------------------------ */
    protected void doStart() throws Exception
    {
//        if(_jetty.getSupportJSR77())
//            setConfigurationClasses (new String[]{"org.jboss.jetty.JBossWebApplicationContext$Configuration", "org.mortbay.jetty.webapp.JettyWebXmlConfiguration","org.mortbay.jetty.servlet.jsr77.Configuration"});
//        else
            setConfigurationClasses (new String[]{ "org.mortbay.jetty.webapp.WebInfConfiguration","org.jboss.jetty.JBossWebApplicationContext$Configuration", "org.mortbay.jetty.webapp.JettyWebXmlConfiguration",  "org.mortbay.jetty.webapp.TagLibConfiguration"});
            
        MultiException e=null;
        try
        {
            super.doStart();
        }
        catch(MultiException me)
        {
            e=me;
        }
//        if(_jetty.getSupportJSR77())
//            setUpDeploymentInfo();
        if(e!=null)
            throw e;
    }

    /* ------------------------------------------------------------ */
    public void doStop() throws Exception
    {
	super.doStop();
	_jetty=null;
	_descriptorParser=null;
	_webApp=null;
	_subjAttrName=null;
    }

    /* ------------------------------------------------------------ */
    public void setContextPath(String contextPathSpec)
    {
        __log=Logger.getLogger(getClass().getName()+"#"+contextPathSpec);
        super.setContextPath(contextPathSpec);
    }
    // this is a hack - but we need the session timeout - in case we are
    // going to use a distributable session manager....
    protected boolean _timeOutPresent=false;
    protected int _timeOutMinutes=0;

/*
    String _separator=System.getProperty("path.separator");

    public String getFileClassPath()
    {
        List list=new ArrayList();
        getFileClassPath(getClassLoader(),list);
        String classpath="";
        for(Iterator i=list.iterator();i.hasNext();)
        {
            URL url=(URL)i.next();
            if(!url.getProtocol().equals("file")) // tmp warning
            {
                __log.warn("JSP classpath: non-'file' protocol: "+url);
                continue;
            }
            try
            {
                Resource res=Resource.newResource(url);
                if(res.getFile()==null)
                    __log.warn("bad classpath entry: "+url);
                else
                {
                    String tmp=res.getFile().getCanonicalPath();
                    //	    _log.info("JSP FILE: "+url+" --> "+tmp+" : "+url.getProtocol());
                    classpath+=(classpath.length()==0?"":_separator)+tmp;
                }
            }
            catch(IOException ioe)
            {
                __log.warn("JSP Classpath is damaged, can't convert path for :"+url,ioe);
            }
        }
        if(__log.isTraceEnabled())
            __log.trace("JSP classpath: "+classpath);
        return classpath;
    }

    public void getFileClassPath(ClassLoader cl,List list)
    {
        if(cl==null)
            return;
        URL[] urls=null;
        try
        {
            Class[] sig={};
            Method getAllURLs=cl.getClass().getMethod("getAllURLs",sig);
            Object[] args={};
            urls=(URL[])getAllURLs.invoke(cl,args);
        }
        catch(Exception ignore)
        {}
        if(urls==null&&cl instanceof java.net.URLClassLoader)
            urls=((java.net.URLClassLoader)cl).getURLs();
        //      _log.info("CLASSLOADER: "+cl);
        //      _log.info("URLs: "+(urls!=null?urls.length:0));
        if(urls!=null)
        {
            for(int i=0;i<urls.length;i++)
            {
                URL url=urls[i];
                if(url!=null)
                {
                    String path=url.getPath();
                    if(path!=null)
                    {
                        File f=new File(path);
                        if(f.exists()&&f.canRead()&&(f.isDirectory()||path.endsWith(".jar")||path.endsWith(".zip"))
                                &&(!list.contains(url)))
                            list.add(url);
                    }
                }
            }
        }
        getFileClassPath(cl.getParent(),list);
    }

    // given a resource name, find the jar file that contains that resource...
    protected String findJarByResource(String resource) throws Exception
    {
        String path=getClass().getClassLoader().getResource(resource).toString();
        // lose initial "jar:file:" and final "!/..."
        return path.substring("jar:file:".length(),path.length()-(resource.length()+2));
    }
*/
    protected void startContext() throws Exception
    {
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
//        if(getDistributable()&&getDistributableSessionManager()!=null)
//            setUpDistributableSessionManager(loader);
        setUpENC(loader);
        if(_realm!=null)
            _realm.init();
        super.startContext();
    }

//    protected void setUpDistributableSessionManager(ClassLoader loader)
//    {
//        try
//        {
//            SessionManager sm=getDistributableSessionManager();
//            Store store=sm.getStore();
//            if(store instanceof AbstractReplicatedStore)
//                ((AbstractReplicatedStore)store).setLoader(loader);
//            if(_timeOutPresent)
//                sm.setMaxInactiveInterval(_timeOutMinutes*60);
//            getServletHandler().setSessionManager(sm);
//            //_log.info("using Distributable HttpSession Manager: "+sm);
//        }
//        catch(Exception e)
//        {
//            __log.error("could not set up Distributable HttpSession Manager - using local one",e);
//        }
//    }

    protected void setUpENC(ClassLoader loader) throws Exception
    {
        _webApp.setClassLoader(loader);
        _webApp.setName(getDisplayName());
        _webApp.setAppData(this);
        __log.debug("setting up ENC...");
        _descriptorParser.parseWebAppDescriptors(_webApp.getClassLoader(),_webApp.getMetaData());
        __log.debug("setting up ENC succeeded");
    }

    // this is really nasty because it builds dependencies between the
    // impl and mbean layer which Greg has been very careful to avoid
    // everywhere else. Think of a better way to do it...
//    protected void setUpDeploymentInfo() throws Exception
//    {
//        if(_mbean==null)
//            return; // we can't do anything...
//        DeploymentInfo di=_descriptorParser.getDeploymentInfo();
//        
//        di.deployedObject=_mbean.getObjectName();
//        List mbeanNames=di.mbeans;
//        ServletHandler wah=(ServletHandler)getServletHandler();
//        List components=new ArrayList();
//        ServletHolder servlets[]=wah.getServlets();
//        if(servlets!=null)
//            for(int i=0;i<servlets.length;i++)
//                components.add(servlets[i]);
//        
//        Object filters[]=wah.getFilters();
//        if(filters!=null)
//            for(int i=0;i<filters.length;i++)
//                components.add(filters[i]);
//        components.add(getSessionHandler().getSessionManager());
//        //make mbeans for all jetty objects
//        ObjectName[] names=_mbean.getComponentMBeans(components.toArray(),null);
//       
//        //      populate JSR77 info...
//        Set jsr77Names = _mbean.getJsr77ObjectNames();
//        Iterator itor = jsr77Names.iterator();
//        while (itor.hasNext())
//        {
//            ObjectName on = (ObjectName)itor.next();
//            __log.info ("Adding jsr77 mbean="+on.toString());
//        
//            mbeanNames.add(on);
//        }
//
//    }
    protected JBossWebApplicationContextMBean _mbean;

    public void setMBeanPeer(JBossWebApplicationContextMBean mbean)
    {
        _mbean=mbean;
    }


    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class Configuration extends WebXmlConfiguration
    {
        public Configuration()
        {
            super();
        }

        
        public JBossWebApplicationContext getJBossWebApplicationContext()
        {
            return (JBossWebApplicationContext)getWebAppContext();
        }

        /* ------------------------------------------------------------ */
        // avoid Jetty moaning about things that it doesn't but AbstractWebContainer does do...
        protected void initWebXmlElement(String element,org.mortbay.xml.XmlParser.Node node) throws Exception
        {
            // this is ugly - should be dispatched through a hash-table or introspection...
            // these are handled by AbstractWebContainer
            if("resource-ref".equals(element)||"resource-env-ref".equals(element)||"env-entry".equals(element)
                    ||"ejb-ref".equals(element)||"ejb-local-ref".equals(element)||"security-domain".equals(element))
            {
                //_log.info("Don't moan : "+element);
            }
            else if("distributable".equals(element))
            {
                getJBossWebApplicationContext().setDistributable(true);
            }
            else if("login-config".equals(element))
            {
                // if the realm-name element is set in web.xml use it as the realm name 
                // otherwise, use the security domain name from jboss-web.xml
                // check if a realm has been explicitly set
                String realmName=null;
                UserRealm userRealm = ((SecurityHandler)getJBossWebApplicationContext().getChildHandlerByClass(SecurityHandler.class)).getUserRealm();
                if (userRealm!= null)
                    realmName=userRealm.getName();
                
                if (null==realmName)
                {
                    WebMetaData metaData = getJBossWebApplicationContext()._webApp.getMetaData();
                    realmName = metaData.getSecurityDomain();
                    if (null!=realmName)
                    {
                        if (realmName.endsWith("/"))
                            realmName = realmName.substring (0, realmName.length());
                        int idx = realmName.lastIndexOf('/');
                        if (idx >= 0)
                            realmName = realmName.substring(idx+1);
                    }
                }
                
                if(__log.isDebugEnabled())
                    __log.debug("setting Realm: "+realmName);
                getJBossWebApplicationContext()._realm=new JBossUserRealm(realmName,getJBossWebApplicationContext()._subjAttrName); // we
                                                                                                                                // init()
                                                                                                                                // it
                                                                                                                                // later
                ((SecurityHandler)getJBossWebApplicationContext().getChildHandlerByClass(SecurityHandler.class)).setUserRealm(getJBossWebApplicationContext()._realm); // cache and reuse ? - TODO
                // get Jetty to parse the realm-name element
                super.initWebXmlElement(element,node); 
            }
            // these are handled by Jetty
            else
                super.initWebXmlElement(element,node);
        }

        protected void initSessionConfig(XmlParser.Node node)
        {
            XmlParser.Node tNode=node.get("session-timeout");
            if(tNode!=null)
            {
                getJBossWebApplicationContext()._timeOutPresent=true;
                getJBossWebApplicationContext()._timeOutMinutes=Integer.parseInt(tNode.toString(false,true));
            }
            // pass up to our super class so they can do all this again !
            super.initSessionConfig(node);
        }
    }
}

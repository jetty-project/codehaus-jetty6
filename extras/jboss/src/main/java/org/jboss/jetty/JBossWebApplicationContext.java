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


import java.io.IOException;



import org.jboss.jetty.security.JBossUserRealm;
import org.jboss.logging.Logger;
import org.jboss.web.WebApplication;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
//import org.mortbay.j2ee.J2EEWebApplicationContext;
//import org.mortbay.j2ee.session.AbstractReplicatedStore;
//import org.mortbay.j2ee.session.Manager;
//import org.mortbay.j2ee.session.Store;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.util.MultiException;


/**
 * JBossWebApplicationContext
 *
 * Customize the jetty WebAppContext to jboss environment.
 * 
 * TODO jsr77 support, distributable session support
 *
 */
public class JBossWebApplicationContext extends WebAppContext
{
    protected static Logger __log=Logger.getLogger(JBossWebApplicationContext.class);

    protected WebDescriptorParser _descriptorParser;
    protected WebApplication _webApp;
    protected String _subjAttrName="j_subject";//TODO what was this doing here?
    protected JBossUserRealm _realm=null;
    // this is a hack - but we need the session timeout - in case we are
    // going to use a distributable session manager....
    protected boolean _timeOutPresent=false;
    protected int _timeOutMinutes=0;

    public JBossWebApplicationContext(WebDescriptorParser descriptorParser,WebApplication webApp,
            String warUrl) throws IOException
    {
        super(warUrl, "");
        _descriptorParser=descriptorParser;
        _webApp=webApp;
    }

    /* ------------------------------------------------------------ */
    protected void doStart() throws Exception
    {
        MultiException e=null;
        try
        {
            super.doStart();
        }
        catch(MultiException me)
        {
            e=me;
        }
         // when jsr77 support is ready, figure out how to do it
         // setUpDeploymentInfo();
        if(e!=null)
            throw e;
    }

    /* ------------------------------------------------------------ */
    public void doStop() throws Exception
    {
        super.doStop();
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

    //TODO sort out jsr77
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

}

//========================================================================
//$Id: $
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
package org.jboss.jetty;


import java.io.IOException;



import org.jboss.deployment.DeploymentInfo;
import org.jboss.jetty.security.JBossUserRealm;
import org.jboss.logging.Logger;
import org.jboss.web.WebApplication;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
//import org.mortbay.j2ee.J2EEWebApplicationContext;
//import org.mortbay.j2ee.session.AbstractReplicatedStore;
//import org.mortbay.j2ee.session.Manager;
//import org.mortbay.j2ee.session.Store;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.servlet.jsr77.Jsr77ServletHandler;
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
public class JBossWebAppContext extends WebAppContext
{
    protected static Logger __log=Logger.getLogger(JBossWebAppContext.class);

    protected WebDescriptorParser _descriptorParser;
    protected WebApplication _webApp;
    protected String _subjAttrName="j_subject";//TODO what was this doing here?
    protected JBossUserRealm _realm=null;
    // this is a hack - but we need the session timeout - in case we are
    // going to use a distributable session manager....
    protected boolean _timeOutPresent=false;
    protected int _timeOutMinutes=0;

    public JBossWebAppContext(WebDescriptorParser descriptorParser,WebApplication webApp, String warUrl) 
    throws IOException
    {
        super(null,null, new Jsr77ServletHandler(), null);
        setWar(warUrl);
        ((Jsr77ServletHandler)getServletHandler()).setWebAppContext(this);
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
        if(e!=null)
            throw e;
    }

    /* ------------------------------------------------------------ */
    public void doStop() throws Exception
    {
        super.doStop();
        //_descriptorParser=null;
        //_webApp=null;
        //_subjAttrName=null;
    }



    public String getUniqueName ()
    {
        return _descriptorParser.getDeploymentInfo().getCanonicalName();
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
}

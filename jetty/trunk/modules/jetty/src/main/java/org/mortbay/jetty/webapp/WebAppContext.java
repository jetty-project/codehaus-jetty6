//========================================================================
//$Id: WebAppContext.java,v 1.5 2005/11/16 22:02:45 gregwilkins Exp $
//Copyright 2004-2006 Mort Bay Consulting Pty. Ltd.
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionListener;

import org.mortbay.io.IO;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.log.Log;
import org.mortbay.resource.JarResource;
import org.mortbay.resource.Resource;
import org.mortbay.util.LazyList;
import org.mortbay.util.Loader;
import org.mortbay.util.TypeUtil;

/* ------------------------------------------------------------ */
/** Web Application Context Handler.
 * The WebAppContext handler is an extension of ContextHandler that
 * coordinates the construction and configuration of nested handlers:
 * {@link org.mortbay.jetty.security.SecurityHandler}, {@link org.mortbay.jetty.servlet.SessionHandler}
 * and {@link org.mortbay.jetty.servlet.ServletHandler}.
 * The handlers are configured by pluggable configuration classes, with
 * the default being  {@link org.mortbay.jetty.webapp.WebXmlConfiguration} and 
 * {@link org.mortbay.jetty.webapp.JettyWebXmlConfiguration}.
 *      
 * @org.apache.xbean.XBean description="Creates a servlet web application at a given context from a resource base"
 * 
 * @author gregw
 *
 */
public class WebAppContext extends ContextHandler
{   
    public final static String WEB_DEFAULTS_XML="org/mortbay/jetty/webapp/webdefault.xml";
    public final static String ERROR_PAGE="org.mortbay.jetty.error_page";
    
    private static String[] __dftConfigurationClasses =  
    { 
        "org.mortbay.jetty.webapp.WebInfConfiguration", 
        "org.mortbay.jetty.webapp.WebXmlConfiguration", 
        "org.mortbay.jetty.webapp.JettyWebXmlConfiguration",
        "org.mortbay.jetty.webapp.TagLibConfiguration" 
    } ;
    private String[] _configurationClasses=__dftConfigurationClasses;
    private Configuration[] _configurations;
    private String _defaultsDescriptor=WEB_DEFAULTS_XML;
    private boolean _distributable=false;
    private boolean _extractWAR=true;
    private boolean _parentLoaderPriority= Boolean.getBoolean("org.mortbay.jetty.webapp.parentLoaderPriorty");
    private PermissionCollection _permissions;
    private SecurityHandler _securityHandler;
    private ServletHandler _servletHandler;
    private SessionHandler _sessionHandler;
    private String[] _systemClasses = {"java.","javax.servlet.","javax.xml.","org.mortbay.","org.xml.","org.w3c."};
    private String[] _serverClasses = {"org.mortbay.", "-org.mortbay.naming.","-org.mortbay.util.", "org.slf4j."}; // TODO hide all mortbay classes
    private File _tmpDir;
    private String _war;
    
    private transient Map _resourceAliases;
    private transient boolean _ownClassLoader=false;

    /* ------------------------------------------------------------ */
    /**  Add Web Applications.
     * Add auto webapplications to the server.  The name of the
     * webapp directory or war is used as the context name. If the
     * webapp matches the rootWebApp it is added as the "/" context.
     * @param host Virtual host name or null
     * @param webapps Directory file name or URL to look for auto
     * webapplication.
     * @param defaults The defaults xml filename or URL which is
     * loaded before any in the web app. Must respect the web.dtd.
     * If null the default defaults file is used. If the empty string, then
     * no defaults file is used.
     * @param extract If true, extract war files
     * @param java2CompliantClassLoader True if java2 compliance is applied to all webapplications
     * @exception IOException 
     */
    public static void addWebApplications(Server server,
                                          String webapps,
                                          String defaults,
                                          boolean extract,
                                          boolean java2CompliantClassLoader)
        throws IOException
    {
        addWebApplications(server, webapps, defaults, __dftConfigurationClasses, extract, java2CompliantClassLoader);
    }
    
    /* ------------------------------------------------------------ */
    /**  Add Web Applications.
     * Add auto webapplications to the server.  The name of the
     * webapp directory or war is used as the context name. If the
     * webapp matches the rootWebApp it is added as the "/" context.
     * @param host Virtual host name or null
     * @param webapps Directory file name or URL to look for auto
     * webapplication.
     * @param defaults The defaults xml filename or URL which is
     * loaded before any in the web app. Must respect the web.dtd.
     * If null the default defaults file is used. If the empty string, then
     * no defaults file is used.
     * @param configurations Array of classnames of {@link Configuration} implementations to apply.
     * @param extract If true, extract war files
     * @param java2CompliantClassLoader True if java2 compliance is applied to all webapplications
     * @exception IOException 
     */
    public static void addWebApplications(Server server,
                                          String webapps,
                                          String defaults,
                                          String[] configurations,
                                          boolean extract,
                                          boolean java2CompliantClassLoader)
        throws IOException
    {
        if (configurations==null)
            configurations=__dftConfigurationClasses;
        
        ArrayList wacs = new ArrayList();
        if(server.getHandlers() != null) {
            java.util.List installedWacs = Arrays.asList(server.getHandlers());
            wacs.addAll(installedWacs);
        } 
        
        Resource r=Resource.newResource(webapps);
        if (!r.exists())
            throw new IllegalArgumentException("No such webapps resource "+r);
        
        if (!r.isDirectory())
            throw new IllegalArgumentException("Not directory webapps resource "+r);
        
        String[] files=r.list();
        
        files: for (int f=0;files!=null && f<files.length;f++)
        {
            String context=files[f];
            
            if (context.equalsIgnoreCase("CVS/") ||
                    context.equalsIgnoreCase("CVS") ||
                    context.startsWith("."))
                continue;
            
            Resource app = r.addPath(r.encode(context));
            
            if (context.toLowerCase().endsWith(".war") ||
                    context.toLowerCase().endsWith(".jar"))
            {
                context=context.substring(0,context.length()-4);
                Resource unpacked=r.addPath(context);
                if (unpacked!=null && unpacked.exists() && unpacked.isDirectory())
                    continue;
            }
            else if (!app.isDirectory())
                continue;
            
            if (context.equalsIgnoreCase("root")||context.equalsIgnoreCase("root/"))
                context="/";
            else
                context="/"+context;
            if (context.endsWith("/") && context.length()>0)
                context=context.substring(0,context.length()-1);
            
            // Check the webapp has not already been added.
            for (int i=0;i<wacs.size();i++)
            {
                Object o =wacs.get(i);
                if (o instanceof WebAppContext)
                {
                    WebAppContext w = (WebAppContext)o;
                    if (app.equals(Resource.newResource(w.getWar())))
                        continue files;
                }
            }
            
            // add it
            WebAppContext wah = new WebAppContext();
            wah.setConfigurationClasses(configurations);
            wah.setServer(server);
            wah.setContextPath(context);
            if (defaults!=null)
                wah.setDefaultsDescriptor(defaults);
            wah.setExtractWAR(extract);
            wah.setWar(app.toString());
            wah.setParentLoaderPriority(java2CompliantClassLoader);
            
            wacs.add(wah);
        }

        server.setHandlers((Handler[])wacs.toArray(new Handler[wacs.size()]));
    }
    
    /* ------------------------------------------------------------ */
    public WebAppContext()
    {
        this(null,null,null,null);
    }


    /* ------------------------------------------------------------ */
    public WebAppContext(SecurityHandler securityHandler,SessionHandler sessionHandler, ServletHandler servletHandler, ErrorHandler errorHandler)
    {
        _sessionHandler = sessionHandler!=null?sessionHandler:new SessionHandler();
        _securityHandler = securityHandler!=null?securityHandler:new SecurityHandler();
        _servletHandler = servletHandler!=null?servletHandler:new ServletHandler();
        
        setHandler(_sessionHandler);
        _sessionHandler.setHandler(_securityHandler);
        _securityHandler.setHandler(_servletHandler);
        
        setErrorHandler(errorHandler!=null?errorHandler:new WebAppErrorHandler());
    }    
    
    /* ------------------------------------------------------------ */
    public void setServer(Server server)
    {
        super.setServer(server);
        _securityHandler.setServer(server);
        _sessionHandler.setServer(server);
        _servletHandler.setServer(server);
    }
    /* ------------------------------------------------------------ */
    /** Set Resource Alias.
     * Resource aliases map resource uri's within a context.
     * They may optionally be used by a handler when looking for
     * a resource.  
     * @param alias 
     * @param uri 
     */
    public void setResourceAlias(String alias, String uri)
    {
        if (_resourceAliases == null)
            _resourceAliases= new HashMap(5);
        _resourceAliases.put(alias, uri);
    }

    /* ------------------------------------------------------------ */
    public Map getResourceAliases()
    {
        if (_resourceAliases == null)
            return null;
        return _resourceAliases;
    }
    
    /* ------------------------------------------------------------ */
    public void setResourceAliases(Map map)
    {
        _resourceAliases = map;
    }
    
    /* ------------------------------------------------------------ */
    public String getResourceAlias(String alias)
    {
        if (_resourceAliases == null)
            return null;
        return (String)_resourceAliases.get(alias);
    }

    /* ------------------------------------------------------------ */
    public String removeResourceAlias(String alias)
    {
        if (_resourceAliases == null)
            return null;
        return (String)_resourceAliases.remove(alias);
    }


    /* ------------------------------------------------------------ */
    public Resource getResource(String uriInContext) throws MalformedURLException
    {
        IOException ioe= null;
        Resource resource= null;
        int loop=0;
        while (uriInContext!=null && loop++<100)
        {
            try
            {
                resource= super.getResource(uriInContext);
                if (resource != null && resource.exists())
                    return resource;
                
                uriInContext = getResourceAlias(uriInContext);
            }
            catch (IOException e)
            {
                Log.ignore(e);
                if (ioe==null)
                    ioe= e;
            }
        }

        if (ioe != null && ioe instanceof MalformedURLException)
            throw (MalformedURLException)ioe;

        return resource;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        // Setup configurations 
        loadConfigurations();
        
        for (int i=0;i<_configurations.length;i++)
            _configurations[i].setWebAppContext(this);
        
        // Configure classloader
        _ownClassLoader=false;
        if (getClassLoader()==null)
        {
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            if (Log.isDebugEnabled()) 
            {
                Log.debug("Thread Context class loader is: " + parent);
                for (ClassLoader loader = parent.getParent(); loader != null; loader = loader.getParent()) {
                    Log.debug("Parent class loader is: " + loader); 
                }
            }
            if (parent==null)
                parent=this.getClass().getClassLoader();
            if (parent==null)
                parent=ClassLoader.getSystemClassLoader();
            
            WebAppClassLoader classLoader = new WebAppClassLoader(parent,this);
            setClassLoader(classLoader);
            _ownClassLoader=true;
        }
        
        for (int i=0;i<_configurations.length;i++)
            _configurations[i].configureClassLoader();

        getTempDirectory();
        
        super.doStart();

    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();

        try
        {
            // Configure classloader
            for (int i=_configurations.length;i-->0;)
                _configurations[i].deconfigureWebApp();
            _configurations=null;
            
            // restore security handler
            _securityHandler.setHandler(_servletHandler);
            _sessionHandler.setHandler(_securityHandler);
        }
        finally
        {
            if (_ownClassLoader)
                setClassLoader(null);
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the configurations.
     */
    public String[] getConfigurationClasses()
    {
        return _configurationClasses;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the configurations.
     */
    public Configuration[] getConfigurations()
    {
        return _configurations;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the defaultsDescriptor.
     */
    public String getDefaultsDescriptor()
    {
        return _defaultsDescriptor;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the permissions.
     */
    public PermissionCollection getPermissions()
    {
        return _permissions;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the securityHandler.
     */
    public SecurityHandler getSecurityHandler()
    {
        return _securityHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the serverClasses.
     */
    public String[] getServerClasses()
    {
        return _serverClasses;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the servletHandler.
     */
    public ServletHandler getServletHandler()
    {
        return _servletHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the sessionHandler.
     */
    public SessionHandler getSessionHandler()
    {
        return _sessionHandler;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the systemClasses.
     */
    public String[] getSystemClasses()
    {
        return _systemClasses;
    }
    
    /* ------------------------------------------------------------ */
    public File getTempDirectory()
    {
        if (_tmpDir!=null)
            return _tmpDir;

        // Initialize temporary directory
        //
        // I'm afraid that this is very much black magic.
        // but if you can think of better....
        Object t = getAttribute(ServletHandler.__J_S_CONTEXT_TEMPDIR);

        if (t!=null && (t instanceof File))
        {
            _tmpDir=(File)t;
            if (_tmpDir.isDirectory() && _tmpDir.canWrite())
                return _tmpDir;
        }

        if (t!=null && (t instanceof String))
        {
            try
            {
                _tmpDir=new File((String)t);

                if (_tmpDir.isDirectory() && _tmpDir.canWrite())
                {
                    if(Log.isDebugEnabled())Log.debug("Converted to File "+_tmpDir+" for "+this);
                    setAttribute(ServletHandler.__J_S_CONTEXT_TEMPDIR,_tmpDir);
                    return _tmpDir;
                }
            }
            catch(Exception e)
            {
                Log.warn(Log.EXCEPTION,e);
            }
        }

        // No tempdir so look for a WEB-INF/work directory to use as tempDir base
        File work=null;
        try
        {
            work=new File(System.getProperty("jetty.home"),"work");
            if (!work.exists() || !work.canWrite() || !work.isDirectory())
                work=null;
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }

        // No tempdir set so make one!
        try
        {
            String temp="Jetty_"+getContextPath();
            temp=temp.replace('/','_');
            temp=temp.replace('.','_');
            temp=temp.replace('\\','_');

            
            if (work!=null)
                _tmpDir=new File(work,temp);
            else
            {
                _tmpDir=new File(System.getProperty("java.io.tmpdir"),temp);
                
                if (_tmpDir.exists())
                {
                    if(Log.isDebugEnabled())Log.debug("Delete existing temp dir "+_tmpDir+" for "+this);
                    if (!IO.delete(_tmpDir))
                    {
                        if(Log.isDebugEnabled())Log.debug("Failed to delete temp dir "+_tmpDir);
                    }
                
                    if (_tmpDir.exists())
                    {
                        String old=_tmpDir.toString();
                        _tmpDir=File.createTempFile(temp+"_","");
                        if (_tmpDir.exists())
                            _tmpDir.delete();
                        Log.warn("Can't reuse "+old+", using "+_tmpDir);
                    }
                }
            }

            if (!_tmpDir.exists())
                _tmpDir.mkdir();
            if (work==null)
                _tmpDir.deleteOnExit();
            if(Log.isDebugEnabled())Log.debug("Created temp dir "+_tmpDir+" for "+this);
        }
        catch(Exception e)
        {
            _tmpDir=null;
            Log.ignore(e);
        }

        if (_tmpDir==null)
        {
            try{
                // that didn't work, so try something simpler (ish)
                _tmpDir=File.createTempFile("JettyContext","");
                if (_tmpDir.exists())
                    _tmpDir.delete();
                _tmpDir.mkdir();
                _tmpDir.deleteOnExit();
                if(Log.isDebugEnabled())Log.debug("Created temp dir "+_tmpDir+" for "+this);
            }
            catch(IOException e)
            {
                Log.warn("tmpdir",e); System.exit(1);
            }
        }

        setAttribute(ServletHandler.__J_S_CONTEXT_TEMPDIR,_tmpDir);
        return _tmpDir;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the war as a file or URL string (Resource)
     */
    public String getWar()
    {
        if (_war==null)
            _war=getResourceBase();
        return _war;
    }

    /* ------------------------------------------------------------ */
    public Resource getWebInf() throws IOException
    {
        resolveWebApp();

        // Iw there a WEB-INF directory?
        Resource web_inf= super.getBaseResource().addPath("WEB-INF/");
        if (!web_inf.exists() || !web_inf.isDirectory())
            return null;
        
        return web_inf;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the distributable.
     */
    public boolean isDistributable()
    {
        return _distributable;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the extractWAR.
     */
    public boolean isExtractWAR()
    {
        return _extractWAR;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the java2compliant.
     */
    public boolean isParentLoaderPriority()
    {
        return _parentLoaderPriority;
    }
    
    /* ------------------------------------------------------------ */
    protected void loadConfigurations() 
    	throws Exception
    {
        if (_configurations!=null)
            return;
        if (_configurationClasses==null)
            _configurationClasses=__dftConfigurationClasses;
        
        _configurations = new Configuration[_configurationClasses.length];
        for (int i=0;i<_configurations.length;i++)
        {
            _configurations[i]=(Configuration)Loader.loadClass(this.getClass(), _configurationClasses[i]).newInstance();
        }
    }
    /* ------------------------------------------------------------ */
    /** Resolve Web App directory
     * If the BaseResource has not been set, use the war resource to
     * derive a webapp resource (expanding WAR if required).
     */
    protected void resolveWebApp() throws IOException
    {
        Resource web_app = super.getBaseResource();
        if (web_app == null)
        {
            if (_war==null || _war.length()==0)
                _war=getResourceBase();
            
            // Set dir or WAR
            web_app= Resource.newResource(_war);

            // Accept aliases for WAR files
            if (web_app.getAlias() != null)
            {
                Log.debug(web_app + " anti-aliased to " + web_app.getAlias());
                web_app= Resource.newResource(web_app.getAlias());
            }

            if (Log.isDebugEnabled())
                Log.debug("Try webapp=" + web_app + ", exists=" + web_app.exists() + ", directory=" + web_app.isDirectory());

            // Is the WAR usable directly?
            if (web_app.exists() && !web_app.isDirectory() && !web_app.toString().startsWith("jar:"))
            {
                // No - then lets see if it can be turned into a jar URL.
                Resource jarWebApp= Resource.newResource("jar:" + web_app + "!/");
                if (jarWebApp.exists() && jarWebApp.isDirectory())
                {
                    web_app= jarWebApp;
                    _war= web_app.toString();
                    if (Log.isDebugEnabled())
                        Log.debug(
                            "Try webapp="
                                + web_app
                                + ", exists="
                                + web_app.exists()
                                + ", directory="
                                + web_app.isDirectory());
                }
            }

            // If we should extract or the URL is still not usable
            if (web_app.exists()
                && (!web_app.isDirectory()
                    || (_extractWAR && web_app.getFile() == null)
                    || (_extractWAR && web_app.getFile() != null && !web_app.getFile().isDirectory())))
            {
                // Then extract it.
                File tempDir= new File(getTempDirectory(), "webapp");
                if (tempDir.exists())
                    tempDir.delete();
                tempDir.mkdir();
                tempDir.deleteOnExit();
                Log.info("Extract " + _war + " to " + tempDir);
                JarResource.extract(web_app, tempDir, true);
                web_app= Resource.newResource(tempDir.getCanonicalPath());

                if (Log.isDebugEnabled())
                    Log.debug(
                        "Try webapp="
                            + web_app
                            + ", exists="
                            + web_app.exists()
                            + ", directory="
                            + web_app.isDirectory());
            }

            // Now do we have something usable?
            if (!web_app.exists() || !web_app.isDirectory())
            {
                Log.warn("Web application not found " + _war);
                throw new java.io.FileNotFoundException(_war);
            }

            if (Log.isDebugEnabled())
                Log.debug("webapp=" + web_app);

            // ResourcePath
            super.setBaseResource(web_app);
        }
    }
    

    /* ------------------------------------------------------------ */
    /**
     * @param configurations The configuration class names.  If setConfigurations is not called
     * these classes are used to create a configurations array.
     */
    public void setConfigurationClasses(String[] configurations)
    {
        _configurationClasses = configurations==null?null:(String[])configurations.clone();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param configurations The configurations to set.
     */
    public void setConfigurations(Configuration[] configurations)
    {
        _configurations = configurations==null?null:(Configuration[])configurations.clone();
    }

    /* ------------------------------------------------------------ */
    /**
     * @param defaultsDescriptor The defaultsDescriptor to set.
     */
    public void setDefaultsDescriptor(String defaultsDescriptor)
    {
        _defaultsDescriptor = defaultsDescriptor;
    }
    
    
    /* ------------------------------------------------------------ */
    /**
     * @param distributable The distributable to set.
     */
    public void setDistributable(boolean distributable)
    {
        this._distributable = distributable;
    }

    /* ------------------------------------------------------------ */
    public void setEventListeners(EventListener[] eventListeners)
    {
        if (_sessionHandler!=null)
            _sessionHandler.clearEventListeners();
            
        super.setEventListeners(eventListeners);
      
        for (int i=0; eventListeners!=null && i<eventListeners.length;i ++)
        {
            EventListener listener = eventListeners[i];
            
            if ((listener instanceof HttpSessionActivationListener)
                            || (listener instanceof HttpSessionAttributeListener)
                            || (listener instanceof HttpSessionBindingListener)
                            || (listener instanceof HttpSessionListener))
            {
                if (_sessionHandler!=null)
                    _sessionHandler.addEventListener(listener);
            }
            
        }
    }

    /* ------------------------------------------------------------ */
    /** Add EventListener
     * Conveniance method that calls {@link #setEventListeners(EventListener[])}
     * @param listener
     */
    public void addEventListener(EventListener listener)
    {
        setEventListeners((EventListener[])LazyList.addToArray(getEventListeners(), listener, EventListener.class));   
    }

    
    /* ------------------------------------------------------------ */
    /**
     * @param extractWAR The extractWAR to set.
     */
    public void setExtractWAR(boolean extractWAR)
    {
        _extractWAR = extractWAR;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param java2compliant The java2compliant to set.
     */
    public void setParentLoaderPriority(boolean java2compliant)
    {
        _parentLoaderPriority = java2compliant;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param permissions The permissions to set.
     */
    public void setPermissions(PermissionCollection permissions)
    {
        _permissions = permissions;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param sessionHandler The sessionHandler to set.
     */
    public void setSecurityHandler(SecurityHandler securityHandler)
    {
        _securityHandler = securityHandler;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param serverClasses The serverClasses to set.
     */
    public void setServerClasses(String[] serverClasses) 
    {
        _serverClasses = serverClasses==null?null:(String[])serverClasses.clone();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param servletHandler The servletHandler to set.
     */
    public void setServletHandler(ServletHandler servletHandler)
    {
        _servletHandler = servletHandler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param sessionHandler The sessionHandler to set.
     */
    public void setSessionHandler(SessionHandler sessionHandler)
    {
        _sessionHandler = sessionHandler;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param systemClasses The systemClasses to set.
     */
    public void setSystemClasses(String[] systemClasses)
    {
        _systemClasses = systemClasses==null?null:(String[])systemClasses.clone();
    }
    

    /* ------------------------------------------------------------ */
    /** Set temporary directory for context.
     * The javax.servlet.context.tempdir attribute is also set.
     * @param dir Writable temporary directory.
     */
    public void setTempDirectory(File dir)
    {
        if (isStarted())
            throw new IllegalStateException("Started");

        if (dir!=null)
        {
            try{dir=new File(dir.getCanonicalPath());}
            catch (IOException e){Log.warn(Log.EXCEPTION,e);}
        }

        if (dir!=null && !dir.exists())
        {
            dir.mkdir();
            dir.deleteOnExit();
        }

        if (dir!=null && ( !dir.exists() || !dir.isDirectory() || !dir.canWrite()))
            throw new IllegalArgumentException("Bad temp directory: "+dir);

        _tmpDir=dir;
        setAttribute(ServletHandler.__J_S_CONTEXT_TEMPDIR,_tmpDir);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param war The war to set as a file name or URL
     */
    public void setWar(String war)
    {
        _war = war;
    }
    
    /* ------------------------------------------------------------ */
    protected void startContext()
        throws Exception
    {
        // Configure defaults
        for (int i=0;i<_configurations.length;i++)
            _configurations[i].configureDefaults();
        
        // Is there a WEB-INF work directory
        Resource web_inf=getWebInf();
        if (web_inf!=null)
        {
            Resource work= getWebInf().addPath("work");
            if (work.exists()
                            && work.isDirectory()
                            && work.getFile() != null
                            && work.getFile().canWrite()
                            && getAttribute(ServletHandler.__J_S_CONTEXT_TEMPDIR) == null)
                setAttribute(ServletHandler.__J_S_CONTEXT_TEMPDIR, work.getFile());
        }
        
        // Configure webapp
        for (int i=0;i<_configurations.length;i++)
            _configurations[i].configureWebApp();
        
        _servletHandler.setInitializeAtStart(false);

        // bypass security handler if not used.
        if (_securityHandler.getConstraintMappings()==null ||
            _securityHandler.getConstraintMappings().length==0)
        {
            _sessionHandler.setHandler(_servletHandler);
            _securityHandler.setHandler(null);
        }
        
        super.startContext();

        // OK to Initialize servlet handler now
        if (_servletHandler != null && _servletHandler.isStarted())
            _servletHandler.initialize();
    }
    
    public class WebAppErrorHandler extends ErrorHandler
    {
        Map _errorPages; // code or exception to URL
        
        /* ------------------------------------------------------------ */
        /**
         * @return Returns the errorPages.
         */
        public Map getErrorPages()
        {
            return _errorPages;
        }

        /* ------------------------------------------------------------ */
        /* 
         * @see org.mortbay.jetty.handler.ErrorHandler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
         */
        public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException
        {
            if (_errorPages!=null)
            {
                String error_page= null;
                Class exClass= (Class)request.getAttribute(ServletHandler.__J_S_ERROR_EXCEPTION_TYPE);
                
                if (ServletException.class.equals(exClass))
                {
                    error_page= (String)_errorPages.get(exClass.getName());
                    if (error_page == null)
                    {
                        Throwable th= (Throwable)request.getAttribute(ServletHandler.__J_S_ERROR_EXCEPTION);
                        while (th instanceof ServletException)
                            th= ((ServletException)th).getRootCause();
                        if (th != null)
                            exClass= th.getClass();
                    }
                }
                
                while (error_page == null && exClass != null )
                {
                    error_page= (String)_errorPages.get(exClass.getName());
                    exClass= exClass.getSuperclass();
                }
                
                if (error_page == null)
                {
                    Integer code=(Integer)request.getAttribute(ServletHandler.__J_S_ERROR_STATUS_CODE);
                    if (code!=null)
                        error_page= (String)_errorPages.get(TypeUtil.toString(code.intValue()));
                }
                
                if (error_page!=null)
                {
                    String old_error_page=(String)request.getAttribute(ERROR_PAGE);
                    if (old_error_page==null || !old_error_page.equals(error_page))
                    {
                        request.setAttribute(ERROR_PAGE, error_page);
                        Dispatcher dispatcher = (Dispatcher) getServletHandler().getServletContext().getRequestDispatcher(error_page);
                        try
                        {
                            dispatcher.error(request, response);
                        }
                        catch (ServletException e)
                        {
                            Log.warn(Log.EXCEPTION, e);
                        }
                        return true;
                    }
                }
            }
            
            return super.handle(target, request, response, dispatch);
        }

        /* ------------------------------------------------------------ */
        /**
         * @param errorPages The errorPages to set.
         */
        public void setErrorPages(Map errorPages)
        {
            _errorPages = errorPages;
        }
    }

}

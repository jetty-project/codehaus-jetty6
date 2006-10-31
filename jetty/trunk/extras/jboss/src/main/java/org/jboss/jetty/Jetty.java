/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

// $Id: Jetty.java,v 1.8 2005/04/20 10:40:18 janb Exp $
// A Jetty HttpServer with the interface expected by JBoss'
// J2EEDeployer...
//------------------------------------------------------------------------------
package org.jboss.jetty;

//------------------------------------------------------------------------------

import java.io.CharArrayWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.web.WebApplication;
import org.jboss.web.AbstractWebContainer.WebDescriptorParser;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
//import org.mortbay.j2ee.J2EEWebApplicationContext;
import org.mortbay.jetty.SessionManager;
import org.mortbay.util.MultiException;
import org.mortbay.xml.XmlConfiguration;
import org.w3c.dom.Element;

//------------------------------------------------------------------------------

/**
 * <description>
 * 
 * @author <a href="mailto:jules_gosnell@yahoo..com">Julian Gosnell </a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer </a>.
 * @version $Revision: 1.8 $
 * 
 * <p>
 * <b>Revisions: </b>
 * 
 * <p>
 * <b>20011201 andreas: </b>
 * <ul>
 * <li>Fixed fixURL() because it is to "Unix" centric. Right now the method looks for the last
 * part of the JAR URL (file:/...) which should be the JAR file name and add a "/." before them.
 * Now this should work for Windows as well (the problem with windows was that after "file:" came
 * the DRIVE LETTER which created a wrong URL).
 * </ul>
 */
public class Jetty extends org.mortbay.jetty.Server
{

    protected static final Logger _log = Logger.getLogger("org.jboss.jetty");

    JettyService _service;

    // the XML snippet
    String _xmlConfigString = null;

    // the XML snippet as a DOM element
    Element _configElement = null;

    Jetty(JettyService service)
    {
        super();

        _service = service;

        // check support for JSP compilation...
        if (findResourceInJar("com/sun/tools/javac/v8/resources/javac.properties") == null)
                _log
                        .warn("JSP compilation requires $JAVA_HOME/lib/tools.jar on your JBOSS_CLASSPATH");
    }

    //----------------------------------------
    // class loader delegation policy property
    //----------------------------------------
    boolean _loaderCompliance = true;

    /**
     * @param loaderCompliance if true, Jetty delegates class loading to parent class loader first,
     *            false implies servlet spec 2.3 compliance
     */
    public synchronized void setJava2ClassLoadingCompliance(boolean loaderCompliance)
    {
        _loaderCompliance = loaderCompliance;
    }

    /**
     * @return true if Java2 style class loading delegation, false if servlet2.3 spec compliance
     */
    public synchronized boolean getJava2ClassLoadingCompliance()
    {
        return _loaderCompliance;
    }

    //----------------------------------------
    // unpackWars property
    //----------------------------------------

    boolean _unpackWars = false;

    public synchronized void setUnpackWars(boolean unpackWars)
    {
        _unpackWars = unpackWars;
    }

    public synchronized boolean getUnpackWars()
    {
        return _unpackWars;
    }

    //----------------------------------------
    // webDefault property
    //----------------------------------------

    String _webDefaultResource;

    /**
     * If a webdefault.xml file has been specified in jboss-service.xml then we try and use that.
     * 
     * If we cannot find it, then we will use the one shipped as standard with Jetty and issue a
     * warning.
     * 
     * If the jboss-service.xml file does not specify a custom one, then we again default to the
     * standard one.
     * 
     * @param webDefault
     */
    public synchronized void setWebDefaultResource(String webDefaultResource)
    {
        if (webDefaultResource != null)
        {
            URL webDefaultURL = findResourceInJar(webDefaultResource);
            if (webDefaultURL != null)
                _webDefaultResource = fixURL(webDefaultURL.toString());
            else
            {
                _webDefaultResource = null;
                _log.warn("Cannot find resource for " + webDefaultResource + ": using default");
            }
        }
        else
            _webDefaultResource = null;

        if (_log.isDebugEnabled())
                _log.debug("webdefault specification is: " + _webDefaultResource);
    }

    public synchronized String getWebDefaultResource()
    {
        return _webDefaultResource;
    }

    //----------------------------------------
    // subjectAttributeName property
    //----------------------------------------

    String _subjectAttributeName;

    public synchronized void setSubjectAttributeName(String subjectAttributeName)
    {
        _subjectAttributeName = subjectAttributeName;
    }

    public synchronized String getSubjectAttributeName()
    {
        return _subjectAttributeName;
    }

    //----------------------------------------
    // configuration property
    //----------------------------------------

    public Element getConfigurationElement()
    {
        return _configElement;
    }

    /**
     * @param configElement XML fragment from jboss-service.xml
     */
    public void setConfigurationElement(Element configElement)
    {

        // convert to an xml string to pass into Jetty's normal
        // configuration mechanism
        _configElement = configElement;

        try
        {
            DOMSource source = new DOMSource(configElement);

            CharArrayWriter writer = new CharArrayWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            _xmlConfigString = writer.toString();

            // get rid of the first line, as this will be prepended by
            // the XmlConfiguration
            int index = _xmlConfigString.indexOf("?>");
            if (index >= 0)
            {
                index += 2;

                while ((_xmlConfigString.charAt(index) == '\n')
                        || (_xmlConfigString.charAt(index) == '\r'))
                    index++;
            }

            _xmlConfigString = _xmlConfigString.substring(index);

            if (_log.isDebugEnabled())
                    _log.debug("Passing xml config to jetty:\n" + _xmlConfigString);

            setXMLConfiguration(_xmlConfigString);

        }
        catch (TransformerConfigurationException tce)
        {
            _log.error("Can't transform config Element -> xml:", tce);
        }
        catch (TransformerException te)
        {
            _log.error("Can't transform config Element -> xml:", te);
        }
        catch (Exception e)
        {
            _log.error("Unexpected exception converting configuration Element -> xml", e);
        }
    }

    /*
     * Actually perform the configuration @param xmlString
     */
    private void setXMLConfiguration(String xmlString)
    {

        try
        {
            XmlConfiguration xmlConfigurator = new XmlConfiguration(xmlString);
            xmlConfigurator.configure(this);
        }
        catch (Exception e)
        {
            _log.error("problem configuring Jetty:", e);
        }
    }

    //----------------------------------------------------------------------------
    // 'deploy' interface
    //----------------------------------------------------------------------------

    Hashtable _deployed = new Hashtable(); // use Hashtable because is is synchronised

    public WebApplication deploy(WebApplication wa, String warUrl,
            WebDescriptorParser descriptorParser) throws DeploymentException
    {
        String contextPath = wa.getMetaData().getContextRoot();
        try
        {
            wa.setURL(new URL(warUrl));

            // check whether the context already exists... - a bit hacky,
            // could be nicer...
            WebAppContext[] contexts = (WebAppContext[])getChildHandlersByClass(WebAppContext.class);
            
            for (int i=0; i<contexts.length;i++)
            {
                if (contexts[i].getContextPath().equals(contextPath))
                    _log.warn("A WebApplication is already deployed in context '" + contextPath
                            + "' - proceed at your own risk.");
            	
            }

            // deploy the WebApp
            WebAppContext app = 
                new JBossWebApplicationContext(this, descriptorParser, wa, warUrl);
            app.setContextPath(contextPath);

//            SessionManager manager = getDistributableSessionManagerPrototype();
//            if (manager != null)
//            {
//                app.setDistributableSessionManager((SessionManager) manager.clone());
//                if (getForceDistributable()) app.setDistributable(true);
//            }

            // configure whether the context is to flatten the classes in
            // the WAR or not
            app.setExtractWAR(getUnpackWars());

            app.setParentLoaderPriority(getJava2ClassLoadingCompliance());
            
            // if a different webdefault.xml file has been provided, use it
            if (getWebDefaultResource() != null)
                    app.setDefaultsDescriptor(getWebDefaultResource());

            Iterator hosts = wa.getMetaData().getVirtualHosts();
            List hostList = new ArrayList();
            
            while(hosts.hasNext())
            	hostList.add((String)hosts.next());
                
            app.setVirtualHosts((String[])hostList.toArray(new String[hostList.size()]));
            
            // Add the webapp
            //addContext(app);
            setHandler(app);
            // keep track of deployed contexts for undeployment
            _deployed.put(warUrl, app);

            try
            {
                // finally start the app
                app.start();
  
                
                _log.info("successfully deployed " + warUrl + " to " + contextPath);
            }
            catch (MultiException me)
            {
                _log.warn("problem deploying " + warUrl + " to " + contextPath);
                for (int i = 0; i < me.size(); i++)
                {
                    Exception e = (Exception)me.getThrowable(i);
                    _log.warn(e, e);
                }
            }

        }
        catch (DeploymentException e)
        {
            _log.error("Undeploying on start due to error", e);
            undeploy(warUrl);
            throw e;
        }
        catch (Exception e)
        {
            _log.error("Undeploying on start due to error", e);
            undeploy(warUrl);
            throw new DeploymentException(e);
        }

        return wa;
    }

    public void undeploy(String warUrl) throws DeploymentException
    {
        // find the WebApp Context in the repository
        JBossWebApplicationContext app = (JBossWebApplicationContext) _deployed.get(warUrl);

        if (app == null)
        {
            _log.warn("app (" + warUrl + ") not currently deployed");
        }
        else
        {
            try
            {
                app.stop();
                removeHandler(app);
                app = null;

                _log.info("Successfully undeployed " + warUrl);
            }
            catch (Exception e)
            {
                throw new DeploymentException(e);
            }
	    finally
	    {
	       _deployed.remove(warUrl);
	    }
        }

    }

    public boolean isDeployed(String warUrl)
    {
        return (_deployed.get(warUrl) != null);
    }

    //----------------------------------------------------------------------------
    // Utils
    //----------------------------------------------------------------------------

    public URL findResourceInJar(String name)
    {
        URL url = null;

        try
        {
            url = getClass().getClassLoader().getResource(name);
        }
        catch (Exception e)
        {
            _log.error("Could not find resource: " + name, e);
        }

        return url;
    }

    // work around broken JarURLConnection caching...
    static String fixURL(String url)
    {
        // Get the separator of the JAR URL and the file reference
        int index = url.indexOf('!');
        if (index >= 0)
        {
            index = url.lastIndexOf('/', index);
        }
        else
        {
            index = url.lastIndexOf('/');
        }
        // Now add a "./" before the JAR file to add a different path
        if (index >= 0)
        {
            return url.substring(0, index) + "/." + url.substring(index);
        }
        else
        {
            // Now forward slash found then there is severe problem with
            // the URL but here we just ignore it
            return url;
        }
    }

    public String[] getCompileClasspath(ClassLoader cl)
    {
        return _service.getCompileClasspath(cl);
    }

//    /**
//     * Override service method to allow ditching of security info after a request has been
//     * processed
//     * 
//     * @param request
//     * @param response
//     * @return @exception IOException
//     * @exception HttpException
//     */
//    public Context service(Request request, Response response) throws IOException,
//            HttpException
//    {
//        try
//        {
//            return super.service(request, response);
//        }
//        finally
//        {
//            // Moved to JBossUserRealm.deAuthenticate(UserPrincipal);
//            // SecurityAssociation.setPrincipal(null);
//            // SecurityAssociation.setCredential(null);
//        }
//    }

    //----------------------------------------------------------------------------
    // DistributedHttpSession support
    //----------------------------------------------------------------------------

    protected SessionManager _distributableSessionManagerPrototype;

    public void setDistributableSessionManagerPrototype(SessionManager manager)
    {
        _distributableSessionManagerPrototype = manager;
    }

    public SessionManager getDistributableSessionManagerPrototype()
    {
        return _distributableSessionManagerPrototype;
    }

    protected boolean _forceDistributable = false;

    public boolean getForceDistributable()
    {
        return _forceDistributable;
    }

    public void setForceDistributable(boolean distributable)
    {
        _forceDistributable = distributable;
    }

    //----------------------------------------------------------------------------

    protected boolean _supportJSR77 = true;

    public boolean getSupportJSR77()
    {
        return _supportJSR77;
    }

    public void setSupportJSR77(boolean graceful)
    {
        _supportJSR77 = graceful;
    }

    //----------------------------------------------------------------------------

    protected boolean _stopWebApplicationsGracefully = false;

    public boolean getStopWebApplicationsGracefully()
    {
        return _stopWebApplicationsGracefully;
    }

    public void setStopWebApplicationsGracefully(boolean graceful)
    {
        _stopWebApplicationsGracefully = graceful;
    }
}

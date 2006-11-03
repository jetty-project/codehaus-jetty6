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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.logging.Logger;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;

//import org.mortbay.j2ee.J2EEWebApplicationContext;
import org.mortbay.jetty.SessionManager;
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

    /**
     * the XML snippet
     */ 
    String _xmlConfigString = null;

    /**
     * the XML snippet as a DOM element
     */ 
    Element _configElement = null;

    //TODO move these to JettyDeployer?
    protected boolean _stopWebApplicationsGracefully = false;
    protected boolean _forceDistributable = false;
    protected SessionManager _distributableSessionManagerPrototype;
    
    Jetty(JettyService service)
    {
        super();
        _service = service;
    }

 
    public HandlerCollection getContextHandlerCollection ()
    {
        return (HandlerCollection)getChildHandlerByClass(ContextHandlerCollection.class);
    }

  


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

    public void setDistributableSessionManagerPrototype(SessionManager manager)
    {
        _distributableSessionManagerPrototype = manager;
    }

    public SessionManager getDistributableSessionManagerPrototype()
    {
        return _distributableSessionManagerPrototype;
    }

    public boolean getForceDistributable()
    {
        return _forceDistributable;
    }

    public void setForceDistributable(boolean distributable)
    {
        _forceDistributable = distributable;
    }


    public boolean getStopWebApplicationsGracefully()
    {
        return _stopWebApplicationsGracefully;
    }

    public void setStopWebApplicationsGracefully(boolean graceful)
    {
        _stopWebApplicationsGracefully = graceful;
    }
}

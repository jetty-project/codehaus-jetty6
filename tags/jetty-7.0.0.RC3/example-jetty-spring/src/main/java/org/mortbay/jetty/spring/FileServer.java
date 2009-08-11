package org.mortbay.jetty.spring;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.UrlResource;


/* ------------------------------------------------------------ */
/** Run Jetty File server from Spring configuration.
 * @see http://svn.codehaus.org/jetty/jetty/trunk/example-jetty-spring/src/main/resources/fileserver-spring.xml
 */
public class FileServer
{
    public static void main(String[] args) throws Exception
    {
        Resource config = Resource.newSystemResource(args.length == 1?args[0]:"fileserver-spring.xml");
        XmlBeanFactory bf = new XmlBeanFactory(new UrlResource(config.getURL()));
        Server server = (Server)bf.getBean(args.length == 2?args[1]:"Server");
        server.join();
    }
}

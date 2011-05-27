package org.mortbay.jetty.spring;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;


/* ------------------------------------------------------------ */
/** Run Jetty from Spring configuration.
 * @see <a href="http://svn.codehaus.org/jetty/jetty/trunk/jetty-spring/src/main/config/etc/jetty-spring.xml">jetty-spring.xml</a>
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        Resource config = Resource.newResource(args.length == 1?args[0]:"src/main/config/etc/jetty-spring.xml");
        XmlConfiguration.main(new String[]{config.getFile().getAbsolutePath()});
        
    }
}

package org.mortbay.jetty.server.nio;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;

import org.mortbay.jetty.util.log.Log;

/**
 * An implementation of the SelectChannelConnector which first tries to  
 * inherit from a channel provided by the system. If there is no inherited
 * channel available, or if the inherited channel provided not usable, then 
 * it will fall back upon normal ServerSocketChannel creation.
 * <p> 
 * Note that System.inheritedChannel() is only available from Java 1.5 onwards.
 * Trying to use this class under Java 1.4 will be the same as using a normal
 * SelectChannelConnector. 
 * <p> 
 * Use it with xinetd/inetd, to launch an instance of Jetty on demand. The port
 * used to access pages on the Jetty instance is the same as the port used to
 * launch Jetty. 
 * 
 * @author athena
 */
public class InheritedChannelConnector extends SelectChannelConnector
{
    /* ------------------------------------------------------------ */
    public void open() throws IOException
    {
        synchronized(this)
        {
            try 
            {
                Channel channel = System.inheritedChannel();
                if ( channel instanceof ServerSocketChannel )
                    _acceptChannel = (ServerSocketChannel)channel;
                else
                    Log.warn("Unable to use System.inheritedChannel() [" +channel+ "]. Trying a new ServerSocketChannel at " + getHost() + ":" + getPort());
                
                if ( _acceptChannel != null )
                    _acceptChannel.configureBlocking(false);
            }
            catch(NoSuchMethodError e)
            {
                Log.warn("Need at least Java 5 to use socket inherited from xinetd/inetd.");
            }

            if (_acceptChannel == null)
                super.open();
        }
    }

}

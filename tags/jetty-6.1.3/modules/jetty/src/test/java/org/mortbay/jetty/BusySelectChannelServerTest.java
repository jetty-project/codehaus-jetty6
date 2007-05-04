package org.mortbay.jetty;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.mortbay.io.Buffer;
import org.mortbay.io.View;
import org.mortbay.io.nio.SelectChannelEndPoint;
import org.mortbay.io.nio.SelectorManager.SelectSet;
import org.mortbay.jetty.nio.SelectChannelConnector;

/**
 * HttpServer Tester.
 */
public class BusySelectChannelServerTest extends HttpServerTestBase
{
    public BusySelectChannelServerTest()
    {
        super(new SelectChannelConnector()
        {
            /* ------------------------------------------------------------ */
            /* (non-Javadoc)
             * @see org.mortbay.jetty.nio.SelectChannelConnector#newEndPoint(java.nio.channels.SocketChannel, org.mortbay.io.nio.SelectorManager.SelectSet, java.nio.channels.SelectionKey)
             */
            protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectSet selectSet, SelectionKey key) throws IOException
            {
                return new ConnectorEndPoint(channel,selectSet,key)
                {

                    int c;
                    
                    /* ------------------------------------------------------------ */
                    /* (non-Javadoc)
                     * @see org.mortbay.io.nio.SelectChannelEndPoint#flush(org.mortbay.io.Buffer, org.mortbay.io.Buffer, org.mortbay.io.Buffer)
                     */
                    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
                    {
                        int x=c++&0xff;
                        if (x<16)
                            return 0;
                        if (x<128)
                            return flush(header);
                        return super.flush(header,buffer,trailer);
                    }

                    /* ------------------------------------------------------------ */
                    /* (non-Javadoc)
                     * @see org.mortbay.io.nio.SelectChannelEndPoint#flush(org.mortbay.io.Buffer)
                     */
                    public int flush(Buffer buffer) throws IOException
                    {
                        int x=c++&0xff;
                        if (x<16)
                            return 0;
                        if (x<96)
                        {
                            View v = new View(buffer);
                            v.setPutIndex(v.getIndex()+1);
                            int l=super.flush(v);
                            if (l>0)
                                buffer.skip(l);
                            return l;
                        }
                        return super.flush(buffer);
                    }
                    
                };
            }
            
        });
    }   
}
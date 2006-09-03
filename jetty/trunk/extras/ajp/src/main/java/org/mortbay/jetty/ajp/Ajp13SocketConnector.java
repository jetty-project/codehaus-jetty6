package org.mortbay.jetty.ajp;

import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;

/**
 * @author Markus Kobler
 */
public class Ajp13SocketConnector extends SocketConnector {


    private int _bufferSize = Ajp13Packet.MAX_DATA_SIZE;


    public int getBufferSize() {
        return _bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        _bufferSize = bufferSize;
        if (_bufferSize > Ajp13Packet.MAX_DATA_SIZE)
            Log.warn("AJP Data buffer > " + Ajp13Packet.MAX_DATA_SIZE + ": " + bufferSize);
    }

    protected void doStart() throws Exception {
        Log.warn("The Ajp13SocketConnector is a pre-ALPHA work in progress!!!!");
        Log.info("AJP13 is not a secure protocol. Please protect port {}", Integer.toString(getPort()));
        super.doStart();
    }

    protected HttpConnection newConnection(EndPoint endpoint) {
        return new Ajp13Connection(this, endpoint, getServer(), _bufferSize);
    }

    // Secured on a packet by packet bases not by connection
    public boolean isConfidential(Request request) {
        throw new UnsupportedOperationException();
    }

    // Secured on a packet by packet bases not by connection
    public boolean isIntegral(Request request) {
        throw new UnsupportedOperationException();
    }

    
    /* TODO temp main - just to help testing */
    public static void main(String[] args)
        throws Exception
    {
        Server server = new Server();
        Ajp13SocketConnector connector=new Ajp13SocketConnector(); 
        
        connector.setPort(8443);
        server.setConnectors(new Connector[]{connector});
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler()});
        server.setHandler(handlers);
        
        HashUserRealm userRealm = new HashUserRealm();
        userRealm.setName("Test Realm");
        userRealm.setConfig("./etc/realm.properties");
        server.setUserRealms(new UserRealm[]{userRealm});
        
        WebAppContext.addWebApplications(server,"webapps","etc/webdefault.xml",false,false);
        
        server.start();
        server.join();
    }


}

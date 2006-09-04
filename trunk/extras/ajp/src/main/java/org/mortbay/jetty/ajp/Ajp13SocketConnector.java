//========================================================================
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
 * @author Greg Wilkins
 * @author Markus Kobler  markus(at)inquisitive-mind.com
 * 
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

    protected HttpConnection newHttpConnection(EndPoint endpoint) {
        System.err.println("New HTTP Connection "+endpoint);
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
        
        connector.setPort(8009);
        server.setConnectors(new Connector[]{connector});
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler()});
        server.setHandler(handlers);
        
        HashUserRealm userRealm = new HashUserRealm();
        userRealm.setName("Test Realm");
        userRealm.setConfig("../../etc/realm.properties");
        server.setUserRealms(new UserRealm[]{userRealm});
        
        WebAppContext.addWebApplications(server,"../../webapps","../../etc/webdefault.xml",false,false);
        
        server.start();
        server.join();
    }


}

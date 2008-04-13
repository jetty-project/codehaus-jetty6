// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.mortbay.jetty.handler.rewrite;

import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.mortbay.io.bio.StringEndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.LocalConnector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;

public abstract class AbstractRuleTestCase extends TestCase
{
    protected Server _server=new Server();
    protected LocalConnector _connector=new LocalConnector();
    protected StringEndPoint _endpoint=new StringEndPoint();
    
    protected Request _request;
    protected Response _response;
    
    
    public void setUp() throws Exception
    {
        _server.setConnectors(new Connector[]{_connector});
        _server.start();
        reset();
    }
    
    public void tearDown() throws Exception
    {
        _server.stop();
        _request = null;
        _response = null;
    }
    
    public void reset()
    {
        HttpConnection connection=new HttpConnection(_connector,_endpoint,_server);
        _request = new Request();
        _response = new Response(connection);
        
        _request.setRequestURI("/test/");
    }
}

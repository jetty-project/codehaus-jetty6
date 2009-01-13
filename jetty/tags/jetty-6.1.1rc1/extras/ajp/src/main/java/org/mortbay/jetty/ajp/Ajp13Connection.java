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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpStatus;
import org.mortbay.log.Log;
import org.mortbay.util.URIUtil;
import org.mortbay.util.ajax.Continuation;

/**
 * Connection implementation of the Ajp13 protocol. <p/> XXX Refactor to remove
 * duplication of HttpConnection
 * 
 * @author Markus Kobler markus(at)inquisitive-mind.com
 * @author Greg Wilkins
 */
public class Ajp13Connection extends HttpConnection
{
    private boolean _sslSecure = false;

    public Ajp13Connection(Connector connector, EndPoint endPoint, Server server)
    {
        super(connector, endPoint, server);
        _request = new Ajp13Request(this);
        _generator = new Ajp13Generator(_connector, _endp, _connector.getHeaderBufferSize(), _connector.getResponseBufferSize());
        _parser = new Ajp13Parser(_connector, _endp, new RequestHandler(), (Ajp13Generator) _generator);
        _generator.setSendServerVersion(server.getSendServerVersion());
        _server = server;

    }

    public boolean isConfidential(Request request)
    {
        return _sslSecure;
    }

    public boolean isIntegral(Request request)
    {
        return _sslSecure;
    }

    public ServletInputStream getInputStream()
    {
        if (_in == null)
            _in = new Ajp13Parser.Input((Ajp13Parser) _parser, _connector.getMaxIdleTime());
        return _in;
    }

    private class RequestHandler implements Ajp13Parser.EventHandler
    {
        boolean _delayedHandling = false;

        public void startForwardRequest() throws IOException
        {
            _delayedHandling = false;
            _uri.clear();
            _sslSecure = false;
            _request.setTimeStamp(System.currentTimeMillis());
            _request.setUri(_uri);
        }

        public void parsedMethod(Buffer method) throws IOException
        {
            _request.setMethod(method.toString());
        }

        public void parsedUri(Buffer uri) throws IOException
        {
            // TODO avoid this copy.
            _uri.parse(uri.asArray(), 0, uri.length());
        }

        public void parsedProtocol(Buffer protocol) throws IOException
        {
            if (protocol != null && protocol.length()>0)
            {
                _request.setProtocol(protocol.toString());
            }
        }

        public void parsedRemoteAddr(Buffer addr) throws IOException
        {
            if (addr != null && addr.length()>0)
            {
                ((Ajp13Request) _request).setRemoteAddr(addr.toString());
            }
        }

        public void parsedRemoteHost(Buffer name) throws IOException
        {
            if (name != null && name.length()>0)
            {
                ((Ajp13Request) _request).setRemoteHost(name.toString());
            }
        }

        public void parsedServerName(Buffer name) throws IOException
        {
            if (name != null && name.length()>0)
            {
                _request.setServerName(name.toString());
            }
        }

        public void parsedServerPort(int port) throws IOException
        {
            ((Ajp13Request) _request).setServerPort(port);
        }

        public void parsedSslSecure(boolean secure) throws IOException
        {
            _sslSecure = secure;
        }

        public void parsedQueryString(Buffer value) throws IOException
        {
            String u = _uri + "?" + value;
            _uri.parse(u);
        }

        public void parsedHeader(Buffer name, Buffer value) throws IOException
        {
            _requestFields.add(name, value);
        }

        public void parsedRequestAttribute(String key, Buffer value) throws IOException
        {
            _request.setAttribute(key, value.toString());
        }

        public void headerComplete() throws IOException
        {
            if (((Ajp13Parser) _parser).getContentLength() <= 0)
            {
                handleRequest();
            }
            else
            {
                _delayedHandling = true;
            }
        }

        public void messageComplete(long contextLength) throws IOException
        {
        }

        public void content(Buffer ref) throws IOException
        {
            if (_delayedHandling)
            {
                _delayedHandling = false;
                handleRequest();
            }
        }

    }

}
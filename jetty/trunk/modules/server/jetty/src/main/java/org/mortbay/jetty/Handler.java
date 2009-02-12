//========================================================================
//$Id: Handler.java,v 1.1 2005/10/05 14:09:21 janb Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.util.component.LifeCycle;


public interface Handler extends LifeCycle
{
    /* ------------------------------------------------------------ */
    /** Handle a request.
     * @param target The target of the request - either a URI or a name.
     * @param request The request either as the {@link Request}
     * object or a wrapper of that request. The {@link HttpConnection#getCurrentConnection()} 
     * method can be used access the Request object if required.
     * @param response The response as the {@link Response}
     * object or a wrapper of that request. The {@link HttpConnection#getCurrentConnection()} 
     * method can be used access the Response object if required.
     * @throws IOException
     * @throws ServletException
     */
    public void handle(String target, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException;
    
    public void setServer(Server server);
    public Server getServer();
    
    public void destroy();
    
}


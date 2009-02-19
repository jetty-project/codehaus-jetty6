// ========================================================================
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
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


package org.mortbay.jetty.security;

import java.util.Map;

import javax.security.auth.Subject;

import org.mortbay.jetty.server.AuthenticationManager;
import org.mortbay.jetty.server.handler.SecurityHandler;
import org.mortbay.jetty.util.component.AbstractLifeCycle;

public abstract class AbstractAuthenticationManager extends AbstractLifeCycle implements AuthenticationManager<ServerAuthentication>
{
    private SecurityHandler _securityHandler;
    //for built-in auth methods
    private String _method;
    private boolean _lazy = false; //You must configure lazy auth if you know it works.
    private String _errorPage; //only for form auth
    private String _loginPage; //only for form auth
    //for jaspi configuration
    private String serverName;
    private String contextRoot;
    private Map authConfigProperties;
    private Subject serviceSubject;


    public String getAuthMethod()
    {
        return _method;
    }

    public void setAuthMethod(String method)
    {
        _method = method;
    }

    public boolean getAllowLazyAuth()
    {
        return _lazy;
    }

    public void setAllowLazyAuth(boolean lazy)
    {
        _lazy = lazy;
    }

    public String getErrorPage()
    {
        return _errorPage;
    }

    public String getLoginPage()
    {
        return _loginPage;
    }

    public void setErrorPage(String errorPage)
    {
        _errorPage = errorPage;  
    }

    public void setLoginPage(String loginPage)
    {
        _loginPage = loginPage;        
    }
    
    public SecurityHandler getSecurityHandler()
    {
       return _securityHandler;
    }

    public void setSecurityHandler(SecurityHandler handler)
    {
        _securityHandler = handler;  
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public Map getAuthConfigProperties() {
         return authConfigProperties;
     }

     public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public void setAuthConfigProperties(Map authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    public Subject getServiceSubject() {
        return serviceSubject;
    }

    public void setServiceSubject(Subject serviceSubject) {
        this.serviceSubject = serviceSubject;
    }

}

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

package org.mortbay.jetty;

import java.util.Map;

import javax.security.auth.Subject;

import org.mortbay.jetty.handler.SecurityHandler;
import org.mortbay.jetty.util.component.LifeCycle;

public interface AuthenticationManager<T> extends LifeCycle
{
    void setAuthMethod (String method);
    String getAuthMethod ();
    
    void setAllowLazyAuth (boolean lazy);
    boolean getAllowLazyAuth ();
    
    void setLoginPage (String loginPage);
    String getLoginPage ();
    
    void setErrorPage (String errorPage);
    String getErrorPage ();
    
    void setSecurityHandler (SecurityHandler handler);

    SecurityHandler getSecurityHandler ();

    String getServerName();

    void setServerName(String serverName);

    String getContextRoot();

    void setContextRoot(String contextRoot);

    Map getAuthConfigProperties();

    void setAuthConfigProperties(Map authConfigProperties);

    Subject getServiceSubject();

    void setServiceSubject(Subject serviceSubject);

    T getServerAuthentication();
}

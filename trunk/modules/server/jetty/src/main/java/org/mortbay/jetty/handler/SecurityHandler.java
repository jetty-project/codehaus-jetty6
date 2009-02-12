
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


package org.mortbay.jetty.handler;

import org.mortbay.jetty.AuthenticationManager;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.RunAsToken;
import org.mortbay.jetty.UserRealm;
import org.mortbay.jetty.util.component.LifeCycle;


/**
 * @version $Rev$ $Date$
 */
public interface SecurityHandler extends LifeCycle, Handler, HandlerContainer
{
    void setHandler(Handler handler);

    Handler getHandler();

    RunAsToken newRunAsToken(String runAsRole);

    void setAuthenticationManager (AuthenticationManager authManager);
    AuthenticationManager getAuthenticationManager();
    
    UserRealm getUserRealm ();
    void setUserRealm (UserRealm realm);
}

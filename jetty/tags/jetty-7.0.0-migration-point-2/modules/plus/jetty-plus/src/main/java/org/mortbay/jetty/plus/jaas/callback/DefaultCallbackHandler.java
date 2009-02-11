// ========================================================================
// $Id$
// Copyright 2003-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.jaas.callback;

import java.io.IOException;
import java.util.Arrays;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.Password;



/* ---------------------------------------------------- */
/** DefaultUsernameCredentialCallbackHandler
 * <p>
 *
 * <p><h4>Notes</h4>
 * <p>
 *
 * <p><h4>Usage</h4>
 * <pre>
 */
/*
 * </pre>
 *
 * @see
 * @version 1.0 Tue Apr 15 2003
 * @author Jan Bartel (janb)
 */
public class DefaultCallbackHandler extends AbstractCallbackHandler
{
     
    private Request _request;
    
    public void setRequest (Request request)
    {
        this._request = request;
    }
    
    public void handle (Callback[] callbacks)
        throws IOException, UnsupportedCallbackException
    {
        for (int i=0; i < callbacks.length; i++)
        {
            if (callbacks[i] instanceof NameCallback)
            {
                ((NameCallback)callbacks[i]).setName(getUserName());
            }
            else if (callbacks[i] instanceof ObjectCallback)
            {
                ((ObjectCallback)callbacks[i]).setObject(getCredential());
            }
            else if (callbacks[i] instanceof PasswordCallback)
            {
                if (getCredential() instanceof Password)
                    ((PasswordCallback)callbacks[i]).setPassword (((Password)getCredential()).toString().toCharArray());
                else if (getCredential() instanceof String)
                {
                    ((PasswordCallback)callbacks[i]).setPassword (((String)getCredential()).toCharArray());
                }
                else
                    throw new UnsupportedCallbackException (callbacks[i], "User supplied credentials cannot be converted to char[] for PasswordCallback: try using an ObjectCallback instead");
            }
            else if (callbacks[i] instanceof RequestParameterCallback)
            {
                RequestParameterCallback callback = (RequestParameterCallback)callbacks[i];
                callback.setParameterValues(Arrays.asList(_request.getParameterValues(callback.getParameterName())));
            }
            else
                throw new UnsupportedCallbackException(callbacks[i]);
        }
        
    }
    
}
        

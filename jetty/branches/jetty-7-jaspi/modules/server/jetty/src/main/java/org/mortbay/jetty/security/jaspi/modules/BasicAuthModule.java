/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.mortbay.jetty.security.jaspi.modules;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.security.B64Code;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.log.Log;
import org.mortbay.util.StringUtil;

/**
 * @version $Rev:$ $Date:$
 */
public class BasicAuthModule extends BaseAuthModule
{

    private String realmName;
    private static final String REALM_KEY = "org.mortbay.jetty.security.jaspi.modules.RealmName";

    public BasicAuthModule()
    {
    }

    public BasicAuthModule(CallbackHandler callbackHandler, LoginService loginService, String realmName)
    {
        super(callbackHandler, loginService);
        this.realmName = realmName;
    }

    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException
    {
        super.initialize(requestPolicy, responsePolicy, handler, options);
        realmName = (String) options.get(REALM_KEY);
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException
    {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        String credentials = request.getHeader(HttpHeaders.AUTHORIZATION);

        try
        {
            if (credentials!=null )
            {
                    if(Log.isDebugEnabled())Log.debug("Credentials: "+credentials);
                    credentials = credentials.substring(credentials.indexOf(' ')+1);
                    credentials = B64Code.decode(credentials, StringUtil.__ISO_8859_1);
                    int i = credentials.indexOf(':');
                    final String username = credentials.substring(0,i);
                    final char[] password = new char[credentials.length() - (i+1)];
                            credentials.getChars(i+1, credentials.length(), password, 0);//substring(i+1);
                    CallbackHandler loginCallbackHandler = new CallbackHandler()
                    {

                        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
                        {
                            for (Callback callback: callbacks)
                            {
                                if (callback instanceof NameCallback)
                                {
                                    ((NameCallback)callback).setName(username);
                                }
                                else if (callback instanceof PasswordCallback)
                                {
                                    ((PasswordCallback)callback).setPassword(password);
                                }
                            }
                        }
                    };
                    LoginResult loginResult = loginService.login(clientSubject,loginCallbackHandler);
                    //TODO what should happen if !isMandatory but credentials exist and are wrong?
                    if (loginResult.isSuccess())
                    {
                        callbackHandler.handle(new Callback[] {loginResult.getCallerPrincipalCallback(), loginResult.getGroupPrincipalCallback()});
                        messageInfo.getMap().put(JettyMessageInfo.AUTH_METHOD_KEY, Constraint.__BASIC_AUTH);
                        return AuthStatus.SUCCESS;
                    }

            }

            if (!isMandatory(messageInfo))
            {
                return AuthStatus.SUCCESS;
            }
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "basic realm=\""+realmName+'"');
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthStatus.SEND_CONTINUE;
        }
        catch (IOException e)
        {
            throw new AuthException(e.getMessage());
        }
        catch (UnsupportedCallbackException e)
        {
            throw new AuthException(e.getMessage());
        }

    }
}

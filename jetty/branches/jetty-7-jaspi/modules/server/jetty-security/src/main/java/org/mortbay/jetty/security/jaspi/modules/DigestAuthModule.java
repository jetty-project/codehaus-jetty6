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
import java.security.MessageDigest;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.LoginService;
import org.mortbay.jetty.LoginCredentials;
import org.mortbay.jetty.LoginResult;
import org.mortbay.jetty.security.B64Code;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.Credential;
import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.log.Log;
import org.mortbay.util.QuotedStringTokenizer;
import org.mortbay.util.StringUtil;
import org.mortbay.util.TypeUtil;

/**
 * @version $Rev$ $Date$
 */
public class DigestAuthModule extends BaseAuthModule
{

    protected long maxNonceAge=0;
    protected long nonceSecret=this.hashCode() ^ System.currentTimeMillis();
    protected boolean useStale=false;

    private String realmName;
    private static final String REALM_KEY = "org.mortbay.jetty.security.jaspi.modules.RealmName";

    public DigestAuthModule()
    {
    }

    public DigestAuthModule(CallbackHandler callbackHandler, LoginService loginService, String realmName)
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
            boolean stale = false;
            //TODO extract from request
            long timestamp = System.currentTimeMillis();
            if (credentials!=null )
            {
                    if(Log.isDebugEnabled())Log.debug("Credentials: "+credentials);
                if(Log.isDebugEnabled())Log.debug("Credentials: "+credentials);
                QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(credentials,
                                                                            "=, ",
                                                                            true,
                                                                            false);
                final Digest digest=new Digest(request.getMethod());
                String last=null;
                String name=null;

                while (tokenizer.hasMoreTokens())
                {
                    String tok = tokenizer.nextToken();
                    char c=(tok.length()==1)?tok.charAt(0):'\0';

                    switch (c)
                    {
                      case '=':
                          name=last;
                          last=tok;
                          break;
                      case ',':
                          name=null;
                      case ' ':
                          break;

                      default:
                          last=tok;
                          if (name!=null)
                          {
                              if ("username".equalsIgnoreCase(name))
                                  digest.username=tok;
                              else if ("realm".equalsIgnoreCase(name))
                                  digest.realm=tok;
                              else if ("nonce".equalsIgnoreCase(name))
                                  digest.nonce=tok;
                              else if ("nc".equalsIgnoreCase(name))
                                  digest.nc=tok;
                              else if ("cnonce".equalsIgnoreCase(name))
                                  digest.cnonce=tok;
                              else if ("qop".equalsIgnoreCase(name))
                                  digest.qop=tok;
                              else if ("uri".equalsIgnoreCase(name))
                                  digest.uri=tok;
                              else if ("response".equalsIgnoreCase(name))
                                  digest.response=tok;
                              break;
                          }
                    }
                }

                int n=checkNonce(digest.nonce,timestamp);

                if (n > 0)
                {
                    LoginCredentials loginCredentials = new UserPasswordLoginCredentials(digest.username,digest.toString().toCharArray());
                    LoginResult loginResult = loginService.login(clientSubject, loginCredentials);
                    //TODO what should happen if !isMandatory but credentials exist and are wrong?
                    if (loginResult.isSuccess())
                    {
                        callbackHandler.handle(new Callback[] {loginResult.getCallerPrincipalCallback(), loginResult.getGroupPrincipalCallback()});
                        messageInfo.getMap().put(JettyMessageInfo.AUTH_METHOD_KEY, Constraint.__DIGEST_AUTH);
                        return AuthStatus.SUCCESS;
                    }
                }
                else if (n == 0)
                    stale = true;

            }

            if (!isMandatory(messageInfo))
            {
                return AuthStatus.SUCCESS;
            }
            String domain=request.getContextPath();
            if (domain==null)
                domain="/";
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Digest realm=\""+realmName+
			    "\", domain=\""+domain +
			    "\", nonce=\""+newNonce(timestamp)+
			    "\", algorithm=MD5, qop=\"auth\"" + (useStale?(" stale="+stale):""));
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
    public String newNonce(long ts)
    {
//        long ts=request.getTimeStamp();
        long sk=nonceSecret;

        byte[] nounce = new byte[24];
        for (int i=0;i<8;i++)
        {
            nounce[i]=(byte)(ts&0xff);
            ts=ts>>8;
            nounce[8+i]=(byte)(sk&0xff);
            sk=sk>>8;
        }

        byte[] hash=null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(nounce,0,16);
            hash = md.digest();
        }
        catch(Exception e)
        {
            Log.warn(e);
        }

        for (int i=0;i<hash.length;i++)
        {
            nounce[8+i]=hash[i];
            if (i==23)
                break;
        }

        return new String(B64Code.encode(nounce));
    }

    /**
     * @param nonce
     * @param timestamp should be timestamp of request.
     * @return -1 for a bad nonce, 0 for a stale none, 1 for a good nonce
     */
    /* ------------------------------------------------------------ */
    public int checkNonce(String nonce, long timestamp)
    {
        try
        {
            byte[] n = B64Code.decode(nonce.toCharArray());
            if (n.length!=24)
                return -1;

            long ts=0;
            long sk=nonceSecret;
            byte[] n2 = new byte[16];
            System.arraycopy(n, 0, n2, 0, 8);
            for (int i=0;i<8;i++)
            {
                n2[8+i]=(byte)(sk&0xff);
                sk=sk>>8;
                ts=(ts<<8)+(0xff&(long)n[7-i]);
            }

            long age=timestamp-ts;
            if (Log.isDebugEnabled()) Log.debug("age="+age);

            byte[] hash=null;
            try
            {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.reset();
                md.update(n2,0,16);
                hash = md.digest();
            }
            catch(Exception e)
            {
                Log.warn(e);
            }

            for (int i=0;i<16;i++)
                if (n[i+8]!=hash[i])
                    return -1;

            if(maxNonceAge>0 && (age<0 || age>maxNonceAge))
                return 0; // stale

            return 1;
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
        return -1;
    }

    private static class Digest extends Credential
    {
        String method=null;
        String username = null;
        String realm = null;
        String nonce = null;
        String nc = null;
        String cnonce = null;
        String qop = null;
        String uri = null;
        String response=null;

        /* ------------------------------------------------------------ */
        Digest(String m)
        {
            method=m;
        }

        /* ------------------------------------------------------------ */
        public boolean check(Object credentials)
        {
            String password=(credentials instanceof String)
                ?(String)credentials
                :credentials.toString();

            try{
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] ha1;
                if(credentials instanceof Credential.MD5)
                {
                    // Credentials are already a MD5 digest - assume it's in
                    // form user:realm:password (we have no way to know since
                    // it's a digest, alright?)
                    ha1 = ((Credential.MD5)credentials).getDigest();
                }
                else
                {
                    // calc A1 digest
                    md.update(username.getBytes(StringUtil.__ISO_8859_1));
                    md.update((byte)':');
                    md.update(realm.getBytes(StringUtil.__ISO_8859_1));
                    md.update((byte)':');
                    md.update(password.getBytes(StringUtil.__ISO_8859_1));
                    ha1=md.digest();
                }
                // calc A2 digest
                md.reset();
                md.update(method.getBytes(StringUtil.__ISO_8859_1));
                md.update((byte)':');
                md.update(uri.getBytes(StringUtil.__ISO_8859_1));
                byte[] ha2=md.digest();





                // calc digest
                // request-digest  = <"> < KD ( H(A1), unq(nonce-value) ":" nc-value ":" unq(cnonce-value) ":" unq(qop-value) ":" H(A2) ) <">
                // request-digest  = <"> < KD ( H(A1), unq(nonce-value) ":" H(A2) ) > <">



                md.update(TypeUtil.toString(ha1,16).getBytes(StringUtil.__ISO_8859_1));
                md.update((byte)':');
                md.update(nonce.getBytes(StringUtil.__ISO_8859_1));
                md.update((byte)':');
                md.update(nc.getBytes(StringUtil.__ISO_8859_1));
                md.update((byte)':');
                md.update(cnonce.getBytes(StringUtil.__ISO_8859_1));
                md.update((byte)':');
                md.update(qop.getBytes(StringUtil.__ISO_8859_1));
                md.update((byte)':');
                md.update(TypeUtil.toString(ha2,16).getBytes(StringUtil.__ISO_8859_1));
                byte[] digest=md.digest();

                // check digest
                return (TypeUtil.toString(digest,16).equalsIgnoreCase(response));
            }
            catch (Exception e)
            {Log.warn(e);}

            return false;
        }

        public String toString()
        {
            return username+","+response;
        }

    }

}
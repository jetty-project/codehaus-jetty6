package org.mortbay.jetty.security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import sun.misc.BASE64Decoder;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.mortbay.jetty.Request;
import org.mortbay.log.Log;
import org.mortbay.util.Loader;

@SuppressWarnings("restriction")
public class SpnegoUserRealm extends HashUserRealm implements UserRealm
{
    private GSSName srcName;
    private CallbackHandler callbackHandler;
 
    private BASE64Decoder base64Decoder = new BASE64Decoder();
    
    public SpnegoUserRealm()
    {
        
    }
    
    
    
    public SpnegoUserRealm(String name, String config) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        super(name);
        setConfig(config);
    }
    
    
    protected void loadConfig() throws IOException
    {
        Properties properties = new Properties();

        properties.load(getConfigResource().getInputStream());
      
    }

    public Principal authenticate(String username, Object credentials, Request request)
    {
               
        try {
            
            Callback[] callbacks = new Callback[1];
            callbacks[0] = new NameCallback("User name");
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();

            if (username == null || username.equals("")) {
                username = null;
                return null;
            }
            
            byte[] token = base64Decoder.decodeBuffer(username);

            GSSManager manager = GSSManager.getInstance();
           
            System.out.println("Got GSSManager");
            
            Oid krb5Oid = new Oid("1.3.6.1.5.5.2"); // http://java.sun.com/javase/6/docs/technotes/guides/security/jgss/jgss-features.html
            GSSName gssName = manager.createName(username, GSSName.NT_USER_NAME);
            GSSCredential serverCreds = manager.createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, krb5Oid, GSSCredential.ACCEPT_ONLY);
            GSSContext gContext = manager.createContext(serverCreds);
            if (gContext == null) {
                Log.info("Failed to create a GSSContext");
            } else {
                while (!gContext.isEstablished()) {
                    token = gContext.acceptSecContext(token, 0, token.length);
                }
                if (gContext.isEstablished()) {
                    srcName = gContext.getSrcName();
                    Log.info("A security context is successfully established " + gContext);
                    
                    // from the hash manager
                    put(srcName, serverCreds);
                    
                    return null;
                } else {
                    Log.info("Failed to establish a security context");
                    //throw new LoginException("Failed to establish a security context");
                }
            }
        } catch (GSSException e) {
            e.printStackTrace();
            //throw (LoginException) new LoginException().initCause(e);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (UnsupportedCallbackException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}

package org.mortbay.jetty.security;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.mortbay.jetty.nio.HttpChannelEndPoint;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;

/* ------------------------------------------------------------ */
/** SslSelectChannelConnector.
 *
 * @author Nik Gonzalez <ngonzalez@exist.com>
 * @author Greg Wilkins <gregw@mortbay.com>
 */
public class SslSelectChannelConnector extends SelectChannelConnector
{
    /** Default value for the keystore location path. */
    public static final String DEFAULT_KEYSTORE = System.getProperty("user.home") + File.separator
            + ".keystore";

    /** String name of key password property. */
    public static final String KEYPASSWORD_PROPERTY = "jetty.ssl.keypassword";

    /** String name of keystore password property. */
    public static final String PASSWORD_PROPERTY = "jetty.ssl.password";
    
    /** Default value for the cipher Suites. */
    private String _excludeCipherSuites[] = null;

    /** Default value for the keystore location path. */
    private String _keystore=DEFAULT_KEYSTORE ;
    private String _keystoreType = "JKS"; // type of the key store

    private transient Password _password;
    private transient Password _keyPassword;
    private transient Password _trustPassword;
    private String _protocol = "TLS";
    private String _algorithm = "SunX509"; // cert algorithm
    private String _provider;
    private String _secureRandomAlgorithm; // cert algorithm
    private String _sslKeyManagerFactoryAlgorithm = System.getProperty("ssl.KeyManagerFactory.algorithm","SunX509"); // cert algorithm
    
    private String _sslTrustManagerFactoryAlgorithm = System.getProperty("ssl.TrustManagerFactory.algorithm","SunX509"); // cert algorithm

    private String _truststore;
    private String _truststoreType = "JKS"; // type of the key store

    private int _applicationBufferSize = 16384;

    /* ------------------------------------------------------------ */
    public String[] getCipherSuites()
    {
        return _excludeCipherSuites;
    }

    /* ------------------------------------------------------------ */
    /**
     * @author Tony Jiang
     */
    public void setCipherSuites(String[] cipherSuites)
    {
        this._excludeCipherSuites = cipherSuites;
    }

    /* ------------------------------------------------------------ */
    public void setPassword(String password)
    {
        _password = Password.getPassword(PASSWORD_PROPERTY, password, null);
    }

    /* ------------------------------------------------------------ */
    public void setTrustPassword(String password)
    {
        _trustPassword = Password.getPassword(PASSWORD_PROPERTY,password,null);
    }

    /* ------------------------------------------------------------ */
    public void setKeyPassword(String password)
    {
        _keyPassword = Password.getPassword(KEYPASSWORD_PROPERTY, password, null);
    }

    /* ------------------------------------------------------------ */
    public String getAlgorithm()
    {
        return (this._algorithm);
    }

    /* ------------------------------------------------------------ */
    public void setAlgorithm(String algorithm)
    {
        this._algorithm = algorithm;
    }

    /* ------------------------------------------------------------ */
    public String getProtocol()
    {
        return _protocol;
    }

    /* ------------------------------------------------------------ */
    public void setProtocol(String protocol)
    {
        _protocol = protocol;
    }

    /* ------------------------------------------------------------ */
    public void setKeystore(String keystore)
    {
        _keystore = keystore;
    }

    /* ------------------------------------------------------------ */
    public String getKeystore()
    {
        return _keystore;
    }

    /* ------------------------------------------------------------ */
    public String getKeystoreType()
    {
        return (_keystoreType);
    }

    /* ------------------------------------------------------------ */
    public void setKeystoreType(String keystoreType)
    {
        _keystoreType = keystoreType;
    }

    /* ------------------------------------------------------------ */
    public String getProvider()
    {
        return _provider;
    }
    public String getSecureRandomAlgorithm() 
    {
        return (this._secureRandomAlgorithm);
    }

    /* ------------------------------------------------------------ */
    public String getSslKeyManagerFactoryAlgorithm() 
    {
        return (this._sslKeyManagerFactoryAlgorithm);
    }

    /* ------------------------------------------------------------ */
    public String getSslTrustManagerFactoryAlgorithm() 
    {
        return (this._sslTrustManagerFactoryAlgorithm);
    }

    /* ------------------------------------------------------------ */
    public String getTruststore()
    {
        return _truststore;
    }

    /* ------------------------------------------------------------ */
    public String getTruststoreType()
    {
        return _truststoreType;
    }

    /* ------------------------------------------------------------ */
    public void setProvider(String _provider)
    {
        this._provider = _provider;
    }

    /* ------------------------------------------------------------ */
    public void setSecureRandomAlgorithm(String algorithm) 
    {
        this._secureRandomAlgorithm = algorithm;
    }

    /* ------------------------------------------------------------ */
    public void setSslKeyManagerFactoryAlgorithm(String algorithm) 
    {
        this._sslKeyManagerFactoryAlgorithm = algorithm;
    }
    
    /* ------------------------------------------------------------ */
    public void setSslTrustManagerFactoryAlgorithm(String algorithm) 
    {
        this._sslTrustManagerFactoryAlgorithm = algorithm;
    }


    public void setTruststore(String truststore)
    {
        _truststore = truststore;
    }
    

    public void setTruststoreType(String truststoreType)
    {
        _truststoreType = truststoreType;
    }

    /* ------------------------------------------------------------ */
    public HttpChannelEndPoint newHttpChannelEndPoint(SelectChannelConnector connector, SocketChannel channel, SelectChannelConnector.SelectSet selectSet, SelectionKey sKey) throws IOException
    {
        return new SslHttpChannelEndPoint(connector, channel, selectSet, sKey, createSSLEngine());
    }

    /* ------------------------------------------------------------ */
    protected SSLEngine createSSLEngine() throws IOException
    {
        SSLEngine engine = null;
        try
        {
            if (_password==null)
                _password=new Password("");
            if (_keyPassword==null)
                _keyPassword=_password;
            if (_trustPassword==null)
                _trustPassword=_password;
            
            if (_truststore==null)
            {
                _truststore=_keystore;
                _truststoreType=_keystoreType;
            }

            KeyManager[] keyManagers = null;
            if (_keystore != null)
            {
                KeyStore keyStore = KeyStore.getInstance(_keystoreType);
                if (_password == null) 
                    throw new SSLException("_password is not set");
                keyStore.load(Resource.newResource(_keystore).getInputStream(), _password.toString().toCharArray());
        
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(_sslKeyManagerFactoryAlgorithm);        
                if (_keyPassword == null) 
                    throw new SSLException("_keypassword is not set");
                keyManagerFactory.init(keyStore,_keyPassword.toString().toCharArray());
                keyManagers = keyManagerFactory.getKeyManagers();
            }

            TrustManager[] trustManagers = null;
            if (_truststore != null)
            {
                KeyStore trustStore = KeyStore.getInstance(_truststoreType);
                trustStore.load(Resource.newResource(_truststore).getInputStream(), _trustPassword.toString().toCharArray());
                
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(_sslTrustManagerFactoryAlgorithm);
                trustManagerFactory.init(trustStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            }

            SecureRandom secureRandom = _secureRandomAlgorithm==null?null:SecureRandom.getInstance(_secureRandomAlgorithm);

            SSLContext context = _provider==null?SSLContext.getInstance(_protocol):SSLContext.getInstance(_protocol, _provider);

            context.init(keyManagers, trustManagers, secureRandom);

            engine = context.createSSLEngine();
            
            if (_excludeCipherSuites != null && _excludeCipherSuites.length >0) 
            {
                List excludedCSList = Arrays.asList(_excludeCipherSuites);
                String[] enabledCipherSuites = engine.getEnabledCipherSuites();
                List enabledCSList = new ArrayList(Arrays.asList(enabledCipherSuites));
                Iterator exIter = excludedCSList.iterator();

                while (exIter.hasNext())
                {
                    String cipherName = (String)exIter.next();
                    if (enabledCSList.contains(cipherName))
                    {
                        enabledCSList.remove(cipherName);
                    }
                }
                enabledCipherSuites = (String[])enabledCSList.toArray(new String[enabledCSList.size()]);

                engine.setEnabledCipherSuites(enabledCipherSuites);
            }

        }
        catch (Exception e)
        {
            Log.debug(e);
        }
        return engine;
    }

    protected void doStart() throws Exception
    {
        setHeaderBufferSize(_applicationBufferSize);        
        super.doStart();
    }
}

package org.mortbay.jetty.security;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.io.nio.IndirectNIOBuffer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.log.Log;
import org.mortbay.util.StringUtil;

public class SslRenegotiateTest extends TestCase
{

    static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
    {
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType )
        {
        }

        public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType )
        {
        }
    } };

    static HostnameVerifier hostnameVerifier = new HostnameVerifier()
    {
        public boolean verify( String urlHostName, SSLSession session )
        {
            Log.warn( "Warning: URL Host: " + urlHostName + " vs." + session.getPeerHost() );
            return true;
        }
    };

    ByteBuffer _outAppB;
    ByteBuffer _outPacketB;
    ByteBuffer _inAppB;
    ByteBuffer _inPacketB;
    SocketChannel _socket;
    SSLEngine _engine;
    

    public void testNoReneg() throws Exception
    {
        Server server=new Server();
        try
        {
            //SslSelectChannelConnector connector=new SslSelectChannelConnector();
            SslSocketConnector connector=new SslSocketConnector();

            String keystore = System.getProperty("user.dir")+File.separator+"src"+File.separator+"test"+File.separator+"resources"+File.separator+"keystore";

            connector.setPort(0);
            connector.setKeystore(keystore);
            connector.setPassword("storepwd");
            connector.setKeyPassword("keypwd");

            server.setConnectors(new Connector[] { connector });
            server.setHandler(new HelloWorldHandler());

            server.start();
            
            SocketAddress addr = new InetSocketAddress("localhost",connector.getLocalPort());
            _socket = SocketChannel.open(addr);
            _socket.configureBlocking(true);
            
            
            SSLContext context=SSLContext.getInstance("SSL");
            context.init( null, trustAllCerts, new java.security.SecureRandom() );

            _engine = context.createSSLEngine();
            _engine.setUseClientMode(true);
            SSLSession session=_engine.getSession();
            
            _outAppB = ByteBuffer.allocate(session.getApplicationBufferSize());
            _outPacketB = ByteBuffer.allocate(session.getPacketBufferSize());
            _inAppB = ByteBuffer.allocate(session.getApplicationBufferSize());
            _inPacketB = ByteBuffer.allocate(session.getPacketBufferSize());
            
            
            _outAppB.put("GET / HTTP/1.0\r\n\r\n".getBytes(StringUtil.__ISO_8859_1));
            _outAppB.flip();
            
            _engine.beginHandshake();
            
            runHandshake();

            doWrap();
            doUnwrap();
            _inAppB.flip();
            System.err.println(new IndirectNIOBuffer(_inAppB,true).toString());
            _inAppB.clear();
            
            _engine.beginHandshake();
            
            try
            {
                runHandshake();
                assertTrue(false);
            }
            catch(IOException e)
            {
                System.err.println(e);
                assertTrue(true);;
            }

        }
        finally
        {
            server.start();
        }
    }
    
    void runHandshake() throws Exception
    {
        SSLEngineResult result;
        
        while (true)
        {
            System.err.println();
            System.err.println(_engine.getHandshakeStatus());

            switch(_engine.getHandshakeStatus())
            {
                case NEED_TASK:
                {
                    System.err.println("running task");
                    _engine.getDelegatedTask().run();
                    break;
                }
                
                case NEED_WRAP:
                {
                    doWrap();
                    break;
                }
                
                case NEED_UNWRAP:
                {
                    doUnwrap();
                    break;
                }
                
                default:
                    return;
            }
        }
    }
    
    private void doWrap() throws Exception
    {
        SSLEngineResult result =_engine.wrap(_outAppB,_outPacketB);
        System.err.println("wrapped "+result.bytesConsumed()+" to "+result.bytesProduced());
        _outPacketB.flip();
        while (_outPacketB.hasRemaining())
        {
            int p = _outPacketB.remaining();
            int l =_socket.write(_outPacketB);
            System.err.println("wrote "+l+" of "+p);
        }
        _outPacketB.clear();
    }
    
    private void doUnwrap() throws Exception
    {
        _inPacketB.clear();
        int l=_socket.read(_inPacketB);
        System.err.println("read "+l);
        if (l<0)
            throw new IOException("EOF");
        
        _inPacketB.flip();
        
        do
        {
            SSLEngineResult result =_engine.unwrap(_inPacketB,_inAppB);
            System.err.println("unwrapped "+result.bytesConsumed()+" to "+result.bytesProduced());
            if (result.bytesProduced()>0)
                return;
        }
        while(_inPacketB.remaining()>0 && _engine.getHandshakeStatus()==HandshakeStatus.NEED_UNWRAP);
        
    }

    private static class HelloWorldHandler extends AbstractHandler
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            ((Request)request).setHandled(true);
            System.err.println("HELLO WORLD HANDLING");
            PrintWriter out=response.getWriter();

            out.print("HELLO WORLD");
        }
    }
}

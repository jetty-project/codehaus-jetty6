package org.mortbay.jetty.integration.ssl;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.ssl.SslSelectChannelConnector;
import org.mortbay.jetty.ssl.SslSocketConnector;

import jsslutils.sslcontext.PKIXSSLContextFactory;
import jsslutils.sslcontext.X509SSLContextFactory;
import junit.framework.TestCase;

/**
 * Testing setSslContext using jSSLutils. (Some code was borrowed from
 * SSLEngineTest.)
 * 
 * 
 * 
 */
public class SSLContextTest extends TestCase {
	// Useful constants
	private static final String HELLO_WORLD = "Hello world\r\n";
	private static final String JETTY_VERSION = Server.getVersion();
	private static final String PROTOCOL_VERSION = "2.0";

	/** The request. */
	private static final String REQUEST0_HEADER = "POST / HTTP/1.1\n"
			+ "Host: localhost\n" + "Content-Type: text/xml\n"
			+ "Content-Length: ";
	private static final String REQUEST1_HEADER = "POST / HTTP/1.1\n"
			+ "Host: localhost\n" + "Content-Type: text/xml\n"
			+ "Connection: close\n" + "Content-Length: ";
	private static final String REQUEST_CONTENT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
			+ "<requests xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ "        xsi:noNamespaceSchemaLocation=\"commander.xsd\" version=\""
			+ PROTOCOL_VERSION + "\">\n" + "</requests>";

	private static final String REQUEST0 = REQUEST0_HEADER
			+ REQUEST_CONTENT.getBytes().length + "\n\n" + REQUEST_CONTENT;
	private static final String REQUEST1 = REQUEST1_HEADER
			+ REQUEST_CONTENT.getBytes().length + "\n\n" + REQUEST_CONTENT;

	/** The expected response. */
	private static final String RESPONSE0 = "HTTP/1.1 200 OK\n"
			+ "Content-Length: " + HELLO_WORLD.length() + "\n"
			+ "Server: Jetty(" + JETTY_VERSION + ")\n" + '\n' + "Hello world\n";
	private static final String RESPONSE1 = "HTTP/1.1 200 OK\n"
			+ "Connection: close\n" + "Server: Jetty(" + JETTY_VERSION + ")\n"
			+ '\n' + "Hello world\n";

	private static final String TEST_KEYSTORES_PATH = "certificates/";

	/**
	 * Returns the store of CA certificates, to be used as a trust store. The
	 * default value is to load 'dummy.jks', part of this test suite.
	 * 
	 * @return KeyStore containing the certificates to trust.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public KeyStore getCaKeyStore() throws IOException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(ClassLoader.getSystemResourceAsStream(TEST_KEYSTORES_PATH
				+ "jks/dummy.jks"), "testtest".toCharArray());
		return keyStore;
	}

	/**
	 * Returns the keystore containing the key and the certificate to be used by
	 * the server.
	 * 
	 * @return KeyStore containing the server credentials.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public KeyStore getServerCertKeyStore() throws IOException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(ClassLoader.getSystemResourceAsStream(TEST_KEYSTORES_PATH
				+ "localhost.p12"), "testtest".toCharArray());
		return keyStore;
	}

	/**
	 * Returns the keystore containing a test key and certificate that is to be
	 * trusted by the server. This is the "good" keystore in that its
	 * certificate has not been revoked by the demo CA. This should work
	 * whether-or-not CRLs are used.
	 * 
	 * @return KeyStore containing the "good" client credentials.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public KeyStore getGoodClientCertKeyStore() throws IOException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(ClassLoader.getSystemResourceAsStream(TEST_KEYSTORES_PATH
				+ "testclient.p12"), "testtest".toCharArray());
		return keyStore;
	}

	/**
	 * Returns the keystore containing a test key and certificate that is not to
	 * be trusted by the server when CRLs are enabled. This is the "bad"
	 * keystore in that its certificate has been revoked by the demo CA. This
	 * should pass work when CRLs checks are disabled, but fail when they are
	 * used.
	 * 
	 * @return KeyStore containing the "bad" client credentials.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public KeyStore getBadClientCertKeyStore() throws IOException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(ClassLoader.getSystemResourceAsStream(TEST_KEYSTORES_PATH
				+ "testclient-r.p12"), "testtest".toCharArray());
		return keyStore;
	}

	/**
	 * Returns a collection of CRLs to be used by the tests. This is loaded from
	 * 'newca.crl'.
	 * 
	 * @return CRLs
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws CRLException
	 */
	public Collection<X509CRL> getLocalCRLs() throws IOException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException,
			CRLException {
		InputStream inStream = ClassLoader
				.getSystemResourceAsStream(TEST_KEYSTORES_PATH + "newca.crl");
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509CRL crl = (X509CRL) cf.generateCRL(inStream);
		inStream.close();
		ArrayList<X509CRL> crls = new ArrayList<X509CRL>();
		crls.add(crl);
		return crls;
	}

	/**
	 * This runs the main test: it runs a client and a server.
	 * 
	 * @param sslClientContext
	 *            SSLContext to be used by the client.
	 * @param sslServerContext
	 *            SSLContext to be used by the server.
	 * @return true if the server accepted the SSL certificate.
	 * @throws SSLContextFactoryException
	 * @throws IOException
	 */
	public boolean runSSLContextTest(SSLContext sslClientContext,
			Connector connector) throws Exception {
		boolean result = false;

		Server server = new Server();
		server.setConnectors(new Connector[] { connector });
		server.setHandler(new HelloWorldHandler());
		server.start();

		int testPort = connector.getLocalPort();

		SSLSocket sslClientSocket = null;
		try {
			SSLSocketFactory sslClientSocketFactory = sslClientContext
					.getSocketFactory();

			sslClientSocket = (SSLSocket) sslClientSocketFactory.createSocket(
					"localhost", testPort);
			assertTrue("Client socket connected", sslClientSocket.isConnected());

			sslClientSocket.setSoTimeout(500);

			OutputStream os = sslClientSocket.getOutputStream();

			os.write(REQUEST0.getBytes());
			os.write(REQUEST0.getBytes());
			os.flush();

			os.write(REQUEST1.getBytes());
			os.flush();

			// Read the response.
			String responses = readResponse(sslClientSocket);
			// Check the response
			assertEquals(RESPONSE0 + RESPONSE0 + RESPONSE1, responses);
			result = true;
		} catch (SocketException e) {
			result = false;
		} catch (SSLHandshakeException e) {
			result = false;
			printSslException("! Client: ", e, sslClientSocket);
		} finally {
			server.stop();
		}

		return result;
	}

	public void testSslSelectChannelConnector_PKIX_GoodClient()
			throws Exception {
		PKIXSSLContextFactory serverSSLContextFactory = new PKIXSSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());
		serverSSLContextFactory.addCrlCollection(getLocalCRLs());

		SslSelectChannelConnector connector = new SslSelectChannelConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		PKIXSSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new PKIXSSLContextFactory(
				getGoodClientCertKeyStore(), "testtest", getCaKeyStore());
		clientSSLContextFactory.addCrlCollection(getLocalCRLs());

		assertTrue(runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	public void testSslSelectChannelConnector_PKIX_BadClient() throws Exception {
		PKIXSSLContextFactory serverSSLContextFactory = new PKIXSSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());
		serverSSLContextFactory.addCrlCollection(getLocalCRLs());

		SslSelectChannelConnector connector = new SslSelectChannelConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		PKIXSSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new PKIXSSLContextFactory(
				getBadClientCertKeyStore(), "testtest", getCaKeyStore());
		clientSSLContextFactory.addCrlCollection(getLocalCRLs());

		assertTrue(!runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	public void testSslSelectChannelConnector_X509_GoodClient()
			throws Exception {
		X509SSLContextFactory serverSSLContextFactory = new X509SSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());

		SslSelectChannelConnector connector = new SslSelectChannelConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		X509SSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new X509SSLContextFactory(
				getGoodClientCertKeyStore(), "testtest", getCaKeyStore());

		assertTrue(runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	public void testSslSelectChannelConnector_X509_BadClient() throws Exception {
		X509SSLContextFactory serverSSLContextFactory = new X509SSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());

		SslSelectChannelConnector connector = new SslSelectChannelConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		X509SSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new X509SSLContextFactory(
				getBadClientCertKeyStore(), "testtest", getCaKeyStore());

		assertTrue(runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	public void testSslSocketConnector_PKIX_GoodClient() throws Exception {
		PKIXSSLContextFactory serverSSLContextFactory = new PKIXSSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());
		serverSSLContextFactory.addCrlCollection(getLocalCRLs());

		SslSocketConnector connector = new SslSocketConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		PKIXSSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new PKIXSSLContextFactory(
				getGoodClientCertKeyStore(), "testtest", getCaKeyStore());
		clientSSLContextFactory.addCrlCollection(getLocalCRLs());

		assertTrue(runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	public void testSslSocketConnector_PKIX_BadClient() throws Exception {
		PKIXSSLContextFactory serverSSLContextFactory = new PKIXSSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());
		serverSSLContextFactory.addCrlCollection(getLocalCRLs());

		SslSocketConnector connector = new SslSocketConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		PKIXSSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new PKIXSSLContextFactory(
				getBadClientCertKeyStore(), "testtest", getCaKeyStore());
		clientSSLContextFactory.addCrlCollection(getLocalCRLs());

		assertTrue(!runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	public void testSslSocketConnector_X509_GoodClient() throws Exception {
		X509SSLContextFactory serverSSLContextFactory = new X509SSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());

		SslSocketConnector connector = new SslSocketConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		X509SSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new X509SSLContextFactory(
				getGoodClientCertKeyStore(), "testtest", getCaKeyStore());

		assertTrue(runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	public void testSslSocketConnector_X509_BadClient() throws Exception {
		X509SSLContextFactory serverSSLContextFactory = new X509SSLContextFactory(
				getServerCertKeyStore(), "testtest", getCaKeyStore());

		SslSocketConnector connector = new SslSocketConnector();
		connector.setNeedClientAuth(true);
		connector.setPort(0);
		connector.setSslContext(serverSSLContextFactory
				.buildSSLContext("SSLv3"));

		X509SSLContextFactory clientSSLContextFactory;
		clientSSLContextFactory = new X509SSLContextFactory(
				getBadClientCertKeyStore(), "testtest", getCaKeyStore());

		assertTrue(runSSLContextTest(clientSSLContextFactory
				.buildSSLContext("SSLv3"), connector));
	}

	/**
	 * Used for printing out more info when there's a problem.
	 * 
	 * @param prefix
	 * @param sslException
	 * @param socket
	 * @return
	 */
	private Throwable printSslException(String prefix,
			SSLException sslException, SSLSocket socket) {
		Throwable cause = sslException;
		while ((cause = cause.getCause()) != null) {
			if (cause instanceof CertPathValidatorException) {
				CertPathValidatorException certException = (CertPathValidatorException) cause;
				CertPath certPath = certException.getCertPath();
				List<? extends Certificate> certificates = certPath
						.getCertificates();
				int index = certException.getIndex();
				if (index >= 0) {
					Certificate pbCertificate = certificates.get(index);
					if (pbCertificate instanceof X509Certificate) {
						System.out.println(prefix
								+ "Problem caused by cert: "
								+ ((X509Certificate) pbCertificate)
										.getSubjectX500Principal().getName());
					} else {
						System.out.println(prefix + "Problem caused by cert: "
								+ pbCertificate);
					}
				} else {
					System.out.println(prefix + "Unknown index: " + cause);
				}
				break;
			} else {
				System.out.println(prefix + cause);
				if (socket != null) {
					printSslSocketInfo(socket);
				}
			}
		}
		return cause;
	}

	/**
	 * Used for printing out more info when there's a problem.
	 * 
	 * @param socket
	 */
	private void printSslSocketInfo(SSLSocket socket) {
		System.out.println("Socket: " + socket);
		SSLSession session = socket.getSession();
		if (session != null) {
			System.out.println("Session: " + session);
			System.out.println("  Local certificates: "
					+ session.getLocalCertificates());
			System.out.println("  Local principal: "
					+ session.getLocalPrincipal());
			SSLSessionContext context = session.getSessionContext();
			if (context != null) {
				System.out.println("Session context: " + context);
			}
		}
	}

	/**
	 * Read entire response from the client. Close the output.
	 * 
	 * @param client
	 *            Open client socket.
	 * 
	 * @return The response string.
	 * 
	 * @throws IOException
	 */
	private static String readResponse(Socket client) throws IOException {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(client
					.getInputStream()));

			StringBuilder sb = new StringBuilder(1000);
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}

			return sb.toString();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	private static class HelloWorldHandler extends AbstractHandler {
		// ~ Methods
		// ------------------------------------------------------------

		public void handle(String target, HttpServletRequest request,
				HttpServletResponse response) throws IOException,
				ServletException {
			PrintWriter out = response.getWriter();

			try {
				out.print(HELLO_WORLD);
			} finally {
				out.close();
			}
		}
	}
}

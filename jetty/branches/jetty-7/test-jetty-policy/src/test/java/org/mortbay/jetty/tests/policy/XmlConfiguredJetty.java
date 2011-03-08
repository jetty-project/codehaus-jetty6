// ========================================================================
// Copyright (c) Webtide LLC
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// The Apache License v2.0 is available at
// http://www.apache.org/licenses/LICENSE-2.0.txt
//
// You may elect to redistribute this code under either of these licenses.
// ========================================================================

package org.mortbay.jetty.tests.policy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.PathAssert;
import org.eclipse.jetty.toolchain.test.TestingDir;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.junit.Assert;

/**
 * Allows for setting up a Jetty server for testing based on XML configuration files.
 */
public class XmlConfiguredJetty
{
    private List<URL> _xmlConfigurations;
    private Map<String, String> _properties = new HashMap<String, String>();
    private Server _server;
    private int _serverPort;
    private String _scheme = HttpSchemes.HTTP;
    private File _jettyHome;

    public XmlConfiguredJetty(TestingDir testdir) throws IOException
    {
        _xmlConfigurations = new ArrayList<URL>();
        Properties properties = new Properties();

        String jettyHomeBase = testdir.getDir().getAbsolutePath();
        // Ensure we have a new (pristene) directory to work with. 
        int idx = 0;
        _jettyHome = new File(jettyHomeBase + "#" + idx);
        while (_jettyHome.exists())
        {
            idx++;
            _jettyHome = new File(jettyHomeBase + "#" + idx);
        }
        deleteContents(_jettyHome);
        // Prepare Jetty.Home (Test) dir
        _jettyHome.mkdirs();

        File logsDir = new File(_jettyHome,"logs");
        logsDir.mkdirs();

        File etcDir = new File(_jettyHome,"etc");
        etcDir.mkdirs();
        IO.copyFile(MavenTestingUtils.getTestResourceFile("etc/realm.properties"),new File(etcDir,"realm.properties"));
        IO.copyFile(MavenTestingUtils.getTestResourceFile("etc/webdefault.xml"),new File(etcDir,"webdefault.xml"));

        File contextsDir = new File(_jettyHome,"contexts");
        if (contextsDir.exists())
        {
            deleteContents(contextsDir);
        }
        contextsDir.mkdirs();

        File webappsDir = new File(_jettyHome,"webapps");
        if (webappsDir.exists())
        {
            deleteContents(webappsDir);
        }
        webappsDir.mkdirs();

        File tmpDir = new File(_jettyHome,"tmp");
        if (tmpDir.exists())
        {
            deleteContents(tmpDir);
        }
        tmpDir.mkdirs();

        File workishDir = new File(_jettyHome,"workish");
        if (workishDir.exists())
        {
            deleteContents(workishDir);
        }
        workishDir.mkdirs();

        File testwarsDir = new File(MavenTestingUtils.getTargetDir(),"test-wars");

        // Setup properties
        System.setProperty("java.io.tmpdir",tmpDir.getAbsolutePath());
        properties.setProperty("jetty.home",_jettyHome.getAbsolutePath());
        System.setProperty("jetty.home",_jettyHome.getAbsolutePath());
        properties.setProperty("test.basedir",MavenTestingUtils.getBasedir().getAbsolutePath());
        properties.setProperty("test.resourcesdir",MavenTestingUtils.getTestResourcesDir().getAbsolutePath());
        properties.setProperty("test.webapps",webappsDir.getAbsolutePath());
        properties.setProperty("test.targetdir",MavenTestingUtils.getTargetDir().getAbsolutePath());
        properties.setProperty("test.warsdir",testwarsDir.getAbsolutePath());
        properties.setProperty("test.workdir",workishDir.getAbsolutePath());

        // Write out configuration for use by ConfigurationManager.
        File testConfig = MavenTestingUtils.getTargetFile("xml-configured-jetty.properties");
        FileOutputStream out = new FileOutputStream(testConfig);
        properties.store(out,"Generated by " + XmlConfiguredJetty.class.getName());
        for (Object key : properties.keySet())
            _properties.put(String.valueOf(key),String.valueOf(properties.get(key)));
    }

    public void addConfiguration(File xmlConfigFile) throws MalformedURLException
    {
        _xmlConfigurations.add(xmlConfigFile.toURI().toURL());
    }

    public void addConfiguration(String testConfigName) throws MalformedURLException
    {
        addConfiguration(MavenTestingUtils.getTestResourceFile(testConfigName));
    }

    public void addConfiguration(URL xmlConfig)
    {
        _xmlConfigurations.add(xmlConfig);
    }

    public void assertNoWebAppContexts()
    {
        List<WebAppContext> contexts = getWebAppContexts();
        if (contexts.size() > 0)
        {
            for (WebAppContext context : contexts)
            {
                System.out.println("WebAppContext should not exist:\n" + context);
            }
            Assert.assertEquals("Contexts.size",0,contexts.size());
        }
    }

    public String getResponse(String path) throws IOException
    {
        URI destUri = getServerURI().resolve(path);
        URL url = destUri.toURL();

        URLConnection conn = url.openConnection();

        InputStream in = null;
        try
        {
            in = conn.getInputStream();
            return IO.toString(in);
        }
        finally
        {
            IO.close(in);
        }
    }

    public void assertResponseContains(String path, String needle) throws IOException
    {
        System.out.println("Issuing request to " + path);
        String content = getResponse(path);
        Assert.assertTrue("Content should contain <" + needle + ">, instead got <" + content + ">",content.contains(needle));
    }

    public void assertWebAppContextsExists(String... expectedContextPaths)
    {
        List<WebAppContext> contexts = getWebAppContexts();
        if (expectedContextPaths.length != contexts.size())
        {
            System.out.println("## Expected Contexts");
            for (String expected : expectedContextPaths)
            {
                System.out.println(expected);
            }
            System.out.println("## Actual Contexts");
            for (WebAppContext context : contexts)
            {
                System.out.printf("%s ## %s%n",context.getContextPath(),context);
            }
            Assert.assertEquals("Contexts.size",expectedContextPaths.length,contexts.size());
        }

        for (String expectedPath : expectedContextPaths)
        {
            boolean found = false;
            for (WebAppContext context : contexts)
            {
                if (context.getContextPath().equals(expectedPath))
                {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Did not find Expected Context Path " + expectedPath,found);
        }
    }

    public void copyContext(String srcName) throws IOException
    {
        copyContext(srcName,srcName);
    }

    public void copyContext(String srcName, String destName) throws IOException
    {
        System.out.printf("Copying Context: %s -> %s%n",srcName,destName);
        File srcDir = MavenTestingUtils.getTestResourceDir("contexts");
        File destDir = new File(_jettyHome,"contexts");

        File srcFile = new File(srcDir,srcName);
        File destFile = new File(destDir,destName);

        copyFile("Context",srcFile,destFile);
    }

    private void copyFile(String type, File srcFile, File destFile) throws IOException
    {
        PathAssert.assertFileExists(type + " File",srcFile);
        IO.copyFile(srcFile,destFile);
        PathAssert.assertFileExists(type + " File",destFile);
        System.out.printf("Copy %s: %s%n  To %s: %s%n",type,srcFile,type,destFile);
        System.out.printf("Destination Exists: %s - %s%n",destFile.exists(),destFile);
    }

    public void copyWebapp(String srcName, String destName) throws IOException
    {
        System.out.printf("Copying Webapp: %s -> %s%n",srcName,destName);
        File srcDir = MavenTestingUtils.getTestResourceDir("webapps");
        File destDir = new File(_jettyHome,"webapps");

        File srcFile = new File(srcDir,srcName);
        File destFile = new File(destDir,destName);

        copyFile("Webapp",srcFile,destFile);
    }

    public void copyTestWar(String warfilename) throws IOException
    {
        File srcDir = new File(MavenTestingUtils.getTargetDir(),"test-wars");
        PathAssert.assertDirExists("Test-Wars",srcDir);
        File destDir = new File(_jettyHome,"webapps");

        File srcFile = new File(srcDir,warfilename);
        File destFile = new File(destDir,warfilename);

        System.out.printf("Copying Webapp: %s -> %s%n",srcFile,destFile);

        copyFile("Test War",srcFile,destFile);
    }

    private void deleteContents(File dir)
    {
        System.out.printf("Delete  (dir) %s/%n",dir);
        if (!dir.exists())
        {
            return;
        }

        for (File file : dir.listFiles())
        {
            // Safety measure. only recursively delete within target directory.
            if (file.isDirectory() && file.getAbsolutePath().contains("target" + File.separator))
            {
                deleteContents(file);
                Assert.assertTrue("Delete failed: " + file.getAbsolutePath(),file.delete());
            }
            else
            {
                System.out.printf("Delete (file) %s%n",file);
                Assert.assertTrue("Delete failed: " + file.getAbsolutePath(),file.delete());
            }
        }
    }

    public DeploymentManager getActiveDeploymentManager()
    {
        return _server.getBean(DeploymentManager.class);
    }

    public File getJettyDir(String name)
    {
        return new File(_jettyHome,name);
    }

    public File getJettyHome()
    {
        return _jettyHome;
    }

    public String getScheme()
    {
        return _scheme;
    }

    public Server getServer()
    {
        return _server;
    }

    public int getServerPort()
    {
        return _serverPort;
    }

    public URI getServerURI() throws UnknownHostException
    {
        StringBuffer uri = new StringBuffer();
        uri.append(this._scheme).append("://");
        uri.append(InetAddress.getLocalHost().getHostAddress());
        uri.append(":").append(this._serverPort);
        return URI.create(uri.toString());
    }

    public List<WebAppContext> getWebAppContexts()
    {
        List<WebAppContext> contexts = new ArrayList<WebAppContext>();
        HandlerCollection handlers = (HandlerCollection)_server.getHandler();
        System.out.println(_server.dump());
        Handler children[] = handlers.getChildHandlers();

        for (Handler handler : children)
        {
            if (handler instanceof WebAppContext)
            {
                WebAppContext context = (WebAppContext)handler;
                contexts.add(context);
            }
        }

        return contexts;
    }

    public void load() throws Exception
    {
        XmlConfiguration last = null;
        Object[] obj = new Object[this._xmlConfigurations.size()];

        // Configure everything
        for (int i = 0; i < this._xmlConfigurations.size(); i++)
        {
            URL configURL = this._xmlConfigurations.get(i);
            XmlConfiguration configuration = new XmlConfiguration(configURL);
            if (last != null)
            {
                configuration.getIdMap().putAll(last.getIdMap());
            }
            configuration.getProperties().putAll(_properties);
            obj[i] = configuration.configure();
            last = configuration;
        }

        // Test for Server Instance.
        Server foundServer = null;
        int serverCount = 0;
        for (int i = 0; i < this._xmlConfigurations.size(); i++)
        {
            if (obj[i] instanceof Server)
            {
                if (obj[i].equals(foundServer))
                {
                    // Identical server instance found
                    break;
                }
                foundServer = (Server)obj[i];
                serverCount++;
            }
        }

        if (serverCount <= 0)
        {
            throw new Exception("Load failed to configure a " + Server.class.getName());
        }

        Assert.assertEquals("Server load count",1,serverCount);

        this._server = foundServer;
        this._server.setGracefulShutdown(10);

    }

    public void removeContext(String name)
    {
        File destDir = new File(_jettyHome,"contexts");
        File contextFile = new File(destDir,name);
        if (contextFile.exists())
        {
            Assert.assertTrue("Delete of Context file: " + contextFile.getAbsolutePath(),contextFile.delete());
        }
    }

    public void setProperty(String key, String value)
    {
        _properties.put(key,value);
    }

    public void setScheme(String scheme)
    {
        this._scheme = scheme;
    }

    public void start() throws Exception
    {
        Assert.assertNotNull("Server should not be null (failed load?)",_server);

        _server.start();
        // _server.dump(System.out);

        // Find the active server port.
        this._serverPort = (-1);
        Connector connectors[] = _server.getConnectors();
        for (int i = 0; i < connectors.length; i++)
        {
            Connector connector = connectors[i];
            if (connector.getLocalPort() > 0)
            {
                this._serverPort = connector.getLocalPort();
                break;
            }
        }

        Assert.assertTrue("Server Port is between 1 and 65535. Actually <" + _serverPort + ">",(1 <= this._serverPort) && (this._serverPort <= 65535));

        // Uncomment to have server start and continue to run (without exiting)
        // System.out.printf("Listening to port %d%n",this.serverPort);
        // server.join();
    }

    public void stop() throws Exception
    {
        _server.stop();
    }

    public void waitForDirectoryScan()
    {
        int ms = 2000;
        System.out.printf("Waiting %d milliseconds for AppProvider to process directory scan ...%n",ms);
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException ignore)
        {
            /* ignore */
        }
    }
}

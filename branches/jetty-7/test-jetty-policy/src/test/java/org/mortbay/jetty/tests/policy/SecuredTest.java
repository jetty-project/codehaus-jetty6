package org.mortbay.jetty.tests.policy;

import static org.hamcrest.Matchers.*;

import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.jetty.policy.JettyPolicy;
import org.eclipse.jetty.toolchain.test.TestingDir;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class SecuredTest
{
    @Rule
    public TestingDir testdir = new TestingDir();
    private XmlConfiguredJetty jetty;

    @Before
    public void initServer() throws Exception
    {
        jetty = new XmlConfiguredJetty(testdir);
        jetty.addConfiguration("jetty.xml");
        jetty.addConfiguration("jetty-deploys.xml");
        jetty.addConfiguration("jetty-policy.xml");

        jetty.copyTestWar("test-war-java_util_logging.war");
        jetty.copyTestWar("test-war-policy.war");
        
        jetty.copyConfig("contexts/foo.xml");
        jetty.copyConfig("contexts/policytests.xml");
        //jetty.copyConfig("lib/policy/jetty.policy");

        // Load Configuration(s)
        jetty.load();

        // Start it
        jetty.start();
    }

    @After
    public void shutdownServer() throws Exception
    {
        if (jetty != null)
        {
            jetty.stop();
        }
    }

    @Test
    public void testFilesystem() throws Exception
    {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
            public Object run() throws Exception
            {
                ((JettyPolicy)Policy.getPolicy()).dump(System.out);
                
                assertCheckerFailure("processFilesystemChecks");
               
                return null;
            }
        }
        );
       
      
    }

    @Test
    public void testJettyLog() throws Exception
    {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
            public Object run() throws Exception
            {
                //((JettyPolicy)Policy.getPolicy()).dump(System.out);
                
                    assertCheckerFailure("processJettyLogChecks");
               
                return null;
            }
        }
        );
    }

    @Test
    public void testLib() throws Exception
    {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
            public Object run() throws Exception
            {
               // ((JettyPolicy)Policy.getPolicy()).dump(System.out);
               
                    assertCheckerFailure("processLibChecks");
               
                return null;
            }
        }
        );
    }

    @Test
    public void testSystemProperty() throws Exception
    {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
            public Object run() throws Exception
            {
             //   ((JettyPolicy)Policy.getPolicy()).dump(System.out);
                
                    assertCheckerFailure("processSystemPropertyChecks");
              
                return null;
            }
        }
        );
    }

    private void assertCheckerFailure(String testname) throws Exception
    {
        SimpleRequest request = new SimpleRequest(jetty);
        Properties props = request.getProperties("/policytests/checker/" + testname);
        @SuppressWarnings("unchecked")
        Enumeration<String> names = (Enumeration<String>)props.propertyNames();
        while (names.hasMoreElements())
        {
            String name = names.nextElement();
            String value = props.getProperty(name);
            Assert.assertThat("[" + testname + "] " + name,value,not(startsWith("Success")));
        }
    }
}

package org.mortbay.jetty.tests.policy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.OS;
import org.eclipse.jetty.util.IO;

public class PolicyFileManager
{

    private String _policyDirectory;
    private String _jettyHome;

    public PolicyFileManager(String policyDirectory, String jettyHome)
    {
        _policyDirectory = policyDirectory;
        _jettyHome = jettyHome;
    }

    public void createJettyGlobalPolicyFile(String classesDir)
    {
        FileWriter writer = null;
        BufferedWriter out = null;
        try
        {
            writer = new FileWriter(OS.separators(_policyDirectory + "/jetty.policy"));
            out = new BufferedWriter(writer);
            out.write("grant {\n\n");

            out.write("   permission java.io.FilePermission \"" + MavenTestingUtils.getTargetFile("xml-configured-jetty.properties") + "\", \"read\"\n");

            writeGlobalPermissions(out);

            out.write("\n}\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IO.close(out);
            IO.close(writer);
        }
    }

    public void createJettyHomePolicyFile(String classesDir)
    {
        FileWriter writer = null;
        BufferedWriter out = null;
        try
        {
            writer = new FileWriter(OS.separators(_policyDirectory + "/jetty-home.policy"));
            out = new BufferedWriter(writer);
            out.write("grant codebase \"file:" + classesDir + "-\" {\n\n");

            out.write("   permission java.io.FilePermission \"" + _jettyHome + "/-\", \"read,write,delete\"\n");
            out.write("   permission java.io.FilePermission \"" + MavenTestingUtils.getTargetFile("xml-configured-jetty.properties") + "\", \"read\"\n");

            writeCorePermissions(out);

            out.write("\n}\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IO.close(out);
            IO.close(writer);
        }
    }

    public void createJettyRepoPolicyFile(String jettyJar)
    {
        FileWriter writer = null;
        BufferedWriter out = null;
        try
        {
            writer = new FileWriter(OS.separators(_policyDirectory + "/base-repo.policy"));
            out = new BufferedWriter(writer);

            URI jarUri = new URI(jettyJar);
            URI repoBaseUri = jarUri.resolve("../../../");

            out.write("grant codebase \"" + repoBaseUri.toString() + "-\" {\n\n");

            out.write("   permission java.io.FilePermission \"" + _jettyHome + "/-\", \"read,write,delete\"\n");
            out.write("   permission java.io.FilePermission \"" + MavenTestingUtils.getTargetFile("xml-configured-jetty.properties") + "\", \"read\"\n");

            writeCorePermissions(out);

            out.write("\n}\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IO.close(out);
            IO.close(writer);
        }
    }

    public void createJettyDirectoryPolicyFile(String jettyLocation)
    {

        FileWriter writer = null;
        BufferedWriter out = null;
        try
        {
            URI jarUri = new URI(jettyLocation);

            File loc = new File(jarUri);

            File policyFileName = new File(jarUri.resolve("../.."));
            System.out.printf("Policy File: %s%n",policyFileName);

            if (loc.isDirectory())
            {
                writer = new FileWriter(OS.separators(_policyDirectory + "/" + policyFileName.getName() + ".policy"));
                out = new BufferedWriter(writer);
                out.write("grant codebase \"" + jarUri.toString() + "-\" {\n\n");

                out.write("   permission java.io.FilePermission \"" + _jettyHome + "/-\", \"read,write,delete\"\n");
                out.write("   permission java.io.FilePermission \"" + MavenTestingUtils.getTargetFile("xml-configured-jetty.properties") + "\", \"read\"\n");

                writeCorePermissions(out);

                out.write("\n}\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IO.close(out);
            IO.close(writer);
        }
    }

    private void writeCorePermissions(Writer out)
    {
        FileReader reader = null;
        BufferedReader buf = null;
        try
        {
            reader = new FileReader(MavenTestingUtils.getTestResourceFile("core-policy.txt"));
            buf = new BufferedReader(reader);

            String line;

            while ((line = buf.readLine()) != null)
            {
                out.write(line + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IO.close(buf);
            IO.close(reader);
        }
    }

    private void writeGlobalPermissions(Writer out)
    {
        FileReader reader = null;
        BufferedReader buf = null;
        try
        {
            reader = new FileReader(MavenTestingUtils.getTestResourceFile("global-policy.txt"));
            buf = new BufferedReader(reader);

            String line;

            while ((line = buf.readLine()) != null)
            {
                out.write(line + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IO.close(buf);
            IO.close(reader);
        }
    }

}

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

public class PolicyFileManager
{

    private String _policyDirectory;
    private String _jettyHome;

    public PolicyFileManager(String policyDirectory, String jettyHome)
    {
        _policyDirectory = policyDirectory;
        _jettyHome = jettyHome;
    }

    
    public void createJettyHomePolicyFile(String classesDir)
    {       
        try
        {                                                     
            BufferedWriter out = new BufferedWriter(new FileWriter(_policyDirectory + File.separator + "jetty-home.policy"));
            out.write("grant codebase \"file:" + classesDir +"-\" {\n\n");
            
            out.write("   permission java.io.FilePermission \"" + _jettyHome + "/-\", \"read,write\"\n" );
            out.write("   permission java.io.FilePermission \"" + MavenTestingUtils.getTargetFile("xml-configured-jetty.properties") + "\", \"read\"\n" );

            writeCorePermissions(out);
            
            out.write("\n}\n");
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void createJettyRepoPolicyFile(String jettyJar)
    {
        
        try
        {
            URI jarUri = new URI(jettyJar);
            
            URI repoBaseUri = jarUri.resolve("../../../../../");         
            File jarFile = new File(jarUri);
                               
            BufferedWriter out = new BufferedWriter(new FileWriter(_policyDirectory + File.separator + "base-repo.policy"));
            out.write("grant codebase \"" + repoBaseUri.toString() +"-\" {\n\n");
            
            out.write("   permission java.io.FilePermission \"" + _jettyHome + "/-\", \"read,write\"\n" );
            out.write("   permission java.io.FilePermission \"" + MavenTestingUtils.getTargetFile("xml-configured-jetty.properties") + "\", \"read\"\n" );

            
            writeCorePermissions(out);

            out.write("\n}\n");
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    
    public void createJettyDirectoryPolicyFile(String jettyLocation)
    {
        
        try
        {
            URI jarUri = new URI(jettyLocation);
            
            File loc = new File(jarUri);
            
            File policyFileName = new File(jarUri.resolve("../..")); 
            
            if (loc.isDirectory())
            {
                BufferedWriter out = new BufferedWriter(new FileWriter(_policyDirectory + File.separator + policyFileName.getName() + ".policy"));
                out.write("grant codebase \"" + jarUri.toString() + "-\" {\n\n");

                out.write("   permission java.io.FilePermission \"" + _jettyHome + "/-\", \"read,write\"\n");
                out.write("   permission java.io.FilePermission \"" + MavenTestingUtils.getTargetFile("xml-configured-jetty.properties") + "\", \"read\"\n");

                writeCorePermissions(out);

                out.write("\n}\n");
                out.close();
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
    }
    
    private void writeCorePermissions(Writer out)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(MavenTestingUtils.getTestResourceFile("core-policy.txt")));
            
            String line;
            
            while ((line = reader.readLine()) != null)
            {
                out.write(line + "\n");
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
}

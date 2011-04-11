package org.mortbay.jetty.tests.policy.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.toolchain.test.FS;
import org.eclipse.jetty.toolchain.test.IO;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.OS;
import org.eclipse.jetty.toolchain.test.PathAssert;
import org.eclipse.jetty.toolchain.test.TestingDir;
import org.junit.Assert;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * Basic executor for the testable Jetty Distribution.
 * <p>
 * Allows for a test specific directory, that is a copied jetty-distribution, and then modified for the test specific
 * testing required.
 */
public class JettyProcess
{
    private File jettyHomeDir;
    private Process pid;
    private URI baseUri;
    private String jmxUrl;
    private boolean _debug = false;
    private String[] _jvmArgs = null;

    /**
     * Setup the JettyHome as belonging in a testing directory associated with a testing clazz.
     * 
     * @param clazz
     *            the testing class using this JettyProcess
     * @throws IOException
     *             if unable to copy unpacked distribution into place for the provided testing directory
     */
    public JettyProcess(Class<?> clazz) throws IOException
    {
        this.jettyHomeDir = MavenTestingUtils.getTargetTestingDir(clazz,"jettyHome");
        copyBaseDistro();
    }

    /**
     * Setup the JettyHome as belonging to a specific testing method directory
     * 
     * @param testdir
     *            the testing directory to use as the JettyHome for this JettyProcess
     * @throws IOException
     *             if unable to copy unpacked distribution into place for the provided testing directory
     */
    public JettyProcess(TestingDir testdir) throws IOException
    {
        this.jettyHomeDir = testdir.getDir();
        copyBaseDistro();
    }

    /**
     * 
     * @throws IOException
     *             if unable to copy unpacked distribution into place for the provided testing directory
     */
    private void copyBaseDistro() throws IOException
    {
        // The outputDirectory for the maven side dependency:unpack goal.
        File distroUnpackDir = MavenTestingUtils.getTargetFile("jetty-distro");
        PathAssert.assertDirExists("jetty-distribution dependency:unpack",distroUnpackDir);

        // The actual jetty-distribution-${version} directory is under this directory.
        // Lets find it.
        File subdirs[] = distroUnpackDir.listFiles(new FileFilter()
        {
            Pattern pat = Pattern.compile("jetty-distribution-[0-9]+\\.[0-9A-Z.-]*");

            public boolean accept(File path)
            {
                if (!path.isDirectory())
                {
                    return false;
                }

                Matcher mat = pat.matcher(path.getName());
                return mat.matches();
            }
        });

        if (subdirs.length == 0)
        {
            // No jetty-distribution found.
            StringBuilder err = new StringBuilder();
            err.append("No target/jetty-distro/jetty-distribution-${version} directory found.");
            err.append("\n  To fix this, run 'mvn process-test-resources' to create the directory.");
            throw new IOException(err.toString());
        }

        if (subdirs.length != 1)
        {
            // Too many jetty-distributions found.
            StringBuilder err = new StringBuilder();
            err.append("Too many target/jetty-distro/jetty-distribution-${version} directories found.");
            for (File dir : subdirs)
            {
                err.append("\n  ").append(dir.getAbsolutePath());
            }
            err.append("\n  To fix this, run 'mvn clean process-test-resources' to recreate the target/jetty-distro directory.");
            throw new IOException(err.toString());
        }

        File distroSrcDir = subdirs[0];
        FS.ensureEmpty(jettyHomeDir);
        System.out.printf("Copying Jetty Distribution: %s%n",distroSrcDir.getAbsolutePath());
        System.out.printf("            To Testing Dir: %s%n",jettyHomeDir.getAbsolutePath());
        IO.copyDir(distroSrcDir,jettyHomeDir);
    }

    /**
     * Return the $(jetty.home) directory being used for this JettyProcess
     * 
     * @return the jetty.home directory being used
     */
    public File getJettyHomeDir()
    {
        return this.jettyHomeDir;
    }

    /**
     * Copy a war file from ${project.basedir}/target/test-wars/${testWarFilename} into the ${jetty.home}/webapps/
     * directory
     * 
     * @param testWarFilename
     *            the war file to copy (must exist)
     * @throws IOException
     *             if unable to copy the war file.
     */
    public void copyTestWar(String testWarFilename) throws IOException
    {
        File srcWar = MavenTestingUtils.getTargetFile("test-wars/" + testWarFilename);
        File destWar = new File(jettyHomeDir,OS.separators("webapps/" + testWarFilename));
        FS.ensureDirExists(destWar.getParentFile());
        IO.copyFile(srcWar,destWar);
    }

    /**
     * Copy an arbitrary file from <code>src/test/resources/${resourcePath}</code> to the testing directory.
     * 
     * @param resourcePath
     *            the relative path for file content within the <code>src/test/resources</code> directory.
     * @param outputPath
     *            the testing directory relative output path for the file output (will result in a file with the
     *            outputPath name being created)
     * @throws IOException
     *             if unable to copy resource file
     */
    public void copyResource(String resourcePath, String outputPath) throws IOException
    {
        File srcFile = MavenTestingUtils.getTestResourceFile(resourcePath);
        File destFile = new File(jettyHomeDir,OS.separators(outputPath));
        FS.ensureDirExists(destFile.getParentFile());
        IO.copyFile(srcFile,destFile);
    }

    /**
     * Copy an arbitrary file from <code>target/test-libs/${libFilename}</code> to the testing directory.
     * 
     * @param libFilename
     *            the <code>target/test-libs/${libFilename}</code> to copy
     * @param outputPath
     *            the destination testing directory relative output path for the lib. (will result in a file with the
     *            outputPath name being created)
     * @throws IOException
     *             if unable to copy lib
     */
    public void copyLib(String libFilename, String outputPath) throws IOException
    {
        File srcLib = MavenTestingUtils.getTargetFile("test-libs/" + libFilename);
        File destLib = new File(jettyHomeDir,OS.separators(outputPath));
        FS.ensureDirExists(destLib.getParentFile());
        IO.copyFile(srcLib,destLib);
    }

    /**
     * Delete a File or Directory found in the ${jetty.home} directory.
     * 
     * @param path
     *            the path to delete. (can be a file or directory)
     */
    public void delete(String path)
    {
        File jettyPath = new File(jettyHomeDir,OS.separators(path));
        FS.delete(jettyPath);
    }

    /**
     * Return the baseUri being used for this Jetty Process Instance.
     * 
     * @return the base URI for this Jetty Process Instance.
     */
    public URI getBaseUri()
    {
        return this.baseUri;
    }

    /**
     * Return the JMX URL being used for this Jetty Process Instance.
     * 
     * @return the JMX URL for this Jetty Process Instance.
     */
    public String getJmxUrl()
    {
        return this.jmxUrl;
    }

    /**
     * Take the directory contents from ${project.basedir}/src/test/resources/${testConfigName}/ and copy it over
     * whatever happens to be at ${jetty.home}
     * 
     * @param testConfigName
     *            the src/test/resources/ directory name to use as the source diretory for the configuration we are
     *            interested in.
     * @throws IOException
     *             if unable to copy directory.
     */
    public void overlayConfig(String testConfigName) throws IOException
    {
        File srcDir = MavenTestingUtils.getTestResourceDir(testConfigName);
        IO.copyDir(srcDir,jettyHomeDir);
    }

    /**
     * Start the jetty server
     * 
     * @throws IOException
     *             if unable to start the server.
     */
    public void start() throws IOException
    {
        List<String> commands = new ArrayList<String>();
        commands.add(getJavaBin());
        
        if (_jvmArgs != null)
        {
            for ( String arg : _jvmArgs )
            {
                commands.add(arg);
            }
        }
        
        commands.add("-jar");
        commands.add("start.jar");
        commands.add("jetty.port=0");
        if (_debug)
        {
           commands.add("-D.DEBUG=true");
        }

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(jettyHomeDir);
        // pb.redirectErrorStream(true);

        StringBuilder msg = new StringBuilder();
        msg.append("Executing:");
        for (String command : commands)
        {
            msg.append(" ");
            msg.append(command);
        }
        System.out.println(msg.toString());
        System.out.printf("Working Dir: %s%n",jettyHomeDir.getAbsolutePath());

        this.pid = pb.start();

        ConsoleParser parser = new ConsoleParser();
        List<String[]> jmxList = parser.newPattern("JMX Remote URL: (.*)", 0);
        List<String[]> connList = parser.newPattern("Started [A-Za-z]*Connector@([0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*):([0-9]*)", 1);

        startPump("STDOUT", parser, this.pid.getInputStream());
        startPump("STDERR", parser, this.pid.getErrorStream());

        try
        {
            parser.waitForDone(1,TimeUnit.MINUTES);

            if (jmxList.size() > 0)
            {
                this.jmxUrl = jmxList.get(0)[0];
                System.out.printf("## Found JMX connector at "+this.jmxUrl);
            }

            if (connList.size() > 0)
            {
                String[] params = connList.get(0);
                if(params.length == 2 )
                {
                    this.baseUri = URI.create("http://localhost:"+params[1]+"/");
                }
                System.out.printf("## Found Jetty connector at host: "+params[0]+" port: "+params[1]);
            }

        }
        catch (InterruptedException e)
        {
            pid.destroy();
            Assert.fail("Unable to find required information within time limit");
        }
    }

    public static class ConsoleParser
    {
        private List<ConsolePattern> patterns = new ArrayList<ConsolePattern>();
        private CountDownLatch latch;
        private int count;
        
        public List<String[]> newPattern(String exp, int cnt)
        {
            ConsolePattern pat = new ConsolePattern(exp, cnt);
            patterns.add(pat);
            count += cnt;
            
            return pat.getMatches();
        }
        
        public void parse(String line)
        {           
            for (ConsolePattern pat : patterns)
            {
                Matcher mat = pat.getMatcher(line);
                if (mat.find())
                {
                    int num = 0, count = mat.groupCount();
                    String[] match = new String[count];
                    while(num++ < count)
                    {
                        match[num-1] = mat.group(num);
                    }
                    pat.getMatches().add(match);
                        
                    if (pat.getCount() > 0)
                    {
                        getLatch().countDown();
                    }
                }
            }
        }

        public void waitForDone(long timeout, TimeUnit unit) throws InterruptedException
        {
            getLatch().await(timeout, unit);
        }
        
        private CountDownLatch getLatch()
        {
            synchronized(this)
            {
                if (latch == null)
                {
                    latch = new CountDownLatch(count);
                }
            }
            
            return latch;
        }
    }

    public static class ConsolePattern
    {
        private Pattern pattern;
        private List<String[]> matches;
        private int count;

        ConsolePattern(String exp, int cnt)
        {
            pattern = Pattern.compile(exp);
            matches = new ArrayList<String[]>();
            count = cnt;
        }
        
        public Matcher getMatcher(String line)
        {
            return pattern.matcher(line);
        }

        public List<String[]> getMatches()
        {
            return matches;
        }

        public int getCount()
        {
            return count;
        }
    }
    

    private void startPump(String mode, ConsoleParser parser, InputStream inputStream)
    {
        ConsoleStreamer pump = new ConsoleStreamer(mode,inputStream);
        pump.setParser(parser);
        Thread thread = new Thread(pump,"ConsoleStreamer/" + mode);
        thread.start();
    }
    
    /** 
     * enable debug on the jetty process
     * 
     * @param debug
     */
    public void setDebug(boolean debug)
    {
        _debug = debug;
    }

    /** 
     * @param args
     */
    public void setJVMArgs( String ... args )
    {
        this._jvmArgs = args;
    }
    
    private String getJavaBin()
    {
        String javaexes[] = new String[]
        { "java", "java.exe" };

        File javaHomeDir = new File(System.getProperty("java.home"));
        for (String javaexe : javaexes)
        {
            File javabin = new File(javaHomeDir,OS.separators("bin/" + javaexe));
            if (javabin.exists() && javabin.isFile())
            {
                return javabin.getAbsolutePath();
            }
        }

        Assert.fail("Unable to find java bin");
        return "java";
    }

    /**
     * Stop the jetty server
     */
    public void stop()
    {
        System.out.println("Stopping JettyProcess ...");
        if (pid != null)
        {
            // TODO: maybe issue a STOP instead?
            pid.destroy();
        }
    }

    /**
     * Simple streamer for the console output from a Process
     */
    public static class ConsoleStreamer implements Runnable
    {
        private String mode;
        private BufferedReader reader;
        private ConsoleParser parser;

        public ConsoleStreamer(String mode, InputStream is)
        {
            this.mode = mode;
            this.reader = new BufferedReader(new InputStreamReader(is));
        }

        public void setParser(ConsoleParser connector)
        {
            this.parser = connector;
        }

        public void run()
        {
            String line;
            System.out.printf("ConsoleStreamer/%s initiated%n",mode);
            try
            {
                while ((line = reader.readLine()) != (null))
                {
                    if (parser != null)
                    {
                        parser.parse(line);
                    }
                    System.out.println("[" + mode + "] " + line);
                }
            }
            catch (IOException ignore)
            {
                /* ignore */
            }
            finally
            {
                IO.close(reader);
            }
            System.out.printf("ConsoleStreamer/%s finished%n",mode);
        }
    }
}

package org.mortbay.jetty.webapp.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import org.eclipse.jetty.toolchain.test.IO;
import org.eclipse.jetty.toolchain.test.JettyDistro;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.OS;
import org.eclipse.jetty.toolchain.test.PathAssert;
import org.junit.Assert;

public class LogAssert
{
    public static void assertContainsEntries(JettyDistro jetty, String logPath, String expectedEntriesPath) throws IOException
    {
        File logFile = assertExists(jetty,logPath);

        File expectedEntriesFile = MavenTestingUtils.getTestResourceFile(expectedEntriesPath);
        List<String> expectedEntries = loadExpectedEntries(expectedEntriesFile);

        FileReader reader = null;
        BufferedReader buf = null;
        try
        {
            reader = new FileReader(logFile);
            buf = new BufferedReader(reader);

            String line;
            while ((line = buf.readLine()) != null)
            {
                line = line.trim();
                if (line.length() <= 0)
                {
                    continue; // skip
                }
                removeFoundEntries(expectedEntries,line);
            }

            if (expectedEntries.size() > 0)
            {
                for (String entry : expectedEntries)
                {
                    System.err.println("[Entry Not Found] " + entry);
                }

                Assert.fail("Failed to find " + expectedEntries.size() + " entries (details found in STDERR output on this test case) in the log file at "
                        + logFile.getAbsolutePath());
            }
        }
        finally
        {
            IO.close(buf);
            IO.close(reader);
        }
    }

    private static void removeFoundEntries(List<String> expectedEntries, String line)
    {
        ListIterator<String> iter = expectedEntries.listIterator();
        while (iter.hasNext())
        {
            String entry = iter.next();
            if (line.contains(entry))
            {
                iter.remove();
            }
        }
    }

    private static List<String> loadExpectedEntries(File expectedEntriesFile) throws IOException
    {
        List<String> entries = new ArrayList<String>();
        FileReader reader = null;
        BufferedReader buf = null;
        try
        {
            reader = new FileReader(expectedEntriesFile);
            buf = new BufferedReader(reader);

            String line;
            while ((line = buf.readLine()) != null)
            {
                line = line.trim();
                if (line.length() <= 0)
                {
                    continue; // skip
                }
                entries.add(line);
            }

            return entries;
        }
        finally
        {
            IO.close(buf);
            IO.close(reader);
        }
    }

    public static File assertExists(JettyDistro jetty, String logPath)
    {
        File jettyHome = jetty.getJettyHomeDir();
        File logFile = new File(jettyHome,OS.separators(logPath));
        PathAssert.assertFileExists("Log File",logFile);
        return logFile;
    }

    public static void assertNoLogsRegex(JettyDistro jetty, String regexPath)
    {
        File jettyHome = jetty.getJettyHomeDir();
        int idx = regexPath.lastIndexOf('/');
        String logDirStr = regexPath.substring(0,idx);
        File logDir = new File(jettyHome,OS.separators(logDirStr));

        Pattern pat = Pattern.compile(regexPath.substring(idx + 1));

        System.out.printf("Looking for forbidden regex matches on %s in dir %s%n",pat.pattern(),logDir);
        boolean found = false;
        for (String logfilename : logDir.list())
        {
            if (pat.matcher(logfilename).matches())
            {
                found = true;
                System.out.printf("Found Forbidden Match: %s%n",logfilename);
            }
            else
            {
                System.out.printf("             No Match: %s%n",logfilename);
            }
        }

        Assert.assertFalse("Should NOT have found a match, but did (see STDOUT)",found);
        System.out.printf("Assertion passed: no match found%n");
    }

    public static void assertLogExistsRegex(JettyDistro jetty, String regexPath)
    {
        File jettyHome = jetty.getJettyHomeDir();
        int idx = regexPath.lastIndexOf('/');
        String logDirStr = regexPath.substring(0,idx);
        File logDir = new File(jettyHome,OS.separators(logDirStr));

        Pattern pat = Pattern.compile(regexPath.substring(idx + 1));

        System.out.printf("Looking for regex matches on %s in dir %s%n",pat.pattern(),logDir);
        boolean found = false;
        for (String logfilename : logDir.list())
        {
            if (pat.matcher(logfilename).matches())
            {
                found = true;
                System.out.printf("Found Match: %s%n",logfilename);
            }
            else
            {
                System.out.printf("   No Match: %s%n",logfilename);
            }
        }

        Assert.assertTrue("Should have found a match, but didn't (see STDOUT)",found);
        System.out.printf("Assertion passed: match found%n");
    }
}

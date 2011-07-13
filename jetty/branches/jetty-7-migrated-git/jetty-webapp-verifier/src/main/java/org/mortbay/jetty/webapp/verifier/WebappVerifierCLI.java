package org.mortbay.jetty.webapp.verifier;

import java.io.File;

public class WebappVerifierCLI
{
    private static final String ARG_WEBARCHIVE = "webarchive";
    private static final String ARG_RULESET = "ruleset";

    public static void main(String[] args)
    {
        File webarchiveFile = null;
        File rulesetFile = null;

        for (String arg : args)
        {
            if (arg.startsWith("--" + ARG_WEBARCHIVE + "="))
            {
                webarchiveFile = new File(arg.substring(3 + ARG_WEBARCHIVE.length()));
                continue;
            }
            if (arg.startsWith("--" + ARG_RULESET + "="))
            {
                rulesetFile = new File(arg.substring(3 + ARG_RULESET.length()));
                continue;
            }
        }

        boolean argsValid = true;

        if (webarchiveFile == null)
        {
            argsValid = false;
            System.err.println("ERROR: no " + ARG_WEBARCHIVE + " provided.");
        }
        else if (!webarchiveFile.exists())
        {
            argsValid = false;
            System.err.println("ERROR: File [" + ARG_WEBARCHIVE + "] Not Found: " + webarchiveFile.getAbsolutePath());
        }

        if (rulesetFile == null)
        {
            argsValid = false;
            System.err.println("ERROR: no " + ARG_RULESET + " provided.");
        }
        else if (!rulesetFile.exists())
        {
            argsValid = false;
            System.err.println("ERROR: File [" + ARG_RULESET + "] Not Found: " + rulesetFile.getAbsolutePath());
        }

        if (!argsValid)
        {
            System.out.println("Usage: java -jar jetty-webapp-verifier.jar --" + ARG_WEBARCHIVE + "=<path_to_archive> " + "--" + ARG_RULESET
                    + "=<path_to_ruleset>");
            System.exit(-1);
        }

        try
        {
            // load ruleset
            System.out.println("Loading Ruleset: " + rulesetFile);
            RuleSet ruleset = RuleSet.load(rulesetFile);

            // load webarchive
            System.out.println("Loading Web Archive: " + webarchiveFile);
            WebappVerifier verifier = ruleset.createWebappVerifier(webarchiveFile.toURI());
            // verifier.setWorkDir(tempDir);

            // submit for verification
            System.out.println("Analyzing ...");
            verifier.visitAll();

            // show report
            for (Violation violation : verifier.getViolations())
            {
                System.out.println(violation);
            }
            System.out.println("Done.");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}

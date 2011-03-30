package org.mortbay.jetty.tests.webapp.policy.checkers;

import java.util.Properties;

import org.mortbay.jetty.tests.webapp.policy.Checker;

public interface ICommonCheckerMethods
{
    public void processFooWebappContextChecks(Properties props, Checker checker);

    public void processFooWebappRequestDispatcherChecks(Properties props, Checker checker);

    public void processClassloaderChecks(Properties props, Checker checker);

    public void processExitChecks(Properties props, Checker checker);

    public void processFilesystemChecks(Properties props, Checker checker);

    public void processJettyLogChecks(Properties props, Checker checker);

    public void processLibChecks(Properties props, Checker checker);

    public void processSystemPropertyChecks(Properties props, Checker checker);

}
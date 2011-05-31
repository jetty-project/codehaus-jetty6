package org.mortbay.jetty.test.validation.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class RunResultsListener extends RunListener
{
    private Map<String, List<RunResult>> resultMap;
    private RunResult activeRun;

    public RunResultsListener()
    {
        resultMap = new TreeMap<String, List<RunResult>>();
    }

    public Map<String, List<RunResult>> getResults()
    {
        return resultMap;
    }

    private void saveResult(String className, RunResult rr)
    {
        List<RunResult> runs = resultMap.get(className);
        if (runs == null)
        {
            runs = new ArrayList<RunResult>();
        }
        runs.add(rr);
        resultMap.put(className,runs);
    }

    @Override
    public void testAssumptionFailure(Failure failure)
    {
        activeRun.setAssumptionFailure(failure);
        super.testAssumptionFailure(failure);
    }

    @Override
    public void testFailure(Failure failure) throws Exception
    {
        activeRun.setFailure(failure);
        super.testFailure(failure);
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        // Run for specific test method finished
        saveResult(description.getClassName(),activeRun);
        activeRun = null;
        super.testFinished(description);
    }

    @Override
    public void testIgnored(Description description) throws Exception
    {
        String ignoredClass = description.getClassName();
        RunResult ignoredRun = new RunResult(description);
        ignoredRun.setIgnored(true);
        saveResult(ignoredClass,ignoredRun);
        activeRun = null;
        super.testIgnored(description);
    }

    @Override
    public void testStarted(Description description) throws Exception
    {
        // Run for specific test method started
        activeRun = new RunResult(description);
        super.testStarted(description);
    }
}

package org.mortbay.jetty.test.validation.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class RunResultsListener extends RunListener
{
    private Map<String, List<RunResult>> resultMap;
    private RunResult activeRun;
    private String activeClass;

    public RunResultsListener()
    {
        resultMap = new TreeMap<String, List<RunResult>>();
    }

    public Map<String, List<RunResult>> getResults()
    {
        return resultMap;
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
        List<RunResult> runs = resultMap.get(activeClass);
        if (runs == null)
        {
            runs = new ArrayList<RunResult>();
        }
        runs.add(activeRun);
        resultMap.put(activeClass,runs);
        activeRun = null;
        super.testFinished(description);
    }

    @Override
    public void testIgnored(Description description) throws Exception
    {
        activeRun.setIgnored(description);
        super.testIgnored(description);
    }

    @Override
    public void testRunFinished(Result result) throws Exception
    {
        // Run finished for all tests.
        activeClass = null;
        super.testRunFinished(result);
    }

    @Override
    public void testRunStarted(Description description) throws Exception
    {
        // Run started for all tests.
        activeClass = description.getClassName();
        super.testRunStarted(description);
    }

    @Override
    public void testStarted(Description description) throws Exception
    {
        // Run for specific test method started
        activeClass = description.getClassName();
        activeRun = new RunResult(description);
        super.testStarted(description);
    }
}

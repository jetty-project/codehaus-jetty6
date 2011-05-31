package org.mortbay.jetty.test.validation.junit;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Test Runner for Junit 4 annotated classes, similar in scope to standard Junit 4 test runner, but this implementation
 * will collect the results in a way suitable for reporting by the RemoteAssertServlet later.
 */
public class ContextJunit4TestRunner extends BlockJUnit4ClassRunner
{
    public ContextJunit4TestRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
    }

    private void DEBUG(String msg, FrameworkMethod method)
    {
        DEBUG(msg,method,null);
    }

    private void DEBUG(String msg, FrameworkMethod method, Throwable t)
    {
        System.out.println("##DEBUG## " + msg + ": " + method.getName());
        if (t != null)
        {
            t.printStackTrace(System.out);
        }
    }

    private EachTestNotifier makeNotifier(FrameworkMethod method, RunNotifier notifier)
    {
        Description description = describeChild(method);
        return new EachTestNotifier(notifier,description);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier)
    {
        EachTestNotifier eachNotifier = makeNotifier(method,notifier);
        if (method.getAnnotation(Ignore.class) != null)
        {
            eachNotifier.fireTestIgnored();
            return;
        }

        eachNotifier.fireTestStarted();
        // Setup capture of stdout/stderr
        DEBUG("Fire Test Started",method);
        try
        {
            // run test method
            DEBUG("Run Method",method);
            methodBlock(method).evaluate();
        }
        catch (AssumptionViolatedException e)
        {
            // collection assertion failures
            DEBUG("Collect Failed Assumptions",method,e);
            eachNotifier.addFailedAssumption(e);
        }
        catch (Throwable e)
        {
            // collect tossed exceptions
            DEBUG("Collect Failure",method,e);
            eachNotifier.addFailure(e);
        }
        finally
        {
            // report test success/failure
            DEBUG("Fire Test Finished",method);
            eachNotifier.fireTestFinished();
        }
    }

}

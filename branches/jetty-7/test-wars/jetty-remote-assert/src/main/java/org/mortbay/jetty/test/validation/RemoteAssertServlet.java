package org.mortbay.jetty.test.validation;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.JUnitCore;
import org.mortbay.jetty.test.validation.junit.RunResult;
import org.mortbay.jetty.test.validation.junit.RunResultsListener;

public abstract class RemoteAssertServlet extends HttpServlet
{
    private static final long serialVersionUID = -5707460195745579917L;
    private Map<String, Class<?>> testSuite = new HashMap<String, Class<?>>();

    public void addTestClass(Class<?> testclass)
    {
        testSuite.put(testclass.getSimpleName(),testclass);
    }

    public void addTestClass(String name, Class<?> testclass)
    {
        testSuite.put(name,testclass);
    }

    private void closeIO(Closeable c)
    {
        if (c == null)
        {
            return;
        }

        try
        {
            c.close();
        }
        catch (IOException ignore)
        {
            /* ignore */
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        TestScope scope = new TestScope(req.getPathInfo());
        System.out.println("TestScope: " + scope);

        ServletRequestContext context = new ServletRequestContext(this,req,resp);
        ThreadLocalServletRequestContext.set(context);

        if (!scope.hasClassName())
        {
            List<Class<?>> testlist = new ArrayList<Class<?>>();
            testlist.addAll(testSuite.values());
            Class<?> testClasses[] = testlist.toArray(new Class<?>[0]);
            runTestClasses(context,testClasses);
            return;
        }

        if (scope.hasMethodName())
        {
            // Run Specific Test Method
            respondWithError(resp,HttpServletResponse.SC_NOT_IMPLEMENTED,"Running specific test method not supported (yet).");
            return;
        }
        else
        {
            // Run All Tests in ClassName
            Class<?> testClass = testSuite.get(scope.getClassName());
            if (testClass == null)
            {
                respondWithError(resp,HttpServletResponse.SC_CONFLICT,"Specified Test Class does not exist: " + scope.getClassName());
                return;
            }

            runTestClasses(context,testClass);
        }
    }

    private void respondWithError(HttpServletResponse resp, int responseCode, String msg) throws IOException
    {
        System.err.printf("ERROR[%d]: %s%n",responseCode,msg);
        resp.sendError(responseCode,msg);
    }

    private void runTestClasses(ServletRequestContext context, Class<?>... testClasses) throws IOException
    {
        RunResultsListener resultsListener = new RunResultsListener();

        try
        {
            JUnitCore junit = new JUnitCore();
            junit.addListener(resultsListener);
            junit.run(testClasses);

            writeResultsJson(context.getResponse(),resultsListener.getResults());
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            StringWriter writer = new StringWriter();
            PrintWriter err = new PrintWriter(writer);
            err.printf("%s: %s%n",t.getClass().getName(),t.getMessage());
            t.printStackTrace(err);
            context.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,err.toString());
            return;
        }
    }

    private void writeResultsJson(HttpServletResponse resp, Map<String, List<RunResult>> resultsMap) throws JSONException, IOException
    {
        JSONArray top = new JSONArray();

        for (String className : resultsMap.keySet())
        {
            JSONObject classresults = new JSONObject();
            classresults.put("name",className);
            List<RunResult> results = resultsMap.get(className);
            classresults.put("testCount",results.size());

            JSONArray resultsArray = new JSONArray();
            for (RunResult result : results)
            {
                resultsArray.put(result.toJSON());
            }
            classresults.put("results",resultsArray);

            top.put(classresults);
        }

        resp.setContentType("application/json");

        PrintWriter writer = null;
        try
        {
            writer = resp.getWriter();
            writer.write(top.toString(2));
            writer.flush();
        }
        finally
        {
            closeIO(writer);
        }
    }

}

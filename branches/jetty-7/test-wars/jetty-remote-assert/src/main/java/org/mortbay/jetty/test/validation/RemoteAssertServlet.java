package org.mortbay.jetty.test.validation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.toolchain.test.IO;
import org.json.JSONArray;
import org.json.JSONException;

public abstract class RemoteAssertServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private void respondWithError(HttpServletResponse resp, int responseCode, String msg) throws IOException
    {
        System.err.printf("ERROR[%d]: %s%n",responseCode,msg);
        resp.sendError(responseCode,msg);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // Attempt to figure out what MODE and TESTNAME the incoming GET request wants run.
        String pathInfo = req.getPathInfo();
        if (pathInfo == null)
        {
            respondWithError(resp,HttpServletResponse.SC_NOT_FOUND,"No request.pathInfo found.");
            return;
        }

        if (pathInfo.startsWith("/"))
        {
            pathInfo = pathInfo.substring(1);
        }

        String testName = null;

        // Expecting pathInfo of /{testName}
        String parts[] = pathInfo.split("/");

        // Look for TESTNAME
        if (parts.length >= 1)
        {
            testName = parts[1];
        }

        if (Validation.isBlank(testName))
        {
            // No TESTNAME, Show list of possible TESTNAMES
            respondTestNames(resp);
            return;
        }

        // We now have a valid TESTNAME
        try
        {
            // Attempt to find the TESTNAME in the Check instance
            Class<?> parameterTypes[] = new Class[]
            { RemoteAsserts.class };
            Method testmethod = this.getClass().getDeclaredMethod(testName,parameterTypes);

            // Setup the Context for results
            RemoteAsserts context = new RemoteAsserts(this,req,resp);

            // Executes the Specific Test Method referred to by TESTNAME
            Object args[] = new Object[]
            { context };
            testmethod.invoke(args);

            // Write out results to response stream
            writeResultsJson(resp,context);
        }
        catch (NoSuchMethodException e)
        {
            respondWithError(resp,HttpServletResponse.SC_NOT_FOUND,"No such method: " + testName);
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            StringWriter writer = new StringWriter();
            PrintWriter err = new PrintWriter(writer);
            err.printf("%s: %s%n",t.getClass().getName(),t.getMessage());
            t.printStackTrace(err);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,err.toString());
            return;
        }
    }

    private void writeResultsJson(HttpServletResponse resp, RemoteAsserts context) throws JSONException, IOException
    {
        JSONArray array = new JSONArray();

        for (RemoteAssertResult result : context.getResults())
        {
            array.put(result.toJSON());
        }

        resp.setContentType("application/json");

        PrintWriter writer = null;
        try
        {
            writer = resp.getWriter();
            writer.write(array.toString(2));
            writer.flush();
        }
        finally
        {
            IO.close(writer);
        }
    }

    private void respondTestNames(HttpServletResponse resp)
    {
        resp.setContentType("text/html");

        // TODO: Change this response to json!

        PrintWriter out = null;
        try
        {
            out = resp.getWriter();
            out.println("<html>");
            out.println("<head>");

            String testClassName = this.getClass().getSimpleName();

            out.printf("<title>Available %s Tests</title>%n",testClassName);
            out.println("</head>");

            out.println("<body>");

            out.printf("<h1>Available %s Tests</h1>%n",testClassName);

            List<Method> testmethods = findTestMethods(this);
            if (testmethods.isEmpty())
            {
                out.println("<p>No Test Methods Yet Defined for this Class/Mode</p>");
            }
            else
            {
                String contextBase = this.getServletContext().getContextPath();
                Collections.sort(testmethods,new MethodNameSorter());
                out.println("<ul>");
                for (Method method : testmethods)
                {
                    out.printf("<li><a href=\"%s%s\">%s</a></li>%n",contextBase,method.getName(),method.getName());
                }
                out.println("</ul>");
            }

            out.println("</body>");

            out.println("</html>");
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
        finally
        {
            out.flush();
        }
    }

    public static List<Method> findTestMethods(RemoteAssertServlet testservlet)
    {
        List<Method> methods = new ArrayList<Method>();

        for (Method method : testservlet.getClass().getDeclaredMethods())
        {
            int mod = method.getModifiers();

            if (!Modifier.isPublic(mod) && Modifier.isStatic(mod))
            {
                continue;
            }

            if (Void.TYPE != method.getReturnType())
            {
                continue;
            }

            Class<?> params[] = method.getParameterTypes();
            if (params == null)
            {
                continue;
            }

            if (params.length != 1)
            {
                continue;
            }

            if (RemoteAsserts.class.isAssignableFrom(params[0]))
            {
                methods.add(method);
            }
        }

        return methods;
    }

}

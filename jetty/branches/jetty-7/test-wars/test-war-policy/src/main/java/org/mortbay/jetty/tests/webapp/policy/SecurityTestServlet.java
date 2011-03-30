package org.mortbay.jetty.tests.webapp.policy;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.mortbay.jetty.tests.webapp.policy.checkers.NoSecurityChecker;
import org.mortbay.jetty.tests.webapp.policy.checkers.ParanoidSecurityChecker;
import org.mortbay.jetty.tests.webapp.policy.checkers.PracticalSecurityChecker;

public class SecurityTestServlet extends HttpServlet
{
    private static final long serialVersionUID = -3708318659896702634L;
    private Map<SecurityCheckMode, AbstractSecurityCheck> checkers;

    public SecurityTestServlet()
    {
        super();
        checkers = new HashMap<SecurityCheckMode, AbstractSecurityCheck>();
        checkers.put(SecurityCheckMode.NO_SECURITY,new NoSecurityChecker());
        checkers.put(SecurityCheckMode.PRACTICAL,new PracticalSecurityChecker());
        checkers.put(SecurityCheckMode.PARANOID,new ParanoidSecurityChecker());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        SecurityCheckMode mode = null;

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

        String securityMode = null;
        String testName = null;

        // Expecting pathInfo of {securityMode}/{testName}
        String parts[] = pathInfo.split("/");
        if (parts.length >= 1)
        {
            securityMode = parts[0];
        }
        else
        {
            // Show list of possible test at top level.
            respondSecurityModes(req,resp);
            return;
        }

        mode = SecurityCheckMode.getMode(securityMode);
        if (mode == null)
        {
            // Show list of possible test modes
            respondSecurityModes(req,resp);
            return;
        }

        if (parts.length >= 2)
        {
            testName = parts[1];
        }
        else
        {
            String contextBase = mode.name();
            respondTestNames(contextBase,resp,mode);
            return;
        }

        AbstractSecurityCheck check = checkers.get(mode);
        if (check == null)
        {
            respondWithError(resp,HttpServletResponse.SC_NOT_FOUND,"No checker for 'mode' [" + mode.name() + "] exists yet");
            return;
        }

        try
        {
            SecurityCheckContext context = new SecurityCheckContext(mode,this,req,resp);

            Class<?> parameterTypes[] = new Class[]
            { SecurityCheckContext.class };
            Method testmethod = check.getClass().getDeclaredMethod(testName,parameterTypes);
            Object args[] = new Object[]
            { context };
            testmethod.invoke(check,args);

            writeSecurityStream(resp,context);
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

    private void respondTestNames(String contextBase, HttpServletResponse resp, SecurityCheckMode mode)
    {
        resp.setContentType("text/html");

        PrintWriter out = null;
        try
        {
            out = resp.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.printf("<title>Available %s Tests</title>%n",mode.name());
            out.println("</head>");

            out.println("<body>");

            out.printf("<h1>Available %s Tests</h1>%n",mode.name());

            AbstractSecurityCheck check = checkers.get(mode);

            out.printf("<h2>%s/ - %s</h2>%n",mode.name(),check.getClass().getName());
            writeAvailableTests(out,"",mode,check);

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

    private void respondSecurityModes(HttpServletRequest req, HttpServletResponse resp)
    {
        resp.setContentType("text/html");

        PrintWriter out = null;
        try
        {
            out = resp.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.printf("<title>Available %s Tests</title>%n",req.getPathInfo());
            out.println("</head>");

            out.println("<body>");

            out.printf("<h1>Available %s Tests</h1>%n",req.getPathInfo());

            List<SecurityCheckMode> modes = new ArrayList<SecurityCheckMode>();
            for(SecurityCheckMode mode: checkers.keySet()) {
                modes.add(mode);
            }

            // Sort by mode name
            Collections.sort(modes, new SecurityCheckModeNameSorter());
            
            AbstractSecurityCheck check;
            for(SecurityCheckMode mode: modes) {
                check = checkers.get(mode);

                writeSecurityMode(out,mode,check);
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

    private void writeSecurityMode(PrintWriter out, SecurityCheckMode mode, AbstractSecurityCheck check)
    {
        out.printf("<h2><a href=\"%s/\">%s/</a> - %s</h2>%n",mode.name(),mode.name(),check.getClass().getName());
        String contextBase = String.format("%s/",mode.name());
        writeAvailableTests(out,contextBase,mode,check);
    }

    private void writeAvailableTests(PrintWriter out, String contextBase, SecurityCheckMode mode, AbstractSecurityCheck check)
    {
        List<Method> testmethods = findTestMethods(check);
        if (testmethods.size() <= 0)
        {
            out.println("<p>No Test Methods Yet Defined for this Class/Mode</p>");
            return;
        }

        Collections.sort(testmethods, new MethodNameSorter());

        out.println("<ul>");
        for (Method method : testmethods)
        {
            out.printf("<li><a href=\"%s%s\">%s</a></li>%n",contextBase,method.getName(),method.getName());
        }
        out.println("</ul>");
    }

    private List<Method> findTestMethods(AbstractSecurityCheck check)
    {
        List<Method> methods = new ArrayList<Method>();

        for (Method method : check.getClass().getDeclaredMethods())
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

            if (SecurityCheckContext.class.isAssignableFrom(params[0]))
            {
                methods.add(method);
            }
        }

        return methods;
    }

    private void writeSecurityStream(HttpServletResponse resp, SecurityCheckContext security) throws IOException, JSONException
    {
        JSONArray array = new JSONArray();

        for (SecurityResult result : security.getResults())
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
            IOUtils.closeQuietly(writer);
        }
    }

    private void respondWithError(HttpServletResponse resp, int responseCode, String msg) throws IOException
    {
        System.err.printf("ERROR[%d]: %s%n",responseCode,msg);
        resp.sendError(responseCode,msg);
    }
}

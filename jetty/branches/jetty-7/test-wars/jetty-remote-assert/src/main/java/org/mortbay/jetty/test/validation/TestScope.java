package org.mortbay.jetty.test.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The scope of the test(s) to run.
 * <p>
 * This is the post-processed PathInfo of the pattern <code>"/{class-name}/{test-name}"</code>, where {class-name} and
 * {test-name} are both optional.
 */
public class TestScope
{
    private static final String PATHINFO_PATTERN = "/([^/]+)";
    private String className;
    private String methodName;

    public TestScope(String pathInfo)
    {
        if (pathInfo == null)
        {
            return;
        }

        Pattern pat = Pattern.compile(PATHINFO_PATTERN);
        Matcher mat = pat.matcher(pathInfo);
        if (mat.find(0))
        {
            className = mat.group(1);
            if (mat.find(mat.end(0)))
            {
                methodName = mat.group(1);
            }
        }
    }

    public String getClassName()
    {
        return className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public boolean hasClassName()
    {
        return className != null;
    }

    public boolean hasMethodName()
    {
        return className != null;
    }

    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append("Scope: ");
        b.append("Class=");
        if (hasClassName())
        {
            b.append(className);
        }
        else
        {
            b.append("*");
        }
        b.append(", Method=");
        if (hasMethodName())
        {
            b.append(methodName);
        }
        else
        {
            b.append("*");
        }
        return b.toString();
    }
}

package org.mortbay.jetty.tests.webapp.policy;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

public class SecurityResult
{
    /**
     * Unique id for the specific test. (should not be duplicated in a single test run)
     */
    private String id;

    private String message;
    private boolean success = false;
    private String expectedValue;
    private String actualValue;
    private Throwable cause;

    public SecurityResult(String idFormat, Object... idArgs)
    {
        this.id = String.format(idFormat,idArgs);
    }

    public void assertEquals(String msgPrefix, boolean expected, boolean actual)
    {
        this.expectedValue = human(expected);
        this.actualValue = human(actual);

        this.success = true;
        try
        {
            this.message = String.format("%s: Comparing %s to %s",msgPrefix,this.expectedValue,this.actualValue);
            Assert.assertEquals(expected,actual);
        }
        catch (AssertionError e)
        {
            this.success = false;
            this.message = String.format("%s: %s",msgPrefix,e.getMessage());
            this.cause = e;
        }
    }

    public void assertEquals(String msgPrefix, Object expected, Object actual)
    {
        this.expectedValue = human(expected);
        this.actualValue = human(actual);

        this.success = true;
        try
        {
            this.message = String.format("%s: Comparing %s to %s",msgPrefix,this.expectedValue,this.actualValue);
            Assert.assertEquals(expected,actual);
        }
        catch (AssertionError e)
        {
            this.success = false;
            this.message = String.format("%s: %s",msgPrefix,e.getMessage());
            this.cause = e;
        }
    }

    public void assertNotNull(String msgPrefix, Object obj)
    {
        this.expectedValue = "not null";
        this.actualValue = (obj == null)?"not null":"null";

        this.success = true;
        try
        {
            this.message = String.format("%s: should not be null",msgPrefix);
            Assert.assertNotNull(obj);
        }
        catch (AssertionError e)
        {
            this.success = false;
            this.message = String.format("%s: %s",msgPrefix,e.getMessage());
            this.cause = e;
        }
    }

    public void failure(Throwable t)
    {
        this.success = false;
        this.cause = t;
        if (this.message == null)
        {
            this.message = String.format("(%s) %s",t.getClass().getName(),t.getMessage());
        }
    }

    /**
     * Got an expected exception.
     */
    public void successExpected(Throwable t)
    {
        this.success = true;
        this.cause = t;
        if (this.message == null)
        {
            this.message = String.format("Expected Throwable encountered: %s",t.getClass().getName());
        }
    }

    public String getActual()
    {
        return actualValue;
    }

    public Throwable getCause()
    {
        return cause;
    }

    public String getExpected()
    {
        return expectedValue;
    }

    public String getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    private String human(Object obj)
    {
        if (obj == null)
        {
            return "<null>";
        }

        if (obj instanceof String)
        {
            return "\"" + obj + "\"";
        }

        return "<" + obj.toString() + ">";
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setActual(String actual)
    {
        this.actualValue = actual;
    }

    public void setExpected(String expected)
    {
        this.expectedValue = expected;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setMessage(String format, Object... args)
    {
        this.message = String.format(format,args);
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();

        json.put("id",this.id);
        json.put("success",this.success);
        json.put("message",this.message);
        json.put("expected",this.expectedValue);
        json.put("actual",this.actualValue);
        JSONObject causeObj = new JSONObject();
        if (cause != null)
        {
            json.put("class",cause.getClass().getName());
            StringWriter stack = new StringWriter();
            cause.printStackTrace(new PrintWriter(stack));
            json.put("stacktrace",stack.toString());
        }
        json.put("cause",causeObj);

        return json;
    }

}

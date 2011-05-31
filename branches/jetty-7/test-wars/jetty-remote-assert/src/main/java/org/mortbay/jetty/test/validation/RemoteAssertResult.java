package org.mortbay.jetty.test.validation;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

class RemoteAssertResult
{
    /**
     * The test method that was executed.
     */
    private String _testName;
    /**
     * Unique id for the specific test method.
     */
    private String _id;
    private String _description;
    private String _message;
    private boolean _success = false;
    private String _expectedValue;
    private String _actualValue;
    private Throwable _cause;

    public RemoteAssertResult(String idFormat, Object... idArgs)
    {
        this._id = String.format(idFormat,idArgs);
    }

    public void assertEquals(String msgPrefix, boolean expected, boolean actual)
    {
        this._expectedValue = human(expected);
        this._actualValue = human(actual);

        this._success = true;
        try
        {
            this._message = String.format("%s: Comparing %s to %s",msgPrefix,this._expectedValue,this._actualValue);
            Assert.assertEquals(expected,actual);
        }
        catch (AssertionError e)
        {
            this._success = false;
            this._message = String.format("%s: %s",msgPrefix,e.getMessage());
            this._cause = e;
        }
    }

    public void assertEquals(String msgPrefix, Object expected, Object actual)
    {
        this._expectedValue = human(expected);
        this._actualValue = human(actual);

        this._success = true;
        try
        {
            this._message = String.format("%s: Comparing %s to %s",msgPrefix,this._expectedValue,this._actualValue);
            Assert.assertEquals(expected,actual);
        }
        catch (AssertionError e)
        {
            this._success = false;
            this._message = String.format("%s: %s",msgPrefix,e.getMessage());
            this._cause = e;
        }
    }

    public void assertNotNull(String msgPrefix, Object obj)
    {
        this._success = true;
        try
        {
            Assert.assertNotNull(obj);
        }
        catch (AssertionError e)
        {
            this._expectedValue = "not null";
            this._actualValue = (obj == null)?"null":human(obj);

            this._success = false;
            this._message = String.format("%s: %s",msgPrefix,e.getMessage());
            this._cause = e;
        }
    }

    public void assertNull(String msgPrefix, Object obj)
    {
        this._expectedValue = "null";
        this._actualValue = (obj == null)?"null":human(obj);

        this._success = true;
        try
        {
            this._message = String.format("%s: should be null",msgPrefix);
            Assert.assertNull(obj);
        }
        catch (AssertionError e)
        {
            this._success = false;
            this._message = String.format("%s: %s",msgPrefix,e.getMessage());
            this._cause = e;
        }
    }

    public void failure(String messageFormat, Object... messageArgs)
    {
        this._success = false;
        this._message = String.format(messageFormat,messageArgs);
    }

    public void failure(Throwable t)
    {
        this._success = false;
        this._cause = t;
        if (this._message == null)
        {
            this._message = String.format("(%s) %s",t.getClass().getName(),t.getMessage());
        }
    }

    public String getActual()
    {
        return _actualValue;
    }

    public Throwable getCause()
    {
        return _cause;
    }

    public String getDescription()
    {
        return _description;
    }

    public String getExpected()
    {
        return _expectedValue;
    }

    public String getId()
    {
        return _id;
    }

    public String getMessage()
    {
        return _message;
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
        return _success;
    }

    public void setActual(String actual)
    {
        this._actualValue = actual;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public void setExpected(String expected)
    {
        this._expectedValue = expected;
    }

    public void setMessage(String message)
    {
        this._message = message;
    }

    public void setMessage(String format, Object... args)
    {
        this._message = String.format(format,args);
    }

    public void setSuccess(boolean success)
    {
        this._success = success;
    }

    public void success(String messageFormat, Object... messageArgs)
    {
        this._success = true;
        this._message = String.format(messageFormat,messageArgs);
    }

    /**
     * Got an expected exception.
     */
    public void successExpected(Throwable t)
    {
        this._success = true;
        this._cause = t;
        if (this._message == null)
        {
            this._message = String.format("Expected Throwable encountered: %s",t.getClass().getName());
        }
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();

        json.put("id",this._id);
        if ( _description != null )
        {
            json.put("description",_description);
        }
        json.put("success",this._success);
        if (!this._success)
        {
            if (this._message != null)
            {
                json.put("message",this._message);
            }
            if (this._expectedValue != null)
            {
                json.put("expected",this._expectedValue);
            }
            if (this._actualValue != null)
            {
                json.put("actual",this._actualValue);
            }

            JSONObject causeObj = new JSONObject();
            if (_cause != null)
            {
                causeObj.put("class",_cause.getClass().getName());
                StringWriter stack = new StringWriter();
                _cause.printStackTrace(new PrintWriter(stack));
                causeObj.put("stacktrace",stack.toString());
            }
            json.put("cause",causeObj);
        }

        return json;
    }

}

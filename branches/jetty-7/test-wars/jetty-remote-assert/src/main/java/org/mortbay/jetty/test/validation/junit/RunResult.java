package org.mortbay.jetty.test.validation.junit;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class RunResult
{
    private Description description;
    private Failure failure;
    private boolean assumptionFailure = true;
    private boolean success = true;
    private Description ignored;

    public RunResult(Description description)
    {
        this.description = description;
        this.success = true;
    }

    public void setAssumptionFailure(Failure failure)
    {
        this.failure = failure;
        this.assumptionFailure = true;
        this.success = false;
    }

    public void setFailure(Failure failure)
    {
        this.failure = failure;
        this.assumptionFailure = false;
        this.success = false;
    }

    public void setIgnored(Description description)
    {
        this.ignored = description;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put("className",description.getClassName());
        json.put("methodName",description.getMethodName());
        if (ignored != null)
        {
            json.put("ignored",true);
            json.put("ignoredLabel",ignored.getDisplayName());
            return json;
        }

        json.put("success",success);
        if (!success)
        {
            json.put("assumptionFailure",assumptionFailure);
            if (failure != null)
            {
                JSONObject failureJson = new JSONObject();
                failureJson.put("message",failure.getMessage());
                failureJson.put("description",failure.getDescription());
                failureJson.put("header",failure.getTestHeader());
                failureJson.put("trace",failure.getTrace());
                json.put("failure",failureJson);
            }
        }

        return json;
    }
}

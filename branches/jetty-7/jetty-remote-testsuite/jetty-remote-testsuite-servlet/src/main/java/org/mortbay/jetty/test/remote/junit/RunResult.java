package org.mortbay.jetty.test.remote.junit;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class RunResult
{
    private Description description;
    private Failure failure;
    private boolean assumptionFailure = true;
    private boolean success = true;
    private boolean ignored = false;

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

    public void setIgnored(boolean flag)
    {
        this.ignored = flag;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put("className",description.getClassName());
        json.put("methodName",description.getMethodName());
        if (ignored)
        {
            json.put("ignored",true);
            Ignore ig = description.getAnnotation(Ignore.class);
            if (ig != null)
            {
                String reason = ig.value();
                json.put("ignoredReason",reason == null?"":reason);
            }
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
                failureJson.put("header",failure.getTestHeader());
                failureJson.put("trace",failure.getTrace());
                json.put("failure",failureJson);
            }
        }

        return json;
    }
}

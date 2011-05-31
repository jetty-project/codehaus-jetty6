package org.mortbay.jetty.test.validation.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteAssertResults
{
    public static class TestClass
    {
        private String className;
        private List<TestResult> results;

        public TestClass(JSONObject robj) throws JSONException
        {
            this.className = robj.getString("name");
            this.results = new ArrayList<TestResult>();

            // parse results
            JSONArray arr = robj.getJSONArray("results");
            int len = arr.length();
            for (int i = 0; i < len; i++)
            {
                JSONObject joresult = arr.getJSONObject(i);
                results.add(new TestResult(joresult));
            }
        }

        public String getClassName()
        {
            return className;
        }

        public List<TestResult> getResults()
        {
            return results;
        }

        public int getTestCount()
        {
            return results.size();
        }
    }

    public static class TestResult
    {
        private String className;
        private String methodName;
        private boolean assumptionFailure;
        private boolean success;
        private boolean ignored;
        private String failureHeader;
        private String failureMessage;
        private String failureTrace;

        public TestResult(JSONObject jobj) throws JSONException
        {
            this.className = jobj.getString("className");
            this.methodName = jobj.getString("methodName");
            this.ignored = jobj.optBoolean("ignored",false);
            if (this.ignored)
            {
                return;
            }

            this.assumptionFailure = jobj.optBoolean("assumptionFailure",false);
            this.success = jobj.optBoolean("success",true);

            JSONObject failure = jobj.optJSONObject("failure");
            if (failure != null)
            {
                this.failureHeader = failure.optString("header");
                this.failureMessage = failure.optString("message");
                this.failureTrace = failure.optString("trace");
            }
        }

        public String getClassName()
        {
            return className;
        }

        public String getFailureHeader()
        {
            return failureHeader;
        }

        public String getFailureMessage()
        {
            return failureMessage;
        }

        public String getFailureTrace()
        {
            return failureTrace;
        }

        public String getMethodName()
        {
            return methodName;
        }

        public boolean isAssumptionFailure()
        {
            return assumptionFailure;
        }

        public boolean isIgnored()
        {
            return ignored;
        }

        public boolean isSuccess()
        {
            return success;
        }
    }

    private Map<String, TestClass> resultMap = new TreeMap<String, TestClass>();

    public RemoteAssertResults(String rawJsonString) throws JSONException
    {
        System.out.println("JSON:\n" + rawJsonString);
        JSONArray arr = new JSONArray(rawJsonString);
        int count = arr.length();
        for (int i = 0; i < count; i++)
        {
            JSONObject robj = arr.getJSONObject(i);
            TestClass tc = new TestClass(robj);
            resultMap.put(tc.className,tc);
        }
    }

    public TestClass getTestClass(String name)
    {
        return resultMap.get(name);
    }

    public int getTestClassCount()
    {
        return resultMap.size();
    }
}

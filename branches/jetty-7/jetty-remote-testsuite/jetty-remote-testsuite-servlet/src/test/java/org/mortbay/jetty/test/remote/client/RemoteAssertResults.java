package org.mortbay.jetty.test.remote.client;

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
        private Map<String, TestResult> results;

        public TestClass(JSONObject robj) throws JSONException
        {
            this.className = robj.getString("name");
            this.results = new TreeMap<String, TestResult>();

            // parse results
            JSONArray arr = robj.getJSONArray("results");
            int len = arr.length();
            for (int i = 0; i < len; i++)
            {
                JSONObject joresult = arr.getJSONObject(i);
                TestResult tr = new TestResult(joresult);
                results.put(tr.getMethodName(),tr);
            }
        }

        public String getClassName()
        {
            return className;
        }

        public Map<String, TestResult> getResults()
        {
            return results;
        }

        public int getTestCount()
        {
            return results.size();
        }

        public TestResult getTestResult(String methodName)
        {
            return results.get(methodName);
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

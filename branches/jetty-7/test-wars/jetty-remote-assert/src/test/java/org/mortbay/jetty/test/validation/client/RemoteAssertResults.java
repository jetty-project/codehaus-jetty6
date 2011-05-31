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
    public static class Details
    {
        private String className;
        private String methodName;
        private boolean assumptionFailure;
        private boolean success;
        private boolean ignored;
        private String failureHeader;
        private String failureMessage;
        private String failureTrace;
    }

    private Map<String, List<Details>> resultMap = new TreeMap<String, List<Details>>();

    public RemoteAssertResults(String rawJsonString) throws JSONException
    {
        System.out.println("JSON:\n" + rawJsonString);
        JSONArray arr = new JSONArray(rawJsonString);
        int count = arr.length();
        for (int i = 0; i < count; i++)
        {
            JSONObject robj = arr.getJSONObject(i);
            String className = robj.getString("name");
            List<Details> details = new ArrayList<Details>();
            // TODO: map details
            resultMap.put(className,details);
        }
    }

    public int getTestClassCount()
    {
        return resultMap.size();
    }
}

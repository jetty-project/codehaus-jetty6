package org.mortbay.jetty.test.validation.util;

import org.json.JSONArray;
import org.json.JSONObject;

public final class Validation
{
    /**
     * Validates if the string is blank.
     * 
     * @param str
     *            test string
     * @return true if blank (unset, empty, or all whitespace)
     */
    public static boolean isBlank(String str)
    {
        if (str == null)
        {
            return true;
        }
        int len = str.length();
        if (len <= 0)
        {
            return true;
        }
        for (char c : str.toCharArray())
        {
            if ((Character.isWhitespace(c) == false))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates if the string is not blank (must contain non-whitespace)
     * 
     * @param str
     *            test string
     * @return true if not blank (contains non-whitespace)
     */
    public static boolean isNotBlank(String str)
    {
        return !isBlank(str);
    }
    
    /**
     * Validates that all test results have success = true
     * 
     * @param str test string
     * @return true if all test results passed
     */
    public static boolean passes(String str)
    {
        try
        {
            JSONArray jsonArray = new JSONArray(str);
            
            int len = jsonArray.length();
            for (int i = 0; i < len ; ++i)
            {
                JSONObject test = jsonArray.getJSONObject(i);
             
                JSONArray resultArray = test.getJSONArray("results");
                
                int resultCount = resultArray.length();
                
                for ( int j = 0; j < resultCount; ++j )
                {
                    JSONObject result = resultArray.getJSONObject(j);
                    
                    if ( !result.getBoolean("success"))
                    {
                        return false;
                    }   
                }              
            }
            
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        
    }
}

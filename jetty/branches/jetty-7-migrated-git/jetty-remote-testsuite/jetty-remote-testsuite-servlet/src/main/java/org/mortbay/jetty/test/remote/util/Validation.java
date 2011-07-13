package org.mortbay.jetty.test.remote.util;


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
}

package org.mortbay.cometd.filter;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * @author gregw
 *
 */
public class RegexFilter extends JSONDataFilter
{
    Pattern[] _patterns;
    String[] _replaces;
    
    /**
     * Assumes the init object is an Array of 2 element Arrays:  [regex,replacement].
     * if the regex replacement string is null, then an IllegalStateException is thrown if
     * the pattern matches.
     */
    public void init(Object init)
    {
        super.init(init);
        
        _patterns=new Pattern[Array.getLength(init)];
        _replaces=new String[_patterns.length];
        
        for (int i=0;i<_patterns.length;i++)
        {
            Object entry = Array.get(init,i);
            _patterns[i]=Pattern.compile((String)Array.get(entry,0));
            _replaces[i]=(String)Array.get(entry,1);
        }
        
    }

    protected Object filterString(String string)
    {
        for (int i=0;i<_patterns.length;i++)
        {
            if (_replaces[i]!=null)
                string=_patterns[i].matcher(string).replaceAll(_replaces[i]);
            else if (_patterns[i].matcher(string).matches())
                throw new IllegalStateException("matched "+_patterns[i]+" in "+string);
        }
        return string;
    }
}

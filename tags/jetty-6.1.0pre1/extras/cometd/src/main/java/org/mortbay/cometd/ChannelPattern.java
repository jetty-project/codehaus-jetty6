package org.mortbay.cometd;

import java.util.regex.Pattern;

import org.mortbay.util.StringUtil;

/** Channel Pattern
 * A string matcher that matches channel name as follows: <pre>
 *  /channel/name     - absolute match
 *  /channel,/other   - coma separated list of patterns
 *  /foo*bah/blah     - wild card not including /
 *  /foo**bah         - wild card including /
 * @author gregw
 *
 */
public class ChannelPattern
{
    String _template;
    Pattern _pattern;
    
    public ChannelPattern(String pattern)
    {
        _template=pattern;
        if (pattern.indexOf('(')>=0 ||
            pattern.indexOf(')')>=0 ||
            pattern.indexOf('|')>=0 ||
            pattern.indexOf('[')>=0 ||
            pattern.indexOf(']')>=0 ||
            pattern.indexOf('?')>=0 ||
            pattern.indexOf('+')>=0 ||
            pattern.indexOf('{')>=0 ||
            pattern.indexOf('\\')>=0)
            throw new IllegalArgumentException("Illegal pattern "+pattern);
                
        pattern = "("+pattern+")";
        pattern = StringUtil.replace(pattern,",",")|(");
        pattern = StringUtil.replace(pattern,"/**/","(/|/([^,]{1,}/))");
        pattern = StringUtil.replace(pattern,"**","[^,]{0,}");
        pattern = StringUtil.replace(pattern,"*","[^/,]{0,}");
        
        _pattern=Pattern.compile(pattern);
    }

    public boolean matches(String channel)
    {
        if (channel.endsWith("/"))
            throw new IllegalArgumentException("Bad channel name: "+channel);
        return _pattern.matcher(channel).matches();
    }
    
    
    public boolean equals(Object obj)
    {
        if (obj instanceof ChannelPattern)
            return ((ChannelPattern)obj)._template.equals(_template);
        return false;
    }

    public int hashCode()
    {
        return _template.hashCode();
    }

    public String toString()
    {
        return _template+"["+_pattern+"]";
    }
}

package org.mortbay.jetty;
import javax.servlet.http.Cookie;

import org.mortbay.log.Log;
import org.mortbay.util.LazyList;
import org.mortbay.util.URIUtil;


/* ------------------------------------------------------------ */
/** Cookie parser
 * <p>Optimized stateful cookie parser.  Cookies fields are added with the
 * {@link #addCookieField(String)} method and parsed on the next subsequent
 * call to {@link #getCookies()}.
 * If the added fields are identical to those last added (as strings), then the 
 * cookies are not re parsed.
 * @author gregw
 *
 */
public class CookieCutter
{
    private static final byte STATE_DELIMITER = 1;
    private static final byte STATE_NAME = 2;
    private static final byte STATE_VALUE = 4;
    private static final byte STATE_QUOTED_VALUE = 8;
    private static final byte STATE_UNQUOTED_VALUE = 16;

    private Cookie[] _cookies;
    private String[] _fields;
    int _added=0;
    boolean _dirty;
 
    
    public Cookie[] getCookies()
    {
        if (_added>0) 
        {
            if (!_dirty && _added==_fields.length)
            {
                // same cookies as last time!
                _added=0;
                return _cookies;
            }
            
            parseFields();
        }
        return _cookies;
    }
    
    public void setCookies(Cookie[] cookies)
    {
        _dirty=false;
        _added=0;
        _cookies=cookies;
    }
    
    public void reset()
    {
        _fields=null;
        _cookies=null;
    }
    
    public void addCookieField(String f)
    {
        if (!_dirty &&
            _fields!=null && 
            _fields.length>_added &&
            _fields[_added].equals(f))
        {
            _added++;
            return;
        }
        
        if (_dirty)
        {
            _added++;
            _fields=(String[])LazyList.addToArray(_fields,f,String.class);
        }
        else
        {
            _dirty=true;
            if (_added>0)
            {
                String[] fields=new String[_added+1];
                System.arraycopy(_fields,0,fields,0,_added);
                fields[_added++]=f;
                _fields=fields;
            }
            else
            {
                _fields = new String[]{f};
                _added=1;
            }
            
        }
    }
    
    protected void parseFields()
    {
        Object cookies = null;

        int version = 0;

        // For each cookie field
        for (int f=0;f<_added;f++)
        {
            String hdr = _fields[f];
            
            // Parse the header
            String name = null;
            String value = null;

            Cookie cookie = null;

            byte state = STATE_NAME;
            for (int i = 0, tokenstart = 0, length = hdr.length(); i < length; i++)
            {
                char c = hdr.charAt(i);
                switch (c)
                {
                    case ',':
                    case ';':
                        switch (state)
                        {
                            case STATE_DELIMITER:
                                state = STATE_NAME;
                                tokenstart = i + 1;
                                break;
                            case STATE_UNQUOTED_VALUE:
                                state = STATE_NAME;
                                // TODO remove this old style jetty cookie support (encoding)
                                value = URIUtil.decodePath(hdr.substring(tokenstart, i).trim());
                                tokenstart = i + 1;
                                break;
                            case STATE_NAME:
                                name = hdr.substring(tokenstart, i);
                                value = "";
                                tokenstart = i + 1;
                                break;
                            case STATE_VALUE:
                                state = STATE_NAME;
                                value = "";
                                tokenstart = i + 1;
                                break;
                        }
                        break;
                    case '=':
                        switch (state)
                        {
                            case STATE_NAME:
                                state = STATE_VALUE;
                                name = hdr.substring(tokenstart, i);
                                tokenstart = i + 1;
                                break;
                            case STATE_VALUE:
                                state = STATE_UNQUOTED_VALUE;
                                tokenstart = i;
                                break;
                        }
                        break;
                    case '"':
                        switch (state)
                        {
                            case STATE_VALUE:
                                state = STATE_QUOTED_VALUE;
                                tokenstart = i + 1;
                                break;
                            case STATE_QUOTED_VALUE:
                                state = STATE_DELIMITER;
                                value = hdr.substring(tokenstart, i);
                                break;
                        }
                        break;
                    case ' ':
                    case '\t':
                        break;
                    default:
                        switch (state)
                        {
                            case STATE_VALUE:
                                state = STATE_UNQUOTED_VALUE;
                                tokenstart = i;
                                break;
                            case STATE_DELIMITER:
                                state = STATE_NAME;
                                tokenstart = i;
                                break;
                        }
                }

                if (i + 1 == length)
                {
                    switch (state)
                    {
                        case STATE_UNQUOTED_VALUE:
                            // TODO remove this old style jetty cookie support (encoding)
                            value = URIUtil.decodePath(hdr.substring(tokenstart).trim());
                            break;
                        case STATE_NAME:
                            name = hdr.substring(tokenstart);
                            value = "";
                            break;
                        case STATE_VALUE:
                            value = "";
                            break;
                    }
                }

                if (name != null && value != null)
                {
                    name = name.trim();

                    try
                    {
                        if (name.startsWith("$"))
                        {
                            String lowercaseName = name.toLowerCase();
                            if ("$path".equals(lowercaseName))
                            {
                                cookie.setPath(value);
                            }
                            else if ("$domain".equals(lowercaseName))
                            {
                                cookie.setDomain(value);
                            }
                            else if ("$version".equals(lowercaseName))
                            {
                                version = Integer.parseInt(value);
                            }
                        }
                        else
                        {
                            cookie = new Cookie(name, value);

                            if (version > 0)
                            {
                                cookie.setVersion(version);
                            }

                            cookies = LazyList.add(cookies, cookie);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.ignore(e);
                    }

                    name = null;
                    value = null;
                }
            }
        }

        int l = LazyList.size(cookies);
        if (l>0)
        {
            if (_cookies != null && _cookies.length == l) 
            {
                for (int i = 0; i < l; i++)
                    _cookies[i] = (Cookie) LazyList.get(cookies, i);
            }
            else
                _cookies = (Cookie[]) LazyList.toArray(cookies,Cookie.class);
        }
        
        _added=0;
        _dirty=false;
        
    }
    
}

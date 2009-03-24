//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.server;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.mortbay.jetty.util.LazyList;
import org.mortbay.jetty.util.URIUtil;
import org.mortbay.jetty.util.log.Log;


/* ------------------------------------------------------------ */
/** Cookie parser
 * <p>Optimized stateful cookie parser.  Cookies fields are added with the
 * {@link #addCookieField(String)} method and parsed on the next subsequent
 * call to {@link #getCookies()}.
 * If the added fields are identical to those last added (as strings), then the 
 * cookies are not re parsed.
 * 
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
    HttpServletRequest _request;
    
    public CookieCutter()
    {
        
    }
 
    public CookieCutter(HttpServletRequest request)
    {
        _request = request;
    }
    
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
                                value = hdr.substring(tokenstart, i).trim();
                                if(_request!=null && _request.isRequestedSessionIdFromURL())
                                    value = URIUtil.decodePath(value);
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
                            value = hdr.substring(tokenstart).trim();
                            if(_request!=null && _request.isRequestedSessionIdFromURL())
                                value = URIUtil.decodePath(value);
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

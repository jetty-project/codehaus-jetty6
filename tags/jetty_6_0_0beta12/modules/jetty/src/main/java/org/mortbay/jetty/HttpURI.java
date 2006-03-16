//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;

import java.io.UnsupportedEncodingException;

import org.mortbay.util.MultiMap;
import org.mortbay.util.TypeUtil;
import org.mortbay.util.URIUtil;
import org.mortbay.util.UrlEncoded;


public class HttpURI
{
    byte[] _raw;
    int _scheme;
    int _authority;
    int _host;
    int _port;
    int _path;
    int _param;
    int _query;
    int _fragment;
    int _end;
    
    
    public HttpURI()
    {
        
    }
    
    public HttpURI(String raw)
    {
        byte[] b = raw.getBytes();
        parse(b,0,b.length);
    }
    
    public HttpURI(byte[] raw,int offset, int length)
    {
        parse(raw,offset,length);
    }
    
    public void parse(String raw)
    {
        byte[] b = raw.getBytes();
        parse(b,0,b.length);
    }

    private final static int 
     START=0,
     AUTH_OR_PATH=1,
     SCHEME_OR_PATH=2,
     AUTH=4,
     PATH=5,
     PARAM=6,
     QUERY=7;
    
    public void parse(byte[] raw,int offset, int length)
    {
        _raw=raw;
        int i=offset;
        int e=offset+length;
        int state=START;
        int m=offset;
        _end=offset+length;
        _scheme=offset;
        _authority=offset;
        _host=offset;
        _port=offset;
        _path=offset;
        _param=_end;
        _query=_end;
        _fragment=_end;
        while (i<e)
        {
            char c=(char)(0xff&_raw[i]);
            int s=i++;
            
            switch (state)
            {
                case START:
                {
                    m=s;
                    if (c=='/')
                    {
                        state=AUTH_OR_PATH;
                    }
                    else if (Character.isLetterOrDigit(c))
                    {
                        state=SCHEME_OR_PATH;
                    }
                    else if (c==';')
                    {
                        _param=s;
                        state=PARAM;
                    }
                    else if (c=='?')
                    {
                        _param=s;
                        _query=s;
                        state=QUERY;
                    }
                    else if (c=='#')
                    {
                        _param=s;
                        _query=s;
                        _fragment=s;
                        break;
                    }
                    else
                        throw new IllegalArgumentException(new String(_raw,offset,length));
                    
                    continue;
                }

                case AUTH_OR_PATH:
                {
                    if (c=='/')
                    {
                        _host=i;
                        state=AUTH;
                    }
                    else
                    {
                        _host=m;
                        _port=m;
                        state=PATH;
                    }  
                    continue;
                }
                
                case SCHEME_OR_PATH:
                {
                    // short cut for http and https
                    if (length>6 && c=='t')
                    {
                        if (_raw[offset+3]==':')
                        {
                            s=offset+3;
                            i=offset+4;
                            c=':';
                        }
                        else if (_raw[offset+4]==':')
                        {
                            s=offset+4;
                            i=offset+5;
                            c=':';
                        }
                        else if (_raw[offset+5]==':')
                        {
                            s=offset+5;
                            i=offset+6;
                            c=':';
                        }
                    }
                    
                    
                    if (c==':')
                    {
                        m=i++;
                        _authority=m;
                        _path=m;
                        c=(char)(0xff&_raw[i]);
                        if (c=='/')
                            state=AUTH_OR_PATH;
                        else 
                        {
                            _host=m;
                            _port=m;
                            state=PATH;
                        }
                    }
                    else if (c=='/')
                    {
                        state=PATH;
                    }
                    else if (c==';')
                    {
                        _param=s;
                        state=PARAM;
                    }
                    else if (c=='?')
                    {
                        _param=s;
                        _query=s;
                        state=QUERY;
                    }
                    else if (c=='#')
                    {
                        _param=s;
                        _query=s;
                        _fragment=s;
                        break;
                    }
                    continue;
                }
                
                case AUTH:
                {
                    if (c=='/')
                    {
                        m=s;
                        _path=m;
                        if (_port<=_authority)
                            _port=_path;
                        state=PATH;
                    }
                    else if (c=='@')
                    {
                        _host=i;
                    }
                    else if (c==':')
                    {
                        _port=s;
                    }
                    continue;
                }
                
                case PATH:
                {
                    if (c==';')
                    {
                        _param=s;
                        state=PARAM;
                    }
                    else if (c=='?')
                    {
                        _param=s;
                        _query=s;
                        state=QUERY;
                    }
                    else if (c=='#')
                    {
                        _param=s;
                        _query=s;
                        _fragment=s;
                        break;
                    }
                    continue;
                }
                
                case PARAM:
                {
                    if (c=='?')
                    {
                        _query=s;
                        state=QUERY;
                    }
                    else if (c=='#')
                    {
                        _query=s;
                        _fragment=s;
                        break;
                    }
                    continue;
                }
                
                case QUERY:
                {
                    if (c=='#')
                    {
                        _fragment=s;
                        break;
                    }
                    continue;
                }
                
            }
        }
    }
    
    
    public String getScheme()
    {
        if (_scheme==_authority)
            return null;
        int l=_authority-_scheme;
        if (l==5 && 
            _raw[_scheme]=='h' && 
            _raw[_scheme+1]=='t' && 
            _raw[_scheme+2]=='t' && 
            _raw[_scheme+3]=='p' )
            return HttpSchemes.HTTP;
        if (l==6 && 
            _raw[_scheme]=='h' && 
            _raw[_scheme+1]=='t' && 
            _raw[_scheme+2]=='t' && 
            _raw[_scheme+3]=='p' && 
            _raw[_scheme+4]=='s' )
            return HttpSchemes.HTTPS;
        return new String(_raw,_scheme,_authority-_scheme-1);
    }
    
    public String getAuthority()
    {
        if (_authority==_path)
            return null;
        return new String(_raw,_authority,_path-_authority);
    }
    
    public String getHost()
    {
        if (_host==_port)
            return null;
        return new String(_raw,_host,_port-_host);
    }
    
    public int getPort()
    {
        if (_port==_path)
            return -1;
        return TypeUtil.parseInt(_raw, _port+1, _path-_port-1,10);
    }
    
    public String getPath()
    {
        if (_path==_param)
            return null;
        return new String(_raw,_path,_param-_path);
    }
    
    public String getDecodedPath()
    {
        if (_path==_param)
            return null;
        return URIUtil.decodePath(_raw,_path,_param-_path);
    }
    
    public String getPathAndParam()
    {
        if (_path==_query)
            return null;
        return new String(_raw,_path,_query-_path);
    }
    
    public String getParam()
    {
        if (_param==_query)
            return null;
        return new String(_raw,_param+1,_query-_param-1);
    }
    
    public String getQuery()
    {
        if (_query==_fragment)
            return null;
        return new String(_raw,_query+1,_fragment-_query-1);
    }
    
    public String getFragment()
    {
        if (_fragment==_end)
            return null;
        return new String(_raw,_fragment+1,_end-_fragment-1);
    }

    public void decodeQueryTo(MultiMap parameters, String encoding) 
        throws UnsupportedEncodingException
    {
        if (_query==_fragment)
            return;
        // TODO need to use bytes directly
        UrlEncoded.decodeUtf8To(_raw,_query+1,_fragment-_query-1,parameters);
    }
    
}

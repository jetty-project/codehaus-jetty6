package org.mortbay.jetty.handler.rewrite;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.util.StringMap;

/**
 * MSIE SSL Rule
 * Disable keep alive for SSL from IE5 or IE6 on windows2000
 *  
 * @author gregw
 *
 */
public class MsieSslRule extends Rule
{
    private static final int IEv5 = '5';
    private static final int IEv6 = '6';
    private static StringMap __IE6_BadOS = new StringMap();
    {
        __IE6_BadOS.put("NT 5.0",Boolean.TRUE);
        __IE6_BadOS.put("NT 4.0",Boolean.TRUE);
        __IE6_BadOS.put("98",Boolean.TRUE);
        __IE6_BadOS.put("98; Win 9x 4.90",Boolean.TRUE);
        __IE6_BadOS.put("95",Boolean.TRUE);
        __IE6_BadOS.put("CE",Boolean.TRUE);
    }
    
    public MsieSslRule()
    {
        _handling = false;
        _terminating = false;
    }
    
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if (request.isSecure())
        {
            String user_agent = request.getHeader(HttpHeaders.USER_AGENT);
            
            if (user_agent!=null)
            {
                int msie=user_agent.indexOf("MSIE");
                if (msie>0 && user_agent.length()-msie>5)
                {
                    // Get Internet Explorer Version
                    int ieVersion = user_agent.charAt(msie+5);
                    
                    if ( ieVersion<=IEv5)
                    {
                        response.setHeader(HttpHeaders.CONNECTION, HttpHeaderValues.CLOSE);
                        return target;
                    }

                    if (ieVersion==IEv6)
                    {
                        int windows = user_agent.indexOf("Windows",msie+5);
                        if (windows>0)
                        {
                            int end=user_agent.indexOf(')',windows+8);
                            if(end<0 || __IE6_BadOS.getEntry(user_agent,windows+8,end-windows-8)!=null)
                            {
                                response.setHeader(HttpHeaders.CONNECTION, HttpHeaderValues.CLOSE);
                                return target;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}

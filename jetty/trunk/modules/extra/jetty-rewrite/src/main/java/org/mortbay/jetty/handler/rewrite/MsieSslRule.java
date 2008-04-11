package org.mortbay.jetty.handler.rewrite;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;

/**
 * MSIE SSL Rule
 * Disable keep alive for SSL from IE5 or IE6 on windows2000
 *  
 * @author gregw
 *
 */
public class MsieSslRule extends Rule
{
    private static final int IEv5 = 53;
    private static final int IEv6 = 54;
    
    public MsieSslRule()
    {
        _handling = true;
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
                    
                    // Check if OS is Windows 2000. 
                    boolean isWin2k = user_agent.indexOf("Windows NT 5",msie)>0;
                    
                    if ( ieVersion<=IEv5 || (ieVersion==IEv6 && isWin2k))
                    {
                        response.setHeader(HttpHeaders.CONNECTION, HttpHeaderValues.CLOSE);
                        return target;
                    }
                }
            }
        }
        return null;
    }
}

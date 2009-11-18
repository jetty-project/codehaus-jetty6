package org.mortbay.servlet.jetty;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.UncheckedPrintWriter;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.servlet.GzipFilter;

/* ------------------------------------------------------------ */
/** Includable GZip Filter.
 * This extension to the {@link GzipFilter} that uses Jetty APIs to allow
 * headers to be set during calls to 
 * {@link javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}.
 * This allows the gzip filter to function correct during includes and to make a decision to gzip or not
 * at the time the buffer fills and on the basis of all response headers.
 * 
 * If the init parameter "uncheckedPrintWriter" is set to "true", then the PrintWriter used by
 * the wrapped getWriter will be {@link UncheckedPrintWriter}.
 *
 */
public class IncludableGzipFilter extends GzipFilter
{
    boolean _uncheckedPrintWriter=false;
    
    public void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        
        String tmp=filterConfig.getInitParameter("uncheckedPrintWriter");
        if (tmp!=null)
            _uncheckedPrintWriter=Boolean.valueOf(tmp).booleanValue();
    }
    
    protected GZIPResponseWrapper newGZIPResponseWrapper(HttpServletRequest request, HttpServletResponse response)
    {
        return new IncludableResponseWrapper(request,response);
    }

    public class IncludableResponseWrapper extends GzipFilter.GZIPResponseWrapper
    {
        public IncludableResponseWrapper(HttpServletRequest request, HttpServletResponse response)
        {
            super(request,response);
        }
        
        protected GzipStream newGzipStream(HttpServletRequest request,HttpServletResponse response,long contentLength,int bufferSize, int minGzipSize) throws IOException
        {
            return new IncludableGzipStream(request,response,contentLength,bufferSize,minGzipSize);
        }
    }
    
    public class IncludableGzipStream extends GzipFilter.GzipStream
    {
        public IncludableGzipStream(HttpServletRequest request, HttpServletResponse response, long contentLength, int bufferSize, int minGzipSize)
                throws IOException
        {
            super(request,response,contentLength,bufferSize,minGzipSize);
        }

        protected boolean setContentEncodingGzip()
        {
            HttpConnection connection = HttpConnection.getCurrentConnection();
            connection.getResponseFields().put("Content-Encoding", "gzip");
            return true;
        }
    }
    
    protected PrintWriter newWriter(OutputStream out,String encoding) throws UnsupportedEncodingException
    {
        if (_uncheckedPrintWriter)
            return encoding==null?new UncheckedPrintWriter(out):new UncheckedPrintWriter(new OutputStreamWriter(out,encoding));
        return super.newWriter(out,encoding);
    }
}

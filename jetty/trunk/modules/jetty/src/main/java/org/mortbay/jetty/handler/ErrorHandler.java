// ========================================================================
// Copyright 1999-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.mortbay.jetty.handler;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpGenerator;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.StringUtil;


/* ------------------------------------------------------------ */
/** Handler for Error pages
 * A handler that is registered at the org.mortbay.http.ErrorHandler
 * context attributed and called by the HttpResponse.sendError method to write a
 * error page.
 * 
 * @author Greg Wilkins (gregw)
 */
public class ErrorHandler extends AbstractHandler
{
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException
    {
        HttpConnection.getCurrentConnection().getRequest().setHandled(true);
        response.setContentType(MimeTypes.TEXT_HTML);
        ByteArrayISO8859Writer writer= new ByteArrayISO8859Writer(2048);
        HttpConnection connection = HttpConnection.getCurrentConnection();
        handleErrorPage(request, writer, connection.getResponse().getStatus(), connection.getResponse().getReason());
        writer.flush();
        response.setContentLength(writer.size());
        writer.writeTo(response.getOutputStream());
        writer.destroy();
        
    }

    /* ------------------------------------------------------------ */
    protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message)
        throws IOException
    {
        writeErrorPage(request, writer, code, message);
    }
    
    /* ------------------------------------------------------------ */
    public static void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message)
        throws IOException
    {
        if (message != null)
        {
            message=URLDecoder.decode(message,"UTF-8");
            message= StringUtil.replace(message, "<", "&lt;");
            message= StringUtil.replace(message, ">", "&gt;");
        }
        String uri= request.getRequestURI();
        uri= StringUtil.replace(uri, "<", "&lt;");
        uri= StringUtil.replace(uri, ">", "&gt;");
        writer.write("<html>\n<head>\n<title>Error ");
        writer.write(Integer.toString(code));
        writer.write(' ');
        if (message==null)
            message=HttpGenerator.getReason(code);
        writer.write(message);
        writer.write("</title>\n</head>\n<body>\n<h2>HTTP ERROR: ");
        writer.write(Integer.toString(code));
        writer.write("</h2><pre>");
        writer.write(message);
        writer.write("</pre>\n");
        writer.write("<p>RequestURI=");
        writer.write(uri);
        writer.write(
            "</p>\n<p><i><small><a href=\"http://jetty.mortbay.org\">Powered by Jetty://</a></small></i></p>");
        
        Throwable th = (Throwable)request.getAttribute("javax.servlet.error.exception");
        while(th!=null)
        {
            writer.write("<h3>Caused by:</h2><pre>");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            pw.flush();
            writer.write(sw.getBuffer().toString());
            writer.write("</pre>\n");
            
            th =th.getCause();
        }
        
        for (int i= 0; i < 20; i++)
            writer.write("\n                                                ");
        writer.write("\n</body>\n</html>\n");
    }

}

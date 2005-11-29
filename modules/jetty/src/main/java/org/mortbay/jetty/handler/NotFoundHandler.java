// ========================================================================
// Copyright 1999-2004 Mort Bay Consulting Pty. Ltd.
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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.StringUtil;


/* ------------------------------------------------------------ */
/** Handler for resources that were not found.
 * Implements OPTIONS and TRACE methods for the server.
 * 
 * @author Greg Wilkins (gregw)
 */
public class NotFoundHandler extends AbstractHandler
{
    Server _server;
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        String method=request.getMethod();
        
        if (!method.equals(HttpMethods.GET) || !request.getRequestURI().equals("/"))
        {
            response.sendError(404);
            return true;   
        }

        response.setStatus(404);
        response.setContentType(MimeTypes.TEXT_HTML);
        
        ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);

        String uri=request.getRequestURI();
        uri=StringUtil.replace(uri,"<","&lt;");
        uri=StringUtil.replace(uri,">","&gt;");
        
        writer.write("<HTML>\n<HEAD>\n<TITLE>Error 404 - Not Found");
        writer.write("</TITLE>\n<BODY>\n<H2>Error 404 - Not Found.</H2>\n");
        writer.write("No context on this server matched or handled this request.<BR>");
        writer.write("Contexts known to this server are: <ul>");


        Handler[] handlers = _server==null?null:_server.getAllHandlers();
        
 
        for (int i=0;handlers!=null && i<handlers.length;i++)
        {
            if (!(handlers[i] instanceof ContextHandler))
                continue;
            ContextHandler context = (ContextHandler)handlers[i];
            if (context.isRunning())
            {
                writer.write("<li><a href=\"");
                writer.write(context.getContextPath());
                writer.write("/\">");
                writer.write(context.getContextPath());
                writer.write("&nbsp;--->&nbsp;");
                writer.write(context.toString());
                writer.write("</a></li>\n");
            }
            else
            {
                writer.write("<li>");
                writer.write(context.getContextPath());
                writer.write("&nbsp;--->&nbsp;");
                writer.write(context.toString());
                writer.write(" [stopped]");
                writer.write("</li>\n");
            }
        }
        
        writer.write("</ul><small><I>The links above may not work if a virtual host is configured</I></small>");
        
        for (int i=0;i<10;i++)
            writer.write("\n<!-- Padding for IE                  -->");
        
        writer.write("\n</BODY>\n</HTML>\n");
        writer.flush();
        response.setContentLength(writer.size());
        OutputStream out=response.getOutputStream();
        writer.writeTo(out);
        out.close();
        
        return true;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the server.
     */
    public Server getServer()
    {
        return _server;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param server The server to set.
     */
    public void setServer(Server server)
    {
        _server = server;
    }


}

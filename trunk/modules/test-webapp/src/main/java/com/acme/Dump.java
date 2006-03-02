// ========================================================================
// Copyright 1996-2005 Mort Bay Consulting Pty. Ltd.
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

package com.acme;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.UnavailableException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;



/* ------------------------------------------------------------ */
/** Dump Servlet Request.
 * 
 */
public class Dump extends HttpServlet
{
    /* ------------------------------------------------------------ */
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config);
    }

    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setAttribute("Dump", this);
        getServletContext().setAttribute("Dump",this);
        getServletContext().log("dump "+request.getRequestURI());

        // Force a content length response
        String length= request.getParameter("length");
        if (length != null && length.length() > 0)
        {
            response.setContentLength(Integer.parseInt(length));
        }

        // Handle a dump of data
        String data= request.getParameter("data");
        String block= request.getParameter("block");
        if (data != null && data.length() > 0)
        {
            int d=Integer.parseInt(data);
            int b=(block!=null&&block.length()>0)?Integer.parseInt(block):50;
            byte[] buf=new byte[b];
            for (int i=0;i<b;i++)
            {
                
                buf[i]=(byte)('0'+(i%10));
                if (i%10==9)
                    buf[i]=(byte)'\n';
            }
            buf[0]='o';
            OutputStream out=response.getOutputStream();
            response.setContentType("text/plain");
            while (d > 0)
            {
                if (d>=b)
                {
                    out.write(buf);
                    d=d-b;
                }
                else
                {
                    out.write(buf,0,d);
                    d=0;
                }
            }
            
            return;
        }
        
        
        // handle an exception
        String info= request.getPathInfo();
        if (info != null && info.endsWith("Exception"))
        {
            try
            {
                throw (Throwable) Thread.currentThread().getContextClassLoader().loadClass(info.substring(1)).newInstance();
            }
            catch (Throwable th)
            {
                throw new ServletException(th);
            }
        }
        
        // handle an redirect
        String redirect= request.getParameter("redirect");
        if (redirect != null && redirect.length() > 0)
        {
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            response.sendRedirect(redirect);
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            return;
        }

        // handle an error
        String error= request.getParameter("error");
        if (error != null && error.length() > 0)
        {
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            response.sendError(Integer.parseInt(error));
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            return;
        }


        String buffer= request.getParameter("buffer");
        if (buffer != null && buffer.length() > 0)
            response.setBufferSize(Integer.parseInt(buffer));

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");

        if (info != null && info.indexOf("Locale/") >= 0)
        {
            try
            {
                String locale_name= info.substring(info.indexOf("Locale/") + 7);
                Field f= java.util.Locale.class.getField(locale_name);
                response.setLocale((Locale)f.get(null));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                response.setLocale(Locale.getDefault());
            }
        }

        String cn= request.getParameter("cookie");
        String cv=request.getParameter("value");
        String v=request.getParameter("version");
        if (cn!=null && cv!=null)
        {
            Cookie cookie= new Cookie(cn, cv);
            cookie.setComment("Cookie from dump servlet");
            if (v!=null)
            {
                cookie.setMaxAge(300);
                cookie.setPath("/");
                cookie.setVersion(Integer.parseInt(v));
            }
            response.addCookie(cookie);
        }

        String pi= request.getPathInfo();
        if (pi != null && pi.startsWith("/ex"))
        {
            OutputStream out= response.getOutputStream();
            out.write("</H1>This text should be reset</H1>".getBytes());
            if ("/ex0".equals(pi))
                throw new ServletException("test ex0", new Throwable());
            if ("/ex1".equals(pi))
                throw new IOException("test ex1");
            if ("/ex2".equals(pi))
                throw new UnavailableException("test ex2");
            throw new RuntimeException("test");
        }

        
        PrintWriter pout=null;
        
        try
        {
            pout =response.getWriter();
        }
        catch(IllegalStateException e)
        {
            pout=new PrintWriter(response.getOutputStream());
        }
        
        
        try
        {
            pout.write("<html>\n<body>\n");
            pout.write("<h1>Dump Servlet</h1>\n");
            pout.write("<table>");
            pout.write("<tr>\n");
            pout.write("<th align=\"right\">getMethod:&nbsp;</th>");
            pout.write("<td>" + request.getMethod()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getContentLength:&nbsp;</th>");
            pout.write("<td>"+Integer.toString(request.getContentLength())+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getContentType:&nbsp;</th>");
            pout.write("<td>"+request.getContentType()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRequestURI:&nbsp;</th>");
            pout.write("<td>"+request.getRequestURI()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRequestURL:&nbsp;</th>");
            pout.write("<td>"+request.getRequestURL()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getContextPath:&nbsp;</th>");
            pout.write("<td>"+request.getContextPath()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getServletPath:&nbsp;</th>");
            pout.write("<td>"+request.getServletPath()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getPathInfo:&nbsp;</th>");
            pout.write("<td>"+request.getPathInfo()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getPathTranslated:&nbsp;</th>");
            pout.write("<td>"+request.getPathTranslated()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getQueryString:&nbsp;</th>");
            pout.write("<td>"+request.getQueryString()+"</td>");

            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getProtocol:&nbsp;</th>");
            pout.write("<td>"+request.getProtocol()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getScheme:&nbsp;</th>");
            pout.write("<td>"+request.getScheme()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getServerName:&nbsp;</th>");
            pout.write("<td>"+request.getServerName()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getServerPort:&nbsp;</th>");
            pout.write("<td>"+Integer.toString(request.getServerPort())+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocalName:&nbsp;</th>");
            pout.write("<td>"+request.getLocalName()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocalAddr:&nbsp;</th>");
            pout.write("<td>"+request.getLocalAddr()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocalPort:&nbsp;</th>");
            pout.write("<td>"+Integer.toString(request.getLocalPort())+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemoteUser:&nbsp;</th>");
            pout.write("<td>"+request.getRemoteUser()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemoteAddr:&nbsp;</th>");
            pout.write("<td>"+request.getRemoteAddr()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemoteHost:&nbsp;</th>");
            pout.write("<td>"+request.getRemoteHost()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemotePort:&nbsp;</th>");
            pout.write("<td>"+request.getRemotePort()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRequestedSessionId:&nbsp;</th>");
            pout.write("<td>"+request.getRequestedSessionId()+"</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">isSecure():&nbsp;</th>");
            pout.write("<td>"+request.isSecure()+"</td>");

            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">isUserInRole(admin):&nbsp;</th>");
            pout.write("<td>"+request.isUserInRole("admin")+"</td>");

            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocale:&nbsp;</th>");
            pout.write("<td>"+request.getLocale()+"</td>");
            
            Enumeration locales= request.getLocales();
            while (locales.hasMoreElements())
            {
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">getLocales:&nbsp;</th>");
                pout.write("<td>"+locales.nextElement()+"</td>");
            }
            pout.write("</tr><tr>\n");
            
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Other HTTP Headers:</big></th>");
            Enumeration h= request.getHeaderNames();
            String name;
            while (h.hasMoreElements())
            {
                name= (String)h.nextElement();

                Enumeration h2= request.getHeaders(name);
                while (h2.hasMoreElements())
                {
                    String hv= (String)h2.nextElement();
                    pout.write("</tr><tr>\n");
                    pout.write("<th align=\"right\">"+name+":&nbsp;</th>");
                    pout.write("<td>"+hv+"</td>");
                }
            }

            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Request Parameters:</big></th>");
            h= request.getParameterNames();
            while (h.hasMoreElements())
            {
                name= (String)h.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">"+name+":&nbsp;</th>");
                pout.write("<td>"+request.getParameter(name)+"</td>");
                String[] values= request.getParameterValues(name);
                if (values == null)
                {
                    pout.write("</tr><tr>\n");
                    pout.write("<th align=\"right\">"+name+" Values:&nbsp;</th>");
                    pout.write("<td>"+"NULL!"+"</td>");
                }
                else
                    if (values.length > 1)
                    {
                        for (int i= 0; i < values.length; i++)
                        {
                            pout.write("</tr><tr>\n");
                            pout.write("<th align=\"right\">"+name+"["+i+"]:&nbsp;</th>");
                            pout.write("<td>"+values[i]+"</td>");
                        }
                    }
            }

            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Cookies:</big></th>");
            Cookie[] cookies = request.getCookies();
            for (int i=0; cookies!=null && i<cookies.length;i++)
            {
                Cookie cookie = cookies[i];

                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">"+cookie.getName()+":&nbsp;</th>");
                pout.write("<td>"+cookie.getValue()+"</td>");
            }
            

            if (!"application/x-www-form-urlencoded".equals(request.getContentType()))
            {
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Content:</big></th>");
                pout.write("</tr><tr>\n");
                pout.write("<td><pre>");
                byte[] content= new byte[4096];
                int len;
                try{
                    InputStream in=request.getInputStream();
                    
                    while((len=in.read(content))>=0)
                        pout.write(new String(content,0,len));
                    // int b;
                    // while ((b=in.read())>=0)
                    //    pout.write((char)b);
                }
                catch(IOException e)
                {
                    pout.write(e.toString());
                }
                
                pout.write("</pre></td>");
            }
            
            
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Request Attributes:</big></th>");
            Enumeration a= request.getAttributeNames();
            while (a.hasMoreElements())
            {
                name= (String)a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">"+name+":&nbsp;</th>");
                pout.write("<td>"+"<pre>" + toString(request.getAttribute(name)) + "</pre>"+"</td>");
            }            

            
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Servlet InitParameters:</big></th>");
            a= getInitParameterNames();
            while (a.hasMoreElements())
            {
                name= (String)a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">"+name+":&nbsp;</th>");
                pout.write("<td>"+"<pre>" + toString(getInitParameter(name)) + "</pre>"+"</td>");
            }

            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Context InitParameters:</big></th>");
            a= getServletContext().getInitParameterNames();
            while (a.hasMoreElements())
            {
                name= (String)a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">"+name+":&nbsp;</th>");
                pout.write("<td>"+"<pre>" + toString(getServletContext().getInitParameter(name)) + "</pre>"+"</td>");
            }

            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Context Attributes:</big></th>");
            a= getServletContext().getAttributeNames();
            while (a.hasMoreElements())
            {
                name= (String)a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">"+name+":&nbsp;</th>");
                pout.write("<td>"+"<pre>" + toString(getServletContext().getAttribute(name)) + "</pre>"+"</td>");
            }


            String res= request.getParameter("resource");
            if (res != null && res.length() > 0)
            {
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Get Resource:</big></th>");
                
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">this.getClass():&nbsp;</th>");
                pout.write("<td>"+this.getClass().getResource(res)+"</td>");

                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">this.getClass().getClassLoader():&nbsp;</th>");
                pout.write("<td>"+this.getClass().getClassLoader().getResource(res)+"</td>");

                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">Thread.currentThread().getContextClassLoader():&nbsp;</th>");
                pout.write("<td>"+Thread.currentThread().getContextClassLoader().getResource(res)+"</td>");

                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">getServletContext():&nbsp;</th>");
                try{pout.write("<td>"+getServletContext().getResource(res)+"</td>");}
                catch(Exception e) {pout.write("<td>"+"" +e+"</td>");}
            }
            
            pout.write("</tr></table>\n");

            /* ------------------------------------------------------------ */
            pout.write("<h2>Request Wrappers</h2>\n");
            ServletRequest rw=request;
            int w=0;
            while (rw !=null)
            {
                pout.write((w++)+": "+rw.getClass().getName()+"<br/>");
                if (rw instanceof HttpServletRequestWrapper)
                    rw=((HttpServletRequestWrapper)rw).getRequest();
                else if (rw  instanceof ServletRequestWrapper)
                    rw=((ServletRequestWrapper)rw).getRequest();
                else
                    rw=null;
            }
            
            pout.write("<br/>");
            pout.write("<h2>International Characters</h2>");
            pout.write("Directly encoced:  Dürst<br/>");
            pout.write("HTML reference: D&uuml;rst<br/>");
            pout.write("Decimal (252) 8859-1: D&#252;rst<br/>");
            pout.write("Hex (xFC) 8859-1: D&#xFC;rst<br/>");
            pout.write("Javascript unicode (00FC) : <script language='javascript'>document.write(\"D\u00FCrst\");</script><br/>");
            pout.write("<br/>");
            pout.write("<h2>Form to generate GET content</h2>");
            pout.write("<form method=\"GET\" action=\""+response.encodeURL(getURI(request))+"\">");
            pout.write("TextField: <input type=\"text\" name=\"TextField\" value=\"value\"/><br/>\n");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"Submit\">");
            pout.write("</form>");

            pout.write("<br/>");
            pout.write("<h2>Form to generate POST content</h2>");
            pout.write("<form method=\"POST\" accept-charset=\"utf-8,iso8859-1\" action=\""+response.encodeURL(getURI(request))+"\">");
            pout.write("TextField: <input type=\"text\" name=\"TextField\" value=\"value\"/><br/>\n");
            pout.write("Select: <select multiple name=\"Select\">\n");
            pout.write("<option>ValueA</option>");
            pout.write("<option>ValueB1,ValueB2</option>");
            pout.write("<option>ValueC</option>");
            pout.write("</select><br/>");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"Submit\"><br/>");
            pout.write("</form>");

            pout.write("<h2>Form to get Resource</h2>");
            pout.write("<form method=\"POST\" action=\""+response.encodeURL(getURI(request))+"\">");
            pout.write("resource: <input type=\"text\" name=\"resource\" /><br/>\n");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"getResource\">");
            pout.write("</form>\n");
            
            pout.write("</body>\n</html>\n");

        }
        catch (Exception e)
        {
            getServletContext().log("dump", e);
        }


        pout.close();

        if (pi != null)
        {
            if ("/ex4".equals(pi))
                throw new ServletException("test ex4", new Throwable());
            if ("/ex5".equals(pi))
                throw new IOException("test ex5");
            if ("/ex6".equals(pi))
                throw new UnavailableException("test ex6");
        }


    }

    /* ------------------------------------------------------------ */
    public String getServletInfo()
    {
        return "Dump Servlet";
    }

    /* ------------------------------------------------------------ */
    public synchronized void destroy()
    {
    }

    /* ------------------------------------------------------------ */
    private String getURI(HttpServletRequest request)
    {
        String uri= (String)request.getAttribute("javax.servlet.forward.request_uri");
        if (uri == null)
            uri= request.getRequestURI();
        return uri;
    }

    /* ------------------------------------------------------------ */
    private static String toString(Object o)
    {
        if (o == null)
            return null;

        if (o.getClass().isArray())
        {
            StringBuffer sb= new StringBuffer();
            Object[] array= (Object[])o;
            for (int i= 0; i < array.length; i++)
            {
                if (i > 0)
                    sb.append("\n");
                sb.append(array.getClass().getComponentType().getName());
                sb.append("[");
                sb.append(i);
                sb.append("]=");
                sb.append(toString(array[i]));
            }
            return sb.toString();
        }
        else
            return o.toString();
    }

}

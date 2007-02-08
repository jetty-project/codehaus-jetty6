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
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.io.WriterOutputStream;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.ContextHandler.SContext;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.util.TypeUtil;
import org.mortbay.util.URIUtil;


/* ------------------------------------------------------------ */
/** Resource Handler.
 * 
 * This handle will serve static content and handle If-Modified-Since headers.
 * No caching is done.
 * Requests that cannot be handled are let pass (Eg no 404's)
 * 
 * @author Greg Wilkins (gregw)
 * @org.apache.xbean.XBean
 */
public class ResourceHandler extends AbstractHandler
{
    ContextHandler _context;
    Resource _baseResource;
    String[] _welcomeFiles={"index.html"};
    MimeTypes _mimeTypes = new MimeTypes();
    
    public ResourceHandler()
    {
    }
    
    
    public void doStart()
    throws Exception
    {
        SContext scontext = ContextHandler.getCurrentContext();
        _context = (scontext==null?null:scontext.getContextHandler());
        super.doStart();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the resourceBase.
     */
    public Resource getBaseResource()
    {
        if (_baseResource==null)
            return null;
        return _baseResource;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the base resource as a string.
     */
    public String getResourceBase()
    {
        if (_baseResource==null)
            return null;
        return _baseResource.toString();
    }

    
    /* ------------------------------------------------------------ */
    /**
     * @param base The resourceBase to set.
     */
    public void setBaseResource(Resource base) 
    {
        _baseResource=base;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param resourceBase The base resource as a string.
     */
    public void setResourceBase(String resourceBase) 
    {
        try
        {
            setBaseResource(Resource.newResource(resourceBase));
        }
        catch (Exception e)
        {
            Log.warn(e);
            throw new IllegalArgumentException(resourceBase);
        }
    }

    /* ------------------------------------------------------------ */
    /* 
     */
    public Resource getResource(String path) throws MalformedURLException
    {
        if (path==null || !path.startsWith("/"))
            throw new MalformedURLException(path);
        
        Resource base = _baseResource;
        if (base==null)
        {
            if (_context==null)
                return null;
            
            base=_context.getBaseResource();
            System.err.println("The base resource="+base);
            if (base==null)
                return null;
        }

        try
        {
            path=URIUtil.canonicalPath(path);
            Resource resource=base.addPath(path);
            return resource;
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
                    
        return null;
    }

    /* ------------------------------------------------------------ */
    protected Resource getResource(HttpServletRequest request) throws MalformedURLException
    {
        String path_info=request.getPathInfo();
        if (path_info==null)
            return null;
        return getResource(path_info);
    }


    /* ------------------------------------------------------------ */
    public String[] getWelcomeFiles()
    {
        return _welcomeFiles;
    }

    /* ------------------------------------------------------------ */
    public void setWelcomeFiles(String[] welcomeFiles)
    {
        _welcomeFiles=welcomeFiles;
    }
    
    /* ------------------------------------------------------------ */
    protected Resource getWelcome(Resource directory) throws MalformedURLException, IOException
    {
        for (int i=0;i<_welcomeFiles.length;i++)
        {
            Resource welcome=directory.addPath(_welcomeFiles[i]);
            if (welcome.exists() && !welcome.isDirectory())
                return welcome;
        }

        return null;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        Request base_request = request instanceof Request?(Request)request:HttpConnection.getCurrentConnection().getRequest();
        if (base_request.isHandled() || !request.getMethod().equals(HttpMethods.GET))
            return;
     
        Resource resource=getResource(request);
        
        if (resource==null || !resource.exists())
            return;

        // We are going to server something
        base_request.setHandled(true);
        
        if (resource.isDirectory())
        {
            if (!request.getPathInfo().endsWith("/"))
            {
                response.sendRedirect(request.getRequestURI()+"/");
                return;
            }
            resource=getWelcome(resource);

            if (resource==null || !resource.exists() || resource.isDirectory())
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        
        // set some headers
        long last_modified=resource.lastModified();
        if (last_modified>0)
        {
            long if_modified=request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if (if_modified>0 && last_modified/1000<=if_modified/1000)
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            response.setDateHeader(HttpHeaders.LAST_MODIFIED,last_modified);
        }
        
        Buffer mime=_mimeTypes.getMimeByExtension(request.getPathInfo());
        if (mime!=null)
            response.setContentType(mime.toString());

        long length=resource.length();
        if (length>0)
            response.setHeader(HttpHeaders.CONTENT_LENGTH,TypeUtil.toString(length));
        
        // Send the content
        OutputStream out =null;
        try{out = response.getOutputStream();}
        catch(IllegalStateException e) {out = new WriterOutputStream(response.getWriter());}
        
        // See if a short direct method can be used?
        if (out instanceof HttpConnection.Output)
        {
            // TODO file mapped buffers
            ((HttpConnection.Output)out).sendContent(resource.getInputStream());
        }
        else
        {
            // Write content normally
            resource.writeTo(out,0,length);
        }
    }

}

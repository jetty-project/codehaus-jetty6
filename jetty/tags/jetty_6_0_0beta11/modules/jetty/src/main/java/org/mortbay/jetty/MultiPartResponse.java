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

package org.mortbay.jetty;

import java.io.IOException;
import java.io.OutputStream;

import org.mortbay.util.StringUtil;

/* ================================================================ */
/** Handle a multipart MIME response.
 *
 * @author Greg Wilkins
 * @author Jim Crossley
*/
public class MultiPartResponse
{
    /* ------------------------------------------------------------ */
    private static byte[] __CRLF;
    private static byte[] __DASHDASH;
    static
    {
        try
        {
            __CRLF="\015\012".getBytes(StringUtil.__ISO_8859_1);
            __DASHDASH="--".getBytes(StringUtil.__ISO_8859_1);
        }
        catch (Exception e) {e.printStackTrace(); System.exit(1);}
    }
    
    /* ------------------------------------------------------------ */
    private String boundary;
    private byte[] boundaryBytes;

    /* ------------------------------------------------------------ */
    private MultiPartResponse()
    {
        try
        {
            boundary = "jetty"+System.identityHashCode(this)+
                Long.toString(System.currentTimeMillis(),36);
            boundaryBytes=boundary.getBytes(StringUtil.__ISO_8859_1);
        }
        catch (Exception e)
        {
            e.printStackTrace(); System.exit(1);
        }
    }    
    
    /* ------------------------------------------------------------ */
    public String getBoundary()
    {
        return boundary;
    }
    
    /* ------------------------------------------------------------ */    
    /** PrintWriter to write content too.
     */
    private OutputStream out = null; 
    public OutputStream getOut() {return out;}

    /* ------------------------------------------------------------ */
    private boolean inPart=false;
    
    /* ------------------------------------------------------------ */
    public MultiPartResponse(OutputStream out)
         throws IOException
    {
        this();
        this.out=out;
        inPart=false;
    }

    /* ------------------------------------------------------------ */
    /** Start creation of the next Content.
     */
    public void startPart(String contentType)
         throws IOException
    {
        if (inPart)
            out.write(__CRLF);
        inPart=true;
        out.write(__DASHDASH);
        out.write(boundaryBytes);
        out.write(__CRLF);
        out.write(("Content-Type: "+contentType).getBytes(StringUtil.__ISO_8859_1));
        out.write(__CRLF);
        out.write(__CRLF);
    }
    
    /* ------------------------------------------------------------ */
    /** Start creation of the next Content.
     */
    public void startPart(String contentType, String[] headers)
         throws IOException
    {
        if (inPart)
            out.write(__CRLF);
        inPart=true;
        out.write(__DASHDASH);
        out.write(boundaryBytes);
        out.write(__CRLF);
        out.write(("Content-Type: "+contentType).getBytes(StringUtil.__ISO_8859_1));
        out.write(__CRLF);
        for (int i=0;headers!=null && i<headers.length;i++)
        {
            out.write(headers[i].getBytes(StringUtil.__ISO_8859_1));
            out.write(__CRLF);
        }
        out.write(__CRLF);
    }
        
    /* ------------------------------------------------------------ */
    /** End the current part.
     * @exception IOException IOException
     */
    public void close()
         throws IOException
    {
        if (inPart)
            out.write(__CRLF);
        out.write(__DASHDASH);
        out.write(boundaryBytes);
        out.write(__DASHDASH);
        out.write(__CRLF);
        inPart=false;
    }
    
};





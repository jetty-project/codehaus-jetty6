//========================================================================
// License and copyright status is a work in progress.
// Parts Copyright 2006 Mort Bay Consulting Pty. Ltd.
// Parts Copyright 2006 Sun Micro system and perhaps CDDL.
//------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//========================================================================*
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.mortbay.jetty.grizzly;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;

import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.bio.StreamEndPoint;
import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpException;
import org.mortbay.log.Log;
import org.mortbay.util.ajax.Continuation;

public class GrizzlyEndPoint extends StreamEndPoint implements EndPoint
{
    HttpConnection _connection;
    JettyProcessorTask _task;
    
    public GrizzlyEndPoint(GrizzlyConnector connector, InputStream in, OutputStream out, JettyProcessorTask task)
        throws IOException
    {
        super(in,out);
        System.err.println("new GrizzlyEndPoint in="+in+" out="+out);
        _connection = new HttpConnection(connector,this,connector.getServer());
        _task=task;
    }

    public void handle()
    {
        try
        {
            System.err.println("handle  "+this);
            _connection.handle();
        }
        catch (ClosedChannelException e)
        {
            Log.ignore(e);
        }
        catch (EofException e)
        {
            Log.debug("EOF", e);
            try{close();}
            catch(IOException e2){Log.ignore(e2);}
        }
        catch (HttpException e)
        {
            Log.debug("BAD", e);
            try{close();}
            catch(IOException e2){Log.ignore(e2);}
        }
        catch (Throwable e)
        {
            Log.warn("handle failed", e);
            try{close();}
            catch(IOException e2){Log.ignore(e2);}
        }
        finally
        {
            System.err.println("handled "+this);
            Continuation continuation =  _connection.getRequest().getContinuation();
            if (continuation != null && continuation.isPending())
            {
                // We have a continuation
                // TODO something!
            }
            else
            {
                // something else... normally re-enable this connection is the selectset with the latest interested ops
            }
        }
    
    }

    public void blockReadable(long millisecs)
    {
        // TODO implement
        System.err.println("blockReadable()");
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public void blockWritable(long millisecs)
    {
        // TODO implement
        System.err.println("blockWritable()");
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public int fill(Buffer buffer) throws IOException
    {
        System.err.println("fill()");
        int len= super.fill(buffer);
        System.err.println("filled: "+buffer);
        return len;
    }

    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException
    {
        int len=0;
        
        // TODO gather operation.
        if (header!=null && header.hasContent())
            len+=flush(header);
        
        if (header==null || !header.hasContent())
        {
            if (buffer!=null && buffer.hasContent())
                len+=flush(buffer);
        }

        if (buffer==null || !buffer.hasContent())
        {
            if (trailer!=null && trailer.hasContent())
                len+=flush(trailer);
        }
        
        return len;
        
    }

    public int flush(Buffer buffer) throws IOException
    {
        System.err.println("flush()");
        return super.flush(buffer);
    }

    public boolean isBlocking()
    {
        return false;
    }
    
}
///========================================================================
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
import java.net.Socket;

import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.TaskBase;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;

/**
 * 
 * @author Jeanfrancois Arcand
 */
public class JettyProcessorTask extends TaskBase implements ProcessorTask
{
    private Handler handler;

    public JettyProcessorTask()
    {
        System.err.println("new JettyProcessorTask "+this);
    }

    public void doTask() throws IOException
    {
        System.err.println(this+" doTask ");
    }

    public void taskEvent(TaskEvent event)
    {
        System.err.println(this+" taskEvent "+event);
    }

    public void initialize()
    {
    }

    /**
     * The Default ReadTask will invoke that method.
     */
    public boolean process(InputStream input, OutputStream output) throws Exception
    {
        System.err.println(this+" process "+input);
        JettySelectorThread selThread=(JettySelectorThread)selectorThread;
        GrizzlyConnector grizzlyConnector=selThread.getGrizzlyConnector();

        // TODO This is wrong? where does the output go?
        if (output==null)
            output=System.out;
        
        // TODO How do we find the existing endpoint?
        GrizzlyEndPoint endpoint=null;
        
        if (endpoint==null)
        {
            endpoint=new GrizzlyEndPoint(grizzlyConnector,input,output,this);
        }
        else
        {
            // TODO maybe just check they have not changed?
            if (endpoint.getInputStream()!=input)
                throw new IllegalStateException();
            endpoint.setOutputStream(output);
        }

        // We are already using a Grizzly WorkerThread, so no need to
        // invoke Jetty Thread Pool
        endpoint.handle();

        // How to find the keepAlive flag?
        return true;
    }


    public void recycle()
    {
        System.err.println("recycle");
    }

    // ---------------------------------------------------- Not Used for now
    // ---//

    public int getBufferSize()
    {
        return -1;
    }

    public boolean getDropConnection()
    {
        return false;
    }

    public int getMaxPostSize()
    {
        return -1;
    }

    public void invokeAdapter()
    {
    }

    public void setBufferSize(int requestBufferSize)
    {
    }

    public void setDropConnection(boolean dropConnection)
    {
    }

    public void setHandler(Handler handler)
    {
        this.handler=handler;
    }

    public Handler getHandler()
    {
        return handler;
    }

    public void setMaxHttpHeaderSize(int maxHttpHeaderSize)
    {
    }

    public void setMaxPostSize(int mps)
    {
    }

    public void setSocket(Socket socket)
    {
    }

    public void setTimeout(int timeouts)
    {
    }

    public void terminateProcess()
    {
    }

    public String getRequestURI()
    {
        return null;
    }

    public long getWorkerThreadID()
    {
        return -1;
    }

    public boolean isKeepAlive()
    {
        // TODO
        return true;
    }

    public boolean isError()
    {
        return false;
    }

    // --------------------------------------------------- Grizzly ARP ------//

    public void parseRequest() throws Exception
    {
        System.err.println("pareRequest()");
    }

    public boolean parseRequest(InputStream input, OutputStream output, boolean keptAlive) throws Exception
    {
        System.err.println("pareRequest(in,out,ka)");
        return true;
    }

    public void postProcess() throws Exception
    {
        System.err.println("postProcess()");
    }

    public void postProcess(InputStream input, OutputStream output) throws Exception
    {
        System.err.println("postProcess(in,out)");
    }

    public void postResponse() throws Exception
    {
        System.err.println("(postResponse)");
    }

    public void preProcess() throws Exception
    {
        System.err.println("preProcess()");
    }

    public void preProcess(InputStream input, OutputStream output) throws Exception
    {
        System.err.println("preProcess(in,out)");
    }
}

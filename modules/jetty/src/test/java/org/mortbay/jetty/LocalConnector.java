//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty;

import java.io.IOException;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.ByteArrayEndPoint;

public class LocalConnector extends AbstractConnector
{
    ByteArrayEndPoint endp;
    ByteArrayBuffer in;
    ByteArrayBuffer out;
    
    Server server;
    HttpConnection connection;
    boolean accepting;
    
    public LocalConnector()
    {
        setPort(1);
    }

    /* ------------------------------------------------------------ */
    public Object getConnection()
    {
        return endp;
    }
    
    /* ------------------------------------------------------------ */
    public HttpConnection getHttpConnection()
    {
        return connection;
    }
    
    public void clear()
    {
        in.clear();
        out.clear();
    }
    
    public void reopen()
    {
        in.clear();
        out.clear();
        endp = new ByteArrayEndPoint();
        endp.setIn(in);
        endp.setOut(out);
        connection=new HttpConnection(this,endp,getServer());
        connectionOpened(connection);
        accepting=false;
    }
    
    public void doStart()
        throws Exception
    {
        super.doStart();
        
        in=new ByteArrayBuffer(8192);
        out=new ByteArrayBuffer(8192);
        endp = new ByteArrayEndPoint();
        endp.setIn(in);
        endp.setOut(out);
        connection=new HttpConnection(this,endp,getServer());
        connectionOpened(connection);
        accepting=false;
    }
    
    String getResponses(String requests)
        throws Exception
    {
        // System.out.println("\nREQUESTS :\n"+requests);
        // System.out.flush();
        
        in.put(new ByteArrayBuffer(requests));

        synchronized (this)
        {
            accepting=true;
            this.notify();

            while(accepting)
                this.wait();
        }

        
        // System.err.println("\nRESPONSES:\n"+out);
        return out.toString();
    }

    protected Buffer newBuffer(int size)
    {
        return new ByteArrayBuffer(size);
    }

    protected void accept(int acceptorID) throws IOException, InterruptedException
    {
        while (isRunning())
        {
            synchronized (this)
            {
                try
                {
                    while(!accepting)
                        this.wait();
                }
                catch(InterruptedException e)
                {
                    return;
                }
            }
            
            try
            {
                connectionOpened(connection);
                while (in.length()>0)
                    connection.handle();
            }
            finally
            {
                synchronized (this)
                {
                    accepting=false;
                    this.notify();
                }
                connectionClosed(connection);
            }
        }
        
    }

    public void open() throws IOException
    {
    }

    public void close() throws IOException
    {
    }
    

    /* ------------------------------------------------------------------------------- */
    public int getLocalPort()
    {
        return -1;
    }

    
}
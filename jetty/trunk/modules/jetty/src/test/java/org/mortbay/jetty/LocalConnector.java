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
import org.mortbay.jetty.handler.NotFoundHandler;

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
            }
        }
        
    }

    public void open() throws IOException
    {
    }

    public void close() throws IOException
    {
    }

    
    public static void main(String[] arg)
        throws Exception
    {
        Server server = new Server();
        LocalConnector connector = new LocalConnector();
        server.setConnectors(new Connector[]{connector});
        server.setHandler(new DumpHandler());
        
        server.start();
        
        connector.getResponses("GET /R1 HTTP/1.1\n"+
                        "Host: localhost\n"+
                        "Transfer-Encoding: chunked\n"+
                        "Content-Type: text/plain\n"+
                        "\n"+
                        "2;\n"+
                        "12\n"+
                        "3;\n"+
                        "345\n"+
                        "0;\n\n"+

                        "GET /R2 HTTP/1.1\n"+
                        "Host: localhost\n"+
                        "Transfer-Encoding: chunked\n"+
                        "Content-Type: text/plain\n"+
                        "\n"+
                        "4;\n"+
                        "6789\n"+
                        "5;\n"+
                        "abcde\n"+
                        "0;\n\n"+
                        "GET /R3 HTTP/1.1\n"+
                        "Host: localhost\n"+
                        "Connection: close\n"+
                        "\n"
                        );
        
        connector.reopen();
        connector.getResponses("GET /R2 HTTP/1.1\n"+
                        "Host: localhost\n"+
                        "Transfer-Encoding: chunked\n"+
                        "Content-Type: text/plain\n"+
                        "\n"+
                        "4;\n"+
                        "PQRS\n"+
                        "5;\n"+
                        "98765\n"+
                        "0;\n\n"+
                        "GET /R3 HTTP/1.1\n"+
                        "Host: localhost\n"+
                        "Connection: close\n"+
                        "\n"
                        );
        
        server.stop();
        
    }
    
    
    
}
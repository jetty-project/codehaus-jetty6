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
import org.mortbay.jetty.RFC2616Test.InBuf;
import org.mortbay.jetty.RFC2616Test.OutBuf;

class LocalConnector extends AbstractConnector
{
    InBuf in;
    OutBuf out;
    Server server;
    HttpConnection connection;
    
    LocalConnector()
    {
        in=new InBuf();
        out=new OutBuf(in);
    }
    
    String getResponses(String requests)
        throws Exception
    {
        System.out.println("IN :"+requests);
        in.fill=requests;
        in.closed=false;
        out.closed=false;

        try
        {
            int loop=0;        
            while((in.fill!=null || in.length()>0) && !out.isClosed())
            {
                if (loop++>10)
                    break;
            }
        }
        catch(Exception e)
        {
            if (!"EOF".equals(e.getMessage()))
                throw e;
        }
        
        String r = out.flush.toString();
        out.flush.setLength(0);
        System.out.println("OUT:"+r);
        return r;
    }

    protected Buffer newBuffer(int size)
    {
        return new ByteArrayBuffer(size);
    }

    protected void accept(int acceptorID) throws IOException, InterruptedException
    {
        // TODO Auto-generated method stub
        
    }

    public void open() throws IOException
    {
    }

    public void close() throws IOException
    {
    }
}
//========================================================================
// Parts Copyright 2006 Mort Bay Consulting Pty. Ltd.
// Parts Copyright 2006 Jeanfrancois Arcand
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
//========================================================================

package org.mortbay.jetty.grizzly;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import org.mortbay.io.nio.ChannelEndPoint;
import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpException;
import org.mortbay.log.Log;
import org.mortbay.util.ajax.Continuation;

public class GrizzlyEndPoint extends ChannelEndPoint
{
    HttpConnection _connection;
    JettyProcessorTask _task;
    
    public GrizzlyEndPoint(GrizzlyConnector connector,ByteChannel channel)
        throws IOException
    {
        // TODO: Needs an empty constructor?
        super(channel);
        
        //System.err.println("\nnew GrizzlyEndPoint channel="+channel);
        _connection = new HttpConnection(connector,this,connector.getServer());
    }

    public void handle()
    {
        //System.err.println("GrizzlyEndPoint.handle "+this);
        
        try
        {
            //System.err.println("handle  "+this);
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
            //System.err.println("handled "+this);
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
        //System.err.println("blockReadable()");
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
    }

    public void blockWritable(long millisecs)
    {
        // TODO implement
        //System.err.println("blockWritable()");
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
    }
    
    public boolean keepAlive()
    {
        return _connection.getGenerator().isPersistent();
    }
    
    public boolean isComplete()
    {
        return _connection.getGenerator().isComplete();       
    }

    public boolean isBlocking()
    {
        return false;
    }
    
    public void setChannel(ByteChannel channel)
    {
        //if (_channel!=null && _channel!=channel)
            //System.err.println("GrizzlyEndPoint.setChannel "+channel+" was "+_channel+" in "+this);
        _channel = channel;
        
        // TO DO: Needs a way to avoid calling instanceof
        if ( channel instanceof GrizzlySocketChannel)
            _socket=((GrizzlySocketChannel)channel).getSocketChannel().socket();        
    }
    
    public void recycle()
    {
        _connection.destroy();
    }
    
}
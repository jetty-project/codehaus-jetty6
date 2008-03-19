// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.mortbay.io.nio.SelectChannelEndPoint;
import org.mortbay.io.nio.SelectorManager.SelectSet;

public class SuspendableSelectChannelEndPoint extends SelectChannelEndPoint
{
    
    public SuspendableSelectChannelEndPoint(SocketChannel channel, SelectSet selectSet, SelectionKey key)
    {
        super(channel,selectSet,key);

        scheduleIdle();
    }

    public void close() throws IOException
    {
        super.close();
    }
    
    public boolean dispatch()
    {
        return super.dispatch();
    }
    
    public boolean undispatch()
    {
        return super.undispatch();
    }
}
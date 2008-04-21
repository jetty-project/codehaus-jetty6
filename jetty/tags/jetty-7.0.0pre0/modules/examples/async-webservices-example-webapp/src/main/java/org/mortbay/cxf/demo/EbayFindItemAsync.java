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

package org.mortbay.cxf.demo;


import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import ebay.apis.eblbasecomponents.*;

import javax.servlet.ServletRequest;
import javax.xml.ws.BindingProvider;

/* ------------------------------------------------------------ */

public class EbayFindItemAsync
{
    ServletRequest _request;
    private long _accessed;
    private long _startTime; // purely for test purposes

    ShoppingInterface _shoppingPort;

    private List<EbayFindItemAsyncHandler> _handlers = new ArrayList<EbayFindItemAsyncHandler>();
    private int _todo;

    /* ------------------------------------------------------------ */

    public EbayFindItemAsync(ShoppingInterface port, ServletRequest request)
    {
        _startTime = System.currentTimeMillis();
        _request=request;
        _shoppingPort=port;
    }

    /* ------------------------------------------------------------ */
    public void search(String item)
    {
        FindItemsRequestType req = new FindItemsRequestType();
        req.setQueryKeywords(item);
        req.setMaxEntries(4);

        synchronized (this)
        {
            _todo++;
            EbayFindItemAsyncHandler handler = new EbayFindItemAsyncHandler(this);
            _shoppingPort.findItemsAsync(req, handler);
            _handlers.add(handler);
        }
    }

    /* ------------------------------------------------------------ */
    public void done()
    {
        synchronized (this)
        {
            System.err.println("done "+_todo);
            if (_todo>0)
            {
                if (--_todo==0)
                {
                    System.err.println("Resuming request");
                    _request.resume();
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    public List<EbayFindItemAsyncHandler> getPayload()
    {
        return _handlers;
    }

    /* ------------------------------------------------------------ */
    public void access()
    {
        synchronized (this)
        {
            // distribute access time in cluster
            _accessed = System.currentTimeMillis();
        }
    }

    /* ------------------------------------------------------------ */
    public synchronized long lastAccessed()
    {
        return _accessed;
    }

    /* ------------------------------------------------------------ */
    public synchronized long getStartTime()
    {
        return _startTime;
    }

}

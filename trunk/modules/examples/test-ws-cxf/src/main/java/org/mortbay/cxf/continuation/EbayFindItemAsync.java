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

package org.mortbay.cxf.continuation;


import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import ebay.apis.eblbasecomponents.*;

import javax.xml.ws.BindingProvider;

/* ------------------------------------------------------------ */

public class EbayFindItemAsync
{
    private long _accessed;
    private long _totalTime; // purely for test purposes

    ShoppingInterface _shoppingPort;
    private EbayFindItemAsyncHandler _handler;
    private Future _freply;

    private List _handlers = new ArrayList();
    private List _futures = new ArrayList();

    /* ------------------------------------------------------------ */

    public EbayFindItemAsync()
    {

        _totalTime = System.currentTimeMillis();

        try
        {

            _shoppingPort = new Shopping().getShopping();
            BindingProvider bp = (BindingProvider) _shoppingPort;

            // retrieve the URL stub from the WSDL
            String ebayURL = (String) bp.getRequestContext().
                    get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            // add eBay-required parameters to the URL
            String endpointURL = ebayURL + "?callname=FindItems&siteid=0" +
                    "&appid=JesseMcC-1aff-4c3c-b0be-e8379d036f56" +
                    "&version=551&requestencoding=SOAP";

            // replace the endpoint address with the new value
            bp.getRequestContext().
                    put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);

        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void search(String item)
    {
        FindItemsRequestType req = new FindItemsRequestType();
        req.setQueryKeywords(item);
        req.setMaxEntries( 100 );

        _handler = new EbayFindItemAsyncHandler();

        _freply = _shoppingPort.findItemsAsync(req, _handler);

        _handlers.add(_handler);
        _futures.add(_freply);
    }


    public boolean isDone()
    {
        boolean isDone = true;
        int f = 0;
        for (Iterator i = _futures.iterator(); i.hasNext();)
        {
            ++f;
            Future future = (Future) i.next();

            if (!future.isDone())
            {
                isDone = false;
            }
            else
            {
                System.out.println( f + "# future is complete");
            }
        }

        return isDone; // all futures are back
    }

    public List getPayload()
    {
        _totalTime = System.currentTimeMillis() - _totalTime;
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

    public synchronized long getTotalTime()
    {
        return _totalTime;
    }

}

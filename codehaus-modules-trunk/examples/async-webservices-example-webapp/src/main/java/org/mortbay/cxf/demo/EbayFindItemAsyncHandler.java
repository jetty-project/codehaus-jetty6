package org.mortbay.cxf.demo;

// ========================================================================
// Copyright 2007 Mort Bay Consulting Pty. Ltd.
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


import ebay.apis.eblbasecomponents.FindItemsResponseType;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;


public class EbayFindItemAsyncHandler implements AsyncHandler<FindItemsResponseType> 
{
    private EbayFindItemAsync finder;
    private FindItemsResponseType reply;
    
    public EbayFindItemAsyncHandler(EbayFindItemAsync finder)
    {
        this.finder=finder;
    }
    
    public void handleResponse(Response<FindItemsResponseType> response) 
    {
        try 
        {
            reply = response.get();
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        finally
        {
            finder.done();
        }
    }

    public FindItemsResponseType getResponse() 
    {
        return reply;
    }
}


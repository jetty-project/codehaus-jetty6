package client.org.mortbay.cxf.test;

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

import ebay.apis.eblbasecomponents.*;

import javax.xml.ws.BindingProvider;
import java.util.List;
import java.util.ListIterator;

public class TestWSClient
{

    public void testWS() throws Exception
    {
        try {

            ShoppingInterface port = new Shopping().getShopping();
            BindingProvider bp = (BindingProvider) port;

            // retrieve the URL stub from the WSDL
            String ebayURL = (String) bp.getRequestContext().
                get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            // add eBay-required parameters to the URL
            String endpointURL = ebayURL+"?callname=FindItems&siteid=0" +
                "&appid=JesseMcC-1aff-4c3c-b0be-e8379d036f56" +
                "&version=549&requestencoding=SOAP";

            // replace the endpoint address with the new value
            bp.getRequestContext().
                put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);

            FindItemsRequestType req = new FindItemsRequestType();
            req.setQueryKeywords( "camera" );

            FindItemsResponseType resp = port.findItems(req);
            List lsit = resp.getItem();

            System.out.println( "Errors: " + resp.getErrors().size() );

            System.out.println( "Found " + lsit.size() + " items." );

            ListIterator lsitItr = lsit.listIterator();
            while(lsitItr.hasNext()) {
                SimpleItemType sit = (SimpleItemType) lsitItr.next();
                System.out.print(sit.getDescription() + " ");
                System.out.print(sit.getItemID() + " ");
                System.out.print(sit.getViewItemURLForNaturalSearch() + " ");
                System.out.println(sit.getLocation() + " ");
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}

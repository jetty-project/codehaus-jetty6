//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.client.ContentExchange;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.util.ajax.JSON;

public class JSFDemoBean
{
    private String itemName = null;
    
    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }
    
    public String getItemName()
    {
        return this.itemName;
    }
    
    public String searchEbay() throws Exception
    {
        System.err.println("Search EBAY "+getItemName());
        
        FacesContext context = FacesContext.getCurrentInstance();
        final ServletRequest request = (ServletRequest)context.getExternalContext().getRequest();
        
        if (request.getAttribute("items")!=null)
            return "success";

        // Create an async HttpClient in the session
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        HttpClient client = (HttpClient)session.getAttribute("httpClient");
        if (client==null)
        {
            client = new HttpClient();
            client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            client.start();
            session.setAttribute("httpClient",client);
        }
        
        
        // Create an async RESTFUL request to ebay
        ContentExchange exchange = new ContentExchange()
        {
            protected void onResponseComplete() throws IOException
            {
                super.onResponseComplete();
                Map<String,Object> query = (Map<String,Object>)JSON.parse(this.getResponseContent());
                Object[] itemsArray = (Object[])query.get("Item");
                
                if (itemsArray==null)
                {
                    request.setAttribute("message","No Items Found!");
                }
                else
                {
                    List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
                    for (Object o : (Object[])query.get("Item"))
                        items.add((Map<String,Object>)o);
                    request.setAttribute("items",items);
                    request.setAttribute("message","&nbsp;");
                }
                request.resume();
            }           
        };
        
        
        // Suspend and send the restful request
        request.suspend();
        request.getServletResponse().disable();
        exchange.setMethod("GET");
        exchange.setURL("http://open.api.ebay.com/shopping?MaxEntries=20&appid=Webtide81-adf4-4f0a-ad58-d91e41bbe85&version=573&siteid=0&callname=FindItems&responseencoding=JSON&QueryKeywords=" +getItemName());
        exchange.setVersion(11);
        request.setAttribute("items",Collections.emptyList());
        client.send(exchange);
       
        return "success";
    }
}
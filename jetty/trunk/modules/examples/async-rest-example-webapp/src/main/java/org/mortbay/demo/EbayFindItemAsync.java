/**
 * 
 */
package org.mortbay.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.mortbay.jetty.client.ContentExchange;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.util.ajax.JSON;

/**
 * @author athena
 * 
 */
public class EbayFindItemAsync
{
    private ServletRequest _request;
    private HttpClient _client;
    private String _appid;
    private List<Map<String, String>> _results = new ArrayList<Map<String, String>>();

    private int _todo;

    public EbayFindItemAsync(HttpClient client, ServletRequest request, String appid)
    {
        _request = request;
        _client = client;
        _appid = appid;
    }

    public void search(String item) throws IOException
    {
        final String itemName = item;
        // Create an async RESTFUL request to ebay
        ContentExchange exchange = new ContentExchange()
        {
            protected void onResponseComplete() throws IOException
            {
                super.onResponseComplete();
                Map query = (Map) JSON.parse(this.getResponseContent());
                Object[] itemsArray = (Object[]) query.get("Item");

                if (itemsArray == null)
                {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("Title", "\"" + itemName + "\" not found!");
                    _results.add(m);
                }
                else
                {
                    for (Object o : (Object[]) query.get("Item"))
                        _results.add((Map) o);
                }

                synchronized (this)
                {
                    _todo--;
                    if (_todo == 0)
                        _request.resume();
                }
            }
        };

        exchange.setMethod("GET");
        exchange.setURL("http://open.api.ebay.com/shopping?MaxEntries=5&appid=" + _appid + "&version=573&siteid=0&callname=FindItems&responseencoding=JSON&QueryKeywords=" + item);

        synchronized (this)
        {
            _todo++;
        }

        _client.send(exchange);
    }

    public List<Map<String, String>> getPayload()
    {
        return _results;
    }
}

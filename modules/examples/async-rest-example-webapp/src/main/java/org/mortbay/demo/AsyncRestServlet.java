package org.mortbay.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.client.HttpClient;

/**
 * Servlet implementation class AsyncRESTServlet
 */
public class AsyncRestServlet extends HttpServlet
{
    private final static String __DEFAULT_APPID = "Webtide81-adf4-4f0a-ad58-d91e41bbe85";

    final static String ITEMS_PARAM = "items";
    final static String APPID_PARAM = "appid";

    final static String CLIENT_ATTR = "org.mortbay.demo.client";
    final static String DURATION_ATTR = "org.mortbay.demo.duration";
    final static String START_ATTR = "org.mortbay.demo.start";

    private HttpClient _client;
    private String _appid;

    public void init(ServletConfig servletConfig) throws ServletException
    {
        if (servletConfig.getInitParameter(APPID_PARAM) == null)
            _appid = __DEFAULT_APPID;
        else
            _appid = servletConfig.getInitParameter(APPID_PARAM);

        _client = new HttpClient();
        _client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);

        try
        {
            _client.start();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Long start=System.currentTimeMillis();

        if (request.isInitial() || request.getAttribute(CLIENT_ATTR)==null)
        {
            System.err.println("Search EBAY " + request.getParameter(ITEMS_PARAM));

            // Suspend and send the restful request
            request.suspend();

            EbayFindItemAsync searches = new EbayFindItemAsync(_client, request, _appid);

            StringTokenizer strtok = new StringTokenizer(request.getParameter(ITEMS_PARAM), ",");
            while (strtok.hasMoreTokens())
            {
                searches.search(URLEncoder.encode(strtok.nextToken(), "UTF-8") );
            }

            request.setAttribute(CLIENT_ATTR, searches);
            request.setAttribute(START_ATTR, start);
            request.setAttribute(DURATION_ATTR, new Long(System.currentTimeMillis() - start));
            return;
        }

        // resumed request: either we got all the results, or we timed out
        EbayFindItemAsync aggregator = (EbayFindItemAsync) request.getAttribute(CLIENT_ATTR);
        List<Map<String, String>> results = aggregator.getPayload();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><style type='text/css'>img:hover {height:75px}</style></head><body><small>");

        for (Map<String, String> m : results)
        {
            out.print("<a href=\""+m.get("ViewItemURLForNaturalSearch")+"\">");
            if (m.containsKey("GalleryURL"))
                out.print("<img border='1px' height='20px' src=\""+m.get("GalleryURL")+"\"/>&nbsp;");

            out.print(m.get("Title"));
            out.println("</a><br/>");
        }

        out.println("<hr />");
        long duration = (Long) request.getAttribute(DURATION_ATTR);
        long start0 = (Long) request.getAttribute(START_ATTR);

        long now = System.currentTimeMillis();
        out.print("Total Time: ");
        out.print(now - start0);
        out.println("ms<br/>");
        out.print("Thread held: ");

        long duration2 = now - start;
        out.print(duration + duration2);
        out.println("ms (" + duration + " initial + " + duration2 + " resume )");
        out.println("</small></body></html>");
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

}

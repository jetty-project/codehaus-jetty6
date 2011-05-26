package org.mortbay.jetty.test.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Collection of RemoteAssertResults along with the context about the specific {@link HttpServlet},
 * {@link HttpServletRequest}, and {@link HttpServletResponse} that this collection was collected within.
 */
public class RemoteAsserts
{
    /** The list of SecurityResult.id that have been already reported */
    private Set<String> seenIds;
    /** The list of results from a single test run, in the order that the tests were executed */
    private List<RemoteAssertResult> results;
    /** The servlet for the test run */
    private HttpServlet servlet;
    /** The incoming request object for the test run */
    private HttpServletRequest request;
    /** The outgoing response object for the test run */
    private HttpServletResponse response;

    public RemoteAsserts(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
    {
        this.servlet = servlet;
        this.request = request;
        this.response = response;
        this.results = new ArrayList<RemoteAssertResult>();
        this.seenIds = new HashSet<String>();
    }

    public void addResult(RemoteAssertResult result)
    {
        if (seenIds.contains(result.getId()))
        {
            StringBuilder err = new StringBuilder();
            err.append("Seen RemoteAssertResult with id [");
            err.append(result.getId()).append("] already!");
            err.append("\n ").append(result.toString());
            err.append("Fix your test to not use duplicate RemoteAssertResult IDs");
            result.failure(new IllegalStateException(err.toString()));
        }
        seenIds.add(result.getId());
        this.results.add(result);
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public HttpServletResponse getResponse()
    {
        return response;
    }

    public List<RemoteAssertResult> getResults()
    {
        return results;
    }

    public HttpServlet getServlet()
    {
        return servlet;
    }
}

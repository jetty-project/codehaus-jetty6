package org.mortbay.jetty.tests.webapp.policy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The context of a set of security checks.
 */
public class SecurityCheckContext
{
    /* The list of SecurityResult.id that have been already reported */
    private Set<String> seenIds;
    /* The list of results from a single test run, in the order that the tests were executed */
    private List<SecurityResult> results;
    /* The servlet for the test run */
    private HttpServlet servlet;
    /* The incoming request object for the test run */
    private HttpServletRequest request;
    /* The outgoing response object for the test run */
    private HttpServletResponse response;
    /* The mode that this security check is running in */
    private SecurityCheckMode mode;

    public SecurityCheckContext(SecurityCheckMode mode, HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp)
    {
        this.mode = mode;
        this.servlet = servlet;
        this.request = req;
        this.response = resp;
        this.results = new ArrayList<SecurityResult>();
        this.seenIds = new HashSet<String>();
    }

    public List<SecurityResult> getResults()
    {
        return results;
    }

    public HttpServlet getServlet()
    {
        return servlet;
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public HttpServletResponse getResponse()
    {
        return response;
    }

    public void addResult(SecurityResult result)
    {
        if (seenIds.contains(result.getId()))
        {
            StringBuilder err = new StringBuilder();
            err.append("Seen SecurityResult with id [");
            err.append(result.getId()).append("] already!");
            err.append("\n ").append(result.toString());
            err.append("Fix your test to not use duplicate SecurityResult IDs");
            result.failure(new IllegalStateException(err.toString()));
        }
        seenIds.add(result.getId());
        this.results.add(result);
    }

    public SecurityCheckMode getMode()
    {
        return mode;
    }
}

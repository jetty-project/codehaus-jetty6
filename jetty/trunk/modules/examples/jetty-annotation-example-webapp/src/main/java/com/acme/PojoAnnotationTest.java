package com.acme;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.annotation.Servlet;
import javax.servlet.http.annotation.InitParam;
import javax.servlet.http.annotation.jaxrs.GET;
import javax.servlet.http.annotation.jaxrs.POST;
    


@Servlet(urlMappings = { "/ttt/*" }, 
        name="PojoAnnotationTest",  
        initParams={@InitParam(name="x", value="y")})
@RunAs("special")        
public class PojoAnnotationTest
{
    @Resource(mappedName="maxAmount")
    private Double maxAmount;
    
    boolean postConstruct;
    boolean preDestroy;
    
    @PostConstruct
    private void myPostConstructMethod ()
    {       
        postConstruct = true;
    }
    
    @PreDestroy
    private void myPreDestroyMethod()
    {
        preDestroy = true;
    }
    
    @GET()
    @POST()
    public void anything (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.getWriter().println("<h1>Pojo Servlet</h1>");
        response.getWriter().println("<p>Acting like a Servlet.</p>");
        boolean result = (request.getMethod().equalsIgnoreCase("GET") || request.getMethod().equalsIgnoreCase("POST"));
        response.getWriter().println("<p>Method = "+request.getMethod()+(result?" PASS":" FAIL")+"</p>");
        String initParam = request.getServletContext().getInitParameter("x");
        result = (initParam != null && initParam.equals("y"));
        response.getWriter().println("<p>InitParam x = "+initParam+(result?" PASS":" FAIL")+"</p>");
        result = (maxAmount != null && maxAmount.doubleValue() == 55.0);
        response.getWriter().println("<p>Resource injection maxAmount = "+maxAmount+(result?" PASS":" FAIL")+"</p>");
        result = request.isUserInRole("special");
        response.getWriter().println("<p>RunAs userIsInRole special = "+result+"</p>");
        response.getWriter().println("<p>PostConstruct called "+postConstruct+"</p>");
        response.getWriter().println("<p>PreDestroy called "+preDestroy+"</p>");
       
        /*
        HttpSession session = request.getSession(true);
        Boolean filterResult = (Boolean)session.getAttribute("filterResult");
        result = (filterResult != null && filterResult.booleanValue());
        response.getWriter().println("<p>Filter was called "+result+"</p>");
        */
    }
}

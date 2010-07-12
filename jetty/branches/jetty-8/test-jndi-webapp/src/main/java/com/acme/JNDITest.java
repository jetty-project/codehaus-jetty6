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

/**
 * 
 */
package com.acme;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.mail.Session;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

/**
 * JNDITest
 * 
 * Use JNDI from within Jetty.
 * 
 * Also, use servlet spec 2.5 resource injection and lifecycle callbacks from within the web.xml
 * to set up some of the JNDI resources.
 *
 */
public class JNDITest extends HttpServlet 
{   
    private DataSource myDS;
  
    private Session myMailSession;
    private Double wiggle;
    private Integer woggle;
    private Double gargle;
    
    private String resourceNameMappingInjectionResult;
    private String envEntryOverrideResult;
    private String postConstructResult = "PostConstruct method called: FALSE";
    private String preDestroyResult = "PreDestroy method called: NOT YET";
    private String envEntryGlobalScopeResult;
    private String envEntryWebAppScopeResult;
    private String userTransactionResult;
    private String mailSessionResult;
    
    
    public void setMyDatasource(DataSource ds)
    {
        myDS=ds;
    }
 
    
    private void postConstruct ()
    {
        String tmp = (myDS == null?"":myDS.toString());
        resourceNameMappingInjectionResult= "Injection of resource to locally mapped name (java:comp/env/mydatasource as java:comp/env/mydatasource1): "+myDS.toString();
        envEntryOverrideResult = "Override of EnvEntry in jetty-env.xml (java:comp/env/wiggle): "+(wiggle==55.0?"PASS":"FAIL(expected 55.0, got "+wiggle+")");
        postConstructResult = "PostConstruct method called: PASS";
    }
    
    private void preDestroy()
    {
        preDestroyResult = "PreDestroy method called: PASS";
        //close datasources - necessary for Atomikos
        close(myDS);
    }
    
    public void close (DataSource ds)
    {
        if (ds != null)
        {
            try
            {
                Method close = ds.getClass().getMethod("close", new Class[]{});
                close.invoke(ds, new Object[]{});
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    
    
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            InitialContext ic = new InitialContext();
            woggle = (Integer)ic.lookup("java:comp/env/woggle");
            envEntryGlobalScopeResult = "EnvEntry defined in context xml lookup result (java:comp/env/woggle): "+(woggle==4000?"PASS":"FAIL(expected 4000, got "+woggle+")");
            gargle = (Double)ic.lookup("java:comp/env/gargle");
            envEntryWebAppScopeResult = "EnvEntry defined in jetty-env.xml lookup result (java:comp/env/gargle): "+(gargle==100.0?"PASS":"FAIL(expected 100, got "+gargle+")");
            UserTransaction utx = (UserTransaction)ic.lookup("java:comp/UserTransaction");
            userTransactionResult = "UserTransaction lookup result (java:comp/UserTransaction): "+(utx!=null?"PASS":"FAIL");
            myMailSession = (Session)ic.lookup("java:comp/env/mail/Session");
            mailSessionResult = "Mail Session lookup result (java:comp/env/mail/Session): "+(myMailSession!=null?"PASS": "FAIL");
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    
    
    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {   
        String mailTo = request.getParameter("mailto");
        String mailFrom = request.getParameter("mailfrom");
        
        if (mailTo != null)
            mailTo = mailTo.trim();
        
        if (mailFrom != null)
            mailFrom = mailFrom.trim();
        
        try
        {
            response.setContentType("text/html");
            ServletOutputStream out = response.getOutputStream();
            out.println("<html>");
            out.println("<h1>Jetty JNDI Tests</h1>");
            out.println("<body>");
            
            out.println("<h2>Injection and JNDI Lookup Results</h2>");
            out.println("<p>"+resourceNameMappingInjectionResult+"</p>");
            out.println("<p>"+envEntryOverrideResult+"</p>");
            out.println("<p>"+postConstructResult+"</p>");
            out.println("<p>"+preDestroyResult+"</p>");
            out.println("<p>"+envEntryGlobalScopeResult+"</p>");
            out.println("<p>"+envEntryWebAppScopeResult+"</p>");
            out.println("<p>"+userTransactionResult+"</p>");
            out.println("<p>"+mailSessionResult+"</p>");
        

            out.println("</body>");            
            out.println("</html>");
            out.flush();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
    
  
    
    public void destroy ()
    {
    }
}

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.sql.XADataSource;
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
public class JNDITest extends HttpServlet {
    public static final String DATE_FORMAT = "EEE, d MMM yy HH:mm:ss Z";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    
    private static final String TABLE1 = "mytestdata1";
    private static final String TABLE2 = "mytestdata2";
    
    private static boolean setupDone = false;
    
    private DataSource myDS;
    private DataSource myDS2;
    private DataSource myDS99;
    private Session myMailSession;
    private Double wiggle;
    private Integer woggle;
    private Double gargle;
    
    private String resourceNameMappingInjectionResult;
    private String resourceContextXmlInjectionResult;
    private String resourceJettyEnvInjectionResult;
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
    
    public void setMyDatasource2(DataSource ds)
    {
        myDS2=ds;
    }

    public void setMyDatasource99(DataSource ds)
    {
        myDS99=ds;
    }
    
    private void postConstruct ()
    {
        String tmp = (myDS == null?"":myDS.toString());
        resourceNameMappingInjectionResult= "Injection of resource to locally mapped name (java:comp/env/mydatasource as java:comp/env/mydatasource1): "+myDS.toString();
        resourceContextXmlInjectionResult = "Injection of resource from context xml file (java:comp/env/mydatasource2): "+myDS2.toString();
        resourceJettyEnvInjectionResult = "Injection of resource from jetty-env xml file (java:comp/env/mydatasource99): "+myDS99.toString();
        envEntryOverrideResult = "Override of EnvEntry in jetty-env.xml (java:comp/env/wiggle): "+(wiggle==55.0?"PASS":"FAIL(expected 55.0, got "+wiggle+")");
        postConstructResult = "PostConstruct method called: PASS";
    }
    
    private void preDestroy()
    {
        preDestroyResult = "PreDestroy method called: PASS";
        //close datasources - necessary for Atomikos
        close(myDS);
        close(myDS2);
        close(myDS99);
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
            
            doSetup();
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
        boolean doCommit = true;
        
        String complete = request.getParameter("completion");
        String mailTo = request.getParameter("mailto");
        String mailFrom = request.getParameter("mailfrom");
        
        if (complete != null)
        {
            complete = complete.trim();
            if (complete.trim().equals("commit"))
                doCommit = true;
            else
                doCommit = false;
        }
       
        if (mailTo != null)
            mailTo = mailTo.trim();
        
        if (mailFrom != null)
            mailFrom = mailFrom.trim();
        
        try
        {
            response.setContentType("text/html");
            ServletOutputStream out = response.getOutputStream();
            out.println("<html>");
            out.println("<h1>Jetty6 JNDI & Transaction Tests</h1>");
            out.println("<body>");
            if (complete != null)
            {
              doTransaction(out, doCommit);
              out.println("<p>Value of foo in myDS after "+(doCommit?"commit":"rollback")+": <b>"+getFoo(myDS)+"</b></p>");
              out.println("<p>Value of foo in myDS2 after "+(doCommit?"commit":"rollback")+": <b>"+getFoo(myDS2)+"</b></p>");
            }
            else if (mailTo != null && mailFrom != null)
            {
                doMail (mailTo, mailFrom);
                out.println("<p>Sent!</p>");
            }
            out.println("<a href=\"index.html\">Try again?</a>");

            out.println("<h2>Injection and JNDI Lookup Results</h2>");
            out.println("<p>"+resourceNameMappingInjectionResult+"</p>");
            out.println("<p>"+resourceContextXmlInjectionResult+"</p>");
            out.println("<p>"+resourceJettyEnvInjectionResult+"</p>");
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
    
    public void doMail (String mailTo, String mailFrom)
    throws Exception
    {
        Message msg = new MimeMessage(myMailSession);

        
        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(mailFrom);
        msg.setFrom(addressFrom);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
        msg.setSubject("Jetty Mail Test Succeeded");
        msg.setContent("The test of Jetty Mail @ "+new Date()+" has been successful.", "text/plain");
        msg.addHeader ("Date", dateFormat.format(new Date()));
        Transport.send(msg);

    }

    public void doTransaction(ServletOutputStream out, boolean doCommit)
    throws Exception
    {
        //check DataSource and Transactions
        Connection c1 = null; 
        Connection c2 = null;
        Statement s1 = null;
        Statement s2 = null;
        UserTransaction utx = null;
        try
        {
            doSetup();
            
            InitialContext ic = new InitialContext();
            utx = (UserTransaction)ic.lookup("java:comp/UserTransaction");
            
            utx.begin();
            
            c1 = myDS.getConnection();
            c2 = myDS2.getConnection();
            
            s1 = c1.createStatement();
            s2 = c2.createStatement();
            
            s1.executeUpdate("update "+TABLE1+" set foo=foo + 1 where id=1");
            s2.executeUpdate("update "+TABLE2+" set foo=foo + 1 where id=1");
            
            s1.close();
            s2.close();
            
            c1.close();
            c2.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            doCommit = false;
        }
        finally
        {
           if (doCommit)
               utx.commit();
           else
               utx.rollback();
        }
        
    }
    
    private Integer getFoo (DataSource ds)
    throws Exception
    {
        Connection c = null;
        Statement s = null;
        Integer value = null;
        try
        {
            c = ds.getConnection();
            s = c.createStatement();
            String tablename = (ds.equals(myDS)?TABLE1:TABLE2);
            ResultSet results = s.executeQuery("select foo from "+tablename+" where id=1");
            if (results.next())
                value = new Integer(results.getInt(1));
            
            results.close();
            
            return value;
        }
        finally
        {
            if (s != null) s.close();
            if (c != null) c.close();
        }
    }
    
    private void doSetup ()
    throws Exception
    {
        
        if (setupDone)
            return;
        
        
        Connection c1=null;
        Connection c2=null;
        Statement s1=null;
        Statement s2=null;
        try
        {
            c1 = myDS.getConnection();
            c2 = myDS2.getConnection();
            
            s1 = c1.createStatement();
            s2 = c2.createStatement();

            try
            {
                s1.execute("create table "+TABLE1+" ( id INTEGER, foo INTEGER )");
                s1.executeUpdate("insert into "+TABLE1+" (id, foo) values (1, 1)");
                c1.commit();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

            try
            {
                s2.execute("create table "+TABLE2+" ( id INTEGER, foo INTEGER )");
                s2.executeUpdate("insert into "+TABLE2+" (id, foo) values (1, 1)");
                c2.commit();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

            setupDone = true;
        }
        finally
        {
            if (s1 != null) s1.close();
            if (s2 != null) s2.close();
            if (c1 != null) c1.close();
            if (c2 != null) c2.close();
        }
    }
    
    private void doTearDown()
    throws Exception
    {
        Connection c1=null;
        Connection c2=null;
        Statement s1=null;
        Statement s2=null;
        try
        {
            c1 = myDS.getConnection();
            c2 = myDS2.getConnection();
            
            s1 = c1.createStatement();
            s2 = c2.createStatement();
            
            s1.execute("drop table "+TABLE1);
            c1.commit();
            s2.execute("drop table "+TABLE2);
            c2.commit();
            
        }
        catch (IllegalStateException e)
        {
            doTearDown();
        }
        finally
        {
            if (s1 != null) s1.close();
            if (s2 != null) s2.close();
            if (c1 != null) c1.close();
            if (c2 != null) c2.close();
        }
    }
    
    public void destroy ()
    {
        
        try
        {
            doTearDown();     
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            super.destroy();
        }
    }
}

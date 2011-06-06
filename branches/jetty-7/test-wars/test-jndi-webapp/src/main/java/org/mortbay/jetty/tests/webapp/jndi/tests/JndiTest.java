package org.mortbay.jetty.tests.webapp.jndi.tests;

import javax.mail.Session;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import junit.framework.Assert;

import org.junit.Test;

public class JndiTest
{

    private DataSource myDS;
    
    private Session myMailSession;
    private Double wiggle;
    private Integer woggle;
    private Double gargle;
    
    
    @Test
    public void testJndiDouble()
    {
        try
        {
            InitialContext ic = new InitialContext();
            
            gargle = (Double)ic.lookup("java:comp/env/gargle");           
            Assert.assertEquals(100.0,gargle.doubleValue());
            
            
        }
        catch (Exception ex)
        {   
            //ex.printStackTrace(); 
            
            Assert.fail(ex.getClass().getSimpleName());
        }
    }
    
    public void setMyDatasource(DataSource ds)
    {
        myDS=ds;
    }
}

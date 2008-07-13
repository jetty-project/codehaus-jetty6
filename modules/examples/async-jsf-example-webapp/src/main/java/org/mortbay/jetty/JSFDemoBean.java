package org.mortbay.jetty;

import javax.faces.context.FacesContext;

public class JSFDemoBean
{
    private String userName = null;
    
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    
    public String getUserName()
    {
        return this.userName;
    }
}
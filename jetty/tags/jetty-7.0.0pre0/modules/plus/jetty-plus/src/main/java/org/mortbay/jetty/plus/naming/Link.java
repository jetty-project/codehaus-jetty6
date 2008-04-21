package org.mortbay.jetty.plus.naming;

import javax.naming.NamingException;



public class Link extends NamingEntry
{

    public Link(String jndiName, Object object) throws NamingException
    {
        //jndiName is the name according to the web.xml
        //objectToBind is the name in the environment
        super(jndiName, object);
    }


    public void bindToENC() throws NamingException
    {
      throw new UnsupportedOperationException();
    }


    public void bindToENC(String overrideName) throws NamingException
    {
      throw new UnsupportedOperationException();
    }
    


}

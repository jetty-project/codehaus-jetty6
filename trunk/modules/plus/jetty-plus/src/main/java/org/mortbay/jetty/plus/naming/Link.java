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



    public void bindToENC(String localName) throws NamingException
    {
        throw new UnsupportedOperationException("Method not supported for Link objects");
    }
    


}

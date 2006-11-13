package org.mortbay.jetty.servlet.jsr77;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

public class Jsr77ServletHandler extends ServletHandler 
{
    
    public ServletHolder newServletHolder ()
    {
        return new Jsr77ServletHolder();
    }
	public ServletHolder newServletHolder(Class servlet) 
	{
		return new Jsr77ServletHolder(servlet);
	}
}

package org.mortbay.jetty.servlet.jsr77;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

public class Jsr77ServletHandler extends ServletHandler 
{
	public ServletHolder newServletHolder(Class servlet) 
	{
        System.err.println("ADDING A NEW JSR77ServletHolder");
		return new Jsr77ServletHolder(servlet);
	}
}

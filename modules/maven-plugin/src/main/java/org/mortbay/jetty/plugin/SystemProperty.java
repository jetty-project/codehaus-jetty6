/**
 * 
 */
package org.mortbay.jetty.plugin;

/**
 * @author janb
 *
 */
public class SystemProperty
{
	
	
	private String name;
	private String value;
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return this.name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * @return Returns the value.
	 */
	public String getValue()
	{
		return this.value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	/** Set a System.property with this value
	 * if it is not already set.
	 * @return
	 */
	public boolean setIfNotSetAlready()
	{
    	if (System.getProperty(getName()) == null)
    	{
    		System.setProperty(getName(), getValue());
    		return true;
    	}
    	
    	return false;
	}
	
}

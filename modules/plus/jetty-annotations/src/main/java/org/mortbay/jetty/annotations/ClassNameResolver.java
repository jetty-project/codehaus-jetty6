package org.mortbay.jetty.annotations;



public interface ClassNameResolver
{
    /**
     * Based on the execution context, should the class represented
     * by "name" be excluded from consideration?
     * @param name
     * @return
     */
    public boolean isExcluded (String name);
    
    
    /**
     * Based on the execution context, if a duplicate class 
     * represented by "name" is detected, should the existing
     * one be overridden or not?
     * @param name
     * @return
     */
    public boolean shouldOverride (String name);
}

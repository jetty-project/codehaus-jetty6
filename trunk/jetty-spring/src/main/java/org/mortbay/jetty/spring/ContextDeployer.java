package org.mortbay.jetty.spring;

import org.eclipse.jetty.util.resource.Resource;


/* ------------------------------------------------------------ */
/**
 * Convenience class for jetty with Spring.
 * This class provides a setContextDir method as an alternate
 * access to the {@link #setConfigurationDir(String)} API.
 */
public class ContextDeployer extends org.eclipse.jetty.deploy.ContextDeployer
{
    public ContextDeployer() throws Exception
    {
        super();
    }
    
    public void setContextsDir(String contexts)
    {
        try
        {
            super.setConfigurationDir(Resource.newResource(contexts));
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public String getContextsDir()
    {
        return super.getConfigurationDir().toString();
    }

}

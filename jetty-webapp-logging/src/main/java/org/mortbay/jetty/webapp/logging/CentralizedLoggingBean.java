// ========================================================================
// Copyright (c) Webtide LLC
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// The Apache License v2.0 is available at
// http://www.apache.org/licenses/LICENSE-2.0.txt
//
// You may elect to redistribute this code under either of these licenses.
// ========================================================================
package org.mortbay.jetty.webapp.logging;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Jetty Centralized Logging bean.
 */
public class CentralizedLoggingBean extends AbstractLifeCycle
{
    private static final String CONFIG_CLASS = CentralizedWebAppLoggingConfiguration.class.getName();
    private Server server;
    private String configurationFilename;

    public String getConfigurationFilename()
    {
        return configurationFilename;
    }

    public void setConfigurationFilename(String filename)
    {
        this.configurationFilename = filename;
    }

    public Server getServer()
    {
        return server;
    }

    public void setServer(Server server)
    {
        this.server = server;
        setWebAppContextConfigurations(server);
    }

    @Override
    protected void doStart() throws Exception
    {
        setWebAppContextConfigurations(server);
        super.doStart();
    }

    public static void setWebAppContextConfigurations(Server server)
    {
        String configs[] = (String[])server.getAttribute(WebAppContext.SERVER_CONFIG);
        if (configs == null)
        {
            WebAppContext wac = new WebAppContext();
            configs = wac.getDefaultConfigurationClasses();
        }

        // Test if config class exists already.
        for(String config: configs) {
            if(config.equals(CONFIG_CLASS)) {
                return; // All done. already exists.
            }
        }
        
        // Add config class.
        String newconfigs[] = new String[configs.length + 1];
        if (configs.length > 0)
        {
            System.arraycopy(configs,0,newconfigs,0,configs.length);
        }
        newconfigs[configs.length] = CONFIG_CLASS;
        server.setAttribute(WebAppContext.SERVER_CONFIG,newconfigs);
    }
}

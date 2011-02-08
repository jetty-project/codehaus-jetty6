package org.mortbay.jetty.webapp.logging;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppLifeCycle;
import org.eclipse.jetty.deploy.graph.Node;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class CentralizedWebAppLoggingBinding implements AppLifeCycle.Binding
{
    @Override
    public String[] getBindingTargets()
    {
        return new String[]
        { "deploying" };
    }

    @Override
    public void processBinding(Node node, App app) throws Exception
    {
        ContextHandler handler = app.getContextHandler();
        if (handler == null)
        {
            throw new NullPointerException("No Handler created for App: " + app);
        }

        if (handler instanceof WebAppContext)
        {
            WebAppContext webapp = (WebAppContext)handler;
            webapp.addSystemClass("org.apache.log4j.");
            webapp.addSystemClass("org.slf4j.");
            webapp.addSystemClass("org.apache.commons.logging.");
        }
    }
}

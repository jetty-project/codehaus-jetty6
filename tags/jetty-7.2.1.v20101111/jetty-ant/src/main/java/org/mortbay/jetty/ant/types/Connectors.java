// ========================================================================
// Copyright 2006-2007 Sabre Holdings.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.ant.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

/**
 * Specifies a jetty configuration <connectors/> element for Ant build file.
 * 
 * @author Jakub Pawlowicz
 */
public class Connectors
{
    private List connectors = new ArrayList();
    private List defaultConnectors = new ArrayList();

    /**
     * Default constructor.
     */
    public Connectors() {
        this(8080, 30000);
    }

    /**
     * Constructor.
     * 
     * @param port The port that the default connector will listen on
     * @param maxIdleTime The maximum idle time for the default connector
     */
    public Connectors(int port, int maxIdleTime) {
        /** Create/configure the default connectors */
        org.eclipse.jetty.server.Connector defaultConnector = new SelectChannelConnector();

        defaultConnector.setPort(port);
        defaultConnector.setMaxIdleTime(maxIdleTime);

        defaultConnectors.add(defaultConnector);
    }

    /**
     * Adds a connector to the list of connectors to deploy.
     * 
     * @param connector A connector to add to the list
     */
    public void add(Connector connector)
    {
        connectors.add(connector);
    }

    /**
     * Returns the list of known connectors to deploy.
     * 
     * @return The list of known connectors
     */
    public List getConnectors()
    {
        return connectors;
    }

    /**
     * Gets the default list of connectors to deploy when no connectors
     * were explicitly added to the list.
     * 
     * @return The list of default connectors
     */
    public List getDefaultConnectors()
    {
        return defaultConnectors;
    }
}

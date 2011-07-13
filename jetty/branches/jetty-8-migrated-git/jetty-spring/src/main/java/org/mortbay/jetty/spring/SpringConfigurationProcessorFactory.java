package org.mortbay.jetty.spring;

import org.eclipse.jetty.xml.ConfigurationProcessor;
import org.eclipse.jetty.xml.ConfigurationProcessorFactory;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 * Spring ConfigurationProcessor Factory
 * <p>
 * Create a {@link SpringConfigurationProcessor} for XML documents with a "beans" element.
 * The factory is discovered by a {@link ServiceLoader} for {@link ConfigurationProcessorFactory}.
 * @see SpringConfigurationProcessor
 * @see XmlConfiguration
 *
 */
public class SpringConfigurationProcessorFactory implements ConfigurationProcessorFactory
{
    public ConfigurationProcessor getConfigurationProcessor(String dtd, String tag)
    {
        if ("beans".equals(tag))
            return new SpringConfigurationProcessor();
        return null;
    }
}

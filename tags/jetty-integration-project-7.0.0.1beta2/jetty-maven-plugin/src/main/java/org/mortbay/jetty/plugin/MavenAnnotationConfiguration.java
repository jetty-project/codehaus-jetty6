package org.mortbay.jetty.plugin;

import java.io.File;

import org.eclipse.jetty.annotations.AnnotationFinder;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class MavenAnnotationConfiguration extends AnnotationConfiguration
{

    public void parseWebInfClasses (final WebAppContext context, final AnnotationFinder finder)
    throws Exception
    {

        JettyWebAppContext jwac = (JettyWebAppContext)context;
        if (jwac.getClassPathFiles() == null)
            super.parseWebInfClasses (context, finder);
        else
        {
            Log.debug("Scanning classes ");
            //Look for directories on the classpath and process each one of those
            for (File f:jwac.getClassPathFiles())
            {
                if (f.isDirectory() && f.exists())
                {
                    finder.find(Resource.newResource(f.toURL()), 
                                new ClassNameResolver()
                    {
                        public boolean isExcluded (String name)
                        {
                            if (context.isSystemClass(name)) return true;
                            if (context.isServerClass(name)) return false;
                            return false;
                        }

                        public boolean shouldOverride (String name)
                        {
                            //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                            if (context.isParentLoaderPriority())
                                return false;
                            return true;
                        }
                    });
                }
            }
        }
    }
}


JETTY
=====

The Jetty project is a 100% Java HTTP Server, HTTP Client
and Servlet Container. The core project is hosted by
the Eclipse Foundation at

  http://www.eclipse.org/jetty/

The jetty integrations with 3rd party modules are hosted
by the Codehaus at

  http://jetty.codehaus.org

JETTY HIGHTIDE
==============

This is the Jetty-hightide distribution which 
contains the core jetty modules, plus the 3rd party 
dependencies and integrations needed to create a full 
featured application server.



JETTY DISTRIBUTION
==================

The jetty-distribution module from Jetty @ eclipse
project is based on the Jetty modules from eclipse plus
dependencies that have been through the eclipse IP
process and conditioning.

  http://www.eclipse.org/jetty/

The jetty-distribution and it's dependencies are  provided under
the terms and conditions of the Eclipse Foundation Software
User Agreement unless otherwise specified.

This distribution contains only the core functionality
of a servlet server and the HTTP client.


MAVEN
=====
All Jetty artefacts are available as maven dependencies
under the org.eclipse.jetty and org.mortbay.hightide group IDs

  http://repo1.maven.org/maven2/org/eclipse/jetty/
  http://repo2.maven.org/maven2/org/mortbay/jetty/


RUNNING JETTY
=============

The run directory is either the top-level of a distribution
or jetty-hightide/target/hightide directory when built from
source.

Jetty start.jar provides a cross platform replacement for startup scripts.
It makes use of executable JAR that builds the classpath and then executes
jetty.

To run with all the demo options:

  java -jar start.jar OPTIONS=All

To run with the default options:

  java -jar start.jar

The default options may be specified in the start.ini file, or if
that is not present, they are defined in the start.config file that
is within the start.jar.

To run with specific configuration file(s)

  java -jar start.jar etc/jetty.xml

To see the available options

  java -jar start.jar --help

To run with JSP support (if available)

  java -jar start.jar OPTIONS=Server,jsp

To run with JMX support

  java -jar start.jar OPTIONS=Server,jmx etc/jetty-jmx.xml etc/jetty.xml

To run with JSP & JMX support

  java -jar start.jar OPTIONS=Server,jsp,jmx etc/jetty-jmx.xml etc/jetty.xml


~                                              

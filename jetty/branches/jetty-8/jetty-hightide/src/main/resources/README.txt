
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
dependencies and integrations needed to create an
application server.



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

Command Line
============

The run directory is either the top-level of a distribution
or jetty-distribution/target/distribution directory when built from
source.

To run with the default options:

  java -jar start.jar

To run with all options enabled:

  java -jar start.jar OPTIONS=All

To run with specific configuration file(s):

  java -jar start.jar etc/jetty.xml

To see the available options:

  java -jar start.jar --help

To run with JMX support

  java -jar start.jar OPTIONS=Server,jmx etc/jetty-jmx.xml etc/jetty.xml


Start.ini File
==============                                              

The start.ini file in the top level directory of the distribution
contains elements that will be applied to the run line when jetty
starts. This mechanism is an alternative to specifying them
on the command line as shown in the previous section. 


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

To run with the default options:

  java -jar start.jar

To see the available options and the default arguments
provided by the start.ini file:

  java -jar start.jar --help

To run with add configuration file(s), eg SSL

  java -jar start.jar etc/jetty-ssl.xml

To run without the args from start.ini

  java -jar start.jar --ini OPTIONS=Server,websocket etc/jetty.xml etc/jetty-deploy.xml etc/jetty-ssl.xml

To run with JNDI support

  java -jar start.jar OPTIONS=Server,jsp


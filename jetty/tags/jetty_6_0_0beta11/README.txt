

This is the Jetty 6 HTTP server and servlet container.

RUNNING JETTY
=============
From the release, you can run the server with:

   java -jar start.jar

and then point your browser at 

   http://localhost:8080

and click to the test webapp, where there are some demos and more
information.

The start command above is equivalent to 

   java -jar start.jar etc/jetty.xml

which gives a configuration file on the commandline. An explicit
configuration file (or multiple configuration files) may be
given to select specific configurations.


RUNNING WITH JMX
================

The server can be run with JMX management with the command:

   java -jar start.jar etc/jetty-jmx.xml etc/jetty.xml
   
This commands adds the jmx configuration file before the server
configuration.

RUNNING WITH JETTY PLUS
=======================

The server can be run as JettyPlus (JNDI, JAAS etc.) with the 
command:

   java -jar start.jar etc/jetty.xml etc/jetty-plus.xml
   
This commands adds the plus configuration file after
the server configuration file


ASSEMBLIES
==========
The assemblies directory contains alternate bundlings of the jetty
jars that can be run from the command line without additional dependencies:

eg

  java -jar assemblies/jetty-standalone.jar

  java -jar assemblies/jetty-standalone.jar 8080 -webapp mywebapp.war
     


BUILDING JETTY
==============

Jetty uses maven 2 as it's build system.  Maven will fetch
the dependancies, build the server and assemble a runnable
version:

  mvn install

DEPENDENCIES 
============
The only real dependancy is the servlet api, so only 
the jars in the top level of the lib directory are needed
to run Jetty (and they can be trimmed from many applications).

The jars in the subdirectories of lib are all optional, but
are included on the classpath by the standard start.jar 
mechanism


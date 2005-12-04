

This is a release of Jetty 6 (was JettyExperimental)

This is mostly a clean slate implementation with only a little code 
taken from Jetty 5, so that 8 years of cruft can be removed.

Thus it has been able to be rearchitected to more closely match/use the 
current servlet API and to closer model concepts such as filters and contexts.

RUNNING JETTY
=============
From the release, you can run the server with:

   java -jar start.jar etc/jetty.xml

and then point your browser at 

   http://localhost:8080

and click to the test webapp, where there are some demos and more
information.



BUILDING JETTY
==============

Jetty uses maven 2 as it's build system.  Maven will fetch
the dependancies, build the server and assemble a runnable
version:

  m2 install

The only real dependancy is slf4j logging.  To run the server 
with JSP support, you will also need the jasper jars, commons 
logging, commons el and xerces.   

the ant build.xml file exist in the top level to assist
with cleaning the project:

 ant clean
 m2 clean:clean



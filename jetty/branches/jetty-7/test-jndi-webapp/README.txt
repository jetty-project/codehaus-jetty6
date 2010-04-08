Build 
-----

To build this test webapp, do "mvn clean install".
This produces the artifacts:
  + jetty-test-jndi-webapp.war
  + jetty-test-jndi-webapp-config.jar

You can now run the demo with maven by doing:

   mvn jetty:run

If you want to deploy this demo webapp to an installed
distribution of jetty, read on.


Installation
------------

Unjar the jetty-test-jndi-webapp.config.jar inside the 
$JETTY-HOME directory.

If you are using a hightide release of jetty:

  + Unpack the jetty-test-jndi-webapp.war file to
    $JETTY-HOME/webapps/test-jndi.

If you are using a non hightide release of jetty:

 + download and copy the Atomikos (http://www.atomikos.com)
   and Derby jars into your $JETTY_HOME/lib/ext directory

 + Edit $JETTY_HOME/etc/jetty-plus.xml and uncomment the 
   DeployerManager section. 

 + Unpack the jetty-test-jndi-webapp.war file to
   $JETTY_HOME/webapps-plus/test-jndi

Running the Demo
----------------
With jetty maven plugin:

   mvn jetty:run


In jetty-hightide:

   java -jar start.jar


In jetty (non hightide releases only):

   java -jar start.jar OPTIONS=All etc/jetty.xml etc/jetty-plus.xml


Alternative Transaction Manager and Database
--------------------------------------------
This demo has been tested with both JOTM (http://jotm.objectweb.org) 
or Atomikos (http://www.atomikos.com).

This demo will use the Derby database. 

To change transaction and/or database implementations, 
edit the $JETTY_HOME/contexts/test-jndi.xml and 
$JETTY-HOME/webapps/test-jndi/WEB-INF/jetty-env.xml files and
replace the references to Atomikos and/or Derby.


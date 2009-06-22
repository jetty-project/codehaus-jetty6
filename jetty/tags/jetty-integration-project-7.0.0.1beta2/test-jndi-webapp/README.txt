Build and Install
-----------------

To build this test webapp, do "mvn clean install".
This will produce the following artifacts in ./target:

 + contexts/
      README-test-jndi.txt
      test-jndi.xml
      test-jndi.d/
         WEB-INF/
            instruction-web.html
            jta.properties
  + jetty-test-jndi-webapp.war

Copy all the contents of the contexts/ directory to 
$JETTY-HOME/contexts.

Unpack the jetty-test-jndi-webapp.war file to
$JETTY-HOME/webapps/test-jndi.


Deploying
--------------
Jetty does not ship with a native transaction manager, but 
can plugin a variety of solutions. This demo has been tested
with both JOTM (http://jotm.objectweb.org) or Atomikos
(http://www.atomikos.com).

You will need to download the relevant jars for your chosen
transaction manager, and install them into $JETTY-HOME/lib/ext.
See the jetty wiki pages for JOTM(http://docs.codehaus.org/display/JETTY/JOTM)
and Atomikos (http://docs.codehaus.org/display/JETTY/Atomikos)
for more info.

The example uses the Derby database, so also download the
derby.jar and derbytools.jar file from the Derby site
(http://db.apache.org/derby) and put them in $JETTY-HOME/lib/ext.

Now edit $JETTY-HOME/contexts/test-jndi.xml and uncomment one of the 
transaction manager setups.

Edit $JETTY-HOME/webapps/test-jndi/WEB-INF/jetty-env.xml and uncomment
one of the transaction manager setups.


Running the Demo
----------------
You run the demo like so:
   
   java -jar start.jar DOPTIONS=All


Adding Support for a Different Transaction Manager
--------------------------------------------------

1. Edit the filter.properties file in 
   $JETTY-HOME/modules/examples/jndi-example-webapp/src/etc/templates
   and add a new set of token and replacement strings following the
   pattern established for ATOMIKOS and JOTM.

2. Edit the jetty-env.xml file in
   $JETTY-HOME/modules/examples/jndi-example-webapp/src/etc/templates
   and add configuration for new transaction manager following the
   pattern established for the other transaction managers.

3. Edit jetty-test-jndi.xml file in
   $JETTY-HOME/modules/examples/jndi-example-webapp/src/etc/templates
   and add configuration for the new transaction manager following
   the pattern established for the other transaction managers.

4. Rebuild $JETTY-HOME/modules/examples/jndi-example-webapp (mvn clean install).

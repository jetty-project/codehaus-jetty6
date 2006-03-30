Building the demo jndi webapp
----------------------------

You must supply the name of a transaction manger on the command line.
eg

  mvn -Dtxmgr=blah install


The names of the currently supported transaction managers for the demo are:

  atomikos
  jotm


Running the demo
----------------

After it is built, you run the demo:
   
   java -jar start.jar etc/jetty.xml etc/jetty-plus.xml etc/jetty-test-jndi.xml
   


To add support for a different transaction manager to the demo
--------------------------------------------------------------

You need to create a new properties file in src/templates of the
same name as you want to be supplied on the command line as the
value of the -Dtxmgr property.

For example, suppose you are adding support for a new transaction
manager called "blah":

  * create a src/templates/blah.properties file using the others
    as a guide

  * edit pom.xml and change the <fail> error message to include
   "blah" as one of the options

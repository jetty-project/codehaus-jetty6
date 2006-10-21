Need to automate the build of this....

but for now do the following:

Build
-----

In this directory do: 
  mvn install

  gcc -I$JDK_HOME/include/ -I$JDK_HOME/include/linux/ -shared src/main/native/org_mortbay_setuid_SetUID.c -o ../../lib/ext/libsetuid.so

Where $JDK_HOME is same as $JAVA_HOME and where linux should be replaced by the name of your operating system.

Install
-------

From this directory do: 

  cp target/jetty-setuid-6.1-SNAPSHOT.jar ../../lib/ext/
  cp etc/jetty-setuid.xml ../../etc


Configuration
-------------

Change etc/jetty-setuid.xml to use the userid you want.


Running
-------
In the top level jetty directory do:

  sudo java -Djava.library.path=lib/ext -jar start.jar etc/jetty-setuid.xml etc/jetty.xml


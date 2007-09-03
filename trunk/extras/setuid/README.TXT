

Build and Install
-----------------

In this directory do (this can only be built on linux or unix with gcc compiler): 

  mvn install




Configuration
-------------

Change etc/jetty-setuid.xml to use the userid you want.


Running
-------
In the top level jetty directory do:
  
- if your libsetuid.so is located at <jetty-root>/lib/ext which is by default located at that folder
  sudo java -jar start.jar etc/jetty-setuid.xml etc/jetty.xml


  
- you could alternately set jetty.libsetuid.path property to /directory/path/to/your/jetty/lib/libsetuid.so  
    * using -Djetty.libsetuid.path=/directory/path/to/your/jetty/lib/libsetuid.so option
        sudo java -Djetty.libsetuid.path=/directory/path/to/your/jetty/lib/libsetuid.so -jar start.jar etc/jetty-setuid.xml etc/jetty.xml

    * or you can set it as a System Property on your jetty configuration and execute the command below
        sudo java -jar start.jar etc/jetty-setuid.xml etc/jetty.xml   

- one last option, is setting java.library.path to the directory where your libsetuid.so is found
  sudo java -Djava.library.path=/directory/path/to/your/jetty/lib -jar start.jar etc/jetty-setuid.xml etc/jetty.xml


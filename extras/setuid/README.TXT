
Need to automate the build of this....
but for now I am manually doing the command:

mvn install

gcc -I$JDK_HOME/include/ -I$JDK_HOME/include/linux/ -shared src/main/native/org_mortbay_setuid_SetUID.c -o ../../lib/ext/libsetuid.so
cp target/jetty-setuid-6.1-SNAPSHOT.jar ../../lib/ext/
cp etc/jetty-setuid.xml ../../etc


cd ../..

sudo java -Djava.library.path=lib/ext -jar start.jar etc/jetty-setuid.xml etc/jetty.xml

     

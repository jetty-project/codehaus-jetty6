== Introduction ==

This module can be used to import a key/certificate pair from a pkcs12 file
into a regular JKS format keystore for use with Jetty and other Java-based 
SSL applications.

== Building ==

Run this command:
  mvn install

That will create a  jetty-pkcs12-*.jar under target/
 
== Generating a .pkcs12 file ==

If you don't already have a pkcs12 file, but do have PEM encoded certificate and key files,
then generate the PKCS12 file using the following openssl command:

  openssl pkcs12 -export -out keystore.pkcs12 -in www.crt -inkey www.key

== Converting from pkcs12 to jks ==

Run this command:
  java -jar jetty-pkcs12.jar keystore.pkcs12 keystore.jks

Upon execution, you will be prompted for the password for the pkcs12 keystore (input)
and a password for the .jks file (output). After execution, you will have a JKS file
which you can use in your application.

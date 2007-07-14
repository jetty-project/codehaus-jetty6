#!/bin/sh

[ $# -eq 1 ] || { echo Usage - $0 new_version >&2 ; exit 1 ; }

sed s/__JETTY_VERSION__/$1/g << _EOF_ | patch -p0
Index: pom.xml
===================================================================
--- pom.xml	(revision 1740)
+++ pom.xml	(working copy)
@@ -4,7 +4,7 @@
   <artifactId>project</artifactId>
   <packaging>pom</packaging>
   <name>Jetty Server Project</name>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <url>http://jetty.mortbay.org</url>
   <issueManagement>
     <system>jira</system>
Index: extras/jboss/pom.xml
===================================================================
--- extras/jboss/pom.xml	(revision 1740)
+++ extras/jboss/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/threadpool/pom.xml
===================================================================
--- extras/threadpool/pom.xml	(revision 1740)
+++ extras/threadpool/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/gwt/pom.xml
===================================================================
--- extras/gwt/pom.xml	(revision 1740)
+++ extras/gwt/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/spring/pom.xml
===================================================================
--- extras/spring/pom.xml	(revision 1740)
+++ extras/spring/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/tests/pom.xml
===================================================================
--- extras/tests/pom.xml	(revision 1740)
+++ extras/tests/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/win32service/pom.xml
===================================================================
--- extras/win32service/pom.xml	(revision 1740)
+++ extras/win32service/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/sslengine/pom.xml
===================================================================
--- extras/sslengine/pom.xml	(revision 1740)
+++ extras/sslengine/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/sessioncache/pom.xml
===================================================================
--- extras/sessioncache/pom.xml	(revision 1740)
+++ extras/sessioncache/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/servlet-tester/pom.xml
===================================================================
--- extras/servlet-tester/pom.xml	(revision 1740)
+++ extras/servlet-tester/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/ajp/pom.xml
===================================================================
--- extras/ajp/pom.xml	(revision 1740)
+++ extras/ajp/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/grizzly/pom.xml
===================================================================
--- extras/grizzly/pom.xml	(revision 1740)
+++ extras/grizzly/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/setuid/pom.xml
===================================================================
--- extras/setuid/pom.xml	(revision 1740)
+++ extras/setuid/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: extras/setuid/README.TXT
===================================================================
--- extras/setuid/README.TXT	(revision 1740)
+++ extras/setuid/README.TXT	(working copy)
@@ -19,7 +19,7 @@
 
 From this directory do: 
 
-  cp target/jetty-setuid-6.1-SNAPSHOT.jar ../../lib/ext/
+  cp target/jetty-setuid-__JETTY_VERSION__.jar ../../lib/ext/
   cp etc/jetty-setuid.xml ../../etc
 
 
Index: extras/xbean/pom.xml
===================================================================
--- extras/xbean/pom.xml	(revision 1740)
+++ extras/xbean/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: project-website/project-site/src/site/site.xml
===================================================================
--- project-website/project-site/src/site/site.xml	(revision 1740)
+++ project-website/project-site/src/site/site.xml	(working copy)
@@ -5,7 +5,7 @@
   <skin>
     <groupId>org.mortbay.jetty</groupId>
     <artifactId>jetty-skin</artifactId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </skin>
   <bannerLeft>
     <name>Jetty</name>
Index: project-website/project-site/pom.xml
===================================================================
--- project-website/project-site/pom.xml	(revision 1740)
+++ project-website/project-site/pom.xml	(working copy)
@@ -3,7 +3,7 @@
   <parent>
     <artifactId>project-website</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
@@ -11,7 +11,7 @@
   <artifactId>project-site</artifactId>
   <packaging>pom</packaging>
   <name>Jetty Site</name>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <build>
     <plugins>
       <plugin>
Index: project-website/project-skin/pom.xml
===================================================================
--- project-website/project-skin/pom.xml	(revision 1740)
+++ project-website/project-skin/pom.xml	(working copy)
@@ -7,7 +7,7 @@
   </parent>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-skin</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Maven Jetty Site Skin</name>
   <description>Maven Jetty Site Skin</description>
 </project>
Index: project-website/pom.xml
===================================================================
--- project-website/pom.xml	(revision 1740)
+++ project-website/pom.xml	(working copy)
@@ -3,7 +3,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
@@ -11,7 +11,7 @@
   <artifactId>project-website</artifactId>
   <packaging>pom</packaging>
   <name>Jetty WebSite</name>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <modules>
     <module>project-skin</module>
     <module>project-site</module>
Index: modules/plus/pom.xml
===================================================================
--- modules/plus/pom.xml	(revision 1740)
+++ modules/plus/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/jsp-2.0/pom.xml
===================================================================
--- modules/jsp-2.0/pom.xml	(revision 1740)
+++ modules/jsp-2.0/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/jsp-2.1/pom.xml
===================================================================
--- modules/jsp-2.1/pom.xml	(revision 1740)
+++ modules/jsp-2.1/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/servlet-api-2.5/pom.xml
===================================================================
--- modules/servlet-api-2.5/pom.xml	(revision 1740)
+++ modules/servlet-api-2.5/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/start/pom.xml
===================================================================
--- modules/start/pom.xml	(revision 1740)
+++ modules/start/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/annotations/pom.xml
===================================================================
--- modules/annotations/pom.xml	(revision 1740)
+++ modules/annotations/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/jspc-maven-plugin/src/site/site.xml
===================================================================
--- modules/jspc-maven-plugin/src/site/site.xml	(revision 1740)
+++ modules/jspc-maven-plugin/src/site/site.xml	(working copy)
@@ -3,7 +3,7 @@
   <skin>
     <groupId>org.mortbay.jetty</groupId>
     <artifactId>jetty-skin</artifactId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </skin>
   <bannerLeft>
     <name>Maven2 Jetty JSPC Plugin</name>
Index: modules/jspc-maven-plugin/src/site/apt/howto.apt
===================================================================
--- modules/jspc-maven-plugin/src/site/apt/howto.apt	(revision 1740)
+++ modules/jspc-maven-plugin/src/site/apt/howto.apt	(working copy)
@@ -8,7 +8,7 @@
         <plugin>
           <groupId>org.mortbay.jetty</groupId>
           <artifactId>maven-jetty-jspc-plugin</artifactId>
-          <version>6.1-SNAPSHOT</version>
+          <version>__JETTY_VERSION__</version>
           <executions>
             <execution>
               <id>jspc</id>
@@ -54,7 +54,7 @@
         <plugin>
           <groupId>org.mortbay.jetty</groupId>
           <artifactId>maven-jetty-jspc-plugin</artifactId>
-          <version>6.1-SNAPSHOT</version>
+          <version>__JETTY_VERSION__</version>
           . . .
         </plugin>
         <plugin>
Index: modules/jspc-maven-plugin/pom.xml
===================================================================
--- modules/jspc-maven-plugin/pom.xml	(revision 1740)
+++ modules/jspc-maven-plugin/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/jetty/pom.xml
===================================================================
--- modules/jetty/pom.xml	(revision 1740)
+++ modules/jetty/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/jsp-api-2.0/pom.xml
===================================================================
--- modules/jsp-api-2.0/pom.xml	(revision 1740)
+++ modules/jsp-api-2.0/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/jsp-api-2.1/pom.xml
===================================================================
--- modules/jsp-api-2.1/pom.xml	(revision 1740)
+++ modules/jsp-api-2.1/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/maven-plugin/src/site/site.xml
===================================================================
--- modules/maven-plugin/src/site/site.xml	(revision 1740)
+++ modules/maven-plugin/src/site/site.xml	(working copy)
@@ -3,7 +3,7 @@
   <skin>
     <groupId>org.mortbay.jetty</groupId>
     <artifactId>jetty-skin</artifactId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </skin>
   <bannerLeft>
     <name>Maven2 Jetty Plugin</name>
Index: modules/maven-plugin/pom.xml
===================================================================
--- modules/maven-plugin/pom.xml	(revision 1740)
+++ modules/maven-plugin/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/html/pom.xml
===================================================================
--- modules/html/pom.xml	(revision 1740)
+++ modules/html/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/naming/pom.xml
===================================================================
--- modules/naming/pom.xml	(revision 1740)
+++ modules/naming/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/management/pom.xml
===================================================================
--- modules/management/pom.xml	(revision 1740)
+++ modules/management/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: modules/util/pom.xml
===================================================================
--- modules/util/pom.xml	(revision 1740)
+++ modules/util/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: examples/spring-ebj3-demo/pom.xml
===================================================================
--- examples/spring-ebj3-demo/pom.xml	(revision 1740)
+++ examples/spring-ebj3-demo/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
Index: examples/test-jaas-webapp/pom.xml
===================================================================
--- examples/test-jaas-webapp/pom.xml	(revision 1740)
+++ examples/test-jaas-webapp/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: examples/embedded/pom.xml
===================================================================
--- examples/embedded/pom.xml	(revision 1740)
+++ examples/embedded/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: examples/test-annotations/pom.xml
===================================================================
--- examples/test-annotations/pom.xml	(revision 1740)
+++ examples/test-annotations/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: examples/test-webapp/pom.xml
===================================================================
--- examples/test-webapp/pom.xml	(revision 1740)
+++ examples/test-webapp/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: examples/test-jndi-webapp/pom.xml
===================================================================
--- examples/test-jndi-webapp/pom.xml	(revision 1740)
+++ examples/test-jndi-webapp/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: contrib/client/pom.xml
===================================================================
--- contrib/client/pom.xml	(revision 142)
+++ contrib/client/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
Index: contrib/cometd/pom.xml
===================================================================
--- contrib/cometd/pom.xml	(revision 142)
+++ contrib/cometd/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
Index: contrib/wadi/pom.xml
===================================================================
--- contrib/wadi/pom.xml	(revision 143)
+++ contrib/wadi/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <!--parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent-->
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-wadi-session-manager</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Wadi - Jetty Session Cache</name>
   <url>http://jetty.mortbay.org</url>
   <licenses>
Index: contrib/jetty-ant/pom.xml
===================================================================
--- contrib/jetty-ant/pom.xml	(revision 144)
+++ contrib/jetty-ant/pom.xml	(working copy)
@@ -3,7 +3,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </parent>
 	<modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
Index: contrib/terracotta/pom.xml
===================================================================
--- contrib/terracotta/pom.xml	(revision 142)
+++ contrib/terracotta/pom.xml	(working copy)
@@ -4,7 +4,7 @@
   <artifactId>terracotta-sessions</artifactId>
   <packaging>jar</packaging>
   <name>Terracotta Sessions for Jetty</name>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <url>http://jetty.mortbay.org</url>
   <licenses>
     <license>
@@ -42,7 +42,7 @@
     <dependency>
       <groupId>org.mortbay.jetty</groupId>
       <artifactId>jetty</artifactId>
-      <version>6.1-SNAPSHOT</version>
+      <version>__JETTY_VERSION__</version>
     </dependency>
     <dependency>
      <groupId>org.terracotta</groupId>
Index: contrib/cometd-demo/bin/runTerracottaNode.sh
===================================================================
--- contrib/cometd-demo/bin/runTerracottaNode.sh	(revision 142)
+++ contrib/cometd-demo/bin/runTerracottaNode.sh	(working copy)
@@ -8,7 +8,7 @@
 cd $(dirname $0)/..
 DEMO_HOME=$(pwd)
 JETTY_HOME=../..
-JETTY_VERSION=6.1-SNAPSHOT
+JETTY_VERSION=__JETTY_VERSION__
 
 TC=/java/terracotta-2.2.0-dso
 # TC_JAVA_HOME=$(dirname $(dirname $(which java)))
Index: contrib/cometd-demo/pom.xml
===================================================================
--- contrib/cometd-demo/pom.xml	(revision 142)
+++ contrib/cometd-demo/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
_EOF_


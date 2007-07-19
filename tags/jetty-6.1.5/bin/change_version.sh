#!/bin/sh

[ $# -eq 1 ] || { echo Usage - $0 new_version >&2 ; exit 1 ; }

sed s/__JETTY_VERSION__/$1/g << "_EOF_" | patch -p0
Index: pom.xml
===================================================================
--- pom.xml	(revision 2012)
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
Index: project-website/project-site/src/site/site.xml
===================================================================
--- project-website/project-site/src/site/site.xml	(revision 2012)
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
--- project-website/project-site/pom.xml	(revision 2012)
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
--- project-website/project-skin/pom.xml	(revision 2012)
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
--- project-website/pom.xml	(revision 2012)
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
Index: extras/jboss/pom.xml
===================================================================
--- extras/jboss/pom.xml	(revision 2012)
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
Index: extras/gwt/pom.xml
===================================================================
--- extras/gwt/pom.xml	(revision 2012)
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
Index: extras/threadpool/pom.xml
===================================================================
--- extras/threadpool/pom.xml	(revision 2012)
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
Index: extras/spring/pom.xml
===================================================================
--- extras/spring/pom.xml	(revision 2012)
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
--- extras/tests/pom.xml	(revision 2012)
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
--- extras/win32service/pom.xml	(revision 2012)
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
Index: extras/sessioncache/pom.xml
===================================================================
--- extras/sessioncache/pom.xml	(revision 2012)
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
Index: extras/sslengine/pom.xml
===================================================================
--- extras/sslengine/pom.xml	(revision 2012)
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
Index: extras/servlet-tester/pom.xml
===================================================================
--- extras/servlet-tester/pom.xml	(revision 2012)
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
--- extras/ajp/pom.xml	(revision 2012)
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
--- extras/grizzly/pom.xml	(revision 2012)
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
--- extras/setuid/pom.xml	(revision 2012)
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
Index: extras/xbean/pom.xml
===================================================================
--- extras/xbean/pom.xml	(revision 2012)
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
Index: modules/plus/pom.xml
===================================================================
--- modules/plus/pom.xml	(revision 2012)
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
--- modules/jsp-2.0/pom.xml	(revision 2012)
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
--- modules/jsp-2.1/pom.xml	(revision 2012)
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
Index: modules/annotations/pom.xml
===================================================================
--- modules/annotations/pom.xml	(revision 2012)
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
Index: modules/servlet-api-2.5/pom.xml
===================================================================
--- modules/servlet-api-2.5/pom.xml	(revision 2012)
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
--- modules/start/pom.xml	(revision 2012)
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
Index: modules/jetty/pom.xml
===================================================================
--- modules/jetty/pom.xml	(revision 2012)
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
Index: modules/jspc-maven-plugin/pom.xml
===================================================================
--- modules/jspc-maven-plugin/pom.xml	(revision 2012)
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
Index: modules/jsp-api-2.0/pom.xml
===================================================================
--- modules/jsp-api-2.0/pom.xml	(revision 2012)
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
--- modules/jsp-api-2.1/pom.xml	(revision 2012)
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
Index: modules/maven-plugin/pom.xml
===================================================================
--- modules/maven-plugin/pom.xml	(revision 2012)
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
--- modules/html/pom.xml	(revision 2012)
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
--- modules/naming/pom.xml	(revision 2012)
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
--- modules/management/pom.xml	(revision 2012)
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
--- modules/util/pom.xml	(revision 2012)
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
--- examples/spring-ebj3-demo/pom.xml	(revision 2012)
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
--- examples/test-jaas-webapp/pom.xml	(revision 2012)
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
--- examples/embedded/pom.xml	(revision 2012)
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
--- examples/test-annotations/pom.xml	(revision 2012)
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
--- examples/test-webapp/pom.xml	(revision 2012)
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
--- examples/test-jndi-webapp/pom.xml	(revision 2012)
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
Index: contrib/jetty-ant/pom.xml
===================================================================
--- contrib/jetty-ant/pom.xml	(revision 361)
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
Index: contrib/jetty-ant-demo/build.xml
===================================================================
--- contrib/jetty-ant-demo/build.xml	(revision 361)
+++ contrib/jetty-ant-demo/build.xml	(working copy)
@@ -1,9 +1,9 @@
 <project name="Test application" basedir=".">
   <property name="project.outputDirectory" value="target" />
-  <property name="project.version" value="6.1-SNAPSHOT" />
+  <property name="project.version" value="__JETTY_VERSION__" />
   <property name="M2_REPO" value="${user.home}/.m2/repository/" />
 
-  <property name="jetty.version" value="6.1-SNAPSHOT" />
+  <property name="jetty.version" value="__JETTY_VERSION__" />
 
   <path id="jetty.plugin.classpath">
     <fileset dir="${M2_REPO}">
Index: contrib/wadi/pom.xml
===================================================================
--- contrib/wadi/pom.xml	(revision 361)
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
Index: contrib/jetty-deb/modules/jetty-deb-package/pom.xml
===================================================================
--- contrib/jetty-deb/modules/jetty-deb-package/pom.xml	(revision 361)
+++ contrib/jetty-deb/modules/jetty-deb-package/pom.xml	(working copy)
@@ -3,13 +3,13 @@
     <parent>
         <artifactId>project</artifactId>
         <groupId>org.mortbay.jetty</groupId>
-        <version>6.1-SNAPSHOT</version>
+        <version>__JETTY_VERSION__</version>
         <relativePath>../../pom.xml</relativePath>
     </parent>
     <modelVersion>4.0.0</modelVersion>
     <groupId>org.mortbay.jetty</groupId>
     <artifactId>jetty-deb-package</artifactId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <name>Jetty Debian Package</name>
     <packaging>pom</packaging>
     <licenses>
@@ -142,4 +142,4 @@
     <properties>
         <deb-install-root>/usr/local/jetty/jetty-${project.version}</deb-install-root>
     </properties>
-</project>
\ No newline at end of file
+</project>
Index: contrib/jetty-deb/modules/maven-deb-plugin/pom.xml
===================================================================
--- contrib/jetty-deb/modules/maven-deb-plugin/pom.xml	(revision 361)
+++ contrib/jetty-deb/modules/maven-deb-plugin/pom.xml	(working copy)
@@ -5,7 +5,7 @@
     <parent>
         <artifactId>project</artifactId>
         <groupId>org.mortbay.jetty</groupId>
-        <version>6.1-SNAPSHOT</version>
+        <version>__JETTY_VERSION__</version>
         <relativePath>../../pom.xml</relativePath>
     </parent>
 
Index: contrib/jetty-deb/pom.xml
===================================================================
--- contrib/jetty-deb/pom.xml	(revision 361)
+++ contrib/jetty-deb/pom.xml	(working copy)
@@ -3,14 +3,14 @@
     <parent>
         <artifactId>project</artifactId>
         <groupId>org.mortbay.jetty</groupId>
-        <version>6.1-SNAPSHOT</version>
+        <version>__JETTY_VERSION__</version>
         <relativePath>../../pom.xml</relativePath>
     </parent>
 
     <modelVersion>4.0.0</modelVersion>
     <groupId>org.mortbay.jetty</groupId>
     <artifactId>jetty-deb-package-parent</artifactId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <name>Jetty Deb Package-parent</name>
     <packaging>pom</packaging>
     <licenses>
Index: contrib/terracotta/pom.xml
===================================================================
--- contrib/terracotta/pom.xml	(revision 361)
+++ contrib/terracotta/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </parent>
 
   <modelVersion>4.0.0</modelVersion>
Index: contrib/j2se6/pom.xml
===================================================================
--- contrib/j2se6/pom.xml	(revision 361)
+++ contrib/j2se6/pom.xml	(working copy)
@@ -3,7 +3,7 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
   </parent>
 	<modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
Index: contrib/cometd/demo/bin/runTerracottaNode.sh
===================================================================
--- contrib/cometd/demo/bin/runTerracottaNode.sh	(revision 361)
+++ contrib/cometd/demo/bin/runTerracottaNode.sh	(working copy)
@@ -8,7 +8,7 @@
 cd $(dirname $0)/..
 DEMO_HOME=$(pwd)
 JETTY_HOME=../..
-JETTY_VERSION=6.1-SNAPSHOT
+JETTY_VERSION=__JETTY_VERSION__
 
 TC_HOME=/java/terracotta-trunk
 TC_BOOT_JAR=$TC_HOME/dso-boot-hotspot_linux_150_08.jar
Index: contrib/cometd/demo/pom.xml
===================================================================
--- contrib/cometd/demo/pom.xml	(revision 361)
+++ contrib/cometd/demo/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>cometd</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: contrib/cometd/api/pom.xml
===================================================================
--- contrib/cometd/api/pom.xml	(revision 361)
+++ contrib/cometd/api/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>cometd</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: contrib/cometd/pom.xml
===================================================================
--- contrib/cometd/pom.xml	(revision 361)
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
Index: contrib/cometd/bayeux/pom.xml
===================================================================
--- contrib/cometd/bayeux/pom.xml	(revision 361)
+++ contrib/cometd/bayeux/pom.xml	(working copy)
@@ -2,7 +2,7 @@
   <parent>
     <artifactId>cometd</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
Index: contrib/client/pom.xml
===================================================================
--- contrib/client/pom.xml	(revision 361)
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
Index: contrib/rpm/modules/jetty6-servlet-2.5-api/pom.xml
===================================================================
--- contrib/rpm/modules/jetty6-servlet-2.5-api/pom.xml	(revision 361)
+++ contrib/rpm/modules/jetty6-servlet-2.5-api/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>jetty-rpm-project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-servlet-2.5-api-rpm-package</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty Servlet 2.5 API RPM Package</name>
   <packaging>pom</packaging>
   
Index: contrib/rpm/modules/jetty6-ext/pom.xml
===================================================================
--- contrib/rpm/modules/jetty6-ext/pom.xml	(revision 361)
+++ contrib/rpm/modules/jetty6-ext/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>jetty-rpm-project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-ext-rpm-package</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty Library RPM Package</name>
   <packaging>pom</packaging>
   
Index: contrib/rpm/modules/jetty6-jsp-2.0-api/pom.xml
===================================================================
--- contrib/rpm/modules/jetty6-jsp-2.0-api/pom.xml	(revision 361)
+++ contrib/rpm/modules/jetty6-jsp-2.0-api/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>jetty-rpm-project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-jsp-2.0-api-rpm-package</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty JSP 2.0 API RPM Package</name>
   <packaging>pom</packaging>
   
Index: contrib/rpm/modules/jetty6-jsp-2.1-api/pom.xml
===================================================================
--- contrib/rpm/modules/jetty6-jsp-2.1-api/pom.xml	(revision 361)
+++ contrib/rpm/modules/jetty6-jsp-2.1-api/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>jetty-rpm-project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-jsp-2.1-api-rpm-package</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty JSP 2.1 API RPM Package</name>
   <packaging>pom</packaging>
   
Index: contrib/rpm/modules/jetty6/pom.xml
===================================================================
--- contrib/rpm/modules/jetty6/pom.xml	(revision 361)
+++ contrib/rpm/modules/jetty6/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>jetty-rpm-project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-rpm-package</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty RPM Package</name>
   <packaging>pom</packaging>
   
Index: contrib/rpm/modules/jetty6-samples/pom.xml
===================================================================
--- contrib/rpm/modules/jetty6-samples/pom.xml	(revision 361)
+++ contrib/rpm/modules/jetty6-samples/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>jetty-rpm-project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-samples-rpm-package</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty Samples RPM Package</name>
   <packaging>pom</packaging>
   
Index: contrib/rpm/modules/jetty6-lib/pom.xml
===================================================================
--- contrib/rpm/modules/jetty6-lib/pom.xml	(revision 361)
+++ contrib/rpm/modules/jetty6-lib/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>jetty-rpm-project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-lib-rpm-package</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty Library RPM Package</name>
   <packaging>pom</packaging>
   
Index: contrib/rpm/pom.xml
===================================================================
--- contrib/rpm/pom.xml	(revision 361)
+++ contrib/rpm/pom.xml	(working copy)
@@ -2,13 +2,13 @@
   <parent>
     <artifactId>project</artifactId>
     <groupId>org.mortbay.jetty</groupId>
-    <version>6.1-SNAPSHOT</version>
+    <version>__JETTY_VERSION__</version>
     <relativePath>../../pom.xml</relativePath>
   </parent>
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.mortbay.jetty</groupId>
   <artifactId>jetty-rpm-project</artifactId>
-  <version>6.1-SNAPSHOT</version>
+  <version>__JETTY_VERSION__</version>
   <name>Jetty RPM Project</name>
   <packaging>pom</packaging>
_EOF_


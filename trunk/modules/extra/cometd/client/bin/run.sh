#!/bin/sh
cd `dirname $0`/..
java -Xmx2048M -cp \
target/classes:target/test-classes/:../bayeux/target/classes/:../../../../lib/cometd/cometd-api-1.0-SNAPSHOT.jar:../../../../lib/ext/jetty-client-7.0-SNAPSHOT.jar:../../../../lib/jetty-util-7.0-SNAPSHOT.jar:../../../../lib/jetty-7.0-SNAPSHOT.jar:../../../../lib/jetty-security-7.0-SNAPSHOT.jar:../../../../lib/jetty-ssl-7.0-SNAPSHOT.jar:../../../../lib/jetty-webapp-7.0-SNAPSHOT.jar:../../../../lib/jetty-xml-7.0-SNAPSHOT.jar:../../../../lib/servlet-api-3.0.pre1.jar \
org.mortbay.cometd.client.BayeuxLoadGenerator

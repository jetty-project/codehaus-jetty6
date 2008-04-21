#!/bin/sh
java -Xmx2048M -cp target/classes:target/test-classes/:../bayeux/target/classes/:../api/target/classes/:../../../../lib/ext/jetty-client-7.0-SNAPSHOT.jar:../../../../lib/jetty-util-7.0-SNAPSHOT.jar:../../../../lib/jetty-7.0-SNAPSHOT.jar:../../../../lib/servlet-api-3.0-7.0-SNAPSHOT.jar org.mortbay.cometd.client.BayeuxLoadGenerator

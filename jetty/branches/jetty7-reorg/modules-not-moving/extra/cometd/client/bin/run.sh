#!/bin/sh
cd `dirname $0`/..
java -Xmx2048M -Djetty.home=../../../../ -DSTOP.PORT=-1 -DSTART=bin/start.config -DOPTIONS=jetty,ext,cometd -jar ../../../../start.jar
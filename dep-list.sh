#/bin/sh

mvn dependency:list | grep -E "\[INFO\]    [a-z]" | sed -e "s/\[INFO\]    //;/^none$/d;/task-segment:/d;/org.mortbay.jetty:jetty/d" | sort -u > EXTERNAL-DEPENDENCIES.txt

svn commit -m "dependency list update" EXTERNAL-DEPENDENCIES.txt

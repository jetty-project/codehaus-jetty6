#/bin/sh

echo 'checking out clean source'
svn export http://svn.codehaus.org/jetty/jetty/trunk jetty-source-export

rm -Rf jetty-source-export/codehaus-modules-trunk
rm jetty-source-export/dep-list.sh

echo 'building migration bundle'
jar cvf jetty-source-export-bundle.jar jetty-source-export

# Copyright (c) 2000-2007, JPackage Project
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the
#    distribution.
# 3. Neither the name of the JPackage Project nor the names of its
#    contributors may be used to endorse or promote products derived
#    from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

%define section     free
%define jettyname   jetty
%define servletspec 2.5
%define jspspec     2.1



# FHS 2.3 compliant tree structure - http://www.pathname.com/fhs/
%define appdir /var/lib/jetty7/webapps
%define ctxdir /var/lib/jetty7/contexts
%define confdir %{_sysconfdir}/jetty7
%define homedir %{_datadir}/jetty7
%define libdir %{_javadir}/jetty7
%define logdir %{_localstatedir}/log/jetty7
#%define tempdir %{_localstatedir}/tmp/jetty7
#%define workdir %{_localstatedir}/cache/jetty7



Name:           jetty7
Version:        @@@VERSION@@@
Release:        1jpp
Epoch:          0
Summary:        The Jetty Webserver and Servlet Container

Group:          Networking/Daemons
License:        Apache Software License
URL:            http://www.mortbay.org/
Source0:        http://dist.codehaus.org/jetty/jetty-%{version}/jetty-%{version}-src.zip
Source1:        %{name}-settings.xml
Source2:        %{name}-jpp-depmap.xml
Patch0:         %{name}.patch

BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Vendor: %{?_vendorinfo:%{_vendorinfo}}%{!?_vendorinfo:%{_vendor}}
Distribution: %{?_distribution:%{_distribution}}%{!?_distribution:%{_vendor}}

BuildArch:      noarch
BuildRequires:  java-devel >= 1.5.0
BuildRequires:  jpackage-utils >= 0:1.7.2
BuildRequires:  ant >= 0:1.6
BuildRequires:  ant-junit >= 0:1.6
BuildRequires:  junit >= 0:3.8.1
BuildRequires:  maven2 >= 2.0.4-10jpp
BuildRequires:  maven2-plugin-compiler
BuildRequires:  maven2-plugin-install
BuildRequires:  maven2-plugin-jar
BuildRequires:  maven2-plugin-javadoc
BuildRequires:  maven2-plugin-resources
BuildRequires:  maven2-plugin-surefire
BuildRequires:  maven2-plugin-antrun
BuildRequires:  maven2-plugin-war

Requires(post):    jpackage-utils >= 0:1.7.2
Requires(postun):  jpackage-utils >= 0:1.7.2
Requires: jetty7-core
Provides: jetty7

%description
Jetty is a 100% Java HTTP Server and Servlet Container.


%package -n %{jettyname}6-servlet-%{servletspec}-api
Summary:        Servlet 2.5 API from %{name}
Group:          Networking/Daemons
Requires: java >= 0:1.4.2
Provides: servlet = 0:2.5
Provides: servlet25

%description -n %{jettyname}6-servlet-%{servletspec}-api
%{summary}


%package -n %{jettyname}6-jsp-%{jspspec}
Summary:        JSP 2.1 API from %{name}
Group:          Networking/Daemons
Provides: jsp = 0:2.1

%description -n %{jettyname}6-jsp-%{jspspec}
%{summary}


%package -n %{jettyname}6-core
Summary:        Core libraries for %{name}
Group:          Networking/Daemons
Provides: jetty7-core
Requires: servlet25
Requires: java >= 0:1.4.2

%description -n %{jettyname}6-core
%{summary}


%package -n %{jettyname}6-plus
Summary:        Optional libraries for %{name}
Group:          Networking/Daemons
Provides: jetty7-plus
Requires: jetty7-core
Requires: java >= 0:1.5.0
Requires: servlet25

%description -n %{jettyname}6-plus
%{summary}. Currently not complete while waiting for more dependencies to be added to jpackage.

%package javadoc
Summary:        Javadoc for %{name}
Group:          Development/Documentation
Requires(post):   /bin/rm,/bin/ln
Requires(postun): /bin/rm

%description javadoc
Javadoc for %{name}

%package -n %{jettyname}6-demos
Summary:        Optional demos for %{name}
Group:          Networking/Daemons
Provides: jetty7-demos
Requires: jetty7

%description -n %{jettyname}6-demos
%{summary}. 

%prep
%setup -q -n %{jettyname}-%{version}
cp %{SOURCE1} settings.xml
%patch0 -b .sav

cp ${RPM_SOURCE_DIR}/start.config ${RPM_BUILD_DIR}/%{jettyname}-%{version}/etc/start.config
cp ${RPM_SOURCE_DIR}/jetty.conf ${RPM_BUILD_DIR}/%{jettyname}-%{version}/etc/jetty.conf

%build
export JAVA_HOME=%{_jvmdir}/java-1.5.0
export OPT_JAR_LIST="ant/ant-trax"
cd ${RPM_BUILD_DIR}/jetty-%{version}

sed -i -e "s|<url>__JPP_URL_PLACEHOLDER__</url>|<url>file://`pwd`/.m2/repository</url>|g" settings.xml
sed -i -e "s|<url>__JAVADIR_PLACEHOLDER__</url>|<url>file://`pwd`/external_repo</url>|g" settings.xml
sed -i -e "s|<url>__MAVENREPO_DIR_PLACEHOLDER__</url>|<url>file://`pwd`/.m2/repository</url>|g" settings.xml
sed -i -e "s|<url>__MAVENDIR_PLUGIN_PLACEHOLDER__</url>|<url>file:///usr/share/maven2/plugins</url>|g" settings.xml
sed -i -e "s|<url>__ECLIPSEDIR_PLUGIN_PLACEHOLDER__</url>|<url>file:///usr/share/eclipse/plugins</url>|g" settings.xml

export MAVEN_REPO_LOCAL=$(pwd)/.m2/repository
mkdir -p $MAVEN_REPO_LOCAL

mkdir external_repo
ln -s %{_javadir} external_repo/JPP

mvn-jpp \
        -e \
        -s $(pwd)/settings.xml \
        -Dmaven2.jpp.mode=true \
        -Dmaven2.jpp.depmap.file=%{SOURCE2} \
        -Dmaven.repo.local=$MAVEN_REPO_LOCAL \
        install javadoc:javadoc

%install
rm -rf $RPM_BUILD_ROOT




# ********************* INSTALL Files To Fake RPM File Structure **************************

install -d -m 755 $RPM_BUILD_ROOT%{_javadir}/jetty7



# ================= Start of Servlet subpackage install

# install jar files
install -m 644 lib/servlet-api-%{servletspec}-7.*.jar \
        ${RPM_BUILD_ROOT}%{_javadir}/%{jettyname}6-servlet-%{servletspec}-api-%{version}.jar

# create symbolic links
pushd ${RPM_BUILD_ROOT}%{_javadir}
        ln -sf %{jettyname}6-servlet-%{servletspec}-api-%{version}.jar \
            %{jettyname}6-servlet-%{servletspec}-api.jar
popd

# ================= End of Servlet subpackage install



# ================= Start of JSP 2.1 subpackage install
# create folder
install -d -m 755 ${RPM_BUILD_ROOT}%{_javadir}/jetty7-jsp-2.1

# install jar files
install -pm 644 lib/jsp-2.1/jsp-2.1.jar ${RPM_BUILD_ROOT}%{_javadir}/jetty7-jsp-2.1/jsp-2.1-%{version}.jar
install -pm 644 lib/jsp-2.1/jsp-api-2.1.jar ${RPM_BUILD_ROOT}%{_javadir}/jetty7-jsp-2.1/jsp-api-2.1-%{version}.jar
pushd ${RPM_BUILD_ROOT}%{_javadir}/jetty7-jsp-2.1
    ln -sf jsp-2.1-%{version}.jar jsp-2.1.jar
    ln -sf jsp-api-2.1-%{version}.jar jsp-api-2.1.jar
popd
# ================= End of JSP 2.1 subpackage install


# ================= Start of Jetty-Core subpackage install
install -d -m 755 ${RPM_BUILD_ROOT}%{_javadir}/jetty7-core
install -pm 644 lib/jetty-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-core/jetty7-%{version}.jar
install -pm 644 lib/jetty-util-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-core/jetty7-util-%{version}.jar
install -pm 644 lib/ext/jetty-ajp-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-core/jetty7-ajp-%{version}.jar
install -pm 644 lib/ext/jetty-sslengine-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-core/jetty7-sslengine-%{version}.jar
pushd ${RPM_BUILD_ROOT}%{_javadir}/jetty7-core
    ln -sf jetty7-%{version}.jar jetty7.jar
    ln -sf jetty7-util-%{version}.jar jetty7-util.jar
popd
# ================= End of Jetty-Core subpackage install

# ================= Start of Jetty-Plus subpackage install
install -d -m 755 ${RPM_BUILD_ROOT}%{_javadir}/jetty7-plus
install -pm 644 lib/ext/jetty-html-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-plus/jetty7-html-%{version}.jar
install -pm 644 lib/jndi/jetty-jndi-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-plus/jetty7-jndi-%{version}.jar
#install -pm 644 lib/jmx/jetty-jmx-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-plus/jetty7-jmx-%{version}.jar
#install -pm 644 lib/plus/jetty-plus-7.*.jar $RPM_BUILD_ROOT%{_javadir}/jetty7-plus/jetty7-plus-%{version}.jar
pushd ${RPM_BUILD_ROOT}%{_javadir}/jetty7-plus
    ln -sf jetty7-html-%{version}.jar jetty7-html.jar
popd
# ================= End of Jetty-Plus subpackage install



# build initial path structure
%{__install} -d -m 0755 ${RPM_BUILD_ROOT}%{homedir}
%{__install} -d -m 0755 ${RPM_BUILD_ROOT}%{appdir}
%{__install} -d -m 0755 ${RPM_BUILD_ROOT}%{ctxdir}
%{__install} -d -m 0755 ${RPM_BUILD_ROOT}%{confdir}
%{__install} -d -m 0755 ${RPM_BUILD_ROOT}%{logdir}
%{__install} -d -m 0755 ${RPM_BUILD_ROOT}/etc/init.d

install -pm 644 contexts/README.TXT ${RPM_BUILD_ROOT}%{ctxdir}/README.TXT
install -pm 644 webapps/README.TXT ${RPM_BUILD_ROOT}%{appdir}/README.TXT

install -pm 644 start.jar $RPM_BUILD_ROOT%{_javadir}/jetty7/start-%{version}.jar

# install etc files <jetty-root>/etc
install -pm 644 etc/start.config ${RPM_BUILD_ROOT}%{confdir}/start.config
install -pm 644 etc/jetty.conf ${RPM_BUILD_ROOT}%{confdir}/jetty.conf
install -pm 644 etc/jetty.xml ${RPM_BUILD_ROOT}%{confdir}/jetty.xml
install -pm 644 etc/jetty-bio.xml ${RPM_BUILD_ROOT}%{confdir}/jetty-bio.xml
install -pm 644 etc/jetty-jmx.xml ${RPM_BUILD_ROOT}%{confdir}/jetty-jmx.xml
install -pm 644 etc/jetty-plus.xml ${RPM_BUILD_ROOT}%{confdir}/jetty-plus.xml
install -pm 644 etc/jetty-ssl.xml ${RPM_BUILD_ROOT}%{confdir}/jetty-ssl.xml
install -pm 644 etc/jetty-logging.xml ${RPM_BUILD_ROOT}%{confdir}/jetty-logging.xml
install -pm 644 etc/webdefault.xml ${RPM_BUILD_ROOT}%{confdir}/webdefault.xml
install -pm 644 etc/realm.properties ${RPM_BUILD_ROOT}%{confdir}/realm.properties
install -pm 644 etc/jdbcRealm.properties ${RPM_BUILD_ROOT}%{confdir}/jdbcRealm.properties
install -pm 644 etc/keystore ${RPM_BUILD_ROOT}%{confdir}/keystore

# install bin files <jetty-root>/bin
install -pm 644 bin/jetty.sh ${RPM_BUILD_ROOT}/etc/init.d/jetty7

# install test webapps 
install -pm 644 contexts/test.xml ${RPM_BUILD_ROOT}%{ctxdir}/test.xml 
%{__install} -d -m 0755 ${RPM_BUILD_ROOT}%{ctxdir}/test.d
install -pm 644 contexts/test.d/override-web.xml ${RPM_BUILD_ROOT}%{ctxdir}/test.d/override-web.xml
cp -R webapps/test ${RPM_BUILD_ROOT}%{appdir}
install -pm 644 contrib/cometd/demo/target/cometd-demo-7.*.war ${RPM_BUILD_ROOT}%{appdir}/cometd.war


# FHS Symlink for Jetty Home
pushd ${RPM_BUILD_ROOT}%{homedir}
    %{__ln_s} %{appdir} webapps
    %{__ln_s} %{ctxdir} contexts
    %{__ln_s} %{confdir} etc
    %{__ln_s} %{libdir} lib
    %{__ln_s} %{logdir} logs
popd

(cd $RPM_BUILD_ROOT%{_javadir}/jetty7 && for jar in *-%{version}.jar; do ln -sf ${jar} `echo $jar| sed "s|-%{version}||g"`; done)
(cd $RPM_BUILD_ROOT%{_javadir} && for jar in jetty7*-%{version}.jar; do ln -sf ${jar} `echo $jar| sed "s|-%{version}||g"`; done)

echo maven depmap
%add_to_maven_depmap org.mortbay.jetty project %{version} JPP/jetty7 project
%add_to_maven_depmap org.mortbay.jetty jetty %{version} JPP/jetty7 jetty7
%add_to_maven_depmap org.mortbay.jetty jetty-html %{version} JPP/jetty7 jetty7-html
%add_to_maven_depmap org.mortbay.jetty jetty-util %{version} JPP/jetty7 jetty7-util
%add_to_maven_depmap org.mortbay.jetty jetty-ajp %{version} JPP/jetty7 jetty7-ajp
%add_to_maven_depmap org.mortbay.jetty jetty-sslengine %{version} JPP/jetty7 jetty7-sslengine
%add_to_maven_depmap org.mortbay.jetty jetty-jndi %{version} JPP/jetty7 jetty7-jndi
#%add_to_maven_depmap org.mortbay.jetty jetty-jmx %{version} JPP/jetty7 jetty7-jmx
#%add_to_maven_depmap org.mortbay.jetty jetty-plus %{version} JPP/jetty7 jetty7-plus
%add_to_maven_depmap org.mortbay.jetty servlet-api-2.5 %{version} JPP jetty7-servlet-2.5-api
%add_to_maven_depmap org.mortbay.jetty jsp-2.1 %{version} JPP/jetty7 jsp-2.1


echo poms
install -d -m 755 $RPM_BUILD_ROOT%{_datadir}/maven2/poms
install -pm 644 pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-project.pom
install -pm 644 modules/jetty/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7.pom
install -pm 644 modules/html/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7-html.pom
install -pm 644 modules/util/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7-util.pom
install -pm 644 extras/ajp/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7-ajp.pom
install -pm 644 extras/ajp/sslengine.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7-sslengine.pom
install -pm 644 modules/jndi/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7-jndi.pom
#install -pm 644 modules/jmx/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7-jmx.pom
#install -pm 644 modules/plus/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jetty7-plus.pom
install -pm 644 modules/servlet-api-2.5/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-servlet-2.5-api.pom
install -pm 644 modules/jsp-api-2.1/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jsp-2.1-api.pom
install -pm 644 modules/jsp-2.1/pom.xml $RPM_BUILD_ROOT%{_datadir}/maven2/poms/JPP.jetty7-jsp-2.1.pom

echo javadoc
install -dm 755 $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}
cp -pr target/site/apidocs/* $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}
ln -s %{name}-%{version} $RPM_BUILD_ROOT%{_javadocdir}/%{name} # ghost symlink

touch $RPM_BUILD_ROOT%{_javadir}/servlet.jar # for %ghost


# ********************* CLEAN SECTION **************************
%clean
rm -rf $RPM_BUILD_ROOT



# ********************* POST-INSTALL AND POST-UNISTALL SECTION **************************


# ========= Start of Jetty package Post-Install and Post-Uninstall    
# Post-Install
%post
%update_maven_depmap

# Post-Uninstall
%postun
%update_maven_depmap

# ========= End of Jetty package Post-Install and Post-Uninstall    


# ========= Start of JAVADOC Subpackage Post-Install and Post-Uninstall    
# Post-Install
%post javadoc
rm -f %{_javadocdir}/%{name}
ln -s %{name}-%{version} %{_javadocdir}/%{name}

# Post-Uninstall
%postun javadoc
if [ "$1" = "0" ]; then
  rm -f %{_javadocdir}/%{name}
fi
# ========= End of JAVADOC Subpackage Post-Install and Post-Uninstall    



# ========= Start of Servlet 2.5 Subpackage Post-Install and Post-Uninstall    
# Post-Install
%post -n %{jettyname}6-servlet-%{servletspec}-api
update-alternatives --install %{_javadir}/servlet.jar servlet \
    %{_javadir}/%{jettyname}6-servlet-%{servletspec}-api.jar 20600

# Post-Uninstall    
%postun -n %{jettyname}6-servlet-%{servletspec}-api
if [ "$1" = "0" ]; then
    update-alternatives --remove servlet \
        %{_javadir}/%{jettyname}6-servlet-%{servletspec}-api.jar
fi
# ========= End of Servlet 2.5 Subpackage Post-Install and Post-Uninstall    


# ========= Start of JSP 2.1 Subpackage Post-Install and Post-Uninstall

# Post-Install
%post -n %{jettyname}6-jsp-%{jspspec}
update-alternatives --install %{_javadir}/jsp.jar jsp \
    %{_javadir}/jetty7-jsp-2.1-api/jsp-api-2.1.jar 20001

# Post-Uninstall
%postun -n %{jettyname}6-jsp-%{jspspec}
if [ "$1" = "0" ]; then
    update-alternatives --remove jsp \
        %{_javadir}/jetty7-jsp-2.1-api/jsp-api-2.1.jar
fi

# ========= End of JSP 2.1 Subpackage Post-Install and Post-Uninstall



# ========= Start of jetty core Subpackage Post-Install and Post-Uninstall

# Post-Install
%post -n %{jettyname}6-core

# Post-Uninstall
%postun -n %{jettyname}6-core

# ========= End of jetty core Subpackage Post-Install and Post-Uninstall



# ========= Start of jetty plus Subpackage Post-Install and Post-Uninstall

# Post-Install
%post -n %{jettyname}6-plus

# Post-Uninstall
%postun -n %{jettyname}6-plus

# ========= End of jetty plus Subpackage Post-Install and Post-Uninstall




# ********************* Files SECTION **************************



# ========= Start of Jetty package Files
%files
%defattr(-,root,root,-)
%{appdir}
%{ctxdir}
%{_javadir}/jetty7/*.jar
%attr(755,root,root) /etc/init.d/jetty7

%dir %{confdir}
%config(noreplace) %{confdir}/*.config
%config(noreplace) %{confdir}/*.conf
%config(noreplace) %{confdir}/*.xml
%config(noreplace) %{confdir}/*.properties
%config(noreplace) %{confdir}/keystore

%{homedir}
%{_mavendepmapfragdir}
%doc *.txt
%doc LICENSES/LICENSE.txt
%doc LICENSES/NOTICE.txt
%doc webapps/README.TXT
%doc contexts/README.TXT
%attr(0775,root,root) %dir %{logdir}

# ========= End of Jetty package Files

# ========= Start of Servlet Subpackage Files
%files -n %{jettyname}6-servlet-%{servletspec}-api
%defattr(-,root,root,-)
%{_javadir}/%{jettyname}6-servlet-%{servletspec}-api*.jar
%ghost %{_javadir}/servlet.jar
%{_datadir}/maven2/poms/JPP.jetty7-servlet-2.5-api.pom
%doc LICENSES/LICENSE.txt
%doc LICENSES/NOTICE.txt
# ========= End of Servlet Subpackage Files


# ========= Start of JSP 2.1 Subpackage Files
%files -n %{jettyname}6-jsp-%{jspspec}
%defattr(-,root,root,-)
%{_javadir}/jetty7-jsp-2.1/*.jar
%{_datadir}/maven2/poms/JPP.jetty7-jsp-2.1-api.pom
%{_datadir}/maven2/poms/JPP.jetty7-jsp-2.1.pom
%doc LICENSES/LICENSE.txt
%doc LICENSES/NOTICE.txt
# ========= End of JSP 2.1 Subpackage Files

# ========= Start of Jetty core Subpackage Files
%files -n %{jettyname}6-core
%defattr(-,root,root,-)
%{_javadir}/jetty7-core/*.jar
%{_datadir}/maven2/poms/JPP.jetty7-project.pom
%{_datadir}/maven2/poms/JPP.jetty7-jetty7-util.pom
%{_datadir}/maven2/poms/JPP.jetty7-jetty7-ajp.pom
%{_datadir}/maven2/poms/JPP.jetty7-jetty7-sslengine.pom
%{_datadir}/maven2/poms/JPP.jetty7-jetty7.pom
%doc *.txt
%doc LICENSES/LICENSE.txt
%doc LICENSES/NOTICE.txt
# ========= End of Jetty core  Subpackage Files

# ========= Start of Jetty plus Subpackage Files
%files -n %{jettyname}6-plus
%defattr(-,root,root,-)
%{_javadir}/jetty7-plus/*.jar
%{_datadir}/maven2/poms/JPP.jetty7-jetty7-html.pom
%{_datadir}/maven2/poms/JPP.jetty7-jetty7-jndi.pom
%doc *.txt
%doc LICENSES/LICENSE.txt
%doc LICENSES/NOTICE.txt
# ========= End of Jetty plus  Subpackage Files


# ========= Start of Jetty Javadoc Subpackage Files
%files javadoc
%defattr(-,root,root,-)
%doc %{_javadocdir}/%{name}-%{version}
%ghost %doc %{_javadocdir}/%{name}
%doc *.txt
%doc LICENSES/LICENSE.txt
%doc LICENSES/NOTICE.txt
# ========= End of Jetty Javadoc Subpackage Files

# ========= Start of Jetty demos Subpackage Files
%files -n %{jettyname}6-demos
%defattr(-,root,root,-)
%{ctxdir}/test.xml
%{ctxdir}/test.d/override-web.xml
%{appdir}/cometd.war
%dir %{appdir}/test

# ========= End of Jetty demos Subpackage Files

%changelog
* Fri Jul 20 2007 Ralph Apel <r.apel at r-apel.de> - 0:%{version}-1jpp
- First release


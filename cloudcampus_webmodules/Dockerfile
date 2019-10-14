FROM tomcat:8-jre8
MAINTAINER Qiming Mei <meiqiming@talkweb.com.cn>

#针对tomcat8 修改编码与支持软连接文件读取
RUN perl -pi -e 's|port="8080"|port="8080" URIEncoding="UTF-8"|g' /usr/local/tomcat/conf/server.xml \
&& perl -pi -e 's|<Context>|<Context>\n  <Resources allowLinking="true" />|g' /usr/local/tomcat/conf/context.xml \
&& rm -rf /usr/local/tomcat/webapps/*  \
&& mkdir -p /nfsdisk200/zhx/tmp \
&& mkdir -p /nfsdisk200/zhx/tmp/scoreReportFiles \
&& mkdir -p /nfsdisk200/zhx/tmp/bakftp \
&& mkdir -p /nfsdisk200/zhx/tmp/ftp \
&& mkdir -p /nfsdisk200/zhx/tmp/tmpdata

 

# 项目编译后的名称
ARG JAVA_WAR
# 项目部署在tomcat的名称
ARG TOMCAT_PROJECT_NAME
COPY  target/${JAVA_WAR}   /usr/local/tomcat/webapps/${TOMCAT_PROJECT_NAME}


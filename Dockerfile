FROM jboss/wildfly:14.0.1.Final

ADD --chown=jboss . /tmp/kunta-api
ADD --chown=jboss ./docker /opt/docker
ADD --chown=jboss ./src/main/resources/fi/metatavu/kuntaapi/server/liquibase /opt/docker/liquibase/fi/metatavu/kuntaapi/server/liquibase
RUN chmod a+x /opt/docker/entrypoint.sh
RUN tar -xvf /opt/docker/m2.tar -C /opt/jboss/

ARG MAVEN_VERSION=3.6.0
ARG LIQUIBASE_VERSION=3.5.5
ARG MARIADB_MODULE_VERSION=2.3.0

RUN mkdir /tmp/maven && curl -o /tmp/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz -L http://www.nic.funet.fi/pub/mirrors/apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
RUN mkdir /tmp/mariadb && curl -o /tmp/mariadb/mariadb-module-${MARIADB_MODULE_VERSION}.zip -L https://s3.eu-central-1.amazonaws.com/static.metatavu.io/wildfly/mariadb-module-${MARIADB_MODULE_VERSION}.zip
RUN mkdir /tmp/liquibase && curl -o /tmp/liquibase/liquibase.tar -L https://github.com/liquibase/liquibase/releases/download/liquibase-parent-${LIQUIBASE_VERSION}/liquibase-${LIQUIBASE_VERSION}-bin.tar.gz

RUN unzip -o /tmp/mariadb/mariadb-module-${MARIADB_MODULE_VERSION}.zip -d /opt/jboss/wildfly/
RUN tar -xvf /tmp/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /tmp/maven
RUN tar -xvf /tmp/liquibase/liquibase.tar -C /tmp/liquibase

USER root
ENV LANG=fi_FI.UTF-8
RUN localedef -v -c -i fi_FI -f UTF-8 fi_FI.UTF-8|| true
RUN mv /tmp/liquibase /opt/liquibase
RUN yum install -y epel-release
RUN yum install -y python34 python34-requests
USER jboss

WORKDIR /tmp/kunta-api
ENV MAVEN_OPTS=-Dfile.encoding=UTF-8 
RUN /tmp/maven/apache-maven-${MAVEN_VERSION}/bin/mvn package -DskipTests
RUN mv target/*.war /opt/jboss/wildfly/standalone/deployments/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/docker/kunta-api.cli
CMD "/opt/docker/entrypoint.sh"

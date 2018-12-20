FROM jboss/wildfly:14.0.1.Final
ADD --chown=jboss . /tmp/kunta-api
RUN mkdir /tmp/maven && curl -o /tmp/maven/apache-maven-3.6.0-bin.tar.gz -L http://www.nic.funet.fi/pub/mirrors/apache.org/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz
RUN mkdir /tmp/mariadb && curl -o /tmp/mariadb/mariadb-module-2.3.0.zip -L https://s3.eu-central-1.amazonaws.com/static.metatavu.io/wildfly/mariadb-module-2.3.0.zip
RUN unzip -o /tmp/mariadb/mariadb-module-2.3.0.zip -d /opt/jboss/wildfly/
WORKDIR /tmp/maven
RUN tar -xvf apache-maven-3.6.0-bin.tar.gz
WORKDIR /tmp/kunta-api
RUN /tmp/maven/apache-maven-3.6.0/bin/mvn package -DskipTests
RUN mv target/*.war /opt/jboss/wildfly/standalone/deployments/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/kunta-api/kunta-api.cli
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "--server-config=standalone-full.xml"]

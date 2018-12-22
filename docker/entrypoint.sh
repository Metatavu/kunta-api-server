#!/bin/sh

echo "Wait for Elastic Search..." &&
sleep 10
curl http://elastic:9300
cd /opt/docker/liquibase &&
echo "Applying database migrations..." &&
/opt/liquibase/liquibase --changeLogFile=fi/metatavu/kuntaapi/server/liquibase/changelog.xml --url=jdbc:mariadb://db:3306/kuntaapi --driver=org.mariadb.jdbc.Driver --classpath=/opt/jboss/wildfly/modules/system/layers/base/com/mariadb/jdbc/main/mariadb-java-client-2.3.0.jar  --username=kuntaapi --password=random update &&
echo "Applying settings..." &&
/opt/liquibase/liquibase --changeLogFile=changelog.xml --url=jdbc:mariadb://db:3306/kuntaapi --driver=org.mariadb.jdbc.Driver --classpath=/opt/jboss/wildfly/modules/system/layers/base/com/mariadb/jdbc/main/mariadb-java-client-2.3.0.jar  --username=kuntaapi --password=random update &&
echo "Starting server..." &&
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 --server-config=standalone-full.xml
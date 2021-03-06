#!/bin/sh

echo "Wait for Elastic Search..."

until $(curl -sSf -XGET --insecure 'http://elastic:9200/_cluster/health?wait_for_status=yellow' --silent --output /dev/null); do
  printf '.'
  sleep 1
done

echo "Elastic search ready."
cd /opt/docker/liquibase &&
echo "Applying database migrations..." &&
/opt/liquibase/liquibase --contexts=production --changeLogFile=fi/metatavu/kuntaapi/server/liquibase/changelog.xml --url=jdbc:mariadb://${KUNTA_API_DB_HOST}:3306/${KUNTA_API_DB_NAME} --driver=org.mariadb.jdbc.Driver --classpath=/opt/jboss/wildfly/modules/system/layers/base/com/mariadb/jdbc/main/mariadb-java-client-2.3.0.jar --username=${KUNTA_API_DB_USER} --password=${KUNTA_API_DB_PASSWORD} update &&
echo "Applying settings..." &&
/opt/liquibase/liquibase --contexts=production --changeLogFile=changelog.xml --url=jdbc:mariadb://${KUNTA_API_DB_HOST}:3306/${KUNTA_API_DB_NAME} --driver=org.mariadb.jdbc.Driver --classpath=/opt/jboss/wildfly/modules/system/layers/base/com/mariadb/jdbc/main/mariadb-java-client-2.3.0.jar --username=${KUNTA_API_DB_USER} --password=${KUNTA_API_DB_PASSWORD} update &&
echo "Starting server..." &&
rm -fR /opt/jboss/wildfly/standalone/configuration/standalone_xml_history &&
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 --server-config=standalone-full.xml
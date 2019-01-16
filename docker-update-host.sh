#!/bin/sh

HOST=${1:-dev.kunta-api.fi}
(grep -v $HOST /etc/hosts; echo $(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q --filter=name=kunta-api-server_kuntaapi_ | head -n 1)) $HOST) > /tmp/hosts.tmp && cp /tmp/hosts.tmp /etc/hosts
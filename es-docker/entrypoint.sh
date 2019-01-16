#!/bin/sh

if [ ! -d "/usr/share/elasticsearch/plugins/ingest-attachment" ]; then
  /usr/share/elasticsearch/bin/elasticsearch-plugin install ingest-attachment -b 
fi

if [ ! -d "/usr/share/elasticsearch/plugins/mapper-attachments" ]; then
  /usr/share/elasticsearch/bin/elasticsearch-plugin install mapper-attachments -b 
fi

/usr/share/elasticsearch/bin/es-docker 
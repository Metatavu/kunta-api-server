#!/bin/sh

/usr/share/elasticsearch/bin/elasticsearch-plugin install ingest-attachment -b 
/usr/share/elasticsearch/bin/elasticsearch-plugin install mapper-attachments -b 
/usr/share/elasticsearch/bin/es-docker 
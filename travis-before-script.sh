#!/bin/bash

if [ ! -f sonar-scanner/bin/sonar-scanner ]; then
  rm -fR sonar-scanner
  wget "https://sonarsource.bintray.com/Distribution/sonar-scanner-cli/sonar-scanner-2.8.zip"
  unzip sonar-scanner-2.8.zip
  mv sonar-scanner-2.8 sonar-scanner
fi;

if [ ! -f elasticsearch-5.0.0 ]; then
  curl -sS https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.0.0.tar.gz|tar -xz
fi;

elasticsearch-5.0.0/bin/elasticsearch -V
elasticsearch-5.0.0/bin/elasticsearch -E cluster.name=elasticsearch -E path.data=/tmp/ -d -v
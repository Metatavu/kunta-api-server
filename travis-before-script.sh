#!/bin/bash

if [ ! -f sonar-scanner/bin/sonar-scanner ]; then
  rm -fR sonar-scanner
  wget "https://sonarsource.bintray.com/Distribution/sonar-scanner-cli/sonar-scanner-2.8.zip"
  unzip sonar-scanner-2.8.zip
  mv sonar-scanner-2.8 sonar-scanner
fi;
#!/bin/bash

if [ ! -f sonar-scanner/bin/sonar-scanner ]; then
  rm -fR sonar-scanner
  wget "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-3.2.0.1227-linux.zip"
  unzip sonar-scanner-cli-3.2.0.1227-linux.zip
  mv sonar-scanner-3.2.0.1227-linux sonar-scanner
fi;

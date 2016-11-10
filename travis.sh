#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN}" ] && [ -n "${SONAR_TOKEN}" ]; then
  echo "Pull request"
  
  PROJECT_VERSION=`mvn -f pom.xml -q -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec`
  
  sh sonar-scanner/bin/sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL \
    -Dsonar.analysis.mode=issues \
    -Dsonar.login=$SONAR_TOKEN \
    -Dsonar.projectKey=$SONAR_PROJECT_KEY \
    -Dsonar.projectName=Kunta\ API\ Server \
    -Dsonar.projectVersion=$PROJECT_VERSION \
    -Dsonar.sources=src \
    -Dsonar.java.source=1.8 \
    -Dsonar.github.oauth=$GITHUB_TOKEN \
    -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
    -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST
    
  sleep 30 && cat elasticsearch-5.0.0/logs/elasticsearch.log &
  sleep 35 && echo "Elastic search port status:" && curl 127.0.0.1:9200 &
  sleep 40 && echo "Elastic search http status:" && curl 127.0.0.1:9300 &
  
  sleep 350 && cat elasticsearch-5.0.0/logs/elasticsearch.log &
  sleep 360 && cat target/cargo/configurations/wildfly10x/log/server.log &
    
  set -e
  mvn clean verify jacoco:report coveralls:report -Pitests -DrepoToken=$COVERALLS_TOKEN
  set +e
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ $TRAVIS_BRANCH == "develop" ]; then

  echo "Develop build"
  
  PROJECT_VERSION=`mvn -f pom.xml -q -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec`
  
  sh sonar-scanner/bin/sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL \
    -Dsonar.analysis.mode=publish \
    -Dsonar.login=$SONAR_TOKEN \
    -Dsonar.projectKey=$SONAR_PROJECT_KEY \
    -Dsonar.projectName=Kunta\ API\ Server \
    -Dsonar.projectVersion=$PROJECT_VERSION \
    -Dsonar.sources=src \
    -Dsonar.java.source=1.8
  set -e
  mvn clean verify jacoco:report coveralls:report -Pitests -DrepoToken=$COVERALLS_TOKEN
  set +e
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ $TRAVIS_BRANCH == "master" ]; then
  echo "Master build"
else
  echo "Push to branch" 	  
fi

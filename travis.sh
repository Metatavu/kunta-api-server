#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ $TRAVIS_BRANCH != "master" ] && [ -n "${GITHUB_TOKEN}" ] && [ -n "${SONAR_TOKEN}" ]; then
  echo "Pull request"
  mvn clean verify jacoco:report coveralls:report -Pitests -DrepoToken=$COVERALLS_TOKEN|grep -v Downloading from|grep -v Downloaded from
  TEST_STATUS=$?
  
  if [ "$TEST_STATUS" != "0" ]; then
    pip install --user awscli
    export PATH=$PATH:$HOME/.local/bin
    export S3_PATH=s3://$AWS_BUCKET/$TRAVIS_REPO_SLUG/$TRAVIS_BUILD_NUMBER
    aws s3 cp target/cargo/configurations/wildfly14x/log $S3_PATH --recursive
  else
    PROJECT_VERSION=`cat pom.xml|grep version -m 1|sed -e 's/.*<version>//'|sed -e 's/<.*//'`
    
    sh sonar-scanner/bin/sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL \
      -Dsonar.analysis.mode=issues \
      -Dsonar.login=$SONAR_TOKEN \
      -Dsonar.projectKey=$SONAR_PROJECT_KEY \
      -Dsonar.projectName=Kunta\ API\ Server \
      -Dsonar.projectVersion=$PROJECT_VERSION \
      -Dsonar.sources=src \
      -Dsonar.java.binaries=target/classes \
      -Dsonar.java.source=1.8 \
      -Dsonar.github.oauth=$GITHUB_TOKEN \
      -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
      -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST  
  fi
  
  exit $TEST_STATUS
  
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ $TRAVIS_BRANCH == "develop" ]; then

  echo "Develop build"

  mvn clean verify jacoco:report coveralls:report -Pitests -DrepoToken=$COVERALLS_TOKEN
  TEST_STATUS=$?
  
  if [ "$TEST_STATUS" != "0" ]; then
    echo "Build failed"
  else
    PROJECT_VERSION=`mvn -f pom.xml -q -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec`
    
    sh sonar-scanner/bin/sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL \
      -Dsonar.analysis.mode=publish \
      -Dsonar.login=$SONAR_TOKEN \
      -Dsonar.projectKey=$SONAR_PROJECT_KEY \
      -Dsonar.projectName=Kunta\ API\ Server \
      -Dsonar.projectVersion=$PROJECT_VERSION \
      -Dsonar.sources=src \
      -Dsonar.java.binaries=target/classes \
      -Dsonar.java.source=1.8
  fi
  
  exit $TEST_STATUS
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ $TRAVIS_BRANCH == "master" ]; then
  echo "Master build"
else
  echo "Push to branch" 	  
fi

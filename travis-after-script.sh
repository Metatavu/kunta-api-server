#!/bin/bash

export S3_PATH=s3://$AWS_BUCKET/$TRAVIS_REPO_SLUG/$TRAVIS_BUILD_NUMBER
aws s3 cp target/cargo/configurations/wildfly10x/log $S3_PATH --recursive
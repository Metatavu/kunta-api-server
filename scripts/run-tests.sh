#!/bin/sh

mysql -u root -p < scripts/setup-db.sql && mvn clean verify -Pitests -Dit.test=$1
#!/bin/sh

if [ ! -f docker/m2.tar ]; then
    curl https://s3.eu-central-1.amazonaws.com/static.metatavu.io/kunta-api/docker/m2.tar -o docker/m2.tar
fi

docker-compose build
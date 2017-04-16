#!/bin/bash


WORKSPACE="$( cd "$( dirname "${BASH_SOURCE[0]}" )"/.. && pwd )"


cd $WORKSPACE

BUILD_BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [ "${BUILD_BRANCH}" = "bintray" ]; then
  DEPLOY_OPTS="uploadArchives"
fi


./gradlew clean check ${DEPLOY_OPTS}
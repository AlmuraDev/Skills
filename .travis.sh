#!/bin/bash

if [ ${TRAVIS_PULL_REQUEST} = 'false' ] && [ ${TRAVIS_BRANCH} = 'api-7' ]; then
  ./gradlew -PsonatypeUsername="${SONATYPE_USERNAME}" -PsonatypePassword="${SONATYPE_PASSWORD}" clean genDatabaseClasses genEventImpl updateLicenses build uploadArchives
else
  ./gradlew clean genDatabaseClasses genEventImpl updateLicenses build
fi
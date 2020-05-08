#!/bin/bash

# Exit if any error occurs
set -e

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Save global script args
ARGS="$@"

# Display a help message.
function displayHelp() {
    echo "This script helps you to perform AtlasMap release."
    echo "The available options are:"
    echo " --docker-user           Docker user for Docker Hub."
    echo " --docker-password       Docker password for Docker Hub."
    echo " --release-version       Version number to be used for release."
    echo " --development-version   Version number to be used for next development iteration."
    echo " --help                  Displays this help message."
}

#
# Checks if a flag is present in the arguments.
function hasflag() {
    filter=$1
    for var in "${@:2}"; do
        if [ "$var" = "$filter" ]; then
            echo 'true'
            break;
        fi
    done
}

#
# Read the value of an option.
function readopt() {
        filter=$1
        next=false
        for var in "${@:2}"; do
                if $next; then
                        echo $var
                        break;
                fi
                if [ "$var" = "$filter" ]; then
                        next=true
                fi
        done
}

# ======================================================
# Build functions

function init_options() {
  HELP=$(hasflag --help $ARGS 2> /dev/null)

  RELEASE_VERSION=$(readopt --release-version $ARGS 2> /dev/null)
  DEVELOPMENT_VERSION=$(readopt --development-version $ARGS 2> /dev/null)

  DOCKER_USER=$(readopt --docker-user $ARGS 2> /dev/null)
  DOCKER_PASSWORD=$(readopt --docker-password $ARGS 2> /dev/null)

  # Internal variable default values
  OC_OPTS=""
  MAVEN_PARAMETERS="--batch-mode -Dfabric8.mode=kubernetes -Pfabric8,release,community-release"
  MAVEN_CMD="${MAVEN_CMD:-${BASEDIR}/mvnw}"
}

# ============================================================================
# Main loop

init_options

if [ -n "$HELP" ]; then
   displayHelp
   exit 0
fi

echo "=========================================================="
echo "Building artifacts ...."
echo "=========================================================="
"${MAVEN_CMD}" $MAVEN_PARAMETERS clean install
pushd docs
"${MAVEN_CMD}" $MAVEN_PARAMETERS -f pom-javadoc.xml -pl \!io.atlasmap:atlasmap-lib-all javadoc:aggregate
popd

echo "=========================================================="
echo "Performing Maven Release ...."
echo "=========================================================="
"${MAVEN_CMD}" $MAVEN_PARAMETERS -Dtag=atlasmap-${RELEASE_VERSION} \
               -DreleaseVersion=${RELEASE_VERSION} \
               -DdevelopmentVersion=${DEVELOPMENT_VERSION} \
               release:prepare
"${MAVEN_CMD}" $MAVEN_PARAMETERS -Dtag=atlasmap-${RELEASE_VERSION} \
               -DreleaseVersion=${RELEASE_VERSION} \
               -DdevelopmentVersion=${DEVELOPMENT_VERSION} \
               release:perform

# tag the major/minor version and docker push it
echo "=========================================================="
echo "Pushing docker images to Docker Hub...."
echo "=========================================================="
ATLASMAP_IMAGE="atlasmap/atlasmap"
MAJOR_MINOR_VERSION=$(echo $RELEASE_VERSION | cut -f1,2 -d'.')

if [ -n "$DOCKER_USER" ] && [ -n "$DOCKER_PASSWORD" ]; then
    echo "==== Login to Docker Hub"
    docker login -u "$DOCKER_USER" -p "$DOCKER_PASSWORD"
fi

docker tag "${ATLASMAP_IMAGE}:${RELEASE_VERSION}" "${ATLASMAP_IMAGE}:${MAJOR_MINOR_VERSION}"
  docker push "${ATLASMAP_IMAGE}:${RELEASE_VERSION}"
  docker push "${ATLASMAP_IMAGE}:${MAJOR_MINOR_VERSION}"

echo "=========================================================="
echo "Publishing NPM package of AtlasMap UI...."
echo "=========================================================="

git reset
git checkout .
pushd ui
CURRENT_BRANCH=$(git branch --show-current)
git checkout tags/atlasmap-${RELEASE_VERSION} -b temp-${RELEASE_VERSION}
./node_modules/.bin/lerna version --no-push --force-publish --amend -y ${RELEASE_VERSION}
./node_modules/.bin/lerna publish from-package -y
git tag -f atlasmap-${RELEASE_VERSION}
git push origin atlasmap-${RELEASE_VERSION}
git checkout ${CURRENT_BRANCH}
git branch -D temp-${RELEASE_VERSION}
./node_modules/.bin/lerna version --no-git-tag-version -y ${DEVELOPMENT_VERSION}
git add .
git commit --amend --no-edit
git push origin ${CURRENT_BRANCH}
popd


# For some reason following no longer works... instead run manually ./node_modules/.bin/gren release --tags atlasmap-${RELEASE_VERSION}..${PREVIOUS_VERSION} --override 
# echo "=========================================================="
# echo "Publishing Release Notes to GitHub...."
# echo "=========================================================="
# yarn add github-release-notes
# ./node_modules/.bin/gren release --tags atlasmap-${RELEASE_VERSION} --override

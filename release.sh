#!/bin/bash
#
# Copyright (C) 2017 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


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
  MAVEN_PARAMETERS="$MAVEN_PARAMETERS --batch-mode -Prelease,community-release"
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
pushd ui
./node_modules/.bin/lerna version --no-push --force-publish --no-git-tag-version -y ${RELEASE_VERSION}
git add .
git commit -m "chore: Set UI version to ${RELEASE_VERSION}"
popd

"${MAVEN_CMD}" $MAVEN_PARAMETERS clean install
pushd docs
"${MAVEN_CMD}" $MAVEN_PARAMETERS -f pom-javadoc.xml -pl \!io.atlasmap:atlasmap-lib-all javadoc:aggregate
popd

echo "=========================================================="
echo "Performing Maven Release & Docker push...."
echo "=========================================================="
"${MAVEN_CMD}" $MAVEN_PARAMETERS -Dtag=atlasmap-${RELEASE_VERSION} \
               -DreleaseVersion=${RELEASE_VERSION} \
               -DdevelopmentVersion=${DEVELOPMENT_VERSION} \
               release:prepare

"${MAVEN_CMD}" $MAVEN_PARAMETERS -Dtag=atlasmap-${RELEASE_VERSION} \
               -DreleaseVersion=${RELEASE_VERSION} \
               -DdevelopmentVersion=${DEVELOPMENT_VERSION} \
               release:perform

xmllint --shell atlasmap-maven-plugin/atlasmap-maven-plugin-example.pom << EOF
cd /*[local-name() = 'project']/*[local-name() = 'version']
set ${DEVELOPMENT_VERSION}
save
EOF

xmllint --shell docs/pom-javadoc.xml << EOF
cd /*[local-name() = 'project']/*[local-name() = 'parent']/*[local-name() = 'version']
set ${DEVELOPMENT_VERSION}
save
EOF

"${MAVEN_CMD}" $MAVEN_PARAMETERS -DskipTests install
git add atlasmap-maven-plugin docs
git diff --quiet HEAD || git commit -m "chore: cleanup after release ${RELEASE_VERSION}"

echo "=========================================================="
echo "Publishing NPM package of AtlasMap UI...."
echo "=========================================================="

git reset
git checkout .
pushd ui
CURRENT_BRANCH=$(git branch --show-current)
git checkout tags/atlasmap-${RELEASE_VERSION} -b temp-${RELEASE_VERSION}
./node_modules/.bin/lerna publish from-package -y
git tag -f atlasmap-${RELEASE_VERSION}
git push origin atlasmap-${RELEASE_VERSION}
git checkout ${CURRENT_BRANCH}
git branch -D temp-${RELEASE_VERSION}
./node_modules/.bin/lerna version --force-publish --no-git-tag-version -y ${DEVELOPMENT_VERSION}
git add .
git commit --amend --no-edit
popd

git push origin ${CURRENT_BRANCH}


# For some reason following no longer works... instead run manually ./node_modules/.bin/gren release --tags atlasmap-${RELEASE_VERSION}..${PREVIOUS_VERSION} --override 
# echo "=========================================================="
# echo "Publishing Release Notes to GitHub...."
# echo "=========================================================="
# yarn add github-release-notes
# ./node_modules/.bin/gren release --tags atlasmap-${RELEASE_VERSION} --override

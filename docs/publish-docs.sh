#!/usr/bin/env bash
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

echo ================================
echo Deploying AtlasMap documentation
echo ================================

../mvnw -Phtml,pdf package
../mvnw -f pom-javadoc.xml -pl \!io.atlasmap:atlasmap-lib-all javadoc:aggregate
mkdir -p target/generated-docs/html/developer-guide/coverage/java
mkdir target/generated-docs/html/developer-guide/coverage/ui
cp -a ../coverage-report/target/site/jacoco-aggregate/* target/generated-docs/html/developer-guide/coverage/java
for dir in `ls --color=never ../ui/packages`; do
  mkdir target/generated-docs/html/developer-guide/coverage/ui/${dir}
  cp -a ../ui/packages/${dir}/coverage/lcov-report/* target/generated-docs/html/developer-guide/coverage/ui/${dir}/ 2>/dev/null || :
done
git clone -b gh-pages https://github.com/atlasmap/atlasmap.git gh-pages
git config --global user.email "atlasmap-dev@redhat.com"
git config --global user.name "AtlasMap"
cd gh-pages
git rm -r *
cd ..
cp src/main/resources/CNAME gh-pages/
cp -rv target/generated-docs/html/user-guide/* gh-pages/
cp -v target/generated-docs/pdf/user-guide/index.pdf gh-pages/user-guide.pdf
mkdir -p gh-pages/developer-guide
cp -rv target/generated-docs/html/developer-guide/* gh-pages/developer-guide/
cp -v target/generated-docs/pdf/developer-guide/index.pdf gh-pages/developer-guide.pdf
cd gh-pages
git add --ignore-errors *
git commit -m "generated documentation"
git push https://${GITHUB_ACTOR}:${GITHUB_TOKEN}@github.com/atlasmap/atlasmap.git gh-pages
cd ..

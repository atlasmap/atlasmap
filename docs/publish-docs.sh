#!/usr/bin/env bash
echo ================================
echo Deploying AtlasMap documentation
echo ================================

../mvnw -Phtml,pdf package && \
../mvnw -f pom-javadoc.xml javadoc:aggreate && \
git clone -b gh-pages https://github.com/atlasmap/atlasmap.git gh-pages && \
git config --global user.email "travis@atlasmap.io" && \
git config --global user.name "Travis" && \
cp -rv target/generated-docs/* gh-pages/ && \
cd gh-pages && \
mv index.pdf atlasmap.pdf && \
git add --ignore-errors * && \
git commit -m "generated documentation" && \
git push origin gh-pages && \
cd ..

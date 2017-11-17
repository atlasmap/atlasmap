#!/usr/bin/env bash
echo ================================
echo Deploying AtlasMap documentation
echo ================================

cd docs && \
../mvnw -Phtml,pdf package && \
git clone -b gh-pages https://atlasci:${GITHUB_TOKEN}@github.com/atlasmap/atlasmap.git gh-pages && \
git config --global user.email "travis@atlasmap.io" && \
git config --global user.name "Travis" && \
cp -rv target/generated-docs/* gh-pages/ && \
cd gh-pages && \
mv index.pdf atlasmap.pdf && \
git add --ignore-errors * && \
git commit -m "generated documentation" && \
git push origin gh-pages && \
cd .. && \
rm -rf gh-pages target

#!/usr/bin/env bash
echo ================================
echo Deploying AtlasMap documentation
echo ================================

cd ..
./mvnw -Pcoverage install
cd docs
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
git config --global user.email "travis@atlasmap.io"
git config --global user.name "Travis"
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
git push origin gh-pages
cd ..

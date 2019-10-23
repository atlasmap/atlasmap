# AtlasMap

[![Runtime @ Maven Central](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/)
[![UI @ NPM](https://badge.fury.io/js/%40atlasmap%2Fatlasmap-data-mapper.svg)](https://badge.fury.io/js/%40atlasmap%2Fatlasmap-data-mapper)
[![CircleCI Badge](https://circleci.com/gh/atlasmap/atlasmap.svg?style=shield)](https://circleci.com/gh/atlasmap/atlasmap)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4acba1646e0a4cbabac3a76ad5df4df7)](https://www.codacy.com/app/atlasmapio/atlasmap?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=atlasmap/atlasmap&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/4acba1646e0a4cbabac3a76ad5df4df7)](https://www.codacy.com/app/atlasmapio/atlasmap?utm_source=github.com&utm_medium=referral&utm_content=atlasmap/atlasmap&utm_campaign=Badge_Coverage)
[![Gitter chat](https://badges.gitter.im/atlasmap/community.png)](https://gitter.im/atlasmap/community)
[Google Group](https://groups.google.com/d/forum/atlasmap)


The AtlasMap is a data mapping solution with interactive web based user interface, that simplifies configuring integrations between Java, XML, and JSON data sources. You can design your data mapping on the AtlasMap Data Mapper UI canvas, and then run that data mapping via runtime engine. AtlasMap Data Mapper UI is primarily designed to work within the [Syndesis UI](https://syndesis.io/), and now we're exploring to improve standalone user experience.

## AtlasMap Documentation
### [AtlasMap User Guide](http://docs.atlasmap.io/)
### [AtlasMap Developer Guide](http://docs.atlasmap.io/developer-guide)


## The shortest path to run standalone AtlasMap Data Mapper UI

1. Download AtlasMap standalone jar
```
$ wget http://central.maven.org/maven2/io/atlasmap/atlasmap-standalone/${VERSION}/atlasmap-standalone-${VERSION}.jar
```

2. Run
```
$ java -jar atlasmap-standalone-${VERSION}.jar 
```

Then AtlasMap Data Mapper UI is available at http://127.0.0.1:8585/ by default.

## Build AtlasMap project

1. Checkout AtlasMap repo from GitHub
```
$ git clone https://github.com/atlasmap/atlasmap ${ATLASMAP}
```
 
2. Build
```
$ cd ${ATLASMAP}
$ ./mvnw clean install
```
or you can skip tests to get the build little bit faster
```
$ ./mvnw clean install -DskipTests
```
3. Run AtlasMap standalone jar from springboot maven plugin
```
$ cd ${ATLASMAP}/standalone
$ ../mvnw -Pitests spring-boot:run
```

AtlasMap Data Mapper UI is available at http://127.0.0.1:8585/ by default.

## Live update for UI development

While standalone AtlasMap design time services are running by following above, you can also run an another AtlasMap Data Mapper UI instance at different port by `yarn start`. This enables live update for UI code so you can see the outcome of your UI code change without build&restart manually.

1. [Install Yarn](https://yarnpkg.com/lang/en/docs/install/)

2. Install Data Mapper UI's dependencies
```
$ cd ${ATLASMAP}/ui
$ yarn install
```

6. Start Data Mapper UI
```
$ yarn start
```

The **yarn start** command will attempt to automatically open your browser window, but if it doesn't, open it directly with this URL: <http://localhost:3000>.


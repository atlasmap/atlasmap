# AtlasMap Data Mapper UI

[![Runtime @ Maven Central](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/)
[![UI @ NPM](https://badge.fury.io/js/%40atlasmap%2Fatlasmap-data-mapper.svg)](https://badge.fury.io/js/%40atlasmap%2Fatlasmap-data-mapper)
[![CircleCI Badge](https://circleci.com/gh/atlasmap/atlasmap.svg?style=shield)](https://circleci.com/gh/atlasmap/atlasmap)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4acba1646e0a4cbabac3a76ad5df4df7)](https://www.codacy.com/app/atlasmapio/atlasmap?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=atlasmap/atlasmap&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/4acba1646e0a4cbabac3a76ad5df4df7)](https://www.codacy.com/app/atlasmapio/atlasmap?utm_source=github.com&utm_medium=referral&utm_content=atlasmap/atlasmap&utm_campaign=Badge_Coverage)

The AtlasMap is a data mapping solution with interactive web based user interface, that simplifies configuring integrations between Java, XML, and JSON data sources. You can design your data mapping on the AtlasMap Data Mapper UI canvas, and then run that data mapping via runtime engine. AtlasMap Data Mapper UI is primarily designed to work within the [Syndesis UI](https://syndesis.io/).

## [AtlasMap Documentation](http://docs.atlasmap.io/)
All developer related documentation can be found at [AtlasMap Documentation](http://docs.atlasmap.io/)

## The shortest path to run standalone AtlasMap

1. Checkout AtlasMap repo from GitHub
```
$ git clone https://github.com/atlasmap/atlasmap ${ATLASMAP}
```

2. Build AtlasMap runtime
```
$ cd ${ATLASMAP}
$ ./mvnw clean install
```
or you can skip tests to get the build little bit faster
```
$ ./mvnw clean install -DskipTests
```

3. Run AtlasMap standalone
```
$ cd ${ATLASMAP}/standalone
$ ../mvnw -Pitests spring-boot:run
```

Then AtlasMap UI is available at http://127.0.0.1:8585/ by default.

## Live update for UI development

While standalone AtlasMap is running by following above, you can also run an another AtlasMap UI instance at different port by `yarn start`. This enables live update for UI code so you can see the outcome of your UI code change without build&restart manually.

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

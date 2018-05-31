# AtlasMap Data Mapper UI

[![Runtime @ Maven Central](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/)
[![UI @ NPM](https://badge.fury.io/js/%40atlasmap%2Fatlasmap-data-mapper.svg)](https://badge.fury.io/js/%40atlasmap%2Fatlasmap-data-mapper)
[![CircleCI Badge](https://circleci.com/gh/atlasmap/atlasmap.svg?style=shield)](https://circleci.com/gh/atlasmap/atlasmap)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4acba1646e0a4cbabac3a76ad5df4df7)](https://www.codacy.com/app/atlasmapio/atlasmap?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=atlasmap/atlasmap&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/4acba1646e0a4cbabac3a76ad5df4df7)](https://www.codacy.com/app/atlasmapio/atlasmap?utm_source=github.com&utm_medium=referral&utm_content=atlasmap/atlasmap&utm_campaign=Badge_Coverage)

The AtlasMap is a data mapping solution with interactive web based user interface, that simplifies configuring integrations between Java, XML, and JSON data sources. You can design your data mapping on the AtlasMap Data Mapper UI canvas, and then run that data mapping via runtime engine. AtlasMap Data Mapper UI is primarily designed to work within the [Syndesis UI](https://syndesis.io/).

## [AtlasMap Documentation](http://docs.atlasmap.io/)
All developer releated documentation can be found at [AtlasMap Documentation](http://docs.atlasmap.io/)

## The shortest path to run standalone AtlasMap

1. Checkout AtlasMap repo from GitHub
```
$ git clone https://github.com/atlasmap/atlasmap ${ATLASMAP}
```

2. Build AtlasMap runtime
```
$ cd ${ATLASMAP}/runtime
$ ../mvnw clean install
```

3. Run AtlasMap Services
```
$ cd ${ATLASMAP}/runtime/runtime
$ ../../mvnw -Pitests spring-boot:run
```

4. [Install Yarn](https://yarnpkg.com/lang/en/docs/install/)

5. In another console, install Data Mapper UI's dependencies
```
$ cd ${ATLASMAP}/ui
$ yarn install
```

6. Start Data Mapper UI
```
$ yarn start
```

The **yarn start** command will attempt to automatically open your browser window, but if it doesn't, open it directy with this URL: <http://localhost:3000>.

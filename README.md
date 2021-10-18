# AtlasMap

[![Runtime @ Maven Central](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/io.atlasmap/atlas-parent/)
[![UI @ NPM](https://badge.fury.io/js/%40atlasmap%2Fatlasmap.svg)](https://badge.fury.io/js/%40atlasmap%2Fatlasmap)
[![Main](https://github.com/atlasmap/atlasmap/actions/workflows/main.yml/badge.svg)](https://github.com/atlasmap/atlasmap/actions/workflows/main.yml)
[![Supported](https://github.com/atlasmap/atlasmap/actions/workflows/supported-build.yml/badge.svg)](https://github.com/atlasmap/atlasmap/actions/workflows/supported-build.yml)
[![Codacy Grade](https://app.codacy.com/project/badge/Grade/57f3935eba6b4438976295efea04ac0c)](https://www.codacy.com/gh/atlasmap/atlasmap/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=atlasmap/atlasmap&amp;utm_campaign=Badge_Grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/57f3935eba6b4438976295efea04ac0c)](https://www.codacy.com/gh/atlasmap/atlasmap/dashboard?utm_source=github.com&utm_medium=referral&utm_content=atlasmap/atlasmap&utm_campaign=Badge_Coverage)
[![Gitter chat](https://badges.gitter.im/atlasmap/community.png)](https://gitter.im/atlasmap/community)
[![GitHub Discussions](https://img.shields.io/github/discussions/atlasmap/atlasmap)](https://github.com/atlasmap/atlasmap/discussions)


The AtlasMap is a data mapping solution with interactive web based user interface, that simplifies configuring integrations between Java, XML, and JSON data sources. You can design your data mapping on the AtlasMap Data Mapper UI canvas, and then run that data mapping via runtime engine. AtlasMap Data Mapper UI is primarily designed to work within the [Syndesis UI](https://syndesis.io/), and now we're exploring to improve standalone user experience.

### AtlasMap Documentation
* [AtlasMap User Guide](http://docs.atlasmap.io/)
* [AtlasMap Developer Guide](http://docs.atlasmap.io/developer-guide)

### Places to discuss and/or ask a question
* Gitter for an instant and/or shorter chat, it could be real time - https://gitter.im/atlasmap/community
* GitHub Discussion for a longer discussion - https://github.com/atlasmap/atlasmap/discussions



## The shortest path to run standalone AtlasMap Data Mapper UI

1. Download AtlasMap standalone jar
```
$ wget https://repo1.maven.org/maven2/io/atlasmap/atlasmap-standalone/${VERSION}/atlasmap-standalone-${VERSION}.jar
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
$ ./mvnw clean install -DskipTests -Pitests
```
or you can skip tests to get the build little bit faster
```
$ ./mvnw clean install -DskipTests
```
3. Run the AtlasMap standalone jar from the springboot maven plugin, as described above:
```
$ cd ${ATLASMAP}/standalone
$ ../mvnw -Pitests spring-boot:run
```

4. In a separate terminal window, run the standalone UI:
```
$ cd ${ATLASMAP}/ui/packages/atlasmap-standalone
$ yarn start
```

## Live update for UI development

You can also run the AtlasMap Data Mapper UI with live updates by starting yarn in both the core and UI folders:

1. Build AtlasMap UI and server
```
$ cd ${ATLASMAP}
$ ./mvnw clean install -DskipTests -Pitests
```
2. Start the AtlasMap server by running the AtlasMap standalone jar from the springboot maven plugin:
```
$ cd ${ATLASMAP}/standalone
$ ../mvnw -Pitests spring-boot:run
```
3. Again in a separate terminal window, run yarn build to make the REACT UI:
```
$ cd ${ATLASMAP}/ui/packages/atlasmap/ui
$ yarn build
```
4.  Run AtlasMap standalone:
Run `yarn start` for each sub packages from 3 different terminal
```
$ cd ${ATLASMAP}/ui/packages/atlasmap-core
$ yarn start
```
```
$ cd ${ATLASMAP}/ui/packages/atlasmap
$ yarn start
```
```
$ cd ${ATLASMAP}/ui/packages/atlasmap-standalone
$ yarn start
```
#### Run AtlasMap from your browser with storybook
```
$ cd ${ATLASMAP}/ui/packages/atlasmap
$ yarn build
$ yarn storybook
```
There is a full function demo in storybook, `AtlasMap|Demo`. If you have AtlasMap server running at http://localhost:8585/, 
then the demo fully works as AtlasMap standalone UI.


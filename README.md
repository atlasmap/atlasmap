# AtlasMap
The AtlasMap is a data mapping solution with interactive web based user interface, that simplifies configuring integrations between Java, XML, and JSON data sources. You can design your data mapping on the [AtlasMap Data Mapper UI](ui/README.md) canvas, and then run that data mapping via runtime engine. AtlasMap Data Mapper UI is primarily designed to work within the [Syndesis UI](https://github.com/syndesisio/syndesis).

In addition to the plain Java API provided by the runtime engine, AtlasMap also provides [Camel Component](camel/README.md) to perform data mapping as a part of Apache Camel route.


![AtlasMap Data Mapper UI Screenshot](https://raw.githubusercontent.com/atlasmap/atlasmap/master/ui/docs/datamapper.png)

## Running Data Mapper UI within Syndesis ##

The easiest way to install and run the Data Mapper UI is to install and run the [Syndesis UI](https://github.com/syndesisio/syndesis). Simply follow the Syndesis UI's [installation instructions](https://github.com/syndesisio/syndesis), and run the Syndesis UI. You will find the Data Mapper UI under the integrations panel after selecting or adding an integration with a data mapping step involved in the integration.

## Running AtlasMap standalone ##

First you need to build AtlasMap.

### Building everything for standalone usage
```
    ./build.sh --skip-image-builds
```

To see all the available options:
```
    ./build.sh --help
```

#### Resume from module    
To resume from a particular module:
```
    ./build.sh --skip-image-builds --resume-from ui
```

#### Using the image streams (in case you want to build a docker image for design runtime)
To build everything using image streams (instead of directly talking to docker):
```
    ./build.sh --with-image-streams
```

Note that this assumes that you are using a template flavor that also supports image streams.

### Run AtlasMap Services and UI

1. Run AtlasMap Services
```
    cd ${ATLASMAP}/runtime/runtime
    ../../mvnw -Pitests spring-boot:run
```

2. [Install Yarn](https://yarnpkg.com/lang/en/docs/install/)

3. In another console, install Data Mapper UI's dependencies
```
    cd ${ATLASMAP}/ui
    yarn install
```

4. Start Data Mapper UI
```
    yarn start
```
5. The **yarn start** command will attempt to automatically open your browser window, but if it doesn't, open it directy with this URL: [http://localhost:3000].


## See also ##
* [AtlasMap Data Mapper UI Developer Quick Start](ui/README.md)
* [AtlasMap Data Mapper UI standalone desktop application](app/README.md)
* [AtlasMap Camel Component](camel/README.md)

## Third Party Libraries ##

//TODO: 

## License ##

//TODO: 


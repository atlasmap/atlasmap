# AtlasMap Data Mapper UI Developer Quick Start

The Data Mapper features a stand-alone development mode which allows developers to run the Data Mapper UI on their local machine outside of the Syndesis UI. 

Running the Data Mapper in stand alone mode will require installing the Data Mapper and the Atlas Map Services on your machine. 

*Installation* 

1. Run the AtlasMap Services

    cd ${ATLASMAP}/runtime/runtime
    mvn -Pitests spring-boot:run

2. [Install Yarn](https://yarnpkg.com/lang/en/docs/install/)

3. In another console, install the Data Mapper UI's dependencies

    cd ${ATLASMAP}/ui
    yarn install

4. Start the Data Mapper UI

    yarn start

5. The **yarn start** command will attempt to automatically open your browser window, but if it doesn't, open it directy with this URL: [http://localhost:3000].

*Troubleshooting Installation*

1. Compile errors: If the UI doesn't run, check the terminal window where you ran `npm start`, there may be compilation errors reported there even if it attempts to run the UI successfully without exiting with error.

2. Check the console window of chrome's developer tools window for errors, this is found via the chrome "view->developer->developer tools" menu, the javascript console will be on the bottom of the tab you've opened the tools in.

*Reference Documents For Installation Guide*

- [Angular 2 Installation Guide](https://angular.io/docs/ts/latest/guide/setup.html)
- [Angular 2 AOT compilation guide](https://angular.io/docs/ts/latest/cookbook/aot-compiler.html)

## Developing Within Syndesis UI ##

The Data Mapper UI is referenced by Syndesis as a dependency. When the Syndesis UI's dependencies are installed during the **yarn install** step, the Data Mapper UI will be cloned from the NPM package repository into the **${SYNDESIS}/ui/node_modules/@atlasmap/atlasmap.data.mapper** directory. 

You can point your local Syndesis UI's Data Mapper UI reference to your working copy of the Data Mapper by changing the **src** directory in the node_modules folder to point to your code. You'll do something like this:

```
    # save the original data mapper library contents for syndesis in case we want to use that version again later.
    # ${SYNDESIS}/ui is your local syndesis ui directory. 
    > mv ${SYNDESIS}/ui/node_modules/@atlasmap/atlasmap.data.mapper/src ${SYNDESIS}/ui/node_modules/@atlasmap/atlasmap.data.mapper/src.old

    # ${ATLASMAP}/ui is your local data mapper ui directory
    > ln -s ${ATLASMAP}/ui/src ${SYNDESIS}/ui/node_modules/@atlasmap/atlasmap.data.mapper/src
```

After making this change, restart the Syndesis UI with **yarn start**.

Note that running `yarn install` in the Syndesis UI directory **will remove and redownload the ${SYNDESIS}/ui/node_modules/@atlasmap/atlasmap.data.mapper directory**. For this reason, do *not* make changes within the **${SYNDESIS}/ui/node_modules/atlasmap_data_mapper** directory. Instead, make changes in another directory and use the soft link (`ln -s`) command shown above to point the Syndesis UI dependency to your code. 


## Debug Configuration ##

The Data Mapper UI features several developer-friendly debug configuration options. These configuration fields are specified on the [ConfigModel](https://github.com/atlasmap/atlasmap/blob/master/ui/src/app/lib/atlasmap-data-mapper/models/config.model.ts) class. 

** Service Endpoint Configuration**

1. baseJavaInspectionServiceUrl - URL for the Java Inspection Service provided by the AtlasMap Services.
2. baseXMLInspectionServiceUrl - URL for the XML Inspection Service provided by the AtlasMap Services.
3. baseJSONInspectionServiceUrl - URL for the JSON Inspection Service provided by the AtlasMap Services.
4. baseMappingServiceUrl - URL for the Mapping Service provided by the AtlasMap Services.

** Mock Source/Target Document Configuration**

These flags control the UI automatically adding the specified mock documents to the system when the UI initializes.

1. addMockJavaSingleSource - Add single Java source document.
1. addMockJavaSources - Add multiple Java source documents.
1. addMockXMLInstanceSources - Add multiple XML instance-based source documents.
1. addMockXMLSchemaSources - Add multiple XML schema-based source documents.
1. addMockJSONSources - Add multiple JSON source documents.
1. addMockJavaTarget - Add a Java target document.
1. addMockXMLInstanceTarget - Add a XML instance target document.
1. addMockXMLSchemaTarget - Add a XML schema target document.
1. addMockJSONTarget - Add a JSON target document.

The code that initializes these mock documents is in the [Initialization Service](https://github.com/atlasmap/atlasmap/blob/master/ui/src/app/lib/atlasmap-data-mapper/services/initialization.service.ts). That service calls various static methods in the [Document Management Service](https://github.com/atlasmap/atlasmap/blob/master/ui/src/app/lib/atlasmap-data-mapper/services/document.management.service.ts) to create example XML instance, XML schema, and JSON documents. Mock Java documents referenced are from the AtlasMap Services' [Atlas Java Test Model Maven Module](https://github.com/atlasmap/atlasmap/tree/master/runtime/atlas-java-parent/atlas-java-test-model/src/main/java/io/atlasmap/java/test).

** Additional Debug Configuration**

1. discardNonMockSources - Automatically discard all user-specified (or Syndesis UI-specified) source/target documents before initializing. This is helpful if you're trying to test with mock documents alone.
2. addMockJSONMappings - This flag bootstraps the UI's mappings from the provided JSON mapping definition. Useful for repeatedly debugging a particular scenario.
3. debugClassPathServiceCalls - Log details about JSON request/responses to/from the class path resolution service.
4. debugDocumentServiceCalls - Log details about JSON request/responses to/from the Java/XML/JSON inspection services.
5. debugMappingServiceCalls - Log details about JSON request/responses to/from the mapping service.
6. debugValidationServiceCalls - Log details about JSON request/responses to/from the mapping validation service.
7. debugFieldActionServiceCalls - Log details about JSON request/responses to/from the mapping field action configuration service.
8. debugDocumentParsing - Log details about parsing JSON responses from the inspection services.

Data Mapper Debug Configuration within the Syndesis UI is defined within your **${SYNDESIS}/ui/src/config.json** file's data mapper section:

```
{
  "apiEndpoint": "https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/api/v1",
  "title": "Syndesis",
  "datamapper": {
    "baseJavaInspectionServiceUrl": "http://localhost:8585/v2/atlas/java/",
    "baseXMLInspectionServiceUrl": "http://localhost:8585/v2/atlas/xml/",
    "baseJSONInspectionServiceUrl": "http://localhost:8585/v2/atlas/json/",
    "baseMappingServiceUrl": "http://localhost:8585/v2/atlas/",
    "discardNonMockSources": true,
    "addMockJSONMappings": false,
    "addMockJavaSingleSource": true, 
    "addMockJavaSources": false,
    "addMockXMLInstanceSources": true,
    "addMockXMLSchemaSources": true,
    "addMockJSONSources": true,
    "addMockJavaTarget": false,
    "addMockXMLInstanceTarget": false,
    "addMockXMLSchemaTarget": false,
    "addMockJSONTarget": true,
    "debugDocumentServiceCalls": true,
    "debugMappingServiceCalls": true,
    "debugClassPathServiceCalls": false,
    "debugValidationServiceCalls": false,
    "debugFieldActionServiceCalls": false,
    "debugDocumentParsing": false
  },
  "oauth": {
    "clientId": "syndesis-ui",
    "scopes": ["openid"],
    "oidc": true,
    "hybrid": true,
    "issuer": "https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/auth/realms/syndesis",
    "auto-link-github": true
  }
}
```

If you're running the Data Mapper UI locally outside of the Syndesis UI, the debug configuration is specified within the [DataMapperAppExampleHostComponent](https://github.com/atlasmap/atlasmap/blob/master/ui/src/app/lib/atlasmap-data-mapper/components/data.mapper.example.host.component.ts).

## Code Overview ##

//todo: list of project files w/ brief descriptions for each

//todo: brief overview of bootstrapping process

//TODO: brief high level overview of what the ui does (service calls made, etc)

[BOOTSTRAPPING OVERVIEW]

Bootstrapping the Data Mapper UI requires a bit of configuration. An example bootstrapping component is provided within the project:

ui/src/app/lib/atlasmap-data-mapper/components/data.mapper.example.host.component.ts

*MODEL OVERVIEW*

All application data and configuration is stored in a centralized ConfigModel object.

The ConfigModel contains:

 * initialization data such as service URLs and source/target document information
 * references to our angular2 services that manage retrieving and saving our documents and mapping data
 * document / mapping model objects

There are two document models contained within the ConfigModel object, both of type DocumentDefinition. A DocumentDefinition contains information about a source or target document such as the document's name, and fields for that document. Fields are represented by our Field model.

A single MappingDefinition model in the ConfigModel object stores information about field mappings and related lookup tables. Individual mappings are represented in instances of MappingModel, and lookup tables are represented by the LookupTable model.

*SERVICE OVERVIEW*

When the Data Mapper UI Bootstraps, a series of service calls are made to the mapping service (MappingManagementService) and document service (DocumentManagementService). 

The document service is used to fetch our source/target document information (name of doc, fields). After these are parsed from the service, they are stored in the ConfigModel's inputDoc and outputDoc DocumentDefinition models.

The mapping service is used to fetch our mappings for the fields mapped from the source to the target document. These mappings (and related lookup tables) are parsed by the management service and stored in the ConfigModel's mappings MappingDefinition model. 

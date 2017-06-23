## Data Mapper UI ##

Variables used in this document:

- ${atlasui.home} is the folder the data-mapper ui was checked out into

[INITIAL SETUP]

Reference: angular 2 env setup guide: https://angular.io/docs/ts/latest/guide/setup.html

1) install NPM: https://docs.npmjs.com/getting-started/installing-node
2) run 'npm install' in ${atlasui.home} to install node modules

[RUNNING THE UI]

1) build all of the atlas 2 maven projects by executing 'mvn clean install' in ${atlasui.home}

2) run 'mvn jetty:run' in ${atlasui.home}/atlas2.java.parent/atlas2.java.service

	This will host the java inspection rest service on port 8585, example url to test that it's up:

	http://localhost:8585/v2/atlas/java/class?className=com.mediadriver.atlas.java.service.v2.TestAddress

3) run 'npm start' in a terminal from ${atlasui.home} to start the UI

	This should automatically open the ui in your browser with a "Angular Quickstart" tab, if it doesn't, open this URL in a tab:

	http://localhost:3000/

[Compiling And Running With AOT]

Reference: angular 2 AOT compilation guide: https://angular.io/docs/ts/latest/cookbook/aot-compiler.html

1) compile with "npm run aot:build"
2) strip unused code with "npm run aot:rollup"
3) copy misc js/html/css files we need with "npm run aot:copy"
4) run aot artifact with "npm run aot:serve"

[SAMPLE]

1) The src/app/lib/syndesis-data-mapper/components/data.mapper.example.host.component.ts provides an example of how to consume the Data Mapper UI component

[TROUBLESHOOTING]

#1: Compile errors: If the UI doesn't run, check the terminal window where you ran 'npm start', there may be compilation errors reported there even if it attempts to run the UI successfully without exiting with error.

#2: Check the console window of chrome's developer tools window for errors, this is found via the chrome "view->developer->developer tools" menu, the javascript console will be on the bottom of the tab you've opened the tools in.

[BOOTSTRAPPING OVERVIEW]

Bootstrapping the Data Mapper UI requires a bit of configuration. An example bootstrapping component is provided within the project:

src/app/lib/syndesis-data-mapper/components/data.mapper.example.host.component.ts

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




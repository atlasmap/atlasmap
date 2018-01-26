= Reference Mappings Overview =

== Introduction == 

The itests/reference-mappings test cases were created with the intent of testing a competely exhaustive (CE) combination of field types and data formats.

== The Tests ==

The tests are broken up by big scenario use cases:

For example:

1. Java to Java, Json and Xml
2. Json to Java, Json and Xml
3. Xml to Java, Json and Xml
4. multidoc

Within each of this high-level scenarios, the use cases are broken down into sub-test types. Each test scenario includes:

1. A complete atlasmapping file
2. Sample generated source document
3. Validation routine to confirm the expected output is correct

The sub-tests include coverage for:

1. Primitive data types (int, long, short, etc)
2. Boxed Primitive data types (Integer, Long, Short, etc)
3. Complex data types (Order, Customer, etc)
4. Repeating primitive types (array, list, etc)
5. Repeating complex types
6. Rooted and un-rooted Json and Xml object types
7. Fully qualified namespaced Xml and non-namespaced Xml documents

== Target Audience ==

The refrence-mappings are targeted to multiple user groups:

1. End Users: The refrence mappings serve as a set of sample mappings that cover every possible scenario, so users will have a knowledge base as to when certain fields need to be applied to certain mapping scenarios.

 For example: 

 In this jsonToJava scenario (jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-4.xml), the AtlasMap runtime's Auto-detection feature is unable to determine the difference between a json number and a "long". In this scenario, the user would need to specify 'fieldType="long"' in order for AtlasMap to treat the source type as a long, vs the default auto-detected type.

       <Mapping xsi:type="Mapping" mappingType="Map">
            <InputField xsi:type="ns3:JsonField" path="/longField" fieldType="Long" />
            <OutputField xsi:type="ns2:JavaField" path="/charField" />
        </Mapping> 

2. Consultants, Pre-Sales Engineers and Support Engineers

 The reference mappings provide a comprehensive knowledge base and sample archive of all possible scenarios in order to quickly address customer requests

3. AtlasMap developers

 When making code changes to the atlasmap-runtime, the reference-mappings serve as integration tests to ensure all possible scenarios pass and signal that a change does not have unintended downstream impacts.

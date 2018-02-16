# Reference Mappings Overview #

## Introduction ##

The itests/reference-mappings test cases were created with the intent of testing a competely exhaustive (CE) combination of field types and data formats.

## Test Scenarios ##


### Format to Format Testing ###

The tests are broken up by high-level functional use cases based on source and target format

For example:

1. Java to Java, Json and Xml mappings
2. Json to Java, Json and Xml mappings
3. Xml to Java, Json and Xml mappings
4. multidoc mappings

### Test artifacts ###

Within each of these high-level scenarios, the use cases are broken down into sub-test types. Each test scenario includes:

1. A complete atlasmapping file
2. Sample generated source document
3. Validation routine to confirm the expected output is correct

Note: For simplicity and the ability to re-use validation methods, many of the test objects share the same name and data shape

General filenaming convention

Base folder structure: 
 Java files: src/test/java/io/atlasmap/reference/${mapping source_to_target}/${source}${sarget}${scenario}Test.java
 Mapping files: src/test/resources/${source}To{$Target}/atlasmapping-${data characteristics}-${scenario}-${index}.xml

Example:
 Java to Java AutoConversion test: 
    src/test/java/io/atlasmap/reference/java_to_java/JavaJavaAutoConversionTest.java
 method(s) / mapping file: 
    testProcessJavaJavaFlatFieldMappingAutoConversion1() / src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-1.xml
    testProcessJavaJavaFlatFieldMappingAutoConversion2() / src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-2.xml
    testProcessJavaJavaFlatFieldMappingAutoConversion3() / src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-3.xml
    ...

Scenario Terminology: 
   flatprimitive: An object that only contains 1 generation of child parameters (aka flat) that are all Java Primitives
   boxedflatprimitive: An object that only contains 1 generation of child parameters (aka flat) that are all Java Boxed Primitives
   complex: An object that contains other child objects 
   combine: A mapping scenario that supports collecting multiple fields from a source document into one field in the target document
   separate: A mapping scenario that supports splitting a single target field into multiple target fields
   autodetect: A mapping where source and target FieldTypes are (for the most part) not specified in the mapping definition. This exercises the AutoDetection features.
   autoconversion: A mapping where source and target fields are not of the same type, and no special field action or conversion is specified. This exercises the AutoConversion features.
   rooted: An XML or JSON document that has a parent field that holds all the child fields in the object. 
   unrooted: An XML or JSON document that does not have a parent field hold the object's child fields.

### Data Type Coverage ###

The sub-tests include coverage for:

1. Primitive data types (int, long, short, etc)
2. Boxed Primitive data types (Integer, Long, Short, etc)
3. Complex data types (Order, Customer, etc)
4. Repeating primitive types (array, list, etc)
5. Repeating complex types
6. Rooted and un-rooted Json and Xml object types
7. Fully qualified namespaced Xml and non-namespaced Xml documents

### Auto-Conversion and Auto-Detection Testing ###

AtlasMap supports 2 advanced features to simplify the syntax and reduce the verbosity of mapping definition files.

 * AutoConversion: The ability to automatically convert between known base types (such as Integer -> String)
 * AutoDetection: The ability to automatically detect source and target field types** (ie. { "foo": 123 } can safely be auto-detected to be a JSON integer)

** There are scenarios where users will need to explicitely specify type in order to support large numbers and decimal precision.

The reference mappings include comprehensive test cases for ensuring that Auto-Conversion and Auto-Detection work properly for all primitive field types and across all supported formats (Java, JSON and Xml)

This is achieved through a series of tests that explicitely map between all type combinations.

For example: 

Reference mapping "flatprimitive-autoconversion1": 
  * int -> byte
  * short -> int
  * long -> short
  * double -> long
  * float -> double
  * boolean -> float
  * char -> boolean
  * byte -> char

Reference mapping "flatprimitive-autoconversion2": 
(shifts all the target-side fields down one)
  * int -> char
  * short -> byte
  * long -> int
  * etc..

.. the autoconversion tests then continue for all combinations

## Target Audience ##

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

# AtlasMap Data Mapper example :: Message Map


### Introduction

This example shows how to perform data mapping from multiple source Documents and corresponding headers stored in Message Map with using AtlasMap Data Mapper running as a part of Camel route.
AtlasMap expects Message Map to hold Camel Message with a Document ID as a key. The Message headers are handled as a scoped property where Document ID is equal to the scope.
Syndesis provides this message mapping feature internally so that the Data Mapper step can consume multiple source Messages as well as headers. In this example, `MessageCaptureProcessor` does that part of managing Message Map.

### Build

You will need to compile this example first:

	mvn compile

### Run

To run the example, type

	mvn camel:run

To stop the example hit <kbd>ctrl</kbd>+<kbd>c</kbd>.

### Forum, Help, etc

If you hit an problems please let us know on the Camel Forums
	<http://camel.apache.org/discussion-forums.html>

Please help us make Apache Camel better - we appreciate any feedback you may
have.  Enjoy!


The Camel riders!

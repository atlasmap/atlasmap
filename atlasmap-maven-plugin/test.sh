#!/bin/sh

mvn -f atlasmap-maven-plugin-example.pom atlasmap:generate-inspections
mvn -f atlasmap-maven-plugin-example.pom atlasmap:generate-field-actions

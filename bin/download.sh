#!/bin/bash

cd dist/assets
if [ ! -f runtime-1.20.0.jar ] ; then
	wget http://central.maven.org/maven2/io/atlasmap/runtime/1.20.0/runtime-1.20.0.jar
fi

#!/bin/bash

cd dist/assets
if [ ! -f runtime-1.32.2.jar ] ; then
	wget http://central.maven.org/maven2/io/atlasmap/runtime/1.32.2/runtime-1.32.2.jar
fi

#!/bin/bash

BUILD_DEST=../out
mkdir $BUILD_DEST
javac  -cp :../libs/core-3.3.0.jar:../libs/javase-3.3.0.jar  ../src/Screen.java -d $BUILD_DEST

echo "Compiled destination: $BUILD_DEST"

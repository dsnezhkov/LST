#!/bin/bash

BUILD_DEST=../out
mkdir $BUILD_DEST
javac  -cp ../libs/TarsosDSP-latest.jar:../libs/commons-cli-1.4.jar:  ../src/PitchAnalyze.java -d $BUILD_DEST

echo "Compiled destination: $BUILD_DEST"

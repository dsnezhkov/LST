#!/bin/bash

BUILD_DEST=../out
[ ! -d $BUILD_DEST ] && mkdir $BUILD_DEST
javac  -cp ../libs/commons-cli-1.4.jar:  ../src/Rook.java -d $BUILD_DEST
javac  ../src/Pointer.java -d $BUILD_DEST

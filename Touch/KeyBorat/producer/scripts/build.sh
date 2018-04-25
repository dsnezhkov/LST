#!/bin/bash

BUILD_DEST=../out
[ ! -d $BUILD_DEST ] && mkdir $BUILD_DEST
cd $BUILD_DEST
echo "Building Keyboard"
javac  ../src/com/keyborat/Keyboard.java -d $BUILD_DEST
echo "Building KeyBorat"
javac  -cp ../libs/commons-cli-1.4.jar:. ../src/com/keyborat/KeyBorat.java -d $BUILD_DEST

#!/bin/bash

BUILD_DEST=../out
cd $BUILD_DEST
java  -cp :../libs/core-3.3.0.jar:../libs/javase-3.3.0.jar:../libs/commons-cli-1.4.jar  Screen $*

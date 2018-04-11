#!/bin/bash

cd  ../out
java -cp ../libs/TarsosDSP-latest.jar:../libs/commons-cli-1.4.jar:. PitchAnalyze $*

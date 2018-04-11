#!/bin/bash

cd  ../out
java -cp ../libs/commons-cli-1.4.jar:. Rook $*

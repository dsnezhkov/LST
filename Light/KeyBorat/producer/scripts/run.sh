#!/bin/bash

cd  ../out
java -cp ../libs/commons-cli-1.4.jar:. com.keyborat.KeyBorat $*

# example: ./run.sh -f ../../../../data/first.c -m2

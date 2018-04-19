#!/bin/bash

cd  ../out
java -cp ../libs/commons-cli-1.4.jar:. Rook $*

# example calibrate: ./runRook.sh -f /Users/dimas/Code/LST/data/B64calibrate.txt -x 72 -y 221 -a 5 -A 10 -d 47 -D 47 -w 3000

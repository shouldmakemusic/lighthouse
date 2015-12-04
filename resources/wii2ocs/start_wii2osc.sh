#!/bin/bash
echo "Script executed from: ${PWD}"

BASEDIR=$(dirname $0)
echo "Script location: ${BASEDIR}"
cd ${BASEDIR}
export DYLD_LIBRARY_PATH=.:$DYLD_LIBRARY_PATH
./wii2osc

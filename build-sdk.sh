#!/usr/bin/env bash

function pauseErr(){
    if [ $? -ne 0 ];then
        echo "pause because of failed ; Ctrl + C to exit"
        sleep 1000
        exit
    fi
}

if [ "$(uname)" == "Darwin" ];then
    echo "alias sed to gsed for Mac, hint: brew install gnu-sed"
    alias sed='gsed'
fi

buildType=$1

if [ ! -d "target/" ]; then
  mkdir "target/"
  mkdir "target/public/"
fi

rm -rf demo/build/

imageeditor="../GDImageEditorSDK/imageeditor"

echo "params: buildType:${buildType}"
./gradlew clean assemble${buildType} -x lint

if [ ${buildType}x != "release"x ];then
    echo "cp debug aar to target"
    cp ${imageeditor}/build/outputs/aar/GDImageEditorSDK-${buildType}.aar target/public/GDImageEditorSDK-${buildType}.aar
    $(pauseErr)
else
    echo "cp release aar to target"
    cp ${imageeditor}/build/outputs/aar/GDImageEditorSDK-${buildType}.aar target/public/GDImageEditorSDK-${buildType}.aar
    $(pauseErr)
fi

# copy aar to
rm -rf demo/libs/*
cp target/public/GDImageEditorSDK-${buildType}.aar demo/libs/

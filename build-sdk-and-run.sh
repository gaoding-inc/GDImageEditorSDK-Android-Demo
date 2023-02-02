#!/usr/bin/env bash

./build-sdk.sh release
# build
./gradlew clean assembleRelease -x lint
apkPath="demo/build/outputs/apk/release/demo-release.apk"
adb install "$apkPath"
adb shell am start -n com.gaoding.editor.image.demo/.MainActivity
# 将demo apk拷贝到target/public目录，供其他需要的人使用
cp demo/build/outputs/apk/release/demo-release.apk target/public/

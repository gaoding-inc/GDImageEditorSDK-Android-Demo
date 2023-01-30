#!/usr/bin/env bash

./build-sdk.sh release
adb shell am start -n com.gaoding.editor.image.demo/.MainActivity

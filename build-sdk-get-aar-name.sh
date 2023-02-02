#!/usr/bin/env bash

# 从gradle.properties获取最新发布版本
getArrName() {
  buildType=$1
  PUBLISH_VERSION=1.0.0
  while read LINE
  do
    if [[ $LINE == PUBLISH_VERSION* ]]
    then
      versionLine=$LINE
      PUBLISH_VERSION=${versionLine:16}
#      echo "hehehe: $LINE"
    fi
  done < "gradle.properties"

#  echo "publish version: $PUBLISH_VERSION"

  aarName="GDImageEditorSDK-${buildType}-${PUBLISH_VERSION}.aar"
#  echo "$aarName"
  echo "$aarName"
}

getArrName "$1"

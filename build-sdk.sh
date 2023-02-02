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

# 将demo的build.gradle中sdk依赖更新为源码依赖
echo "update to source dependency"
annotateSourceDependencyStr="\/\/    implementation project(path: \':imageeditor\')"
nonAnnotateSourceDependencyStr="    implementation project(path: \':imageeditor\')"
sed -i "" "s/^${annotateSourceDependencyStr}/${nonAnnotateSourceDependencyStr}/g" demo/build.gradle
nonAnnotateAarDependencyStr="    implementation(name: \"GDImageEditorSDK-release-\${PUBLISH_VERSION}\", ext: \'aar\')"
annotateAarDependencyStr="\/\/    implementation(name: \"GDImageEditorSDK-release-\${PUBLISH_VERSION}\", ext: \'aar\')"
sed -i "" "s/^${nonAnnotateAarDependencyStr}/${annotateAarDependencyStr}/g" demo/build.gradle
nonAnnotateVersionDependencyStr="    implementation \"com.gaoding.imageeditor:imageeditor:\${PUBLISH_VERSION}\""
annotateVersionDependencyStr="\/\/    implementation \"com.gaoding.imageeditor:imageeditor:\${PUBLISH_VERSION}\""
sed -i "" "s/^${nonAnnotateVersionDependencyStr}/${annotateVersionDependencyStr}/g" demo/build.gradle

echo "params: buildType:${buildType}"
./gradlew clean assemble${buildType} -x lint

# 从gradle.properties获取最新发布版本
aarName=$(sh build-sdk-get-aar-name.sh $buildType)

if [ ${buildType}x != "release"x ];then
    echo "cp debug aar to target"
    cp ${imageeditor}/build/outputs/aar/${aarName} target/public/${aarName}
    $(pauseErr)
else
    echo "cp release aar to target"
    cp ${imageeditor}/build/outputs/aar/${aarName} target/public/${aarName}
    $(pauseErr)
fi

# copy aar to
rm -rf demo/libs/*
cp target/public/${aarName} demo/libs/

# 将demo的build.gradle中sdk依赖更新为aar依赖
echo "update to aar dependency"
sed -i "" "s/^${nonAnnotateSourceDependencyStr}/${annotateSourceDependencyStr}/g" demo/build.gradle
sed -i "" "s/^${annotateAarDependencyStr}/${nonAnnotateAarDependencyStr}/g" demo/build.gradle
#sed -i "" "s/^${annotateVersionDependencyStr}/${nonAnnotateVersionDependencyStr}/g" demo/build.gradle



#!/bin/bash
echo "==============begin compile apk=============="

ls $ANDROID_HOME/platforms
java -version
gradle -version

echo "gradle project"
rm -rf ./build

gradle clean

gradle openLog
gradle app-headunit-portrait-open:assembleDebug || exit 1

gradle closeLog
gradle app-headunit-portrait-open:assembleRelease || exit 1

mkdir -p output
ls -l ./app-headunit-portrait-open/build/outputs/apk/
cp ./app-headunit-portrait-open/build/outputs/apk/*.apk ./output/

ls -l ./output/
echo "==============end compile apk=============="

#!/bin/bash
echo "==============begin compile apk=============="

ls $ANDROID_HOME/platforms
java -version
gradle -version

echo "gradle project"
rm -rf ./build

gradle clean

gradle openLog
gradle app-mirror-open:assembleDebug || exit 1

gradle closeLog
gradle app-mirror-open:assembleRelease || exit 1

mkdir -p output
ls -l ./app-mirror-open/build/outputs/apk/
cp ./app-mirror-open/build/outputs/apk/*.apk ./output/

ls -l ./output/
echo "==============end compile apk=============="

#!/usr/bin/env bash by EddyLiu
echo "start excute bash.sh"

echo "configure build system environment"
source ~/.bash_profile

export JAVA_HOME=/home/scmtools/buildkit/jdk-1.8u92
export ANT_HOME=/home/scmtools/buildkit/ant-1.9.2
export GRADLE_HOME=/home/scmtools/buildkit/gradle/gradle_2.14.1
export ANDROID_HOME=/home/scmtools/buildkit/android-sdk
export ANDROID_NDK_HOME=/home/scmtools/buildkit/android-ndk-r12b
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$GRADLE_HOME/bin:$ANDROID_NDK_HOME:$PATH

ls $ANDROID_HOME/platforms
java -version
gradle -version

echo "project build start !"

echo "readIcodeNumber start"
git fetch origin refs/notes/commits:refs/notes/commits
git notes show | grep 'changeset:' | awk '{print $2}'
git_icode_number=`git notes show | grep 'changeset:' | awk '{print $2}' | head -1`

echo $git_icode_number

export GIT_ICODE_NUMBER=$git_icode_number

echo $GIT_ICODE_NUMBER
echo "readIcodeNumber end"

rm -rf ./build

gradle clean
gradle readBuildNumber


gradle assembleDebug || exit 1

gradle assembleRelease || exit 1


mkdir output
cd output
mkdir debug
mkdir release
cd ..
ls -l ./build/outputs/apk/
cp ./build/outputs/apk/*debug.apk ./output/debug/
cp ./build/outputs/apk/*release.apk ./output/release/

ls -l ./output/debug/
ls -l ./output/release/

cd output
pwd
ls -al
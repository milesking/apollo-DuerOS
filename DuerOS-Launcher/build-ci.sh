#!/bin/bash
echo "==============build all begin=============="

echo "=====configure build system environment"
source ~/.bash_profile

export JAVA_HOME=/home/scmtools/buildkit/jdk-1.8u92
export ANT_HOME=/home/scmtools/buildkit/ant-1.9.2
export GRADLE_HOME=/home/scmtools/buildkit/gradle/gradle_3.3
export ANDROID_HOME=/home/scmtools/buildkit/android-sdk
export ANDROID_NDK_HOME=/home/scmtools/buildkit/android-ndk-r12b
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$GRADLE_HOME/bin:$ANDROID_NDK_HOME:$PATH

sh build-local.sh

echo "==============build all end=============="

#!/bin/bash
ant build-number-debug -buildfile build.xml
ant open-proguard -buildfile build.xml
ant open-log -buildfile build.xml
ant carlife-vehicle-release-normal -buildfile build.xml
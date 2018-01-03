#!/bin/bash
ant carlife-vehicle-release -buildfile build.xml
ant carlife-vehicle-release-normal -buildfile build.xml
ant carlife-vehicle-release-aftermarket -buildfile build.xml
ant carlife-vehicle-release-preinstallmarket -buildfile build.xml
ant carlife-vehicle-release-aftermarket-autolaunch -buildfile build.xml
#!/bin/bash
ant carlife-vehicle-debug -buildfile build.xml
ant carlife-vehicle-debug-normal -buildfile build.xml
ant carlife-vehicle-debug-aftermarket -buildfile build.xml
ant carlife-vehicle-debug-preinstallmarket -buildfile build.xml
ant carlife-vehicle-debug-aftermarket-autolaunch -buildfile build.xml
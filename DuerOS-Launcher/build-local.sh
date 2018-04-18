#!/bin/bash
echo "==============build all begin=============="

echo "=====build app-mirror-open=========="
sh build-app-mirror-open.sh

echo "=====build app-headunit-open=========="
sh build-app-headunit-open.sh

echo "=====build app-headunit-open=========="
sh build-app-headunit-portrait-open.sh

echo "==============build all end=============="

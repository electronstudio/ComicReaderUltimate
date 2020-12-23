#!/bin/bash
JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [[ "$JAVA_VER" -eq 15 ]]
then
  echo "Using JDK 15"
else
  echo "ONLY TESTED WITH JDK 15"
  exit 10
fi
echo "BUILDING WITH GRADLE"
./gradlew clean
./gradlew dist
echo "CREATING JLINK RUNTIME"
rm -rf /tmp/myjdk
jlink --output /tmp/myjdk --add-modules java.base,java.xml,java.desktop,java.logging,jdk.unsupported,java.sql.rowset --no-header-files --no-man-pages --compress 2 --strip-native-commands --strip-debug
echo "PACKAGING INTO build/out WITH JPACKAGE"
rm -rf build/out
mkdir build/out
jpackage -n cru --main-jar desktop-1.0.jar --input desktop/build/libs/ -t app-image -d build/out --runtime-image /tmp/myjdk
echo "MAKING LINUX APP IMAGE FROM JPACKAGE PACKAGE"
cp cru.desktop build/out/cru
cp icon.png build/out/cru
cp AppRun build/out/cru
appimagetool-x86_64.AppImage build/out/cru build/out/ComicReaderUltimate.AppImage
#,,jdk.unsupported.desktop,java.xml,jdk8internals/sun.reflect,java.logging
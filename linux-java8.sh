#!/bin/bash
export JAVA_HOME=/home/richard/.jdks/corretto-1.8.0_302/
echo "BUILDING WITH GRADLE"
./gradlew clean
./gradlew dist

echo "PACKAGING INTO build/out WITH JAVAPACKAGER"
rm -rf build/out
mkdir build/out
$JAVA_HOME/bin/javapackager -deploy -native -outdir build/out -outfile cru -name cru -title ComicReaderUltimate -srcdir desktop/build/libs/ -srcfiles desktop-1.0.jar -appclass uk.co.electronstudio.desktop.DesktopLauncher -Bruntime=$JAVA_HOME/jre
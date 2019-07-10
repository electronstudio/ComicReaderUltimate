#!/bin/bash
rm -rf '/Volumes/Home/rich/ComicReaderUltimate/desktop/build/Comic Reader Ultimate.app'
~/Downloads/jdk-14.jdk/Contents/Home/bin/jpackage \
--verbose \
--input desktop/build/libs \
--output desktop/build \
--name 'Comic Reader Ultimate' \
--icon desktop/src/main/deploy/package/macosx/CRU.icns \
--mac-bundle-name 'CRU' \
--main-jar desktop-1.0.jar  \
--main-class uk.co.electronstudio.desktop.DesktopLauncher \
--file-associations assoc1.properties \
--file-associations assoc2.properties \
--file-associations assoc3.properties \
--file-associations assoc4.properties \
--file-associations assoc5.properties \
--add-modules java.desktop,java.base,java.sql.rowset,jdk.unsupported,jdk.unsupported.desktop,java.xml,jdk8internals/sun.reflect,java.logging \
--java-options '-XstartOnFirstThread'
#--package-type dmg
#\
#        --mac-bundle-identifier uk.co.electronstudio.CRU \
#        --mac-sign \
#        --mac-signing-key-user-name  \
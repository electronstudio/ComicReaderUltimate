./gradlew desktop:dist
rm -rf /Volumes/Home/rich/ComicReaderUltimate/desktop/build/libs/ComicReaderUltimate.app
~/Downloads/jdk-14.jdk/Contents/Home/bin/jpackage \
--verbose \
--input desktop/build/libs \
--output desktop/build/libs \
--name ComicReaderUltimate \
--mac-bundle-name ComicReaderUltimate \
--main-jar desktop-1.0.jar  \
--main-class uk.co.electronstudio.desktop.DesktopLauncher \
--file-associations assoc1.properties \
        --mac-bundle-identifier uk.co.electronstudio.CRU \
        --mac-sign \
        --mac-signing-key-user-name  \
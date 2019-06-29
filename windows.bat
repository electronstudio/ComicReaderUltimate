C:\jdk-14\bin\jpackage ^
--verbose ^
--input desktop/build/libs ^
--output desktop/build ^
--name ComicReaderUltimate ^
--icon desktop/src/main/deploy/package/windows/CRU.ico ^
--main-jar desktop-1.0.jar  ^
--main-class uk.co.electronstudio.desktop.DesktopLauncher ^
--file-associations assoc1.properties ^
--file-associations assoc2.properties ^
--file-associations assoc3.properties ^
--file-associations assoc4.properties ^
--file-associations assoc5.properties ^
--add-modules java.desktop,java.base,java.sql.rowset,jdk.unsupported,jdk.unsupported.desktop,java.xml,jdk8internals/sun.reflect,java.logging ^
--package-type msi
rem --win-menu ^
rem    --win-shortcut ^
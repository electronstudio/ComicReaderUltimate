package uk.co.electronstudio.desktop;


import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.spookygames.gdx.nativefilechooser.desktop.DesktopFileChooser;
import uk.co.electronstudio.App;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.io.File;
import java.util.List;

public class DesktopLauncher {
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//config.fullscreen=true;
        config.vSyncEnabled = true;
//        config.useGL30 = true;
//        config.gles30ContextMajorVersion = 3;
//        config.gles30ContextMinorVersion = 2;


        App app = new App(new DesktopFileChooser(), args);

        Desktop.getDesktop().setOpenFileHandler(new OpenFilesHandler() {
            @Override
            public void openFiles(OpenFilesEvent ofe) {
                List<File> files = ofe.getFiles();
                if (files != null && files.size() > 0) {
                    System.out.println("file handler "+ files.get(0));
                    app.viewScreen.loadComic(files.get(0).getAbsolutePath());
                }
            }
        });

       // var testfile2 = "/Volumes/Home/rich/Documents/Vuze Downloads/Buffy Comics Season 9 complete/Buffy the Vampire Slayer Season 9 08.cbz";

        Desktop.getDesktop().setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(AboutEvent e) {
                System.out.println("about");

            }
        });


//        Application.getApplication().setAboutHandler(new AboutHandler() {
//            @Override
//            public void handleAbout(AppEvent.AboutEvent aboutEvent) {
//                System.out.println("about");
//            }
//        });
//
		new LwjglApplication(app, config);


	}
}

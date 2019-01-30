package uk.co.electronstudio.desktop;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.spookygames.gdx.nativefilechooser.desktop.DesktopFileChooser;
import uk.co.electronstudio.App;

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

        Application.getApplication().setOpenFileHandler((AppEvent.OpenFilesEvent ofe) -> {
            List<File> files = ofe.getFiles();
            if (files != null && files.size() > 0) {
                System.out.println("osx file handler "+ files.get(0));
                System.exit(0);
            }
        });


        Application.getApplication().setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(AppEvent.AboutEvent aboutEvent) {
                System.out.println("about");
            }
        });
		new LwjglApplication(new App(new DesktopFileChooser(), args), config);


	}
}

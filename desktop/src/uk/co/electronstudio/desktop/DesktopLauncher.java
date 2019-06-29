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
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DesktopLauncher {

    static Logger logger = Logger.getLogger("MyLog");
    static FileHandler fh;
    volatile static String fileToLoad = null;

    public static void main (String[] args) throws IOException {

        fh = new FileHandler("/tmp/c.log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        logger.info("log");

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//config.fullscreen=true;
        config.vSyncEnabled = true;
//        config.useGL30 = true;
//        config.gles30ContextMajorVersion = 3;
//        config.gles30ContextMinorVersion = 2;

        logger.info("fileToLoad: "+fileToLoad);


        App app = new App(new DesktopFileChooser(), args, logger);

//        Desktop.getDesktop().setOpenFileHandler(new OpenFilesHandler() {
//            @Override
//            public void openFiles(OpenFilesEvent ofe) {
//                List<File> files = ofe.getFiles();
//                if (files != null && files.size() > 0) {
//                    System.out.println("file handler "+ files.get(0));
//                    app.viewScreen.loadComic(files.get(0).getAbsolutePath());
//                }
//            }
//        });
//
//        Desktop.getDesktop().setAboutHandler(new AboutHandler() {
//            @Override
//            public void handleAbout(AboutEvent e) {
//                System.out.println("about");
//            }
//        });




        Desktop.getDesktop().setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(AboutEvent e) {
                logger.info("about");
            }
        });

        Desktop.getDesktop().setOpenFileHandler(new OpenFilesHandler() {
            @Override
            public void openFiles(OpenFilesEvent ofe) {
                //System.exit(0);
                logger.info("open files");
                List<File> files = ofe.getFiles();
                if (files != null && files.size() > 0) {
                    logger.info("file handler "+ files.get(0).getAbsolutePath());
            //        try {
                    fileToLoad = files.get(0).getAbsolutePath();
                    app.requestLoad(fileToLoad);
                        app.viewScreen.loadComic(files.get(0).getAbsolutePath());
           //         }catch (Throwable e){
           //            logger.log(Level.SEVERE, "error in openfilesevent", e);
           //         }
                    logger.info("file handler done");
                }
            }
        });

		new LwjglApplication(app, config);


	}
}

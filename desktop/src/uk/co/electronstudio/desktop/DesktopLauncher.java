package uk.co.electronstudio.desktop;


//import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
//import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import net.spookygames.gdx.nativefilechooser.desktop.DesktopFileChooser;

import uk.co.electronstudio.App;

import java.awt.Desktop;
//import java.awt.desktop.AboutEvent;
//import java.awt.desktop.AboutHandler;
//import java.awt.desktop.OpenFilesEvent;
//import java.awt.desktop.OpenFilesHandler;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DesktopLauncher {





    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger("MyLog");
        //String logfile = System.getProperty("user.home") + "/.cru.log";
        //System.out.println(logfile);
        //FileHandler fh = new FileHandler(logfile);
        //logger.addHandler(fh);
        //SimpleFormatter formatter = new SimpleFormatter();
        //fh.setFormatter(formatter);

        //LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        Lwjgl3ApplicationConfiguration config3 = new Lwjgl3ApplicationConfiguration();
        config3.setHdpiMode(Lwjgl3ApplicationConfiguration.HdpiMode.Pixels);
        //config3.setFullscreenMode();

        //config.fullscreen=true;
        //config.vSyncEnabled = true;

        //config.useGL30 = true;
        //config.gles30ContextMajorVersion = 3;
        //config.gles30ContextMinorVersion = 2;


        App app = new App(new DesktopFileChooser(), logger);


        if (args.length > 0) {
            app.requestLoad(args[0]);
        }




//        if(Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
//            Desktop.getDesktop().setOpenFileHandler(new OpenFilesHandler() {
//                @Override
//                public void openFiles(OpenFilesEvent ofe) {
//                    logger.info("open files");
//                    List<File> files = ofe.getFiles();
//                    if (files != null && files.size() > 0) {
//                        String fileToLoad = files.get(0).getAbsolutePath();
//                        logger.info("file handler " + fileToLoad);
//                        //        try {
//                        app.requestLoad(fileToLoad);
//                        //app.viewScreen.loadComic(files.get(0).getAbsolutePath());
//                        //         }catch (Throwable e){
//                        //            logger.log(Level.SEVERE, "error in openfilesevent", e);
//                        //         }
//                        logger.info("file handler done");
//                    }
//                }
//            });
//        }

       // new LwjglApplication(app, config);
        new Lwjgl3Application(app, config3);

    }
}
